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

public class OIDCSignedRequestFilter extends AbstractOIDCAuthenticationFilter {
	
	protected OIDCServerConfiguration oidcServerConfig;

	/**
	 * OpenIdConnectSignedRequestFilter constructor
	 */
	protected OIDCSignedRequestFilter() {
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

		Assert.notNull(oidcServerConfig.getAuthorizationEndpointURI(),
				"An Authorization Endpoint URI must be supplied");

		Assert.notNull(oidcServerConfig.getTokenEndpointURI(),
				"A Token ID Endpoint URI must be supplied");
		
		Assert.notNull(oidcServerConfig.getClientId(),
				"A Client ID must be supplied");

		Assert.notNull(oidcServerConfig.getClientSecret(),
				"A Client Secret must be supplied");
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
				return handleAuthorizationGrantResponse(request.getParameter("code"), new SanatizedRequest(request,	new String[] { "code" }), oidcServerConfig);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			handleAuthorizationRequest(request, response, oidcServerConfig);
		}

		return null;
	}

	public void setAuthorizationEndpointURI(String authorizationEndpointURI) {
		oidcServerConfig.setAuthorizationEndpointURI(authorizationEndpointURI);
	}

	public void setClientId(String clientId) {
		oidcServerConfig.setClientId(clientId);
	}

	public void setClientSecret(String clientSecret) {
		oidcServerConfig.setClientSecret(clientSecret);
	}

	public void setErrorRedirectURI(String errorRedirectURI) {
		this.errorRedirectURI = errorRedirectURI;
	}

	public void setTokenEndpointURI(String tokenEndpointURI) {
		oidcServerConfig.setTokenEndpointURI(tokenEndpointURI);
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

}
