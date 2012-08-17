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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

/**
 * The OpenID Connect Authentication Filter
 * 
 * See README.md to to configure
 * 
 * @author nemonik
 * 
 */
public class OIDCAuthenticationFilter extends AbstractOIDCAuthenticationFilter {

	protected OIDCServerConfiguration oidcServerConfig;

	/**
	 * OpenIdConnectAuthenticationFilter constructor
	 */
	protected OIDCAuthenticationFilter() {
		super();

		oidcServerConfig = new OIDCServerConfiguration();
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

		Assert.notNull(oidcServerConfig.getAuthorizationEndpointUrl(), "An Authorization Endpoint URI must be supplied");

		Assert.notNull(oidcServerConfig.getTokenEndpointUrl(), "A Token ID Endpoint URI must be supplied");
		
		Assert.notNull(oidcServerConfig.getClientId(), "A Client ID must be supplied");

		Assert.notNull(oidcServerConfig.getClientSecret(), "A Client Secret must be supplied");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mitre.openid.connect.client.AbstractOIDCAuthenticationFilter#
	 * attemptAuthentication(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {

		// Enter AuthenticationFilter here...

		super.attemptAuthentication(request, response);

		if (StringUtils.isNotBlank(request.getParameter("error"))) {

			handleError(request, response);

		} else if (StringUtils.isNotBlank(request.getParameter("code"))) {

			try {
				return handleAuthorizationGrantResponse(request.getParameter("code"), request, oidcServerConfig);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			handleAuthorizationRequest(request, response, oidcServerConfig);
		}

		return null;
	}

	public void setAuthorizationEndpointUrl(String authorizationEndpointUrl) {
		oidcServerConfig.setAuthorizationEndpointUrl(authorizationEndpointUrl);
	}

	public void setTokenEndpointUrl(String tokenEndpointUrl) {
		oidcServerConfig.setTokenEndpointUrl(tokenEndpointUrl);
	}
	
	public void setClientId(String clientId) {
		oidcServerConfig.setClientId(clientId);
	}

	public void setClientSecret(String clientSecret) {
		oidcServerConfig.setClientSecret(clientSecret);
	}

	public void setX509EncryptUrl(String x509EncryptUrl) {
		oidcServerConfig.setX509EncryptUrl(x509EncryptUrl);
	}
	
	public void setX509SigningUrl(String x509SigningUrl) {
		oidcServerConfig.setX509SigningUrl(x509SigningUrl);
	}
	
	public void setJwkEncryptUrl(String jwkEncryptUrl) {
		oidcServerConfig.setJwkEncryptUrl(jwkEncryptUrl);
	}
	
	public void setJwkSigningUrl(String jwkSigningUrl) {
		oidcServerConfig.setJwkSigningUrl(jwkSigningUrl);
	}

	/**
     * @param issuer
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setIssuer(java.lang.String)
     */
    public void setIssuer(String issuer) {
	    oidcServerConfig.setIssuer(issuer);
    }

	/**
     * @param userInfoUrl
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setUserInfoUrl(java.lang.String)
     */
    public void setUserInfoUrl(String userInfoUrl) {
	    oidcServerConfig.setUserInfoUrl(userInfoUrl);
    }
}
