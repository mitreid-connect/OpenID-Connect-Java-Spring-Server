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
package org.mitre.openid.connect.token;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.model.ApprovedSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class ConnectTokenEnhancer implements TokenEnhancer {

	Logger logger = LoggerFactory.getLogger(ConnectTokenEnhancer.class);
	
	@Autowired
	private ConfigurationPropertiesBean configBean;
	
	@Autowired
	private JwtSigningAndValidationService jwtService;
	
	@Autowired
	private ClientDetailsEntityService clientService;
	
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,	OAuth2Authentication authentication) {
		
		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) accessToken;
		AuthorizationRequest originalAuthRequest = authentication.getAuthorizationRequest();
		
		if (originalAuthRequest.getExtensionProperties().containsKey("approved_site")) {
			//Add the token to the approved site reference, if there is one
			ApprovedSite ap = (ApprovedSite)originalAuthRequest.getExtensionProperties().get("approved_site");
			//ap.addApprovedAccessToken(token);
			token.setApprovedSite(ap);
		}
		
		String clientId = originalAuthRequest.getClientId();
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
		
		JWTClaimsSet claims = new JWTClaimsSet();
		
		claims.setAudience(Lists.newArrayList(clientId));
		
		claims.setIssuer(configBean.getIssuer());

		claims.setIssueTime(new Date());
		
		claims.setExpirationTime(token.getExpiration());

		claims.setJWTID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it
		
		// TODO: use client's default signing algorithm

		SignedJWT signed = new SignedJWT(new JWSHeader(jwtService.getDefaultSigningAlgorithm()), claims);
		
        jwtService.signJwt(signed);
	    
	    token.setJwt(signed);
		
		/**
		 * Authorization request scope MUST include "openid" in OIDC, but access token request 
		 * may or may not include the scope parameter. As long as the AuthorizationRequest 
		 * has the proper scope, we can consider this a valid OpenID Connect request. Otherwise,
		 * we consider it to be a vanilla OAuth2 request. 
		 */
		if (originalAuthRequest.getScope().contains("openid")) {

			// TODO: maybe id tokens need a service layer
			
			String userId = authentication.getName();
		
			OAuth2AccessTokenEntity idTokenEntity = new OAuth2AccessTokenEntity();
			
			// FIXME: extend the "claims" section for id tokens
			JWTClaimsSet idClaims = new JWTClaimsSet();
			
			
			idClaims.setCustomClaim("auth_time", new Date().getTime());
			
			idClaims.setIssueTime(new Date());
			
			if (client.getIdTokenValiditySeconds() != null) {
				Date expiration = new Date(System.currentTimeMillis() + (client.getIdTokenValiditySeconds() * 1000L));
				idClaims.setExpirationTime(expiration);
				idTokenEntity.setExpiration(expiration);
			}
			
			idClaims.setIssuer(configBean.getIssuer());
			idClaims.setSubject(userId);
			idClaims.setAudience(Lists.newArrayList(clientId));
			
			
			String nonce = originalAuthRequest.getAuthorizationParameters().get("nonce");
			if (!Strings.isNullOrEmpty(nonce)) {
				idClaims.setCustomClaim("nonce", nonce);
			}

			SignedJWT idToken = new SignedJWT(new JWSHeader(jwtService.getDefaultSigningAlgorithm()), idClaims);

			//TODO: check for client's preferred signer alg and use that
			
			jwtService.signJwt(idToken);

			idTokenEntity.setJwt(idToken);
			
			// TODO: might want to create a specialty authentication object here instead of copying
			idTokenEntity.setAuthenticationHolder(token.getAuthenticationHolder());

			// create a scope set with just the special "id-token" scope
			//Set<String> idScopes = new HashSet<String>(token.getScope()); // this would copy the original token's scopes in, we don't really want that
			Set<String> idScopes = Sets.newHashSet(OAuth2AccessTokenEntity.ID_TOKEN_SCOPE);
			idTokenEntity.setScope(idScopes);
			
			idTokenEntity.setClient(token.getClient());
			
			// attach the id token to the parent access token
			// TODO: this relationship is one-to-one right now, this might change
			token.setIdToken(idTokenEntity);
		}
		
		return token;
	}

	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}

	public JwtSigningAndValidationService getJwtService() {
		return jwtService;
	}

	public void setJwtService(JwtSigningAndValidationService jwtService) {
		this.jwtService = jwtService;
	}

	public ClientDetailsEntityService getClientService() {
		return clientService;
	}

	public void setClientService(ClientDetailsEntityService clientService) {
		this.clientService = clientService;
	}

}
