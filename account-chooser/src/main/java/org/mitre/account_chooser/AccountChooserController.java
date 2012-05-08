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
 * See README.md for configuration.
 * 
 */
@Controller
public class AccountChooserController {

	@Autowired
	OIDCServers servers;

	private static Log logger = LogFactory
			.getLog(AccountChooserController.class);

	/**
	 * Handles request to choose an Account
	 * 
	 * @param redirectUri
	 *            A redirection URI where the response will be sent
	 * @return
	 */
	@RequestMapping(value = "/", method = { RequestMethod.GET,
			RequestMethod.POST })
	public ModelAndView handleChooserRequest(
			@RequestParam("redirect_uri") String redirectUri) {

		ModelAndView modelAndView = new ModelAndView("form");
		modelAndView.addObject("servers", servers);
		modelAndView.addObject("redirect_uri", redirectUri);
		modelAndView.setViewName("chooser");

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
			@RequestParam("alias") String alias, HttpServletResponse response)
			throws IOException {

		response.sendRedirect(redirectUri + "?oidc_alias=" + alias);
	}
}
