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
/**
 *
 */
package cz.muni.ics.openid.connect.assertion;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.jwt.signer.service.impl.ClientKeyCacheService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AuthMethod;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;

/**
 * @author jricher
 *
 */
@Slf4j
public class JWTBearerAuthenticationProvider implements AuthenticationProvider {

	private static final GrantedAuthority ROLE_CLIENT = new SimpleGrantedAuthority("ROLE_CLIENT");

	// map of verifiers, load keys for clients
	@Autowired
	private ClientKeyCacheService validators;

	// Allow for time sync issues by having a window of X seconds.
	private int timeSkewAllowance = 300;

	// to load clients
	@Autowired
	private ClientDetailsEntityService clientService;

	// to get our server's issuer url
	@Autowired
	private ConfigurationPropertiesBean config;

	/**
	 * Try to validate the client credentials by parsing and validating the JWT.
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		JWTBearerAssertionAuthenticationToken jwtAuth = (JWTBearerAssertionAuthenticationToken)authentication;


		try {
			ClientDetailsEntity client = clientService.loadClientByClientId(jwtAuth.getName());

			JWT jwt = jwtAuth.getJwt();
			JWTClaimsSet jwtClaims = jwt.getJWTClaimsSet();

			if (!(jwt instanceof SignedJWT)) {
				throw new AuthenticationServiceException("Unsupported JWT type: " + jwt.getClass().getName());
			}

			// check the signature with nimbus
			SignedJWT jws = (SignedJWT) jwt;

			JWSAlgorithm alg = jws.getHeader().getAlgorithm();

			if (client.getTokenEndpointAuthSigningAlg() != null &&
					!client.getTokenEndpointAuthSigningAlg().equals(alg)) {
				throw new AuthenticationServiceException("Client's registered token endpoint signing algorithm (" + client.getTokenEndpointAuthSigningAlg()
						+ ") does not match token's actual algorithm (" + alg.getName() + ")");
			}

			if (client.getTokenEndpointAuthMethod() == null ||
					client.getTokenEndpointAuthMethod().equals(AuthMethod.NONE) ||
					client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_BASIC) ||
					client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_POST)) {

				// this client doesn't support this type of authentication
				throw new AuthenticationServiceException("Client does not support this authentication method.");

			} else if ((client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY) &&
					(alg.equals(JWSAlgorithm.RS256)
							|| alg.equals(JWSAlgorithm.RS384)
							|| alg.equals(JWSAlgorithm.RS512)
							|| alg.equals(JWSAlgorithm.ES256)
							|| alg.equals(JWSAlgorithm.ES384)
							|| alg.equals(JWSAlgorithm.ES512)
							|| alg.equals(JWSAlgorithm.PS256)
							|| alg.equals(JWSAlgorithm.PS384)
							|| alg.equals(JWSAlgorithm.PS512)))
					|| (client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT) &&
					(alg.equals(JWSAlgorithm.HS256)
							|| alg.equals(JWSAlgorithm.HS384)
							|| alg.equals(JWSAlgorithm.HS512)))) {

				// double-check the method is asymmetrical if we're in HEART mode
				if (config.isHeartMode() && !client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY)) {
					throw new AuthenticationServiceException("[HEART mode] Invalid authentication method");
				}

				JWTSigningAndValidationService validator = validators.getValidator(client, alg);

				if (validator == null) {
					throw new AuthenticationServiceException("Unable to create signature validator for client " + client + " and algorithm " + alg);
				}

				if (!validator.validateSignature(jws)) {
					throw new AuthenticationServiceException("Signature did not validate for presented JWT authentication.");
				}
			} else {
				throw new AuthenticationServiceException("Unable to create signature validator for method " + client.getTokenEndpointAuthMethod() + " and algorithm " + alg);
			}

			// check the issuer
			if (jwtClaims.getIssuer() == null) {
				throw new AuthenticationServiceException("Assertion Token Issuer is null");
			} else if (!jwtClaims.getIssuer().equals(client.getClientId())){
				throw new AuthenticationServiceException("Issuers do not match, expected " + client.getClientId() + " got " + jwtClaims.getIssuer());
			}

			// check expiration
			if (jwtClaims.getExpirationTime() == null) {
				throw new AuthenticationServiceException("Assertion Token does not have required expiration claim");
			} else {
				// it's not null, see if it's expired
				Date now = new Date(System.currentTimeMillis() - (timeSkewAllowance * 1000));
				if (now.after(jwtClaims.getExpirationTime())) {
					throw new AuthenticationServiceException("Assertion Token is expired: " + jwtClaims.getExpirationTime());
				}
			}

			// check not before
			if (jwtClaims.getNotBeforeTime() != null) {
				Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
				if (now.before(jwtClaims.getNotBeforeTime())){
					throw new AuthenticationServiceException("Assertion Token not valid untill: " + jwtClaims.getNotBeforeTime());
				}
			}

			// check issued at
			if (jwtClaims.getIssueTime() != null) {
				// since it's not null, see if it was issued in the future
				Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
				if (now.before(jwtClaims.getIssueTime())) {
					throw new AuthenticationServiceException("Assertion Token was issued in the future: " + jwtClaims.getIssueTime());
				}
			}

			// check audience
			if (jwtClaims.getAudience() == null) {
				throw new AuthenticationServiceException("Assertion token audience is null");
			} else if (!(jwtClaims.getAudience().contains(config.getIssuer()) || jwtClaims.getAudience().contains(config.getIssuer() + "token"))) {
				throw new AuthenticationServiceException("Audience does not match, expected " + config.getIssuer() + " or " + (config.getIssuer() + "token") + " got " + jwtClaims.getAudience());
			}

			// IFF we managed to get all the way down here, the token is valid

			// add in the ROLE_CLIENT authority
			Set<GrantedAuthority> authorities = new HashSet<>(client.getAuthorities());
			authorities.add(ROLE_CLIENT);

			return new JWTBearerAssertionAuthenticationToken(jwt, authorities);

		} catch (InvalidClientException e) {
			throw new UsernameNotFoundException("Could not find client: " + jwtAuth.getName());
		} catch (ParseException e) {

			log.error("Failure during authentication, error was: ", e);

			throw new AuthenticationServiceException("Invalid JWT format");
		}
	}

	/**
	 * We support {@link JWTBearerAssertionAuthenticationToken}s only.
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return (JWTBearerAssertionAuthenticationToken.class.isAssignableFrom(authentication));
	}

}
