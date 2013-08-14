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
package org.mitre.openid.connect.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;


/**
 * This @Controller is a hack to get around SECOAUTH's AuthorizationEndpoint requiring the response_type parameter to be passed in.
 * 
 * @author jricher
 *
 */
@Controller("requestObjectAuthorzationEndpoint")
//@Component
public class RequestObjectAuthorizationEndpoint {

	private static Logger logger = LoggerFactory.getLogger(RequestObjectAuthorizationEndpoint.class);

	@RequestMapping(value = "/authorize", params = "request")
	public String authorizeRequestObject(@RequestParam("request") String jwtString, @RequestParam(value = "response_type", required = false) String responseType, HttpServletRequest request, ModelAndView mav) {

		String query = request.getQueryString();

		if (responseType == null) {
			try {
				JWT requestObject = JWTParser.parse(jwtString);
				responseType = (String)requestObject.getJWTClaimsSet().getClaim("response_type");

				URI uri = new URIBuilder(Strings.nullToEmpty(request.getServletPath()) + Strings.nullToEmpty(request.getPathInfo()) + "?" + query)
				.addParameter("response_type", responseType)
				.build();

				query = uri.getRawQuery();//uri.toString();

			} catch (ParseException e) {
				logger.error("ParseException while attempting to authorize request object", e);
				mav.addObject("code", HttpStatus.BAD_REQUEST);
				return "httpCodeView";

			} catch (URISyntaxException e) {
				logger.error("URISyntaxError while attempting to authorize request object", e);
				mav.addObject("code", HttpStatus.BAD_REQUEST);
				return "httpCodeView";
			}
		}

		return "forward:/oauth/authorize?" + query;

	}

}
