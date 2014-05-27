/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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
 ******************************************************************************/
/**
 * 
 */
package org.mitre.openid.connect.filter;

import java.io.IOException;
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

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.web.AuthenticationTimeStamper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * @author jricher
 *
 */
@Component("promptFilter")
public class PromptFilter extends GenericFilterBean {

	private Logger logger = LoggerFactory.getLogger(PromptFilter.class);

	public final static String PROMPTED = "PROMPT_FILTER_PROMPTED";
	public final static String PROMPT_REQUESTED = "PROMPT_FILTER_REQUESTED";

	@Autowired
	private OAuth2RequestFactory authRequestFactory;

	@Autowired
	private ClientDetailsEntityService clientService;

	/**
	 * 
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		// skip everything that's not an authorize URL
		if (!request.getServletPath().startsWith("/authorize")) {
			chain.doFilter(req, res);
			return;
		}

		// we have to create our own auth request in order to get at all the parmeters appropriately
		AuthorizationRequest authRequest = authRequestFactory.createAuthorizationRequest(createRequestMap(request.getParameterMap()));

		ClientDetailsEntity client = null;

		try {
			client = clientService.loadClientByClientId(authRequest.getClientId());
		} catch (InvalidClientException e) {
			// no need to worry about this here, it would be caught elsewhere
		} catch (IllegalArgumentException e) {
			// no need to worry about this here, it would be caught elsewhere
		}

		if (authRequest.getExtensions().get("prompt") != null) {
			// we have a "prompt" parameter
			String prompt = (String)authRequest.getExtensions().get("prompt");
			List<String> prompts = Splitter.on(" ").splitToList(Strings.nullToEmpty(prompt));

			if (prompts.contains("none")) {
				logger.info("Client requested no prompt");
				// see if the user's logged in
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();

				if (auth != null) {
					// user's been logged in already (by session management)
					// we're OK, continue without prompting
					chain.doFilter(req, res);
				} else {
					// user hasn't been logged in, we need to "return an error"
					logger.info("User not logged in, no prompt requested, returning 403 from filter");
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
					return;
				}
			} else if (prompts.contains("login")) {

				// first see if the user's already been prompted in this session
				HttpSession session = request.getSession();
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

		} else if (authRequest.getExtensions().get("max_age") != null ||
				(client != null && client.getDefaultMaxAge() != null)) {

			// default to the client's stored value, check the string parameter
			Integer max = (client != null ? client.getDefaultMaxAge() : null);
			String maxAge = (String) authRequest.getExtensions().get("max_age");
			if (maxAge != null) {
				max = Integer.parseInt(maxAge);
			}

			if (max != null) {

				HttpSession session = request.getSession();
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

	}

	/**
	 * @param parameterMap
	 * @return
	 */
	private Map<String, String> createRequestMap(Map<String, String[]> parameterMap) {
		Map<String, String> requestMap = new HashMap<String, String>();
		for (String key : parameterMap.keySet()) {
			String[] val = parameterMap.get(key);
			if (val != null && val.length > 0) {
				requestMap.put(key, val[0]); // add the first value only (which is what Spring seems to do)
			}
		}

		return requestMap;
	}

}
