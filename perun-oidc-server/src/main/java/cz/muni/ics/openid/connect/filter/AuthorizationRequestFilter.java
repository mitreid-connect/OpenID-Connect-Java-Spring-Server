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
package cz.muni.ics.openid.connect.filter;

import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.request.ConnectRequestParameters;
import cz.muni.ics.openid.connect.service.LoginHintExtracter;
import cz.muni.ics.openid.connect.service.impl.RemoveLoginHintsWithHTTP;
import cz.muni.ics.openid.connect.web.AuthenticationTimeStamper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * @author jricher
 *
 */
@Component("authRequestFilter")
@Slf4j
public class AuthorizationRequestFilter extends GenericFilterBean {

	public final static String PROMPTED = "PROMPT_FILTER_PROMPTED";
	public final static String PROMPT_REQUESTED = "PROMPT_FILTER_REQUESTED";

	@Autowired
	private OAuth2RequestFactory authRequestFactory;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private RedirectResolver redirectResolver;

	@Autowired(required = false)
	private LoginHintExtracter loginHintExtracter = new RemoveLoginHintsWithHTTP();

	private RequestMatcher requestMatcher = new AntPathRequestMatcher("/authorize");

	/**
	 *
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession();

		// skip everything that's not an authorize URL
		if (!requestMatcher.matches(request)) {
			chain.doFilter(req, res);
			return;
		}

		try {
			// we have to create our own auth request in order to get at all the parmeters appropriately
			AuthorizationRequest authRequest = null;

			ClientDetailsEntity client = null;

			authRequest = authRequestFactory.createAuthorizationRequest(createRequestMap(request.getParameterMap()));
			if (!Strings.isNullOrEmpty(authRequest.getClientId())) {
				client = clientService.loadClientByClientId(authRequest.getClientId());
			}

			// save the login hint to the session
			// but first check to see if the login hint makes any sense
			String loginHint = loginHintExtracter.extractHint((String) authRequest.getExtensions().get(ConnectRequestParameters.LOGIN_HINT));
			if (!Strings.isNullOrEmpty(loginHint)) {
				session.setAttribute(ConnectRequestParameters.LOGIN_HINT, loginHint);
			} else {
				session.removeAttribute(ConnectRequestParameters.LOGIN_HINT);
			}

			if (authRequest.getExtensions().get(ConnectRequestParameters.PROMPT) != null) {
				// we have a "prompt" parameter
				String prompt = (String)authRequest.getExtensions().get(ConnectRequestParameters.PROMPT);
				List<String> prompts = Splitter.on(ConnectRequestParameters.PROMPT_SEPARATOR).splitToList(Strings.nullToEmpty(prompt));

				if (prompts.contains(ConnectRequestParameters.PROMPT_NONE)) {
					// see if the user's logged in
					Authentication auth = SecurityContextHolder.getContext().getAuthentication();

					if (auth != null) {
						// user's been logged in already (by session management)
						// we're OK, continue without prompting
						chain.doFilter(req, res);
					} else {
						log.info("Client requested no prompt");
						// user hasn't been logged in, we need to "return an error"
						if (client != null && authRequest.getRedirectUri() != null) {

							// if we've got a redirect URI then we'll send it

							String url = redirectResolver.resolveRedirect(authRequest.getRedirectUri(), client);

							try {
								URIBuilder uriBuilder = new URIBuilder(url);

								uriBuilder.addParameter(ConnectRequestParameters.ERROR, ConnectRequestParameters.LOGIN_REQUIRED);
								if (!Strings.isNullOrEmpty(authRequest.getState())) {
									uriBuilder.addParameter(ConnectRequestParameters.STATE, authRequest.getState()); // copy the state parameter if one was given
								}

								response.sendRedirect(uriBuilder.toString());
								return;

							} catch (URISyntaxException e) {
								log.error("Can't build redirect URI for prompt=none, sending error instead", e);
								response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
								return;
							}
						}

						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
						return;
					}
				} else if (prompts.contains(ConnectRequestParameters.PROMPT_LOGIN)) {

					// first see if the user's already been prompted in this session
					if (session.getAttribute(PROMPTED) == null) {
						// user hasn't been PROMPTED yet, we need to check

						session.setAttribute(PROMPT_REQUESTED, Boolean.TRUE);

						// see if the user's logged in
						Authentication auth = SecurityContextHolder.getContext().getAuthentication();
						if (auth != null) {
							// user's been logged in already (by session management)
							// log them out and continue
							SecurityContextHolder.getContext().setAuthentication(null);
							chain.doFilter(req, res);
						} else {
							// user hasn't been logged in yet, we can keep going since we'll get there
							chain.doFilter(req, res);
						}
					} else {
						// user has been PROMPTED, we're fine

						// but first, undo the prompt tag
						session.removeAttribute(PROMPTED);
						chain.doFilter(req, res);
					}
				} else {
					// prompt parameter is a value we don't care about, not our business
					chain.doFilter(req, res);
				}

			} else if (authRequest.getExtensions().get(ConnectRequestParameters.MAX_AGE) != null ||
					(client != null && client.getDefaultMaxAge() != null)) {

				// default to the client's stored value, check the string parameter
				Integer max = (client != null ? client.getDefaultMaxAge() : null);
				String maxAge = (String) authRequest.getExtensions().get(ConnectRequestParameters.MAX_AGE);
				if (maxAge != null) {
					max = Integer.parseInt(maxAge);
				}

				if (max != null) {

					Date authTime = (Date) session.getAttribute(AuthenticationTimeStamper.AUTH_TIMESTAMP);

					Date now = new Date();
					if (authTime != null) {
						long seconds = (now.getTime() - authTime.getTime()) / 1000;
						if (seconds > max) {
							// session is too old, log the user out and continue
							SecurityContextHolder.getContext().setAuthentication(null);
						}
					}
				}
				chain.doFilter(req, res);
			} else {
				// no prompt parameter, not our business
				chain.doFilter(req, res);
			}

		} catch (InvalidClientException e) {
			// we couldn't find the client, move on and let the rest of the system catch the error
			chain.doFilter(req, res);
		}
	}

	/**
	 * @param parameterMap
	 * @return
	 */
	private Map<String, String> createRequestMap(Map<String, String[]> parameterMap) {
		Map<String, String> requestMap = new HashMap<>();
		for (String key : parameterMap.keySet()) {
			String[] val = parameterMap.get(key);
			if (val != null && val.length > 0) {
				requestMap.put(key, val[0]); // add the first value only (which is what Spring seems to do)
			}
		}

		return requestMap;
	}

	/**
	 * @return the requestMatcher
	 */
	public RequestMatcher getRequestMatcher() {
		return requestMatcher;
	}

	/**
	 * @param requestMatcher the requestMatcher to set
	 */
	public void setRequestMatcher(RequestMatcher requestMatcher) {
		this.requestMatcher = requestMatcher;
	}

}
