/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
package org.mitre.account_chooser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Account Chooser UI application
 * 
 * @author nemonik
 * 
 *         See README.md for configuration.
 * 
 */
@Controller
public class AccountChooserController {

	/**
	 * Return the URL w/ GET parameters
	 * 
	 * @param baseURI
	 *            A String containing the protocol, server address, path, and
	 *            program as per "http://server/path/program"
	 * @param queryStringFields
	 *            A map where each key is the field name and the associated
	 *            key's value is the field value used to populate the URL's
	 *            query string
	 * @return A String representing the URL in form of
	 *         http://server/path/program?query_string from the messaged
	 *         parameters.
	 */
	public static String buildURL(String baseURI,
			Map<String, String> queryStringFields) {

		StringBuilder URLBuilder = new StringBuilder(baseURI);

		char appendChar = '?';

		for (Map.Entry<String, String> param : queryStringFields.entrySet()) {
			try {
				URLBuilder.append(appendChar).append(param.getKey())
						.append('=')
						.append(URLEncoder.encode(param.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException uee) {
				throw new IllegalStateException(uee);
			}
			appendChar = '&';
		}

		return URLBuilder.toString();
	}

	@Autowired
	AccountChooserConfig accountChooserConfig;

	private static Log logger = LogFactory
			.getLog(AccountChooserController.class);

	/**
	 * Handles request to choose an Account
	 * 
	 * @param redirectUri
	 *            A redirection URI where the response will be sent
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/", method = { RequestMethod.GET,
			RequestMethod.POST })
	public ModelAndView handleChooserRequest(
			@RequestParam("redirect_uri") String redirectUri,
			@RequestParam("client_id") String clientId,
			HttpServletResponse response) throws IOException {

		ModelAndView modelAndView = null;

		if (Arrays.asList(accountChooserConfig.getValidClientIds()).contains(
				clientId)) {

			// client_id supported

			modelAndView = new ModelAndView("chooser");
			modelAndView
					.addObject("issuers", accountChooserConfig.getIssuers());
			modelAndView.addObject("redirect_uri", redirectUri);
			modelAndView.addObject("client_id", clientId);

		} else {

			// client_id not supported

			Map<String, String> urlVariables = new HashMap<String, String>();

			urlVariables.put("error", "not_supported");
			urlVariables
					.put("error_description",
							"The client_id is not supported by the Account Chooser UI application.");

			modelAndView = new ModelAndView("error");

			modelAndView.addObject("error", urlVariables.get("error"));
			modelAndView.addObject("error_description",
					urlVariables.get("error_description"));
			modelAndView.addObject("client_uri", AccountChooserController
					.buildURL(redirectUri, urlVariables));

		}

		return modelAndView;

	}

	/**
	 * Handles form submits
	 * 
	 * @param redirectUri
	 *            A redirection URI where the response will be sent.
	 * @param alias
	 *            The OIDC alias selected.
	 * @param response
	 *            Provide the HTTP-specific functionality for sending a
	 *            response. In this case a redirect to redirect the End-User
	 *            back to the OpenID Connect Client.
	 * @throws IOException
	 *             If an output exception occurs in sending the redirect.
	 */
	@RequestMapping(value = "/selected")
	public void processSubmit(@RequestParam("redirect_uri") String redirectUri,
			@RequestParam("issuer") String issuer,
			@RequestParam("client_id") String clientId,
			HttpServletResponse response)
			throws IOException {

		// Handle Submit

		Map<String, String> urlVariables = new HashMap<String, String>();
		urlVariables.put("issuer", issuer);

		response.sendRedirect(AccountChooserController.buildURL(redirectUri,
				urlVariables));
	}

	/**
	 * Handles form submits
	 * 
	 * @param redirectUri
	 *            A redirection URI where the response will be sent.
	 * @param alias
	 *            The OIDC alias selected.
	 * @param response
	 *            Provide the HTTP-specific functionality for sending a
	 *            response. In this case a redirect to redirect the End-User
	 *            back to the OpenID Connect Client.
	 * @throws IOException
	 *             If an output exception occurs in sending the redirect.
	 */
	@RequestMapping(value = "/cancel")
	public void processCancel(@RequestParam("redirect_uri") String redirectUri,
			@RequestParam("issuer") String issuer, 
			@RequestParam("client_id") String clientId,
			HttpServletResponse response)
			throws IOException {

		// Handle Cancel

		Map<String, String> urlVariables = new HashMap<String, String>();
		urlVariables.put("error", "end_user_cancelled");
		urlVariables.put("error_description",
				"The end-user refused to select an Account.");

		response.sendRedirect(AccountChooserController.buildURL(redirectUri,
				urlVariables));

	}
}
