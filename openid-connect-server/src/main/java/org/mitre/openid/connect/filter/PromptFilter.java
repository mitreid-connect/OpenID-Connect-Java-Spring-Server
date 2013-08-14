/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mitre.openid.connect.web.AuthenticationTimeStamper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

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
	
	/**
	 * 
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (!Strings.isNullOrEmpty(request.getParameter("prompt"))) {
			// we have a "prompt" parameter

			if (request.getParameter("prompt").equals("none")) {
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
			} else if (request.getParameter("prompt").equals("login")) {

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

		} else if (!Strings.isNullOrEmpty(request.getParameter("max_age"))) {
			// TODO: issue #450
			String maxAge = request.getParameter("max_age");
			HttpSession session = request.getSession();
			Date authTime = (Date) session.getAttribute(AuthenticationTimeStamper.AUTH_TIMESTAMP);

			Date now = new Date();
			if (authTime != null) {
				Integer max = Integer.parseInt(maxAge);
				long seconds = (now.getTime() - authTime.getTime()) / 1000;
				if (seconds > max) {
					// session is too old, log the user out and continue
        			SecurityContextHolder.getContext().setAuthentication(null);
				}
			}
			
			chain.doFilter(req, res);
    	} else {
			// no prompt parameter, not our business
			chain.doFilter(req, res);
		}

	}

}
