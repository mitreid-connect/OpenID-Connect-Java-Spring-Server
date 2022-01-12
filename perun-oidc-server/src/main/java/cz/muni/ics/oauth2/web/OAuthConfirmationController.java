/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
/**
 *
 */
package cz.muni.ics.oauth2.web;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.oidc.saml.SamlPrincipal;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.request.ConnectRequestParameters;
import cz.muni.ics.openid.connect.service.ScopeClaimTranslationService;
import cz.muni.ics.openid.connect.service.UserInfoService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * @author jricher
 *
 */
@Controller
@SessionAttributes("authorizationRequest")
@Slf4j
public class OAuthConfirmationController {

	public static final String AUTHORIZATION_REQUEST = "authorizationRequest";
	public static final String ERROR = "error";
	public static final String INTERACTION_REQUIRED = "interaction_required";
	public static final String STATE = "state";
	public static final String NONE = "none";
	public static final String REDIRECT = "redirect";
	public static final String CODE = "code";
	public static final String AUTH_REQUEST = "auth_request";
	public static final String CLIENT = "client";
	public static final String REDIRECT_URI = "redirect_uri";
	public static final String SCOPES = "scopes";
	public static final String CLAIMS = "claims";
	public static final String CONTACTS = "contacts";
	public static final String GRAS = "gras";
	public static final String DEFAULT = "default";
	public static final String PAGE = "page";
	public static final String CONSENT = "consent";
	public static final String THEMED_APPROVE = "themedApprove";
	public static final String APPROVE = "approve";
	public static final String REMEMBER_ENABLED = "rememberEnabled";

	@Getter
	@Setter
	private ClientDetailsEntityService clientService;

	private SystemScopeService scopeService;
	private ScopeClaimTranslationService scopeClaimTranslationService;
	private UserInfoService userInfoService;
	private RedirectResolver redirectResolver;
	private PerunOidcConfig perunOidcConfig;
	private WebHtmlClasses htmlClasses;

	@Autowired
	public OAuthConfirmationController(ClientDetailsEntityService clientService,
									   SystemScopeService scopeService,
									   ScopeClaimTranslationService scopeClaimTranslationService,
									   UserInfoService userInfoService,
									   RedirectResolver redirectResolver,
									   PerunOidcConfig perunOidcConfig,
									   WebHtmlClasses htmlClasses) {

		this.clientService = clientService;
		this.scopeService = scopeService;
		this.scopeClaimTranslationService = scopeClaimTranslationService;
		this.userInfoService = userInfoService;
		this.redirectResolver = redirectResolver;
		this.perunOidcConfig = perunOidcConfig;
		this.htmlClasses = htmlClasses;
	}

	public OAuthConfirmationController(ClientDetailsEntityService clientService) {
		this.clientService = clientService;
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping("/oauth/confirm_access")
	public String confirmAccess(Map<String, Object> model, HttpServletRequest req, Authentication auth) {
		AuthorizationRequest authRequest = (AuthorizationRequest) model.get(AUTHORIZATION_REQUEST);
		// Check the "prompt" parameter to see if we need to do special processing

		String prompt = (String)authRequest.getExtensions().get(ConnectRequestParameters.PROMPT);
		List<String> prompts = Splitter.on(ConnectRequestParameters.PROMPT_SEPARATOR).splitToList(Strings.nullToEmpty(prompt));
		ClientDetailsEntity client;

		try {
			client = clientService.loadClientByClientId(authRequest.getClientId());
		} catch (OAuth2Exception e) {
			log.error("confirmAccess: OAuth2Exception was thrown when attempting to load client", e);
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		} catch (IllegalArgumentException e) {
			log.error("confirmAccess: IllegalArgumentException was thrown when attempting to load client", e);
			model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		}

		if (client == null) {
			log.error("confirmAccess: could not find client " + authRequest.getClientId());
			model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		if (prompts.contains(NONE)) {
			// if we've got a redirect URI then we'll send it
			return sendRedirect(authRequest, model, client);
		}

		model.put(AUTH_REQUEST, authRequest);
		model.put(CLIENT, client);
		model.put(REDIRECT_URI, authRequest.getRedirectUri());
		model.put(REMEMBER_ENABLED, !prompts.contains(CONSENT));
		model.put(GRAS, true);

		// get the userinfo claims for each scope

		// contacts
		if (client.getContacts() != null) {
			String contacts = Joiner.on(", ").join(client.getContacts());
			model.put(CONTACTS, contacts);
		}

		SamlPrincipal p = (SamlPrincipal) auth.getPrincipal();
		UserInfo user = userInfoService.get(p.getUsername(), client.getClientId(), authRequest.getScope(), p.getSamlCredential());

		// contacts
		if (client.getContacts() != null) {
			model.put(CONTACTS, Joiner.on(", ").join(client.getContacts()));
		}

		if (perunOidcConfig.getTheme().equalsIgnoreCase(DEFAULT)) {
			Set<SystemScope> sortedScopes = ControllerUtils.getSortedScopes(authRequest.getScope(), scopeService);
			model.put(SCOPES, sortedScopes);
			model.put(CLAIMS, getClaimsForScopes(user, sortedScopes));
			return APPROVE;
		}

		ControllerUtils.setScopesAndClaims(scopeService, scopeClaimTranslationService, model, authRequest.getScope(),
				user);
		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

		model.put(PAGE, CONSENT);
		return THEMED_APPROVE;
	}

	private String sendRedirect(AuthorizationRequest authRequest, Map<String, Object> model, ClientDetailsEntity client) {
		String url = redirectResolver.resolveRedirect(authRequest.getRedirectUri(), client);

		try {
			URIBuilder uriBuilder = new URIBuilder(url);

			uriBuilder.addParameter(ERROR, INTERACTION_REQUIRED);
			if (!Strings.isNullOrEmpty(authRequest.getState())) {
				uriBuilder.addParameter(STATE, authRequest.getState()); // copy the state parameter if one was given
			}

			return REDIRECT + ":" + uriBuilder;

		} catch (URISyntaxException e) {
			log.error("Can't build redirect URI for prompt=none, sending error instead", e);
			model.put(CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}
	}

	private Map<String, Map<String, String>> getClaimsForScopes(UserInfo user, Set<SystemScope> sortedScopes) {
		Map<String, Map<String, String>> claimsForScopes = new HashMap<>();

		if (user != null) {
			JsonObject userJson = user.toJson();

			for (SystemScope systemScope : sortedScopes) {
				Map<String, String> claimValues = new HashMap<>();

				Set<String> claims = scopeClaimTranslationService.getClaimsForScope(systemScope.getValue());
				for (String claim : claims) {
					if (userJson.has(claim) && userJson.get(claim).isJsonPrimitive()) {
						// TODO: this skips the address claim
						claimValues.put(claim, userJson.get(claim).getAsString());
					}
				}

				claimsForScopes.put(systemScope.getValue(), claimValues);
			}
		}

		return claimsForScopes;
	}
}
