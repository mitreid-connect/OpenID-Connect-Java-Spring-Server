package org.mitre.openid.connect.client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.jwt.signer.impl.HmacSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public class OIDCEncryptedRequestFilter extends AbstractOIDCAuthenticationFilter {
	protected OIDCServerConfiguration oidcServerConfig;

	protected OIDCEncryptedRequestFilter() {
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

		} else if (StringUtils.isNotBlank(request.getParameter("token"))) {
			
			handleAuthorizationRequest(request, response, oidcServerConfig);
		
		}

		return null;
	}
	
	@Override
	public void handleAuthorizationRequest(HttpServletRequest request, HttpServletResponse response, 
			OIDCServerConfiguration serverConfiguration) throws IOException {
		
		if(StringUtils.isNotBlank(request.getParameter("token"))) {
			
			//TODO: encryption pull request needs to be accepted for these classes to be imported
			Jwe jwe = new Jwe();
			JweHeader header = jwe.getHeader();
			JwtClaims claims = jwe.getClaims();
			
			//set parameters to JweHeader
			header.setAlgorithm(JwsAlgorithm.getByName(SIGNING_ALGORITHM).toString());
			header.setIntegrity(/*TODO: put something here*/);
			header.setKeyDerivationFunction(/*TODO: put something here*/);
			header.setEncryptionMethod(/*TODO: put something here*/);
			
			//set parameters to JweClaims
			claims.setClaim("response_type", "token");
			claims.setClaim("client_id", serverConfiguration.getClientId());
			claims.setClaim("scope", scope);
			claims.setClaim("redirect_uri", AbstractOIDCAuthenticationFilter.buildRedirectURI(request, null));
			claims.setClaim("nonce", NONCE_SIGNATURE_COOKIE_NAME);
			
			//encrypt and sign jwe
			encryptAndSign(jwe, publicKey);
			
			Map<String, String> urlVariables = new HashMap<String, String>();
			
			urlVariables.put("request", jwe.toString());
			
			String authRequest = AbstractOIDCAuthenticationFilter.buildURL(serverConfiguration.getAuthorizationEndpointURI(), urlVariables);

			logger.debug("Auth Request:  " + authRequest);

			response.sendRedirect(authRequest);
		}

	}

}
