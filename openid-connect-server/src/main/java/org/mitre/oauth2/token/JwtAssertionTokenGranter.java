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
package org.mitre.oauth2.token;

import java.text.ParseException;
import java.util.Date;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
@Component("jwtAssertionTokenGranter")
public class JwtAssertionTokenGranter extends AbstractTokenGranter {

	private static final String grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer";

	// keep down-cast versions so we can get to the right queries
	private OAuth2TokenEntityService tokenServices;

	@Autowired
	private JwtSigningAndValidationService jwtService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@Autowired
	public JwtAssertionTokenGranter(OAuth2TokenEntityService tokenServices, ClientDetailsEntityService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
		this.tokenServices = tokenServices;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getOAuth2Authentication(org.springframework.security.oauth2.provider.AuthorizationRequest)
	 */
	@Override
	protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) throws AuthenticationException, InvalidTokenException {
		// read and load up the existing token
		String incomingTokenValue = tokenRequest.getRequestParameters().get("assertion");
		OAuth2AccessTokenEntity incomingToken = tokenServices.readAccessToken(incomingTokenValue);

		if (incomingToken.getScope().contains(OAuth2AccessTokenEntity.ID_TOKEN_SCOPE)) {

			if (!client.getClientId().equals(tokenRequest.getClientId())) {
				throw new InvalidClientException("Not the right client for this token");
			}

			// it's an ID token, process it accordingly

			try {

				// TODO: make this use a more specific idtoken class
				JWT idToken = JWTParser.parse(incomingTokenValue);

				OAuth2AccessTokenEntity accessToken = tokenServices.getAccessTokenForIdToken(incomingToken);

				if (accessToken != null) {

					//OAuth2AccessTokenEntity newIdToken = tokenServices.get

					OAuth2AccessTokenEntity newIdTokenEntity = new OAuth2AccessTokenEntity();

					// copy over all existing claims
					JWTClaimsSet claims = new JWTClaimsSet(idToken.getJWTClaimsSet());

					if (client instanceof ClientDetailsEntity) {

						ClientDetailsEntity clientEntity = (ClientDetailsEntity) client;

						// update expiration and issued-at claims
						if (clientEntity.getIdTokenValiditySeconds() != null) {
							Date expiration = new Date(System.currentTimeMillis() + (clientEntity.getIdTokenValiditySeconds() * 1000L));
							claims.setExpirationTime(expiration);
							newIdTokenEntity.setExpiration(expiration);
						}

					} else {
						//This should never happen
						logger.fatal("SEVERE: Client is not an instance of OAuth2AccessTokenEntity.");
						throw new BadCredentialsException("SEVERE: Client is not an instance of ClientDetailsEntity; JwtAssertionTokenGranter cannot process this request.");
					}

					claims.setIssueTime(new Date());


					SignedJWT newIdToken = new SignedJWT((JWSHeader) idToken.getHeader(), claims);
					jwtService.signJwt(newIdToken);

					newIdTokenEntity.setJwt(newIdToken);
					newIdTokenEntity.setAuthenticationHolder(incomingToken.getAuthenticationHolder());
					newIdTokenEntity.setScope(incomingToken.getScope());
					newIdTokenEntity.setClient(incomingToken.getClient());

					newIdTokenEntity = tokenServices.saveAccessToken(newIdTokenEntity);

					// attach the ID token to the access token entity
					accessToken.setIdToken(newIdTokenEntity);
					accessToken = tokenServices.saveAccessToken(accessToken);

					// delete the old ID token
					tokenServices.revokeAccessToken(incomingToken);

					return newIdTokenEntity;

				}
			} catch (ParseException e) {
				logger.warn("Couldn't parse id token", e);
			}

		}

		// if we got down here, we didn't actually create any tokens, so return null

		return null;

		/*
		 * Otherwise, process it like an access token assertion ... which we don't support yet so this is all commented out
		 * /
	    if (jwtService.validateSignature(incomingTokenValue)) {

	    	Jwt jwt = Jwt.parse(incomingTokenValue);


	    	if (oldToken.getScope().contains("id-token")) {
	    		// TODO: things
	    	}

	    	// TODO: should any of these throw an exception instead of returning null?
	    	JwtClaims claims = jwt.getClaims();
	    	if (!config.getIssuer().equals(claims.getIssuer())) {
	    		// issuer isn't us
	    		return null;
	    	}

	    	if (!authorizationRequest.getClientId().equals(claims.getAudience())) {
	    		// audience isn't the client
	    		return null;
	    	}

	    	Date now = new Date();
	    	if (!now.after(claims.getExpiration())) {
	    		// token is expired
	    		return null;
	    	}

	    	// FIXME
	    	// This doesn't work. We need to look up the old token, figure out its scopes and bind it appropriately.
	    	// In the case of an ID token, we need to look up its parent access token and change the reference, and revoke the old one, and
	    	// that's tricky.
	    	// we might need new calls on the token services layer to handle this, and we might
	    	// need to handle id tokens separately.
	    	return new OAuth2Authentication(authorizationRequest, null);

	    } else {
	    	return null; // throw error??
	    }
		 */

	}



}
