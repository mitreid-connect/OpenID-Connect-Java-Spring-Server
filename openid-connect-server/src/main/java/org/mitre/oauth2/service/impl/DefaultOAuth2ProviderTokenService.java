/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
/**
 * 
 */
package org.mitre.oauth2.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;


/**
 * @author jricher
 * 
 */
@Service
public class DefaultOAuth2ProviderTokenService implements OAuth2TokenEntityService {

	private static Logger logger = LoggerFactory.getLogger(DefaultOAuth2ProviderTokenService.class);

	@Autowired
	private OAuth2TokenRepository tokenRepository;

	@Autowired
	private AuthenticationHolderRepository authenticationHolderRepository;

	@Autowired
	private ClientDetailsEntityService clientDetailsService;

	@Autowired
	private TokenEnhancer tokenEnhancer;
	
	@Override
	public Set<OAuth2AccessTokenEntity> getAllAccessTokensForUser(String id) {

		Set<OAuth2AccessTokenEntity> all = tokenRepository.getAllAccessTokens();
		Set<OAuth2AccessTokenEntity> results = Sets.newLinkedHashSet();

		for (OAuth2AccessTokenEntity token : all) {
			if (token.getAuthenticationHolder().getAuthentication().getName().equals(id)) {
				results.add(token);
			}
		}

		return results;
	}


	@Override
	public Set<OAuth2RefreshTokenEntity> getAllRefreshTokensForUser(String id) {
		Set<OAuth2RefreshTokenEntity> all = tokenRepository.getAllRefreshTokens();
		Set<OAuth2RefreshTokenEntity> results = Sets.newLinkedHashSet();

		for (OAuth2RefreshTokenEntity token : all) {
			if (token.getAuthenticationHolder().getAuthentication().getName().equals(id)) {
				results.add(token);
			}
		}

		return results;
	}

	@Override
	public OAuth2AccessTokenEntity getAccessTokenById(Long id) {
		return tokenRepository.getAccessTokenById(id);
	}

	@Override
	public OAuth2RefreshTokenEntity getRefreshTokenById(Long id) {
		return tokenRepository.getRefreshTokenById(id);
	}

	@Autowired
	private ApprovedSiteService approvedSiteService;
	

	@Override
	public OAuth2AccessTokenEntity createAccessToken(OAuth2Authentication authentication) throws AuthenticationException, InvalidClientException {
		if (authentication != null && authentication.getOAuth2Request() != null) {
			// look up our client
			OAuth2Request clientAuth = authentication.getOAuth2Request();

			ClientDetailsEntity client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());

			if (client == null) {
				throw new InvalidClientException("Client not found: " + clientAuth.getClientId());
			}

			OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();//accessTokenFactory.createNewAccessToken();

			// attach the client
			token.setClient(client);

			// inherit the scope from the auth, but make a new set so it is
			//not unmodifiable. Unmodifiables don't play nicely with Eclipselink, which
			//wants to use the clone operation.
			Set<String> scopes = Sets.newHashSet(clientAuth.getScope());
			token.setScope(scopes);

			// make it expire if necessary 
			if (client.getAccessTokenValiditySeconds() != null && client.getAccessTokenValiditySeconds() > 0) {
				Date expiration = new Date(System.currentTimeMillis() + (client.getAccessTokenValiditySeconds() * 1000L));
				token.setExpiration(expiration);
			}

			// attach the authorization so that we can look it up later
			AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
			authHolder.setAuthentication(authentication);
			authHolder = authenticationHolderRepository.save(authHolder);

			token.setAuthenticationHolder(authHolder);

			// attach a refresh token, if this client is allowed to request them and the user gets the offline scope
			// TODO: tie this to some kind of scope service
			if (client.isAllowRefresh() && scopes.contains("offline_access")) {
				OAuth2RefreshTokenEntity refreshToken = new OAuth2RefreshTokenEntity(); //refreshTokenFactory.createNewRefreshToken();
				JWTClaimsSet refreshClaims = new JWTClaimsSet();


				// make it expire if necessary
				if (client.getRefreshTokenValiditySeconds() != null) {
					Date expiration = new Date(System.currentTimeMillis() + (client.getRefreshTokenValiditySeconds() * 1000L));
					refreshToken.setExpiration(expiration);
					refreshClaims.setExpirationTime(expiration);
				}

				// set a random identifier
				refreshClaims.setJWTID(UUID.randomUUID().toString());

				// TODO: add issuer fields, signature to JWT

				PlainJWT refreshJwt = new PlainJWT(refreshClaims);
				refreshToken.setJwt(refreshJwt);

				//Add the authentication
				refreshToken.setAuthenticationHolder(authHolder);
				refreshToken.setClient(client);



				// save the token first so that we can set it to a member of the access token (NOTE: is this step necessary?)
				tokenRepository.saveRefreshToken(refreshToken);

				token.setRefreshToken(refreshToken);
			}

			tokenEnhancer.enhance(token, authentication);

			tokenRepository.saveAccessToken(token);

			//Add approved site reference, if any
			OAuth2Request originalAuthRequest = authHolder.getAuthentication().getOAuth2Request();

			if (originalAuthRequest.getExtensions() != null && originalAuthRequest.getExtensions().containsKey("approved_site")) {

				Long apId = (Long) originalAuthRequest.getExtensions().get("approved_site");
				ApprovedSite ap = approvedSiteService.getById(apId);
				Set<OAuth2AccessTokenEntity> apTokens = ap.getApprovedAccessTokens();
				apTokens.add(token);
				ap.setApprovedAccessTokens(apTokens);
				approvedSiteService.save(ap);

			}

			if (token.getRefreshToken() != null) {
				tokenRepository.saveRefreshToken(token.getRefreshToken()); // make sure we save any changes that might have been enhanced
			}

			return token;
		}

		throw new AuthenticationCredentialsNotFoundException("No authentication credentials found");
	}

	@Override
	public OAuth2AccessTokenEntity refreshAccessToken(String refreshTokenValue, TokenRequest authRequest) throws AuthenticationException {

		OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenByValue(refreshTokenValue);

		if (refreshToken == null) {
			throw new InvalidTokenException("Invalid refresh token: " + refreshTokenValue);
		}

		ClientDetailsEntity client = refreshToken.getClient();

		AuthenticationHolderEntity authHolder = refreshToken.getAuthenticationHolder();

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

		OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();

		// get the stored scopes from the authentication holder's authorization request; these are the scopes associated with the refresh token
		Set<String> refreshScopes = new HashSet<String>(refreshToken.getAuthenticationHolder().getAuthentication().getOAuth2Request().getScope());

		Set<String> scope = authRequest.getScope() == null ? new HashSet<String>() : new HashSet<String>(authRequest.getScope());
		if (scope != null && !scope.isEmpty()) {
			// ensure a proper subset of scopes
			if (refreshScopes != null && refreshScopes.containsAll(scope)) {
				// set the scope of the new access token if requested
				token.setScope(scope);
			} else {
				String errorMsg = "Up-scoping is not allowed.";
				logger.error(errorMsg);
				throw new InvalidScopeException(errorMsg);
			}
		} else {
			// otherwise inherit the scope of the refresh token (if it's there -- this can return a null scope set)
			token.setScope(refreshScopes);
		}

		token.setClient(client);

		if (client.getAccessTokenValiditySeconds() != null) {
			Date expiration = new Date(System.currentTimeMillis() + (client.getAccessTokenValiditySeconds() * 1000L));
			token.setExpiration(expiration);
		}

		token.setRefreshToken(refreshToken);

		token.setAuthenticationHolder(authHolder);

		tokenEnhancer.enhance(token, authHolder.getAuthentication());

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
			//tokenRepository.removeAccessToken(accessToken);
			revokeAccessToken(accessToken);
			throw new InvalidTokenException("Expired access token: " + accessTokenValue);
		}

		return accessToken.getAuthenticationHolder().getAuthentication();
	}


	/**
	 * Get an access token from its token value.
	 */
	@Override
	public OAuth2AccessTokenEntity readAccessToken(String accessTokenValue) throws AuthenticationException {
		OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenByValue(accessTokenValue);
		if (accessToken == null) {
			throw new InvalidTokenException("Access token for value " + accessTokenValue + " was not found");
		}
		else {
			return accessToken;
		}
	}

	/**
	 * Get an access token by its authentication object.
	 */
	@Override
	public OAuth2AccessTokenEntity getAccessToken(OAuth2Authentication authentication) {

		OAuth2AccessTokenEntity accessToken = tokenRepository.getByAuthentication(authentication);

		return accessToken;
	}

	/**
	 * Get a refresh token by its token value.
	 */
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

	/**
	 * Revoke a refresh token and all access tokens issued to it.
	 */
	@Override
	public void revokeRefreshToken(OAuth2RefreshTokenEntity refreshToken) {
		tokenRepository.clearAccessTokensForRefreshToken(refreshToken);
		tokenRepository.removeRefreshToken(refreshToken);
	}

	/**
	 * Revoke an access token.
	 */
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
	public void clearExpiredTokens() {
		logger.info("Cleaning out all expired tokens");

		Collection<OAuth2AccessTokenEntity> accessTokens = getExpiredAccessTokens();
		logger.info("Found " + accessTokens.size() + " expired access tokens");
		for (OAuth2AccessTokenEntity oAuth2AccessTokenEntity : accessTokens) {
			revokeAccessToken(oAuth2AccessTokenEntity);
		}

		Collection<OAuth2RefreshTokenEntity> refreshTokens = getExpiredRefreshTokens();
		logger.info("Found " + refreshTokens.size() + " expired refresh tokens");
		for (OAuth2RefreshTokenEntity oAuth2RefreshTokenEntity : refreshTokens) {
			revokeRefreshToken(oAuth2RefreshTokenEntity);
		}
	}
	
	private Predicate<OAuth2AccessTokenEntity> isAccessTokenExpired = new Predicate<OAuth2AccessTokenEntity>() {
		@Override
		public boolean apply(OAuth2AccessTokenEntity input) {
			return (input != null && input.isExpired());
		}
	};
	
	private Predicate<OAuth2RefreshTokenEntity> isRefreshTokenExpired = new Predicate<OAuth2RefreshTokenEntity>() {
		@Override
		public boolean apply(OAuth2RefreshTokenEntity input) {
			return (input != null && input.isExpired());
		}
	};
	
	private Collection<OAuth2AccessTokenEntity> getExpiredAccessTokens() {
		return Collections2.filter(tokenRepository.getAllAccessTokens(), isAccessTokenExpired);
	}

	private Collection<OAuth2RefreshTokenEntity> getExpiredRefreshTokens() {
		return Collections2.filter(tokenRepository.getAllRefreshTokens(), isRefreshTokenExpired);
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

	/**
	 * @return the tokenEnhancer
	 */
	public TokenEnhancer getTokenEnhancer() {
		return tokenEnhancer;
	}

	/**
	 * @param tokenEnhancer the tokenEnhancer to set
	 */
	public void setTokenEnhancer(TokenEnhancer tokenEnhancer) {
		this.tokenEnhancer = tokenEnhancer;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.OAuth2TokenEntityService#getAccessTokenForIdToken(org.mitre.oauth2.model.OAuth2AccessTokenEntity)
	 */
	@Override
	public OAuth2AccessTokenEntity getAccessTokenForIdToken(OAuth2AccessTokenEntity idToken) {
		return tokenRepository.getAccessTokenForIdToken(idToken);
	}

}
