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
package cz.muni.ics.openid.connect.service.impl;

import static cz.muni.ics.openid.connect.request.ConnectRequestParameters.MAX_AGE;
import static cz.muni.ics.openid.connect.request.ConnectRequestParameters.NONCE;

import cz.muni.ics.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.openid.connect.util.IdTokenHashUtils;
import cz.muni.ics.openid.connect.web.AuthenticationTimeStamper;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cz.muni.ics.jwt.signer.service.impl.ClientKeyCacheService;
import cz.muni.ics.jwt.signer.service.impl.SymmetricKeyJWTValidatorCacheService;
import cz.muni.ics.oauth2.model.AuthenticationHolderEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.repository.AuthenticationHolderRepository;
import cz.muni.ics.oauth2.service.OAuth2TokenEntityService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.service.OIDCTokenService;
import lombok.extern.slf4j.Slf4j;
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
import com.nimbusds.jose.JWEObject;
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
@Slf4j
public class DefaultOIDCTokenService implements OIDCTokenService {

	@Autowired
	private JWTSigningAndValidationService jwtService;

	@Autowired
	private AuthenticationHolderRepository authenticationHolderRepository;

	@Autowired
	private ConfigurationPropertiesBean configBean;

	@Autowired
	private ClientKeyCacheService encrypters;

	@Autowired
	private SymmetricKeyJWTValidatorCacheService symmetricCacheService;

	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Override
	public JWT createIdToken(ClientDetailsEntity client, OAuth2Request request, Date issueTime, String sub, OAuth2AccessTokenEntity accessToken) {

		JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();

		if (client.getIdTokenSignedResponseAlg() != null) {
			signingAlg = client.getIdTokenSignedResponseAlg();
		}


		JWT idToken = null;

		JWTClaimsSet.Builder idClaims = new JWTClaimsSet.Builder();

		// if the auth time claim was explicitly requested OR if the client always wants the auth time, put it in
		if (request.getExtensions().containsKey(MAX_AGE)
				|| (request.getExtensions().containsKey("idtoken")) // TODO: parse the ID Token claims (#473) -- for now assume it could be in there
				|| (client.getRequireAuthTime() != null && client.getRequireAuthTime())) {

			if (request.getExtensions().get(AuthenticationTimeStamper.AUTH_TIMESTAMP) != null) {

				Long authTimestamp = Long.parseLong((String) request.getExtensions().get(AuthenticationTimeStamper.AUTH_TIMESTAMP));
				if (authTimestamp != null) {
					idClaims.claim("auth_time", authTimestamp / 1000L);
				}
			} else {
				// we couldn't find the timestamp!
				log.warn("Unable to find authentication timestamp! There is likely something wrong with the configuration.");
			}
		}

		idClaims.issueTime(issueTime);

		if (client.getIdTokenValiditySeconds() != null) {
			Date expiration = new Date(System.currentTimeMillis() + (client.getIdTokenValiditySeconds() * 1000L));
			idClaims.expirationTime(expiration);
		}

		idClaims.issuer(configBean.getIssuer());
		idClaims.subject(sub);
		idClaims.audience(Lists.newArrayList(client.getClientId()));
		idClaims.jwtID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it

		String nonce = (String)request.getExtensions().get(NONCE);
		if (!Strings.isNullOrEmpty(nonce)) {
			idClaims.claim("nonce", nonce);
		}

		Set<String> responseTypes = request.getResponseTypes();

		if (responseTypes.contains("token")) {
			// calculate the token hash
			Base64URL at_hash = IdTokenHashUtils.getAccessTokenHash(signingAlg, accessToken);
			idClaims.claim("at_hash", at_hash);
		}

		addCustomIdTokenClaims(idClaims, client, request, sub, accessToken);

		if (client.getIdTokenEncryptedResponseAlg() != null && !client.getIdTokenEncryptedResponseAlg().equals(Algorithm.NONE)
				&& client.getIdTokenEncryptedResponseEnc() != null && !client.getIdTokenEncryptedResponseEnc().equals(Algorithm.NONE)
				&& (!Strings.isNullOrEmpty(client.getJwksUri()) || client.getJwks() != null)) {

			JWTEncryptionAndDecryptionService encrypter = encrypters.getEncrypter(client);

			if (encrypter != null) {

				idToken = new EncryptedJWT(new JWEHeader(client.getIdTokenEncryptedResponseAlg(), client.getIdTokenEncryptedResponseEnc()), idClaims.build());

				encrypter.encryptJwt((JWEObject) idToken);

			} else {
				log.error("Couldn't find encrypter for client: " + client.getClientId());
			}

		} else {

			if (signingAlg.equals(Algorithm.NONE)) {
				// unsigned ID token
				idToken = new PlainJWT(idClaims.build());

			} else {

				// signed ID token

				if (signingAlg.equals(JWSAlgorithm.HS256)
						|| signingAlg.equals(JWSAlgorithm.HS384)
						|| signingAlg.equals(JWSAlgorithm.HS512)) {

					JWSHeader header = new JWSHeader(signingAlg, null, null, null, null, null, null, null, null, null,
							jwtService.getDefaultSignerKeyId(),
							null, null);
					idToken = new SignedJWT(header, idClaims.build());

					JWTSigningAndValidationService signer = symmetricCacheService.getSymmetricValidator(client);

					// sign it with the client's secret
					signer.signJwt((SignedJWT) idToken);
				} else {
					idClaims.claim("kid", jwtService.getDefaultSignerKeyId());

					JWSHeader header = new JWSHeader(signingAlg, null, null, null, null, null, null, null, null, null,
							jwtService.getDefaultSignerKeyId(),
							null, null);

					idToken = new SignedJWT(header, idClaims.build());

					// sign it with the server's key
					jwtService.signJwt((SignedJWT) idToken);
				}
			}

		}

		return idToken;
	}

	/**
	 * @param client
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	public OAuth2AccessTokenEntity createRegistrationAccessToken(ClientDetailsEntity client) {

		return createAssociatedToken(client, Sets.newHashSet(SystemScopeService.REGISTRATION_TOKEN_SCOPE));

	}

	/**
	 * @param client
	 * @return
	 */
	@Override
	public OAuth2AccessTokenEntity createResourceAccessToken(ClientDetailsEntity client) {

		return createAssociatedToken(client, Sets.newHashSet(SystemScopeService.RESOURCE_TOKEN_SCOPE));

	}

	@Override
	public OAuth2AccessTokenEntity rotateRegistrationAccessTokenForClient(ClientDetailsEntity client) {
		// revoke any previous tokens
		OAuth2AccessTokenEntity oldToken = tokenService.getRegistrationAccessTokenForClient(client);
		if (oldToken != null) {
			Set<String> scope = oldToken.getScope();
			tokenService.revokeAccessToken(oldToken);
			return createAssociatedToken(client, scope);
		} else {
			return null;
		}

	}

	private OAuth2AccessTokenEntity createAssociatedToken(ClientDetailsEntity client, Set<String> scope) {

		// revoke any previous tokens that might exist, just to be sure
		OAuth2AccessTokenEntity oldToken = tokenService.getRegistrationAccessTokenForClient(client);
		if (oldToken != null) {
			tokenService.revokeAccessToken(oldToken);
		}

		// create a new token

		Map<String, String> authorizationParameters = Maps.newHashMap();
		OAuth2Request clientAuth = new OAuth2Request(authorizationParameters, client.getClientId(),
				Sets.newHashSet(new SimpleGrantedAuthority("ROLE_CLIENT")), true,
				scope, null, null, null, null);
		OAuth2Authentication authentication = new OAuth2Authentication(clientAuth, null);

		OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
		token.setClient(client);
		token.setScope(scope);

		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setAuthentication(authentication);
		authHolder = authenticationHolderRepository.save(authHolder);
		token.setAuthenticationHolder(authHolder);

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.audience(Lists.newArrayList(client.getClientId()))
				.issuer(configBean.getIssuer())
				.issueTime(new Date())
				.expirationTime(token.getExpiration())
				.jwtID(UUID.randomUUID().toString()) // set a random NONCE in the middle of it
				.build();

		JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();
		JWSHeader header = new JWSHeader(signingAlg, null, null, null, null, null, null, null, null, null,
				jwtService.getDefaultSignerKeyId(),
				null, null);
		SignedJWT signed = new SignedJWT(header, claims);

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
	public JWTSigningAndValidationService getJwtService() {
		return jwtService;
	}

	/**
	 * @param jwtService the jwtService to set
	 */
	public void setJwtService(JWTSigningAndValidationService jwtService) {
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

	/**
	 * Hook for subclasses that allows adding custom claims to the JWT
	 * that will be used as id token.
	 * @param idClaims the builder holding the current claims
	 * @param client information about the requesting client
	 * @param request request that caused the id token to be created
	 * @param sub subject auf the id token
	 * @param accessToken the access token
	 * @param authentication current authentication
	 */
	protected void addCustomIdTokenClaims(JWTClaimsSet.Builder idClaims, ClientDetailsEntity client, OAuth2Request request,
	    String sub, OAuth2AccessTokenEntity accessToken) {
	}

}
