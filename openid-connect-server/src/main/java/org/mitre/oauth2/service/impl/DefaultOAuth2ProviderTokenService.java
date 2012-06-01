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

package org.mitre.oauth2.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntityFactory;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntityFactory;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

/**
 * @author jricher
 * 
 */
@Service
@Transactional
public class DefaultOAuth2ProviderTokenService implements OAuth2TokenEntityService {
	
	private static Logger logger = LoggerFactory.getLogger(DefaultOAuth2ProviderTokenService.class);

	@Autowired
	private OAuth2TokenRepository tokenRepository;
	
	@Autowired
	private ClientDetailsEntityService clientDetailsService;
	
	@Autowired
	private OAuth2AccessTokenEntityFactory accessTokenFactory;
	
	@Autowired
	private OAuth2RefreshTokenEntityFactory refreshTokenFactory;
	
	@Override
    public OAuth2AccessTokenEntity createAccessToken(OAuth2Authentication authentication) throws AuthenticationException, InvalidClientException {
		if (authentication != null || authentication.getAuthorizationRequest() != null) {
		    throw new AuthenticationCredentialsNotFoundException("No authentication credentials found");
		}
			// look up our client
			AuthorizationRequest clientAuth = authentication.getAuthorizationRequest();
			
			ClientDetailsEntity client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
			if (client == null) {
				throw new InvalidClientException("Client not found: " + clientAuth.getClientId());
			}
			
			OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();//accessTokenFactory.createNewAccessToken();
		    
		    // attach the client
	    	token.setClient(client);
	    	
		    // inherit the scope from the auth
	    	// this lets us match which scope is requested 
		    if (client.isScoped()) {
		    	
		    	// restrict granted scopes to a valid subset of those 
		    	Set<String> validScopes = Sets.newHashSet();
		    	
		    	for (String requested : clientAuth.getScope()) {
	                if (client.getScope().contains(requested)) {
	                	validScopes.add(requested);
	                } else {
	                	logger.warn("Client " + client.getClientId() + " requested out of permission scope: " + requested);
	                }
                }
		    	
		    	token.setScope(validScopes);
		    }

		    // make it expire if necessary
	    	if (client.getAccessTokenTimeout() != null) {
	    		Date expiration = new Date(System.currentTimeMillis() + (client.getAccessTokenTimeout() * 1000L));
	    		token.setExpiration(expiration);
	    	}
		    
	    	// attach the authorization so that we can look it up later
	    	token.setAuthentication(authentication);
	    	
	    	// attach a refresh token, if this client is allowed to request them
	    	if (client.isAllowRefresh()) {
	    		OAuth2RefreshTokenEntity refreshToken = refreshTokenFactory.createNewRefreshToken();
	    		
	    		// make it expire if necessary
	    		if (client.getRefreshTokenTimeout() != null) {
		    		Date expiration = new Date(System.currentTimeMillis() + (client.getRefreshTokenTimeout() * 1000L));
		    		refreshToken.setExpiration(expiration);
	    		}
	    		
	    		// save our scopes so that we can reuse them later for more auth tokens
	    		// TODO: save the auth instead of the just the scope?
			    if (client.isScoped()) {
			    	refreshToken.setScope(clientAuth.getScope());
			    }
			
			    tokenRepository.saveRefreshToken(refreshToken);
			    
	    		token.setRefreshToken(refreshToken);
	    	}
	    	
		    tokenRepository.saveAccessToken(token);		    
		    
		    return token;
    }

	@Override
    public OAuth2AccessTokenEntity refreshAccessToken(String refreshTokenValue, Set<String> scope) throws AuthenticationException {
		
		OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenByValue(refreshTokenValue);
		
		if (refreshToken == null) {
			throw new InvalidTokenException("Invalid refresh token: " + refreshTokenValue);
		}
		
		ClientDetailsEntity client = refreshToken.getClient();
		
		//Make sure this client allows access token refreshing
		if (!client.isAllowRefresh()) {
			throw new InvalidClientException("Client does not allow refreshing access token!");
		}
		
		// clear out any access tokens
		// TODO: make this a configurable option
		tokenRepository.clearAccessTokensForRefreshToken(refreshToken);
		
		if (refreshToken.isExpired()) {
			tokenRepository.removeRefreshToken(refreshToken);
			throw new InvalidTokenException("Expired refresh token: " + refreshTokenValue);			
		}
		
		// TODO: have the option to recycle the refresh token here, too
		// for now, we just reuse it as long as it's valid, which is the original intent

		OAuth2AccessTokenEntity token = accessTokenFactory.createNewAccessToken();

		
		if (scope != null && !scope.isEmpty()) { 
			// ensure a proper subset of scopes 
			if (refreshToken.getScope() != null && refreshToken.getScope().containsAll(scope)) {
				// set the scope of the new access token if requested
				refreshToken.setScope(scope);
			} else {
				// up-scoping is not allowed
				// (TODO: should this throw InvalidScopeException? For now just pass through)
				token.setScope(refreshToken.getScope());
			}
		} else {
			// otherwise inherit the scope of the refresh token (if it's there -- this can return a null scope set)
			token.setScope(refreshToken.getScope());
		}
	    
    	token.setClient(client);
    	
    	if (client.getAccessTokenTimeout() != null) {
    		Date expiration = new Date(System.currentTimeMillis() + (client.getAccessTokenTimeout() * 1000L));
    		token.setExpiration(expiration);
    	}
    	
    	token.setRefreshToken(refreshToken);
    	
    	tokenRepository.saveAccessToken(token);
    	
    	return token;
		
    }

	@Override
    public OAuth2Authentication loadAuthentication(String accessTokenValue) throws AuthenticationException {
		
		OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenByValue(accessTokenValue);
		
		if (accessToken == null) {
			throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
		}
		
		if (accessToken.isExpired()) {
			tokenRepository.removeAccessToken(accessToken);
			throw new InvalidTokenException("Expired access token: " + accessTokenValue);
		}
		
	    return accessToken.getAuthentication();
    }

	@Override
    public OAuth2AccessTokenEntity getAccessToken(String accessTokenValue) throws AuthenticationException {
		OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenByValue(accessTokenValue);
		if (accessToken == null) {
			throw new InvalidTokenException("Access token for value " + accessTokenValue + " was not found");
		}
		else {
			return accessToken;	
		}
    }

	@Override
	public OAuth2AccessTokenEntity getAccessToken(OAuth2Authentication authentication) {
		
		OAuth2AccessTokenEntity accessToken = tokenRepository.getByAuthentication(authentication);
		
		return accessToken;
	}
	
	@Override
    public OAuth2RefreshTokenEntity getRefreshToken(String refreshTokenValue) throws AuthenticationException {
		OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenByValue(refreshTokenValue);
		if (refreshToken == null) {
			throw new InvalidTokenException("Refresh token for value " + refreshTokenValue + " was not found");
		}
		else {
			return refreshToken;	
		}
    }
	
	@Override
    public void revokeRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
	    tokenRepository.clearAccessTokensForRefreshToken(refreshToken);
	    tokenRepository.removeRefreshToken(refreshToken);	    
    }

	@Override
    public void revokeAccessToken(OAuth2AccessTokenEntity accessToken) {
		tokenRepository.removeAccessToken(accessToken);	    
    }
	
	/* (non-Javadoc)
     * @see org.mitre.oauth2.service.OAuth2TokenEntityService#getAccessTokensForClient(org.mitre.oauth2.model.ClientDetailsEntity)
     */
    @Override
    public List<OAuth2AccessTokenEntity> getAccessTokensForClient(ClientDetailsEntity client) {
	    return tokenRepository.getAccessTokensForClient(client);
    }

	/* (non-Javadoc)
     * @see org.mitre.oauth2.service.OAuth2TokenEntityService#getRefreshTokensForClient(org.mitre.oauth2.model.ClientDetailsEntity)
     */
    @Override
    public List<OAuth2RefreshTokenEntity> getRefreshTokensForClient(ClientDetailsEntity client) {
    	return tokenRepository.getRefreshTokensForClient(client);
    }

    @Override
    @Scheduled(fixedRate = 5 * 60 * 1000) // schedule this task every five minutes
    public void clearExpiredTokens() {
    	logger.info("Cleaning out all expired tokens");
    	
    	List<OAuth2AccessTokenEntity> accessTokens = tokenRepository.getExpiredAccessTokens();
    	logger.info("Found " + accessTokens.size() + " expired access tokens");
    	for (OAuth2AccessTokenEntity oAuth2AccessTokenEntity : accessTokens) {
	        revokeAccessToken(oAuth2AccessTokenEntity);
        }
    	
    	List<OAuth2RefreshTokenEntity> refreshTokens = tokenRepository.getExpiredRefreshTokens();
    	logger.info("Found " + refreshTokens.size() + " expired refresh tokens");
    	for (OAuth2RefreshTokenEntity oAuth2RefreshTokenEntity : refreshTokens) {
	        revokeRefreshToken(oAuth2RefreshTokenEntity);
        }
    }
    
    /**
	 * Get a builder object for this class (for tests)
	 * @return
	 */
	public static DefaultOAuth2ProviderTokenServicesBuilder makeBuilder() {
		return new DefaultOAuth2ProviderTokenServicesBuilder();
	}
	
	/**
	 * Builder class for test harnesses.
	 */
	public static class DefaultOAuth2ProviderTokenServicesBuilder {
		private DefaultOAuth2ProviderTokenService instance;
		
		private DefaultOAuth2ProviderTokenServicesBuilder() {
			instance = new DefaultOAuth2ProviderTokenService();
		}
		
		public DefaultOAuth2ProviderTokenServicesBuilder setTokenRepository(OAuth2TokenRepository tokenRepository) {
			instance.tokenRepository = tokenRepository;
			return this;
		}
		
		public DefaultOAuth2ProviderTokenServicesBuilder setClientDetailsService(ClientDetailsEntityService clientDetailsService) {
			instance.clientDetailsService = clientDetailsService;
			return this;
		}
		
		public DefaultOAuth2ProviderTokenServicesBuilder setAccessTokenFactory(OAuth2AccessTokenEntityFactory accessTokenFactory) {
			instance.accessTokenFactory = accessTokenFactory;
			return this;
		}
		
		public DefaultOAuth2ProviderTokenServicesBuilder setRefreshTokenFactory(OAuth2RefreshTokenEntityFactory refreshTokenFactory) {
			instance.refreshTokenFactory = refreshTokenFactory;
			return this;
		}
		
		public OAuth2TokenEntityService finish() {
			return instance;
		}
	}

	@Override
	public OAuth2AccessToken readAccessToken(String accessToken) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
     * @see org.mitre.oauth2.service.OAuth2TokenEntityService#saveAccessToken(org.mitre.oauth2.model.OAuth2AccessTokenEntity)
     */
    @Override
    public OAuth2AccessTokenEntity saveAccessToken(OAuth2AccessTokenEntity accessToken) {
	    return tokenRepository.saveAccessToken(accessToken);	    
    }

	/* (non-Javadoc)
     * @see org.mitre.oauth2.service.OAuth2TokenEntityService#saveRefreshToken(org.mitre.oauth2.model.OAuth2RefreshTokenEntity)
     */
    @Override
    public OAuth2RefreshTokenEntity saveRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
    	return tokenRepository.saveRefreshToken(refreshToken);
    }
	
}
