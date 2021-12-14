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

import com.google.common.collect.Sets;
import cz.muni.ics.oauth2.exception.DeviceCodeCreationException;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientWithScopes;
import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.DeviceCodeService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.oauth2.token.DeviceTokenGranter;
import cz.muni.ics.oidc.server.PerunScopeClaimTranslationService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.userInfo.PerunUserInfo;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.openid.connect.service.UserInfoService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import cz.muni.ics.openid.connect.view.JsonErrorView;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	public static final String ACR = "acr";
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
	public static final String URL = "devicecode";
	public static final String USER_URL = "device";

	private final ClientDetailsEntityService clientService;
	private final SystemScopeService scopeService;
	private final DeviceCodeService deviceCodeService;
	private final OAuth2RequestFactory oAuth2RequestFactory;
	private final PerunOidcConfig perunOidcConfig;
	private final WebHtmlClasses htmlClasses;
	private final PerunScopeClaimTranslationService scopeClaimTranslationService;
	private final UserInfoService userInfoService;

	@Autowired
	public DeviceEndpoint(ClientDetailsEntityService clientService,
						  SystemScopeService scopeService,
						  DeviceCodeService deviceCodeService,
						  OAuth2RequestFactory oAuth2RequestFactory,
						  PerunOidcConfig perunOidcConfig,
						  WebHtmlClasses htmlClasses,
						  PerunScopeClaimTranslationService scopeClaimTranslationService,
						  UserInfoService userInfoService) {

		this.clientService = clientService;
		this.scopeService = scopeService;
		this.deviceCodeService = deviceCodeService;
		this.oAuth2RequestFactory = oAuth2RequestFactory;
		this.perunOidcConfig = perunOidcConfig;
		this.htmlClasses = htmlClasses;
		this.scopeClaimTranslationService = scopeClaimTranslationService;
		this.userInfoService = userInfoService;
	}

	@RequestMapping(
			value = "/" + URL,
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public String requestDeviceCode(@RequestParam(CLIENT_ID) String clientId,
									@RequestParam(name = SCOPE, required = false) String scope,
									@RequestParam(name = ACR_VALUES, required = false) String acrValues,
									Map<String, String> parameters,
									ModelMap model) {

		ClientWithScopes clientWithScopes = new ClientWithScopes();
		String errorViewName = handleRequest(clientId, clientWithScopes, scope, model);

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

			if (StringUtils.hasText(acrValues)) {
				response.put(
						VERIFICATION_URI,
						constructURI(perunOidcConfig.getConfigBean().getIssuer() + USER_URL, Map.of(ACR_VALUES, acrValues))
				);
			} else {
				response.put(VERIFICATION_URI, perunOidcConfig.getConfigBean().getIssuer() + USER_URL);
			}

			if (clientWithScopes.getClient().getDeviceCodeValiditySeconds() != null) {
				response.put(EXPIRES_IN, clientWithScopes.getClient().getDeviceCodeValiditySeconds());
			}
			
			if (perunOidcConfig.getConfigBean().isAllowCompleteDeviceCodeUri()) {
				URI verificationUriComplete  = new URIBuilder(perunOidcConfig.getConfigBean().getIssuer() + USER_URL)
					.addParameter(USER_CODE, dc.getUserCode())
					.build();

				response.put(
						VERIFICATION_URI_COMPLETE,
						constructURI(
								perunOidcConfig.getConfigBean().getIssuer() + USER_URL,
								Map.of(ACR_VALUES, acrValues, USER_CODE, dc.getUserCode())
						)
				);
			}

			model.put(JsonEntityView.ENTITY, response);
			return JsonEntityView.VIEWNAME;
		} catch (DeviceCodeCreationException dcce) {
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			model.put(JsonErrorView.ERROR, dcce.getError());
			model.put(JsonErrorView.ERROR_MESSAGE, dcce.getMessage());
			
			return JsonErrorView.VIEWNAME;
		} catch (URISyntaxException use) {
			log.error("unable to build verification_uri_complete due to wrong syntax of uri components");
			model.put(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);

			return HttpCodeView.VIEWNAME;
		}
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = "/" + USER_URL, method = RequestMethod.GET)
	public String requestUserCode(@RequestParam(value = USER_CODE, required = false) String userCode,
								  @ModelAttribute(AUTHORIZATION_REQUEST) AuthorizationRequest authRequest,
								  Principal p,
								  HttpServletRequest req,
								  ModelMap model,
								  HttpSession session) {

		if (!perunOidcConfig.getConfigBean().isAllowCompleteDeviceCodeUri() || userCode == null) {
			// if we don't allow the complete URI or we didn't get a user code on the way in,
			// print out a page that asks the user to enter their user code
			// user must be logged in
			return getRequestUserCodeViewName(req, model);
		} else {
			// complete verification uri was used, we received user code directly
			// skip requesting code page
			// user must be logged in
			return readUserCode(userCode, model, p, req, session);
		}
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = "/" + USER_URL + "/verify", method = RequestMethod.POST)
	public String readUserCode(@RequestParam(USER_CODE) String userCode,
							   ModelMap model,
							   Principal p,
							   HttpServletRequest req,
							   HttpSession session) {

		DeviceCode dc = deviceCodeService.lookUpByUserCode(userCode);

		String errorViewName = checkDeviceCodeIsValid(dc, req, model);
		if (errorViewName != null) {
			return errorViewName;
		}

		ClientDetailsEntity client = clientService.loadClientByClientId(dc.getClientId());

		model.put(CLIENT, client);
		model.put(DC, dc);
		model.put(SCOPES, getSortedScopes(dc));

		AuthorizationRequest authorizationRequest = oAuth2RequestFactory.createAuthorizationRequest(dc.getRequestParameters());

		session.setAttribute(AUTHORIZATION_REQUEST, authorizationRequest);
		session.setAttribute(DEVICE_CODE_SESSION_ATTRIBUTE, dc);

		return getApproveDeviceViewName(model, p, req, (DeviceCode) model.get(DC));
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value = "/" + USER_URL + "/approve", method = RequestMethod.POST)
	public String approveDevice(@RequestParam(USER_CODE) String userCode,
								@RequestParam(value = USER_OAUTH_APPROVAL) Boolean approve,
								Principal p,
								HttpServletRequest req,
								ModelMap model,
								Authentication auth,
								HttpSession session) {

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
			return getApproveDeviceViewName(model, p, req, dc);
		}

		// create an OAuth request for storage
		OAuth2Request o2req = oAuth2RequestFactory.createOAuth2Request(authorizationRequest);
		OAuth2Authentication o2Auth = new OAuth2Authentication(o2req, auth);
		
		DeviceCode approvedCode = deviceCodeService.approveDeviceCode(dc, o2Auth);

		model.put(SCOPES, getSortedScopes(dc));
		model.put(APPROVED, true);

		return DEVICE_APPROVED;
	}

	private String handleRequest(String clientId, ClientWithScopes clientWithScopes, String scope, ModelMap model) {
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
			log.error("Client asked for {} but is allowed {}", requestedScopes, allowedScopes);
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
		String shibAuthnContextClass = "";

		model.put(ACR, shibAuthnContextClass);
		return THEMED_REQUEST_USER_CODE;
	}

	private String getApproveDeviceViewName(ModelMap model, Principal p, HttpServletRequest req, DeviceCode dc) {
		if (perunOidcConfig.getTheme().equalsIgnoreCase(DEFAULT)) {
			return APPROVE_DEVICE;
		}

		model.remove(SCOPES);
		ClientDetailsEntity client = (ClientDetailsEntity) model.get(CLIENT);

		PerunUserInfo user = (PerunUserInfo) userInfoService.getByUsernameAndClientId(
				p.getName(),
				client.getClientId()
		);

		ControllerUtils.setScopesAndClaims(scopeService, scopeClaimTranslationService, model, dc.getScope(), user);
		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

		model.put(PAGE, APPROVE_DEVICE);
		return THEMED_APPROVE_DEVICE;
	}

	private Set<SystemScope> getSortedScopes(DeviceCode dc) {
		Set<SystemScope> scopes = scopeService.fromStrings(dc.getScope());

		Set<SystemScope> sortedScopes = new LinkedHashSet<>(scopes.size());
		Set<SystemScope> systemScopes = scopeService.getAll();

		for (SystemScope s : systemScopes) {
			if (scopes.contains(s)) {
				sortedScopes.add(s);
			}
		}

		sortedScopes.addAll(Sets.difference(scopes, systemScopes));
		return sortedScopes;
	}

	private String constructURI(String uri, Map<String, String> params) {
		if (params.isEmpty()) {
			return uri;
		}

		StringBuilder uriBuilder = new StringBuilder(uri);

		if (!uri.contains("?")) {
			Optional<String> key = params.keySet().stream().findFirst();
			uriBuilder.append("?").append(key).append("=").append(params.get(key));
			params.remove(key);
		}

		for (Map.Entry<String, String> entry : params.entrySet()) {
			uriBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
		}

		return uriBuilder.toString();
	}
}
