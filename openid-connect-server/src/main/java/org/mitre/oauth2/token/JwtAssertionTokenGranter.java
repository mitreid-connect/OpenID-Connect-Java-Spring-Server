/**
 * 
 */
package org.mitre.oauth2.token;

import java.util.Date;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;

/**
 * @author jricher
 *
 */
@Component("jwtAssertionTokenGranter")
public class JwtAssertionTokenGranter extends AbstractTokenGranter {

	private static final String grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer";

	// keep down-cast versions so we can get to the right queries
	private OAuth2TokenEntityService tokenServices;
	private ClientDetailsEntityService clientDetailsService;
	
	@Autowired
	private JwtSigningAndValidationService jwtService;
	
	@Autowired
	private ConfigurationPropertiesBean config;
	
	public JwtAssertionTokenGranter(OAuth2TokenEntityService tokenServices, ClientDetailsEntityService clientDetailsService) {
	    super(tokenServices, clientDetailsService, grantType);
	    this.tokenServices = tokenServices;
	    this.clientDetailsService = clientDetailsService;
    }

	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getOAuth2Authentication(org.springframework.security.oauth2.provider.AuthorizationRequest)
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(AuthorizationRequest authorizationRequest) {
    	// read and load up the existing token
	    String incomingTokenValue = authorizationRequest.getAuthorizationParameters().get("assertion");
	    
	    if (jwtService.validateSignature(incomingTokenValue)) {

	    	Jwt jwt = Jwt.parse(incomingTokenValue);
	    	
	    	OAuth2AccessTokenEntity oldToken = tokenServices.readAccessToken(incomingTokenValue);
	    	
	    	if (oldToken.getScope().contains("id-token")) {
	    		// TODO: things
	    	}
	    	
	    	// TODO: should any of these throw an exception instead of returning null?
	    	JwtClaims claims = jwt.getClaims();
	    	if (!config.getIssuer().equals(claims.getIssuer())) {
	    		// issuer isn't us
	    		return null;
	    	}
	    	
	    	if (!authorizationRequest.getClientId().equals(claims.getAudience())) {
	    		// audience isn't the client
	    		return null;
	    	}
	    	
	    	Date now = new Date();
	    	if (!now.after(claims.getExpiration())) {
	    		// token is expired
	    		return null;
	    	}

	    	// FIXME
	    	// This doesn't work. We need to look up the old token, figure out its scopes and bind it appropriately.
	    	// In the case of an ID token, we need to look up its parent access token and change the reference, and revoke the old one, and
	    	// that's tricky.
	    	// we might need new calls on the token services layer to handle this, and we might
	    	// need to handle id tokens separately.
	    	return new OAuth2Authentication(authorizationRequest, null);
	    	
	    } else {
	    	return null; // throw error??
	    }
	    
	    
    }

	
	
}
