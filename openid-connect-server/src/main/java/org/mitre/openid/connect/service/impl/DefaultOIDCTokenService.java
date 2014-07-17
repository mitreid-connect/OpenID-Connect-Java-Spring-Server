/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
package org.mitre.openid.connect.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.jwt.signer.service.impl.SymmetricCacheService;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.service.OIDCTokenService;
import org.mitre.openid.connect.util.IdTokenHashUtils;
import org.mitre.openid.connect.web.AuthenticationTimeStamper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

/**
 * Default implementation of service to create specialty OpenID Connect tokens.
 * 
 * @author Amanda Anganes
 *
 */
@Service
public class DefaultOIDCTokenService implements OIDCTokenService {

	Logger logger = LoggerFactory.getLogger(DefaultOIDCTokenService.class);

	@Autowired
	private JwtSigningAndValidationService jwtService;

	@Autowired
	private AuthenticationHolderRepository authenticationHolderRepository;

	@Autowired
	private ConfigurationPropertiesBean configBean;

	@Autowired
	private JWKSetCacheService encrypters;

	@Autowired
	private SymmetricCacheService symmetricCacheService;

	@Override
	public OAuth2AccessTokenEntity createIdToken(ClientDetailsEntity client, OAuth2Request request, Date issueTime, String sub, OAuth2AccessTokenEntity accessToken) {

		JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();

		if (client.getIdTokenSignedResponseAlg() != null) {
			signingAlg = client.getIdTokenSignedResponseAlg();
		}


		OAuth2AccessTokenEntity idTokenEntity = new OAuth2AccessTokenEntity();
		JWTClaimsSet idClaims = new JWTClaimsSet();

		// if the auth time claim was explicitly requested OR if the client always wants the auth time, put it in
		if (request.getExtensions().containsKey("max_age")
				|| (request.getExtensions().containsKey("idtoken")) // TODO: parse the ID Token claims (#473) -- for now assume it could be in there
				|| (client.getRequireAuthTime() != null && client.getRequireAuthTime())) {

			Date authTime = (Date) request.getExtensions().get(AuthenticationTimeStamper.AUTH_TIMESTAMP);
			if (authTime != null) {
				idClaims.setClaim("auth_time", authTime.getTime() / 1000);
			}
		}

		idClaims.setIssueTime(issueTime);

		if (client.getIdTokenValiditySeconds() != null) {
			Date expiration = new Date(System.currentTimeMillis() + (client.getIdTokenValiditySeconds() * 1000L));
			idClaims.setExpirationTime(expiration);
			idTokenEntity.setExpiration(expiration);
		}

		idClaims.setIssuer(configBean.getIssuer());
		idClaims.setSubject(sub);
		idClaims.setAudience(Lists.newArrayList(client.getClientId()));

		String nonce = (String)request.getExtensions().get("nonce");
		if (!Strings.isNullOrEmpty(nonce)) {
			idClaims.setCustomClaim("nonce", nonce);
		}

		Set<String> responseTypes = request.getResponseTypes();

		if (responseTypes.contains("token")) {
			// calculate the token hash
			Base64URL at_hash = IdTokenHashUtils.getAccessTokenHash(signingAlg, accessToken);
			idClaims.setClaim("at_hash", at_hash);
		}

		if (client.getIdTokenEncryptedResponseAlg() != null && !client.getIdTokenEncryptedResponseAlg().equals(Algorithm.NONE)
				&& client.getIdTokenEncryptedResponseEnc() != null && !client.getIdTokenEncryptedResponseEnc().equals(Algorithm.NONE)
				&& !Strings.isNullOrEmpty(client.getJwksUri())) {

			JwtEncryptionAndDecryptionService encrypter = encrypters.getEncrypter(client.getJwksUri());

			if (encrypter != null) {

				EncryptedJWT idToken = new EncryptedJWT(new JWEHeader(client.getIdTokenEncryptedResponseAlg(), client.getIdTokenEncryptedResponseEnc()), idClaims);

				encrypter.encryptJwt(idToken);

				idTokenEntity.setJwt(idToken);

			} else {
				logger.error("Couldn't find encrypter for client: " + client.getClientId());
			}

		} else {
			
			JWT idToken;
			
			if (signingAlg.equals(JWSAlgorithm.NONE)) {
				// unsigned ID token
				idToken = new PlainJWT(idClaims);

			} else {

				// signed ID token
				idToken = new SignedJWT(new JWSHeader(signingAlg), idClaims);
	
				if (signingAlg.equals(JWSAlgorithm.HS256)
						|| signingAlg.equals(JWSAlgorithm.HS384)
						|| signingAlg.equals(JWSAlgorithm.HS512)) {
					JwtSigningAndValidationService signer = symmetricCacheService.getSymmetricValidtor(client);
	
					// sign it with the client's secret
					signer.signJwt((SignedJWT) idToken);
				} else {
	
					// sign it with the server's key
					jwtService.signJwt((SignedJWT) idToken);
				}
			}
				

			idTokenEntity.setJwt(idToken);
		}

		idTokenEntity.setAuthenticationHolder(accessToken.getAuthenticationHolder());

		// create a scope set with just the special "id-token" scope
		//Set<String> idScopes = new HashSet<String>(token.getScope()); // this would copy the original token's scopes in, we don't really want that
		Set<String> idScopes = Sets.newHashSet(SystemScopeService.ID_TOKEN_SCOPE);
		idTokenEntity.setScope(idScopes);

		idTokenEntity.setClient(accessToken.getClient());

		return idTokenEntity;
	}

	/**
	 * @param client
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	public OAuth2AccessTokenEntity createRegistrationAccessToken(ClientDetailsEntity client) {

		Map<String, String> authorizationParameters = Maps.newHashMap();
		OAuth2Request clientAuth = new OAuth2Request(authorizationParameters, client.getClientId(),
				Sets.newHashSet(new SimpleGrantedAuthority("ROLE_CLIENT")), true,
				Sets.newHashSet(SystemScopeService.REGISTRATION_TOKEN_SCOPE), null, null, null, null);
		OAuth2Authentication authentication = new OAuth2Authentication(clientAuth, null);

		OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
		token.setClient(client);
		token.setScope(Sets.newHashSet(SystemScopeService.REGISTRATION_TOKEN_SCOPE));

		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setAuthentication(authentication);
		authHolder = authenticationHolderRepository.save(authHolder);
		token.setAuthenticationHolder(authHolder);

		JWTClaimsSet claims = new JWTClaimsSet();

		claims.setAudience(Lists.newArrayList(client.getClientId()));
		claims.setIssuer(configBean.getIssuer());
		claims.setIssueTime(new Date());
		claims.setExpirationTime(token.getExpiration());
		claims.setJWTID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it

		JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();
		SignedJWT signed = new SignedJWT(new JWSHeader(signingAlg), claims);

		jwtService.signJwt(signed);

		token.setJwt(signed);

		return token;
	}

	/**
	 * @param client
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	public OAuth2AccessTokenEntity createResourceAccessToken(ClientDetailsEntity client) {

		Map<String, String> authorizationParameters = Maps.newHashMap();
		OAuth2Request clientAuth = new OAuth2Request(authorizationParameters, client.getClientId(),
				Sets.newHashSet(new SimpleGrantedAuthority("ROLE_CLIENT")), true,
				Sets.newHashSet(SystemScopeService.RESOURCE_TOKEN_SCOPE), null, null, null, null);
		OAuth2Authentication authentication = new OAuth2Authentication(clientAuth, null);

		OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
		token.setClient(client);
		token.setScope(Sets.newHashSet(SystemScopeService.RESOURCE_TOKEN_SCOPE));

		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setAuthentication(authentication);
		authHolder = authenticationHolderRepository.save(authHolder);
		token.setAuthenticationHolder(authHolder);

		JWTClaimsSet claims = new JWTClaimsSet();

		claims.setAudience(Lists.newArrayList(client.getClientId()));
		claims.setIssuer(configBean.getIssuer());
		claims.setIssueTime(new Date());
		claims.setExpirationTime(token.getExpiration());
		claims.setJWTID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it

		JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();
		SignedJWT signed = new SignedJWT(new JWSHeader(signingAlg), claims);

		jwtService.signJwt(signed);

		token.setJwt(signed);

		return token;
	}

	/**
	 * @return the configBean
	 */
	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	/**
	 * @param configBean the configBean to set
	 */
	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}

	/**
	 * @return the jwtService
	 */
	public JwtSigningAndValidationService getJwtService() {
		return jwtService;
	}

	/**
	 * @param jwtService the jwtService to set
	 */
	public void setJwtService(JwtSigningAndValidationService jwtService) {
		this.jwtService = jwtService;
	}

	/**
	 * @return the authenticationHolderRepository
	 */
	public AuthenticationHolderRepository getAuthenticationHolderRepository() {
		return authenticationHolderRepository;
	}

	/**
	 * @param authenticationHolderRepository the authenticationHolderRepository to set
	 */
	public void setAuthenticationHolderRepository(
			AuthenticationHolderRepository authenticationHolderRepository) {
		this.authenticationHolderRepository = authenticationHolderRepository;
	}

}
