/**
 * 
 */
package org.mitre.oauth2.token;

import java.util.HashSet;
import java.util.Set;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

/**
 * @author jricher
 *
 */
@Component("chainedTokenGranter")
public class ChainedTokenGranter extends AbstractTokenGranter {

	private static final String grantType = "urn:ietf:params:oauth:grant_type:redelegate";
	
	// keep down-cast versions so we can get to the right queries
	private OAuth2TokenEntityService tokenServices;
	
	
	
	/**
	 * @param tokenServices
	 * @param clientDetailsService
	 * @param grantType
	 */
	@Autowired
	public ChainedTokenGranter(OAuth2TokenEntityService tokenServices, ClientDetailsEntityService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
		this.tokenServices = tokenServices;
	}
	
	/* (non-Javadoc)
     * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getOAuth2Authentication(org.springframework.security.oauth2.provider.AuthorizationRequest)
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) throws AuthenticationException, InvalidTokenException {
    	// read and load up the existing token
	    String incomingTokenValue = tokenRequest.getRequestParameters().get("token");
	    OAuth2AccessTokenEntity incomingToken = tokenServices.readAccessToken(incomingTokenValue);
	    
	    // check for scoping in the request, can't up-scope with a chained request
	    Set<String> approvedScopes = incomingToken.getScope();
	    Set<String> requestedScopes = tokenRequest.getScope();
	    
	    if (requestedScopes == null) {
	    	requestedScopes = new HashSet<String>();
	    }
	    
	    // do a check on the requested scopes -- if they exactly match the client scopes, they were probably shadowed by the token granter
	    if (client.getScope().equals(requestedScopes)) {
	    	requestedScopes = new HashSet<String>();
	    }
	    
	    // if our scopes are a valid subset of what's allowed, we can continue
	    if (approvedScopes.containsAll(requestedScopes)) {

	    	if (requestedScopes.isEmpty()) {
	    		// if there are no scopes, inherit the original scopes from the token
	    		tokenRequest.setScope(approvedScopes);
	    	} else {
	    		// if scopes were asked for, give only the subset of scopes requested
	    		// this allows safe downscoping
	    		tokenRequest.setScope(Sets.intersection(requestedScopes, approvedScopes));
	    	}
		    
		    // NOTE: don't revoke the existing access token

	    	// create a new access token
	    	OAuth2Authentication authentication = new OAuth2Authentication(getRequestFactory().createOAuth2Request(client, tokenRequest), incomingToken.getAuthenticationHolder().getAuthentication().getUserAuthentication()); 
	    	
	    	return authentication;
	    	
	    } else {
	    	throw new InvalidScopeException("Invalid scope requested in chained request", approvedScopes);
	    }
	    
    }
	
}
