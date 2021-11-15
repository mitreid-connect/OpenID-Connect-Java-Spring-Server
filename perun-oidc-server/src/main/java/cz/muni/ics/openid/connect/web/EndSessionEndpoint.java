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

package cz.muni.ics.openid.connect.web;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_POST_LOGOUT_REDIRECT_URI;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_STATE;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_TARGET;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import cz.muni.ics.jwt.assertion.impl.SelfAssertionValidator;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.langs.Localization;
import java.text.ParseException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * End Session Endpoint from OIDC session management.
 * <p>
 * This is a copy of the original file with modification at the end of processLogout().
 * </p>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Controller
//TODO: implement according to spec (https://openid.net/specs/openid-connect-rpinitiated-1_0.html)
// other specs:
//TODO: https://openid.net/specs/openid-connect-frontchannel-1_0.html
//TODO: https://openid.net/specs/openid-connect-backchannel-1_0.html
//TODO: https://openid.net/specs/openid-connect-session-1_0.html
@Slf4j
public class EndSessionEndpoint {

	public static final String URL = "endsession";

	private static final String CLIENT_KEY = "client";
	private static final String STATE_KEY = "state";
	private static final String REDIRECT_URI_KEY = "redirectUri";

	private final SelfAssertionValidator validator;
	private final PerunOidcConfig perunOidcConfig;
	private final ClientDetailsEntityService clientService;
	private final Localization localization;
	private final WebHtmlClasses htmlClasses;

	@Autowired
	public EndSessionEndpoint(SelfAssertionValidator validator,
							  PerunOidcConfig perunOidcConfig,
							  ClientDetailsEntityService clientService,
							  Localization localization,
							  WebHtmlClasses htmlClasses) {
		this.validator = validator;
		this.perunOidcConfig = perunOidcConfig;
		this.clientService = clientService;
		this.localization = localization;
		this.htmlClasses = htmlClasses;
	}

	@RequestMapping(value = "/" + URL, method = RequestMethod.GET)
	public String endSession(@RequestParam(value = "id_token_hint", required = false) String idTokenHint,
							 @RequestParam(value = PARAM_POST_LOGOUT_REDIRECT_URI, required = false) String postLogoutRedirectUri,
							 @RequestParam(value = STATE_KEY, required = false) String state,
							 HttpServletRequest request,
							 HttpSession session,
							 Authentication auth, Map<String, Object> model)
	{
		JWTClaimsSet idTokenClaims = null; // pulled from the parsed and validated ID token
		ClientDetailsEntity client = null; // pulled from ID token's audience field

		if (!Strings.isNullOrEmpty(postLogoutRedirectUri)) {
			session.setAttribute(REDIRECT_URI_KEY, postLogoutRedirectUri);
		}
		if (!Strings.isNullOrEmpty(state)) {
			session.setAttribute(STATE_KEY, state);
		}

		// parse the ID token hint to see if it's valid
		if (!Strings.isNullOrEmpty(idTokenHint)) {
			try {
				JWT idToken = JWTParser.parse(idTokenHint);

				if (validator.isValid(idToken)) {
					// we issued this ID token, figure out who it's for
					idTokenClaims = idToken.getJWTClaimsSet();

					String clientId = Iterables.getOnlyElement(idTokenClaims.getAudience());

					client = clientService.loadClientByClientId(clientId);

					// save a reference in the session for us to pick up later
					//session.setAttribute("endSession_idTokenHint_claims", idTokenClaims);
					session.setAttribute(CLIENT_KEY, client);
				}
			} catch (ParseException e) {
				// it's not a valid ID token, ignore it
				log.debug("Invalid id token hint", e);
			} catch (InvalidClientException e) {
				// couldn't find the client, ignore it
				log.debug("Invalid client", e);
			}
		}

		// are we logged in or not?
		if (auth == null || !request.isUserInRole("ROLE_USER")) {
			// we're not logged in anyway, process the final redirect bits if needed
			return processLogout(null, null, request, session);
		} else {
			log.info("Logout confirmating for user {} from client {}", auth.getName(), client != null ? client.getClientName() : "unknown");
			// we are logged in, need to prompt the user before we log out
			model.put("client", client);
			model.put("idToken", idTokenClaims);

			ControllerUtils.setPageOptions(model, request, localization, htmlClasses, perunOidcConfig);

			// display the log out confirmation page
			return "logout";
		}
	}

	@RequestMapping(value = "/" + URL, method = RequestMethod.POST)
	public String processLogout(@RequestParam(value = "approve", required = false) String approved,
								@RequestParam(value = "deny", required = false) String deny,
								HttpServletRequest request,
								HttpSession session)
	{
		String redirectUri = (String) session.getAttribute(REDIRECT_URI_KEY);
		String state = (String) session.getAttribute(STATE_KEY);
		ClientDetailsEntity client = (ClientDetailsEntity) session.getAttribute(CLIENT_KEY);
		String redirectURL = null;

		// if we have a client AND the client has post-logout redirect URIs
		// registered AND the URI given is in that list, then...
		if (isUriValid(redirectUri, client)) {
			UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(redirectUri);
			if (StringUtils.hasText(state)) {
				uri = uri.queryParam("state", state);
			}
			UriComponents uriComponents = uri.build();
			log.trace("redirect URL: {}", uriComponents);
			redirectURL = uriComponents.toString();
		}

		if (redirectURL != null) {
			String target = getRedirectUrl(redirectUri, state);
			if (StringUtils.hasText(approved)) {
				target = getLogoutUrl(target);
				log.trace("redirecting to logout SAML and then {}", target);
				return "redirect:" + target;
			} else {
				log.trace("redirecting to {}", target);
				return "redirect:" + redirectURL;
			}
		} else {
			if (StringUtils.hasText(approved)) {
				log.trace("redirecting to logout SAML only");
				return "redirect:" + getLogoutUrl(null);
			} else {
				return "logout_denied";
			}
		}
	}

	private boolean isUriValid(String redirectUri, ClientDetailsEntity client) {
		return StringUtils.hasText(redirectUri)
			&& client != null
			&& client.getPostLogoutRedirectUris() != null
			&& client.getPostLogoutRedirectUris().contains(redirectUri);
	}

	private String getLogoutUrl(String target) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(SAMLLogoutFilter.FILTER_URL);
		if (StringUtils.hasText(target)) {
			builder.queryParam(PARAM_TARGET, target);
		}
		return builder.build().toString();
	}

	private String getRedirectUrl(String postLogoutRedirectUri, String state) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(postLogoutRedirectUri);
		if (StringUtils.hasText(state)) {
			builder.queryParam(PARAM_STATE, state);
		}
		return builder.build().toString();
	}

}
