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
package org.mitre.openid.connect.assertion;

import java.text.ParseException;
import java.util.Date;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
public class JwtBearerAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtBearerAuthenticationProvider.class);

	// map of verifiers, load keys for clients
	@Autowired
	private JWKSetCacheService validators;

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

		JwtBearerAssertionAuthenticationToken jwtAuth = (JwtBearerAssertionAuthenticationToken)authentication;


		try {
			ClientDetailsEntity client = clientService.loadClientByClientId(jwtAuth.getClientId());

			JWT jwt = jwtAuth.getJwt();
			ReadOnlyJWTClaimsSet jwtClaims = jwt.getJWTClaimsSet();

			// check the signature with nimbus
			if (jwt instanceof SignedJWT) {
				SignedJWT jws = (SignedJWT)jwt;
				JwtSigningAndValidationService validator = validators.getValidator(client.getJwksUri());
				if (validator == null || !validator.validateSignature(jws)) {
					throw new AuthenticationServiceException("Invalid signature");
				}
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
			} else if (!jwtClaims.getAudience().contains(config.getIssuer())) {
				throw new AuthenticationServiceException("Audience does not match, expected " + config.getIssuer() + " got " + jwtClaims.getAudience());
			}

			// IFF we managed to get all the way down here, the token is valid
			return new JwtBearerAssertionAuthenticationToken(client.getClientId(), jwt, client.getAuthorities());

		} catch (InvalidClientException e) {
			throw new UsernameNotFoundException("Could not find client: " + jwtAuth.getClientId());
		} catch (ParseException e) {

			logger.error("Failure during authentication, error was: ", e);

			throw new AuthenticationServiceException("Invalid JWT format");
		}
	}

	/**
	 * We support {@link JwtBearerAssertionAuthenticationToken}s only.
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return (JwtBearerAssertionAuthenticationToken.class.isAssignableFrom(authentication));
	}


}
