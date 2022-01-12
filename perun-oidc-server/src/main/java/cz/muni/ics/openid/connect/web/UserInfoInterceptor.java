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
package cz.muni.ics.openid.connect.web;

import static cz.muni.ics.openid.connect.request.ConnectRequestParameters.CLIENT_ID;
import static cz.muni.ics.openid.connect.request.ConnectRequestParameters.SCOPE;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.openid.connect.model.OIDCAuthenticationToken;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.request.ConnectRequestParameters;
import cz.muni.ics.openid.connect.service.UserInfoService;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Injects the UserInfo object for the current user into the current model's context, if both exist. Allows JSPs and the like to call "userInfo.name" and other fields.
 *
 * @author jricher
 *
 */
@Slf4j
public class UserInfoInterceptor extends HandlerInterceptorAdapter {

	private final Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(GrantedAuthority.class,
				(JsonSerializer<GrantedAuthority>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAuthority()))
			.create();

	@Autowired
	private UserInfoService userInfoService;

	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null){
			request.setAttribute("userAuthorities", gson.toJson(auth.getAuthorities()));
		}

		if (!trustResolver.isAnonymous(auth)) { // skip lookup on anonymous logins
			if (auth instanceof OIDCAuthenticationToken) {
				// if they're logging into this server from a remote OIDC server, pass through their user info
				OIDCAuthenticationToken oidc = (OIDCAuthenticationToken) auth;
				if (oidc.getUserInfo() != null) {
					request.setAttribute("userInfo", oidc.getUserInfo());
					request.setAttribute("userInfoJson", oidc.getUserInfo().toJson());
				} else {
					request.setAttribute("userInfo", null);
					request.setAttribute("userInfoJson", "null");
				}
			} else {
				if (auth == null || auth.getName() == null || userInfoService == null) {
					log.debug("No point to handle, skip");
				} else {
					if (request.getAttribute("userInfo") == null && request.getAttribute("userInfoJson") == null) {
						String clientId = request.getParameter(CLIENT_ID);
						if (clientId == null) {
							log.debug("No client provided, no reason to continue processing");
							return true;
						}
						Set<String> scopes = OAuth2Utils.parseParameterList(request.getParameter(ConnectRequestParameters.SCOPE));
						UserInfo user = userInfoService.get(auth.getName(), clientId, scopes, (SAMLCredential) auth.getCredentials());
						if (user != null) {
							request.setAttribute("userInfo", user);
							request.setAttribute("userInfoJson", user.toJson());
						}
					} else {
						log.debug("Already has userInfo or userInfoJson");
						log.trace("userInfo: {}", request.getAttribute("userInfo"));
						log.trace("userInfoJson: {}", request.getAttribute("userInfoJson"));
					}
				}
			}
		}

		return true;
	}

}
