package org.mitre.openid.connect.client;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public class OIDCSignedRequestFilter extends AbstractOIDCAuthenticationFilter {
	
	protected OIDCServerConfiguration oidcServerConfig;
	
	private JwtSigningAndValidationService signingAndValidationService;

	protected OIDCSignedRequestFilter() {
		super();

		oidcServerConfig = new OIDCServerConfiguration();
	}

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
	
	@Override
	public void handleAuthorizationRequest(HttpServletRequest request, HttpServletResponse response, 
			OIDCServerConfiguration serverConfiguration) throws IOException {

			Jwt jwt = createAndSignRequestJwt(request, response, serverConfiguration);
			
			Map<String, String> urlVariables = new HashMap<String, String>();
			
			urlVariables.put("request", jwt.toString());
			
			String authRequest = AbstractOIDCAuthenticationFilter.buildURL(serverConfiguration.getAuthorizationEndpointURI(), urlVariables);

			logger.debug("Auth Request:  " + authRequest);

			response.sendRedirect(authRequest);
	}
	
	public Jwt createAndSignRequestJwt(HttpServletRequest request, HttpServletResponse response, OIDCServerConfiguration serverConfiguration) {
		Jwt jwt = new Jwt();
		JwtHeader header = jwt.getHeader();
		JwtClaims claims = jwt.getClaims();
		
		//set parameters to JwtHeader
//		header.setAlgorithm(JwsAlgorithm.getByName(SIGNING_ALGORITHM).toString());
		
		//set parameters to JwtClaims
		claims.setClaim("response_type", "code");
		claims.setClaim("client_id", serverConfiguration.getClientId());
		claims.setClaim("scope", scope);
		claims.setClaim("redirect_uri", AbstractOIDCAuthenticationFilter.buildRedirectURI(request, null));
		
		//create random nonce
		String nonce = new BigInteger(50, new SecureRandom()).toString(16);
		Cookie nonceCookie = new Cookie(NONCE_SIGNATURE_COOKIE_NAME, sign(signer, privateKey, nonce.getBytes()));
		
		response.addCookie(nonceCookie);
		
		claims.setClaim("nonce", nonceCookie);
		
		try {
			signingAndValidationService.signJwt(jwt);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jwt;
	}

	/**
	 * @return the signingAndValidationService
	 */
	public JwtSigningAndValidationService getSigningAndValidationService() {
		return signingAndValidationService;
	}

	/**
	 * @param signingAndValidationService the signingAndValidationService to set
	 */
	public void setSigningAndValidationService(JwtSigningAndValidationService signingAndValidationService) {
		this.signingAndValidationService = signingAndValidationService;
	}

	/**
	 * @param authorizationEndpointURI
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setAuthorizationEndpointURI(java.lang.String)
	 */
	public void setAuthorizationEndpointURI(String authorizationEndpointURI) {
		oidcServerConfig.setAuthorizationEndpointURI(authorizationEndpointURI);
	}

	/**
	 * @param clientId
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setClientId(java.lang.String)
	 */
	public void setClientId(String clientId) {
		oidcServerConfig.setClientId(clientId);
	}

	/**
	 * @param issuer
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setIssuer(java.lang.String)
	 */
	public void setIssuer(String issuer) {
		oidcServerConfig.setIssuer(issuer);
	}

	/**
	 * @param clientSecret
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setClientSecret(java.lang.String)
	 */
	public void setClientSecret(String clientSecret) {
		oidcServerConfig.setClientSecret(clientSecret);
	}

	/**
	 * @param tokenEndpointURI
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setTokenEndpointURI(java.lang.String)
	 */
	public void setTokenEndpointURI(String tokenEndpointURI) {
		oidcServerConfig.setTokenEndpointURI(tokenEndpointURI);
	}

	/**
	 * @param x509EncryptUrl
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setX509EncryptUrl(java.lang.String)
	 */
	public void setX509EncryptUrl(String x509EncryptUrl) {
		oidcServerConfig.setX509EncryptUrl(x509EncryptUrl);
	}

	/**
	 * @param x509SigningUrl
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setX509SigningUrl(java.lang.String)
	 */
	public void setX509SigningUrl(String x509SigningUrl) {
		oidcServerConfig.setX509SigningUrl(x509SigningUrl);
	}

	/**
	 * @param jwkEncryptUrl
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setJwkEncryptUrl(java.lang.String)
	 */
	public void setJwkEncryptUrl(String jwkEncryptUrl) {
		oidcServerConfig.setJwkEncryptUrl(jwkEncryptUrl);
	}

	/**
	 * @param jwkSigningUrl
	 * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setJwkSigningUrl(java.lang.String)
	 */
	public void setJwkSigningUrl(String jwkSigningUrl) {
		oidcServerConfig.setJwkSigningUrl(jwkSigningUrl);
	}

}
