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
package org.mitre.openid.connect.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

/**
 * The OpenID Connect Authentication Filter using Acount Chooser UI Application
 * 
 * See README.md to to configure
 * 
 * @author nemonik
 * 
 */
public class OIDCAuthenticationUsingChooserFilter extends
		AbstractOIDCAuthenticationFilter {

	protected final static String ISSUER_COOKIE_NAME = "issuer";

	protected String accountChooserURI;

	protected String accountChooserClientID;

	protected Map<String, ? extends OIDCServerConfiguration> oidcServerConfigs = new HashMap<String, OIDCServerConfiguration>();

	/**
	 * OpenIdConnectAuthenticationFilter constructor
	 */
	protected OIDCAuthenticationUsingChooserFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mitre.openid.connect.client.AbstractOIDCAuthenticationFilter#
	 * afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		// Validating configuration

		Assert.notNull(
				oidcServerConfigs,
				"Server Configurations must be supplied if the Account Chooser UI Application is to be used.");

		Assert.notNull(
				accountChooserURI,
				"Account Chooser URI must be supplied if the Account Chooser UI Application is to be used.");

		Assert.notNull(
				accountChooserClientID,
				"Account Chooser Client ID must be supplied if the Account Chooser UI Application is to be used.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.web.authentication.
	 * AbstractAuthenticationProcessingFilter
	 * #attemptAuthentication(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws IOException,
			AuthenticationException, ServletException {

		// Enter AuthenticationFilter here...
		super.attemptAuthentication(request, response);

		if (StringUtils.isNotBlank(request.getParameter("error"))) {

			handleError(request, response);

		} else if (request.getParameter("code") != null) {

			// Which OIDC configuration?
			Cookie issuerCookie = WebUtils.getCookie(request,
					ISSUER_COOKIE_NAME);

			try {
				return handleAuthorizationGrantResponse(request.getParameter("code"), new SanatizedRequest(request,	new String[] { "code" }),
						oidcServerConfigs.get(issuerCookie.getValue()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			String issuer = request.getParameter("issuer");

			if (StringUtils.isNotBlank(issuer)) {

				// Account Chooser UI provided and Issuer Identifier

				OIDCServerConfiguration oidcServerConfig = oidcServerConfigs
						.get(issuer);

				if (oidcServerConfig != null) {

					// The Client is configured to support this Issuer
					// Identifier

					Cookie issuerCookie = new Cookie(ISSUER_COOKIE_NAME, issuer);
					response.addCookie(issuerCookie);

					handleAuthorizationRequest(new SanatizedRequest(request,
							new String[] { "issuer" }), response,
							oidcServerConfig);

				} else {

					// The Client is NOT configured to support this Issuer
					// Identifier

					throw new AuthenticationServiceException(
							"Security Filter not configured for issuer: "
									+ issuer);
				}

			} else {

				// Redirect the End-User to the configured Account Chooser UI
				// application

				Map<String, String> urlVariables = new HashMap<String, String>();

				urlVariables.put("redirect_uri",
						OIDCAuthenticationUsingChooserFilter.buildRedirectURI(
								request, null));

				urlVariables.put("client_id", accountChooserClientID);

				response.sendRedirect(OIDCAuthenticationUsingChooserFilter
						.buildURL(accountChooserURI, urlVariables));
			}
		}

		return null;
	}

	public void setAccountChooserClientID(String accountChooserClientID) {
		this.accountChooserClientID = accountChooserClientID;
	}

	public void setAccountChooserURI(String accountChooserURI) {
		this.accountChooserURI = accountChooserURI;
	}

	public void setOidcServerConfigs(
			Map<String, ? extends OIDCServerConfiguration> oidcServerConfigs) {
		this.oidcServerConfigs = oidcServerConfigs;
	}
}
