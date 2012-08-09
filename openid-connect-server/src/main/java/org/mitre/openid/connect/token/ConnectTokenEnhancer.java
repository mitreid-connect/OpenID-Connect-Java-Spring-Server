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

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.model.IdToken;
import org.mitre.openid.connect.model.IdTokenClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

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
		
		String clientId = authentication.getAuthorizationRequest().getClientId();
		
		token.getJwt().getClaims().setAudience(clientId);
		
		token.getJwt().getClaims().setIssuer(configBean.getIssuer());

		token.getJwt().getClaims().setIssuedAt(new Date());
		
		token.getJwt().getClaims().setExpiration(token.getExpiration());

		token.getJwt().getClaims().setNonce(UUID.randomUUID().toString()); // set a random NONCE in the middle of it
		
		if (token.getRefreshToken() != null && Strings.isNullOrEmpty(token.getRefreshToken().getJwt().getClaims().getNonce())) {
			token.getRefreshToken().getJwt().getClaims().setNonce(UUID.randomUUID().toString()); // set a random nonce in the middle of it
		}
		
		try {
	        jwtService.signJwt(token.getJwt());
        } catch (NoSuchAlgorithmException e) {
	        // couldn't sign token
        	logger.warn("Couldn't sign access token", e);
        }
		
		/**
		 * Authorization request scope MUST include "openid", but access token request 
		 * may or may not include the scope parameter. As long as the AuthorizationRequest 
		 * has the proper scope, we can consider this a valid OpenID Connect request.
		 */
		if (authentication.getAuthorizationRequest().getScope().contains("openid")) {

			String userId = authentication.getName();
		
			IdToken idToken = new IdToken();
			
			IdTokenClaims claims = new IdTokenClaims();
			claims.setAuthTime(new Date());
			claims.setIssuedAt(new Date());
			
			ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
			
			if (client.getIdTokenValiditySeconds() != null) {
				Date expiration = new Date(System.currentTimeMillis() + (client.getIdTokenValiditySeconds() * 1000L));
				claims.setExpiration(expiration);
			}
			
			claims.setIssuer(configBean.getIssuer());
			claims.setUserId(userId);
			claims.setAudience(clientId);
			
			idToken.setClaims(claims);
			
			String nonce = authentication.getAuthorizationRequest().getAuthorizationParameters().get("nonce");
			if (!Strings.isNullOrEmpty(nonce)) {
				idToken.getClaims().setNonce(nonce);
			}

			//TODO: check for client's preferred signer alg and use that
			
			try {
	            jwtService.signJwt(idToken);
            } catch (NoSuchAlgorithmException e) {
            	logger.warn("Couldn't sign id token", e);
            }
			
			token.setIdToken(idToken);
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
