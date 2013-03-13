/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.client.utils.URIBuilder;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.client.service.AuthRequestUrlBuilder;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.provider.ClientDetails;

import com.google.common.base.Joiner;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
public class SignedAuthRequestUrlBuilder implements AuthRequestUrlBuilder {

	private JwtSigningAndValidationService signingAndValidationService;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.AuthRequestUrlBuilder#buildAuthRequestUrl(org.mitre.openid.connect.config.ServerConfiguration, org.springframework.security.oauth2.provider.ClientDetails, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, ClientDetails clientConfig, String redirectUri, String nonce, String state) {

		// create our signed JWT for the request object
		JWTClaimsSet claims = new JWTClaimsSet();
		
		//set parameters to JwtClaims
		claims.setCustomClaim("response_type", "code");
		claims.setCustomClaim("client_id", clientConfig.getClientId());
		claims.setCustomClaim("scope", Joiner.on(" ").join(clientConfig.getScope()));
		
		// build our redirect URI
		claims.setCustomClaim("redirect_uri", redirectUri);
		
		// this comes back in the id token
		claims.setCustomClaim("nonce", nonce);
		
		// this comes back in the auth request return
		claims.setCustomClaim("state", state);
		
		SignedJWT jwt = new SignedJWT(new JWSHeader(signingAndValidationService.getDefaultSigningAlgorithm()), claims);
		
		try {
			signingAndValidationService.signJwt(jwt);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
	        URIBuilder uriBuilder = new URIBuilder(serverConfig.getAuthorizationEndpointUri());
	        uriBuilder.addParameter("request", jwt.serialize());
	        
	        // build out the URI
	        return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
        	throw new AuthenticationServiceException("Malformed Authorization Endpoint Uri", e);
        }
	}

}
