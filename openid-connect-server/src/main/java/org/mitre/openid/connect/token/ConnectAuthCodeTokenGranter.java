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
/**
 * 
 */
package org.mitre.openid.connect.token;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.impl.DefaultOAuth2ProviderTokenService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.model.IdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.AuthorizationRequestFactory;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

/**
 * AccessToken granter for Authorization Code flow. 
 * 
 * Note: does this need to be able to grant straight OAuth2.0 Access Tokens as 
 * well as Connect Access Tokens?
 * 
 * 
 * @author AANGANES
 * 
 */
@Component
public class ConnectAuthCodeTokenGranter implements TokenGranter {

	private static final String GRANT_TYPE = "authorization_code";
	
	@Autowired
	private AuthorizationCodeServices authorizationCodeServices;

	@Autowired
	private AuthorizationRequestFactory authorizationRequestFactory;

	@Autowired
	private ConfigurationPropertiesBean configBean;

	//TODO: Do we need to modify/update this?	
	@Autowired
	private OAuth2TokenEntityService tokenServices;
	
	@Autowired
	private IdTokenGeneratorService idTokenService;
	
	@Autowired
	private JwtSigningAndValidationService jwtService;
	
	/**
	 * Default empty constructor
	 */
	public ConnectAuthCodeTokenGranter() {
	}
	
	/**
	 * Constructor for unit tests
	 * 
	 * @param tokenServices
	 * @param authorizationCodeServices
	 * @param clientDetailsService
	 */
	public ConnectAuthCodeTokenGranter(
			DefaultOAuth2ProviderTokenService tokenServices,
			AuthorizationCodeServices authorizationCodeServices,
			ClientDetailsService clientDetailsService, AuthorizationRequestFactory authorizationRequestFactory) {
		
		setTokenServices(tokenServices);
		setAuthorizationCodeServices(authorizationCodeServices);
		setAuthorizationRequestFactory(authorizationRequestFactory);

	}

	/**
	 * Grant an OpenID Connect Access Token 
	 * 
	 * @param grantType
	 * @param parameters
	 * @param clientId
	 * @param scope
	 */
	@Override
	public OAuth2AccessToken grant(String grantType,
			Map<String, String> parameters, String clientId, Set<String> scope) {
		
		if (!GRANT_TYPE.equals(grantType)) {
			return null;
		}

		String authorizationCode = parameters.get("code");
		String redirectUri = parameters.get("redirect_uri");

		if (authorizationCode == null) {
			throw new OAuth2Exception("An authorization code must be supplied.");
		}

		AuthorizationRequestHolder storedAuth = authorizationCodeServices.consumeAuthorizationCode(authorizationCode);
		if (storedAuth == null) {
			throw new InvalidGrantException("Invalid authorization code: " + authorizationCode);
		}

		AuthorizationRequest unconfirmedAuthorizationRequest = storedAuth.getAuthenticationRequest();
		if (unconfirmedAuthorizationRequest.getRedirectUri() != null
				&& !unconfirmedAuthorizationRequest.getRedirectUri().equals(redirectUri)) {
			throw new RedirectMismatchException("Redirect URI mismatch.");
		}

		if (clientId != null && !clientId.equals(unconfirmedAuthorizationRequest.getClientId())) {
			// just a sanity check.
			throw new InvalidClientException("Client ID mismatch");
		}

		// From SECOAUTH: Secret is not required in the authorization request, so it won't be available
		// in the unconfirmedAuthorizationCodeAuth. We do want to check that a secret is provided
		// in the new request, but that happens elsewhere.

		//Validate credentials
		AuthorizationRequest authorizationRequest = authorizationRequestFactory.createAuthorizationRequest(parameters, clientId,
				grantType, unconfirmedAuthorizationRequest.getScope());
		if (authorizationRequest == null) {
			return null;
		}

		Authentication userAuth = storedAuth.getUserAuthentication();
		
		//TODO: should not need cast
		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) tokenServices.createAccessToken(new OAuth2Authentication(authorizationRequest, userAuth));
		
		token.getJwt().getClaims().setAudience(clientId);
		
		token.getJwt().getClaims().setIssuer(configBean.getIssuer());

		token.getJwt().getClaims().setIssuedAt(new Date());
		// handle expiration
		token.getJwt().getClaims().setExpiration(token.getExpiration());
		
		jwtService.signJwt(token.getJwt());
		
		/**
		 * Authorization request scope MUST include "openid", but access token request 
		 * may or may not include the scope parameter. As long as the AuthorizationRequest 
		 * has the proper scope, we can consider this a valid OpenID Connect request.
		 */
		if (authorizationRequest.getScope().contains("openid")) {

			String userId = userAuth.getName();
		
			IdToken idToken = idTokenService.generateIdToken(userId, configBean.getIssuer());
			idToken.getClaims().setAudience(clientId);
			idToken.getClaims().setIssuedAt(new Date());
			idToken.getClaims().setIssuer(configBean.getIssuer());
			
			
			String nonce = unconfirmedAuthorizationRequest.getAuthorizationParameters().get("nonce");
			if (!Strings.isNullOrEmpty(nonce)) {
				idToken.getClaims().setNonce(nonce);
			}
			// TODO: expiration? other fields?
			
			//Sign
			//TODO: check client to see if they have a preferred alg, attempt to use that
			
			jwtService.signJwt(idToken);
			
			token.setIdToken(idToken);
		}
		
		tokenServices.saveAccessToken(token);
		
		return token;
	}

	/**
	 * @return the authorizationCodeServices
	 */
	public AuthorizationCodeServices getAuthorizationCodeServices() {
		return authorizationCodeServices;
	}

	/**
	 * @param authorizationCodeServices the authorizationCodeServices to set
	 */
	public void setAuthorizationCodeServices(AuthorizationCodeServices authorizationCodeServices) {
		this.authorizationCodeServices = authorizationCodeServices;
	}

	public AuthorizationRequestFactory getAuthorizationRequestFactory() {
		return this.authorizationRequestFactory;
	}
	
	public void setAuthorizationRequestFactory(AuthorizationRequestFactory authorizationRequestFactory) {
		this.authorizationRequestFactory = authorizationRequestFactory;
	}

	public OAuth2TokenEntityService getTokenServices() {
		return tokenServices;
	}

	public void setTokenServices(OAuth2TokenEntityService tokenServices) {
		this.tokenServices = tokenServices;
	}

	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}

	public IdTokenGeneratorService getIdTokenService() {
		return idTokenService;
	}

	public void setIdTokenService(IdTokenGeneratorService idTokenService) {
		this.idTokenService = idTokenService;
	}

	public JwtSigningAndValidationService getJwtService() {
		return jwtService;
	}

	public void setJwtService(JwtSigningAndValidationService jwtService) {
		this.jwtService = jwtService;
	}
	
}
