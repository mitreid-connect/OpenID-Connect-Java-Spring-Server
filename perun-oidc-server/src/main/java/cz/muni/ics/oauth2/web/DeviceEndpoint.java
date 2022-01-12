/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package cz.muni.ics.oauth2.web;

import cz.muni.ics.oauth2.exception.DeviceCodeCreationException;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientWithScopes;
import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.DeviceCodeService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.oauth2.token.DeviceTokenGranter;
import cz.muni.ics.oidc.saml.SamlPrincipal;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.userInfo.PerunUserInfo;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.openid.connect.service.ScopeClaimTranslationService;
import cz.muni.ics.openid.connect.service.UserInfoService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import cz.muni.ics.openid.connect.view.JsonErrorView;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Implements https://tools.ietf.org/html/draft-ietf-oauth-device-flow
 *
 * @see DeviceTokenGranter
 *
 * @author jricher
 * @author Dominik Baranek (baranek@ics.muni.cz)
 */
@Controller
@Slf4j
public class DeviceEndpoint {

	// views
	public static final String REQUEST_USER_CODE = "requestUserCode";
	public static final String THEMED_REQUEST_USER_CODE = "themedRequestUserCode";
	public static final String APPROVE_DEVICE = "approveDevice";
	public static final String THEMED_APPROVE_DEVICE = "themedApproveDevice";
	public static final String DEVICE_APPROVED = "deviceApproved";
	public static final String THEMED_DEVICE_APPROVED = "themedDeviceApproved";

	// response keys
	public static final String DEVICE_CODE = "device_code";
	public static final String USER_CODE = "user_code";
	public static final String VERIFICATION_URI_COMPLETE = "verification_uri_complete";
	public static final String EXPIRES_IN = "expires_in";
	public static final String VERIFICATION_URI = "verification_uri";

	// request params
	public static final String CLIENT_ID = "client_id";
	public static final String USER_OAUTH_APPROVAL = "user_oauth_approval";
	public static final String SCOPE = "scope";
	public static final String ACR_VALUES = "acr_values";

	// model keys
	public static final String CLIENT = "client";
	public static final String DC = "dc";
	public static final String APPROVED = "approved";
	public static final String SCOPES = "scopes";
	public static final String PAGE = "page";
	public static final String ERROR = "error";

	// session attributes
	public static final String DEVICE_CODE_SESSION_ATTRIBUTE = "deviceCode";
	public static final String AUTHORIZATION_REQUEST = "authorizationRequest";

	// errors
	public static final String NO_USER_CODE = "noUserCode";
	public static final String EXPIRED_USER_CODE = "expiredUserCode";
	public static final String USER_CODE_ALREADY_APPROVED = "userCodeAlreadyApproved";
	public static final String USER_CODE_MISMATCH = "userCodeMismatch";
	public static final String INVALID_SCOPE = "invalidScope";

	// other
	public static final String DEFAULT = "default";
	public static final String ENDPOINT_URL = "/devicecode";
	public static final String REQUEST_USER_CODE_URL = "/device/code";
	public static final String CHECK_USER_CODE_URL = "/device/checkcode";
	public static final String DEVICE_APPROVED_URL = "/device/approved";

	private final ClientDetailsEntityService clientService;
	private final SystemScopeService scopeService;
	private final DeviceCodeService deviceCodeService;
	private final OAuth2RequestFactory oAuth2RequestFactory;
	private final PerunOidcConfig perunOidcConfig;
	private final WebHtmlClasses htmlClasses;
	private final ScopeClaimTranslationService scopeClaimTranslationService;
	private final UserInfoService userInfoService;

	@Autowired
	public DeviceEndpoint(ClientDetailsEntityService clientService,
						  SystemScopeService scopeService,
						  DeviceCodeService deviceCodeService,
						  OAuth2RequestFactory oAuth2RequestFactory,
						  PerunOidcConfig perunOidcConfig,
						  WebHtmlClasses htmlClasses,
						  ScopeClaimTranslationService scopeClaimTranslationService,
						  UserInfoService userInfoService)
	{
		this.clientService = clientService;
		this.scopeService = scopeService;
		this.deviceCodeService = deviceCodeService;
		this.oAuth2RequestFactory = oAuth2RequestFactory;
		this.perunOidcConfig = perunOidcConfig;
		this.htmlClasses = htmlClasses;
		this.scopeClaimTranslationService = scopeClaimTranslationService;
		this.userInfoService = userInfoService;
	}

	@PostMapping(value = ENDPOINT_URL, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public String requestDeviceCode(@RequestParam(CLIENT_ID) String clientId,
									@RequestParam(name = SCOPE, required = false) String scope,
									@RequestParam(name = ACR_VALUES, required = false) String acrValues,
									Map<String, String> parameters,
									ModelMap model)
	{
		ClientWithScopes clientWithScopes = new ClientWithScopes();
		String errorViewName = preprocessRequest(clientId, clientWithScopes, scope, model);

		if (errorViewName != null) {
			return errorViewName;
		}

		try {
			DeviceCode dc = deviceCodeService.createNewDeviceCode(
					clientWithScopes.getRequestedScopes(),
					clientWithScopes.getClient(),
					parameters
			);

			Map<String, Object> response = new HashMap<>();

			response.put(DEVICE_CODE, dc.getDeviceCode());
			response.put(USER_CODE, dc.getUserCode());

			Map<String, String> uriParams = new HashMap<>();
			if (StringUtils.hasText(acrValues)) {
				uriParams.put(ACR_VALUES, acrValues);
			}
			String uriBase = perunOidcConfig.getConfigBean().getIssuer(false) + REQUEST_USER_CODE_URL;
			response.put(VERIFICATION_URI, constructVerificationURI(uriBase, uriParams));
			
			if (perunOidcConfig.getConfigBean().isAllowCompleteDeviceCodeUri()) {
				uriParams.put(USER_CODE, dc.getUserCode());
				response.put(VERIFICATION_URI_COMPLETE, constructVerificationURI(uriBase, uriParams));
			}

			if (clientWithScopes.getClient().getDeviceCodeValiditySeconds() != null) {
				response.put(EXPIRES_IN, clientWithScopes.getClient().getDeviceCodeValiditySeconds());
			}

			model.put(JsonEntityView.ENTITY, response);
			return JsonEntityView.VIEWNAME;
		} catch (DeviceCodeCreationException ex) {
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			model.put(JsonErrorView.ERROR, ex.getError());
			model.put(JsonErrorView.ERROR_MESSAGE, ex.getMessage());
			return JsonErrorView.VIEWNAME;
		} catch (URISyntaxException ex) {
			log.error("unable to build verification_uri_complete due to wrong syntax of uri components");
			model.put(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
			return HttpCodeView.VIEWNAME;
		}
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping(value = REQUEST_USER_CODE_URL)
	public String requestUserCode(@RequestParam(value = USER_CODE, required = false) String userCode,
								  HttpServletRequest req,
								  ModelMap model)
	{
		if (perunOidcConfig.getConfigBean().isAllowCompleteDeviceCodeUri() && StringUtils.hasText(userCode)) {
			return verifyUserCode(userCode, req, model);
		} else {
			return getRequestUserCodeViewName(req, model);
		}
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@PostMapping(value = REQUEST_USER_CODE_URL)
	public String verifyUserCode(@RequestParam(value = USER_CODE) String userCode,
								 HttpServletRequest req,
								 ModelMap model)
	{
		model.put(USER_CODE, userCode);

		DeviceCode dc = deviceCodeService.lookUpByUserCode(userCode);

		String errorViewName = checkDeviceCodeIsValid(dc, req, model);
		if (errorViewName != null) {
			return errorViewName;
		}

		HttpSession session = req.getSession();
		session.setAttribute(DEVICE_CODE_SESSION_ATTRIBUTE, dc);

		return "redirect:" + CHECK_USER_CODE_URL + '?' + USER_CODE + '=' + userCode;
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping(value = CHECK_USER_CODE_URL)
	public String startApproveDevice(@RequestParam(USER_CODE) String userCode,
									 ModelMap model,
									 Authentication auth,
									 HttpServletRequest req)
	{
		DeviceCode dc = deviceCodeService.lookUpByUserCode(userCode);
		model.put(USER_CODE, userCode);

		String errorViewName = checkDeviceCodeIsValid(dc, req, model);
		if (errorViewName != null) {
			return errorViewName;
		}

		ClientDetailsEntity client = clientService.loadClientByClientId(dc.getClientId());

		model.put(CLIENT, client);
		model.put(DC, dc);

		HttpSession session = req.getSession();
		AuthorizationRequest authorizationRequest = oAuth2RequestFactory.createAuthorizationRequest(dc.getRequestParameters());
		session.setAttribute(AUTHORIZATION_REQUEST, authorizationRequest);

		SamlPrincipal p = (SamlPrincipal) auth.getPrincipal();

		return getApproveDeviceViewName(model, p, req, dc);
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@PostMapping(value = DEVICE_APPROVED_URL)
	public String processApproveDevice(@RequestParam(USER_CODE) String userCode,
									   @RequestParam(value = USER_OAUTH_APPROVAL) Boolean approve,
									   HttpServletRequest req,
									   ModelMap model,
									   Authentication auth)
	{
		HttpSession session = req.getSession();
		AuthorizationRequest authorizationRequest = (AuthorizationRequest) session.getAttribute(AUTHORIZATION_REQUEST);
		if (authorizationRequest == null) {
			throw new IllegalArgumentException("Authorization request not found in the session");
		}

		DeviceCode dc = (DeviceCode) session.getAttribute(DEVICE_CODE_SESSION_ATTRIBUTE);
		if (dc == null) {
			throw new IllegalArgumentException("Device code not found in the session");
		}

		// make sure the form that was submitted is the one that we were expecting
		if (!dc.getUserCode().equals(userCode)) {
			model.addAttribute(ERROR, USER_CODE_MISMATCH);
			return REQUEST_USER_CODE;
		}

		// make sure the code hasn't expired yet
		if (dc.getExpiration() != null && dc.getExpiration().before(new Date())) {
			model.addAttribute(ERROR, EXPIRED_USER_CODE);
			return REQUEST_USER_CODE;
		}

		ClientDetailsEntity client = clientService.loadClientByClientId(dc.getClientId());
		model.put(CLIENT, client);

		// user did not approve
		if (!approve) {
			model.addAttribute(APPROVED, false);
			SamlPrincipal p = (SamlPrincipal) auth.getPrincipal();
			return getApproveDeviceViewName(model, p, req, dc);
		}

		// create an OAuth request for storage
		OAuth2Request o2req = oAuth2RequestFactory.createOAuth2Request(authorizationRequest);
		OAuth2Authentication o2Auth = new OAuth2Authentication(o2req, auth);
		
		deviceCodeService.approveDeviceCode(dc, o2Auth);

		model.put(APPROVED, true);
		return getDeviceApprovedViewName(req, model);
	}

	private String preprocessRequest(String clientId, ClientWithScopes clientWithScopes, String scope, ModelMap model) {
		ClientDetailsEntity client;

		try {
			client = clientService.loadClientByClientId(clientId);
			Collection<String> authorizedGrantTypes = client.getAuthorizedGrantTypes();

			if (authorizedGrantTypes != null
					&& !authorizedGrantTypes.isEmpty()
					&& !authorizedGrantTypes.contains(DeviceTokenGranter.GRANT_TYPE)) {
				throw new InvalidClientException("Unauthorized grant type: " + DeviceTokenGranter.GRANT_TYPE);
			}
		} catch (IllegalArgumentException e) {
			log.error("IllegalArgumentException was thrown when attempting to load client", e);
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		} catch (OAuth2Exception e) {
			log.error("could not find client {}", clientId);
			model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		// make sure the client is allowed to ask for those scopes
		Set<String> requestedScopes = OAuth2Utils.parseParameterList(scope);
		Set<String> allowedScopes = client.getScope();

		if (!scopeService.scopesMatch(allowedScopes, requestedScopes)) {
			log.error("Client asked for {} but is allowed to request only {}", requestedScopes, allowedScopes);
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			model.put(JsonErrorView.ERROR, INVALID_SCOPE);
			return JsonErrorView.VIEWNAME;
		}

		clientWithScopes.setClient(client);
		clientWithScopes.setRequestedScopes(requestedScopes);

		return null;
	}

	private String checkDeviceCodeIsValid(DeviceCode dc, HttpServletRequest req, ModelMap model) {
		// we couldn't find the device code
		if (dc == null) {
			model.addAttribute(ERROR, NO_USER_CODE);
			return getRequestUserCodeViewName(req, model);
		}

		// make sure the code hasn't expired yet
		if (dc.getExpiration() != null && dc.getExpiration().before(new Date())) {
			model.addAttribute(ERROR, EXPIRED_USER_CODE);
			return getRequestUserCodeViewName(req, model);
		}

		// make sure the device code hasn't already been approved
		if (dc.isApproved()) {
			model.addAttribute(ERROR, USER_CODE_ALREADY_APPROVED);
			return getRequestUserCodeViewName(req, model);
		}

		return null;
	}

	private String getRequestUserCodeViewName(HttpServletRequest req, ModelMap model) {
		if (perunOidcConfig.getTheme().equalsIgnoreCase(DEFAULT)) {
			return REQUEST_USER_CODE;
		}

		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);
		model.put(PAGE, REQUEST_USER_CODE);
		return THEMED_REQUEST_USER_CODE;
	}

	private String getDeviceApprovedViewName(HttpServletRequest req, ModelMap model) {
		if (perunOidcConfig.getTheme().equalsIgnoreCase(DEFAULT)) {
			return DEVICE_APPROVED;
		}

		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);
		model.put(PAGE, DEVICE_APPROVED);
		return THEMED_DEVICE_APPROVED;
	}

	private String getApproveDeviceViewName(ModelMap model, SamlPrincipal p, HttpServletRequest req, DeviceCode dc) {
		if (perunOidcConfig.getTheme().equalsIgnoreCase(DEFAULT)) {
			model.put(SCOPES, ControllerUtils.getSortedScopes(dc.getScope(), scopeService));
			return APPROVE_DEVICE;
		}

		ClientDetailsEntity client = (ClientDetailsEntity) model.get(CLIENT);

		PerunUserInfo user = (PerunUserInfo) userInfoService.get(
				p.getUsername(),
				client.getClientId(),
				dc.getScope(),
				p.getSamlCredential()
		);

		ControllerUtils.setScopesAndClaims(scopeService, scopeClaimTranslationService, model, dc.getScope(), user);
		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

		model.put(PAGE, APPROVE_DEVICE);
		return THEMED_APPROVE_DEVICE;
	}

	private String constructVerificationURI(String uri, Map<String, String> params) throws URISyntaxException {
		if (params == null || params.isEmpty()) {
			return uri;
		}

		URIBuilder builder = new URIBuilder(uri);
		for (Map.Entry<String, String> param: params.entrySet()) {
			builder.addParameter(param.getKey(), param.getValue());
		}
		return builder.build().toString();
	}
}
