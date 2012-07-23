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
			
			Jwe jwe = new Jwe();
			JweHeader header = jwe.getHeader();
			JwtClaims claims = jwe.getClaims();
			
			//set parameters to JwtHeader
			header.setAlgorithm(JwsAlgorithm.getByName(SIGNING_ALGORITHM).toString());
			
			//set parameters to JwtClaims
			claims.setClaim("response_type", "token");
			claims.setClaim("client_id", serverConfiguration.getClientId());
			claims.setClaim("scope", scope);
			claims.setClaim("redirect_uri", AbstractOIDCAuthenticationFilter.buildRedirectURI(request, null));
			claims.setClaim("nonce", NONCE_SIGNATURE_COOKIE_NAME);
			
			if(header.getAlgorithm().equals("RS256") || header.getAlgorithm().equals("RS384") || header.getAlgorithm().equals("RS512")) {
				RsaSigner jwtSigner = new RsaSigner();
				try {
					jwt = jwtSigner.sign(jwt);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(header.getAlgorithm().equals("HS256") || header.getAlgorithm().equals("HS384") || header.getAlgorithm().equals("HS512")) {
				HmacSigner jwtSigner = new HmacSigner();
				try {
					jwt = jwtSigner.sign(jwt);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				throw new IllegalArgumentException(header.getAlgorithm() + " is not a valid signing algorithm.");
			}
			
			Map<String, String> urlVariables = new HashMap<String, String>();
			
			urlVariables.put("request", jwt.toString());
			
			String authRequest = AbstractOIDCAuthenticationFilter.buildURL(serverConfiguration.getAuthorizationEndpointURI(), urlVariables);

			logger.debug("Auth Request:  " + authRequest);

			response.sendRedirect(authRequest);
		}

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

    public void setIssuer(String issuer) {
	    oidcServerConfig.setIssuer(issuer);
    }

}
