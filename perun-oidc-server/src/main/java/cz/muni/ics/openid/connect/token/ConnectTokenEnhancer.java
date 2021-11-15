/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
 *******************************************************************************/
package cz.muni.ics.openid.connect.token;

import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.service.OIDCTokenService;
import cz.muni.ics.openid.connect.service.UserInfoService;
import java.util.Date;
import java.util.UUID;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

@Service
@Slf4j
public class ConnectTokenEnhancer implements TokenEnhancer {

	@Autowired
	private ConfigurationPropertiesBean configBean;

	@Autowired
	private JWTSigningAndValidationService jwtService;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private OIDCTokenService connectTokenService;

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,	OAuth2Authentication authentication) {

		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) accessToken;
		OAuth2Request originalAuthRequest = authentication.getOAuth2Request();

		String clientId = originalAuthRequest.getClientId();
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		Builder builder = new JWTClaimsSet.Builder()
				.claim("azp", clientId)
				.issuer(configBean.getIssuer())
				.issueTime(new Date())
				.expirationTime(token.getExpiration())
				.subject(authentication.getName())
				.jwtID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it

		String audience = (String) authentication.getOAuth2Request().getExtensions().get("aud");
		if (!Strings.isNullOrEmpty(audience)) {
			builder.audience(Lists.newArrayList(audience));
		}

		addCustomAccessTokenClaims(builder, token, authentication);

		JWTClaimsSet claims = builder.build();

		JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();
		JWSHeader header = new JWSHeader(signingAlg, null, null, null, null, null, null, null, null, null,
				jwtService.getDefaultSignerKeyId(),
				null, null);
		SignedJWT signed = new SignedJWT(header, claims);

		jwtService.signJwt(signed);

		token.setJwt(signed);

		/**
		 * Authorization request scope MUST include "openid" in OIDC, but access token request
		 * may or may not include the scope parameter. As long as the AuthorizationRequest
		 * has the proper scope, we can consider this a valid OpenID Connect request. Otherwise,
		 * we consider it to be a vanilla OAuth2 request.
		 *
		 * Also, there must be a user authentication involved in the request for it to be considered
		 * OIDC and not OAuth, so we check for that as well.
		 */
		if (originalAuthRequest.getScope().contains(SystemScopeService.OPENID_SCOPE)
				&& !authentication.isClientOnly()) {

			String username = authentication.getName();
			UserInfo userInfo = userInfoService.getByUsernameAndClientId(username, clientId);

			if (userInfo != null) {

				JWT idToken = connectTokenService.createIdToken(client,
						originalAuthRequest, claims.getIssueTime(),
						userInfo.getSub(), token);

				// attach the id token to the parent access token
				token.setIdToken(idToken);
			} else {
				// can't create an id token if we can't find the user
				log.warn("Request for ID token when no user is present.");
			}
		}

		return token;
	}

	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}

	public JWTSigningAndValidationService getJwtService() {
		return jwtService;
	}

	public void setJwtService(JWTSigningAndValidationService jwtService) {
		this.jwtService = jwtService;
	}

	public ClientDetailsEntityService getClientService() {
		return clientService;
	}

	public void setClientService(ClientDetailsEntityService clientService) {
		this.clientService = clientService;
	}


	/**
	 * Hook for subclasses that allows adding custom claims to the JWT that will be used as access token.
	 * @param builder the builder holding the current claims
	 * @param token the un-enhanced token
	 * @param authentication current authentication
	 */
    protected void addCustomAccessTokenClaims(JWTClaimsSet.Builder builder, OAuth2AccessTokenEntity token,
	    OAuth2Authentication authentication) {
	}

}
