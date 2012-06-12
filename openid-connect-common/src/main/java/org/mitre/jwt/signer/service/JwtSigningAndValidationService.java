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
package org.mitre.jwt.signer.service;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Map;

import org.mitre.jwt.model.Jwt;

public interface JwtSigningAndValidationService {

	/**
	 * Returns all public keys this service is configured with, indexed by key id
	 * 
	 * @return
	 */
	public Map<String, PublicKey> getAllPublicKeys();

	/**
	 * Check to see if this JWT has expired or not
	 * 
	 * @param jwt
	 *            the JWT to check
	 * @return true if this JWT has an expiration and it has passed, false if
	 *         the JWT has no expiration or it has an expiration and the
	 *         expiration has not passed
	 */
	public boolean isJwtExpired(Jwt jwt);

	/**
	 * Checks to see if this JWT has been issued by us
	 * 
	 * @param jwt
	 *            the JWT to check the issuer of
	 * @param expectedIssuer
	 *            the expected issuer
	 * @return true if the JWT was issued by this expected issuer, false if not
	 */
	public boolean validateIssuedJwt(Jwt jwt, String expectedIssuer);

	/**
	 * Checks the signature of the given JWT against all configured signers,
	 * returns true if at least one of the signers validates it.
	 * 
	 * @param jwtString
	 *            the string representation of the JWT as sent on the wire
	 * @return true if the signature is valid, false if not
	 * @throws NoSuchAlgorithmException 
	 */
	public boolean validateSignature(String jwtString) throws NoSuchAlgorithmException;
	
	/**
	 * Called to sign a jwt in place for a client that hasn't registered a preferred signing algorithm.
	 * Use the default algorithm to sign.
	 * 
	 * @param jwt the jwt to sign
	 * @return the signed jwt
	 * @throws NoSuchAlgorithmException 
	 */
	public boolean validateIssuedAt(Jwt jwt);
	
	/**
	 * Checks to see when this JWT was issued
	 * 
	 * @param jwt
	 * 		the JWT to check
	 * @return true if the issued at is valid, false if not
	 * @throws NoSuchAlgorithmException
	 */
	public boolean validateAudience(Jwt jwt, String clientId);
	
	/**
	 * Checks the audience that the given JWT against the client_id of the Client
	 * 
	 * @param jwt
	 * @param clientId
	 * 			the string representation of the client_id
	 * @return true if the audience matches the clinet_id, false if otherwise
	 * @throws NoSuchAlgorithmException
	 */
	public boolean validateNonce(Jwt jwt, String nonce);
	
	/**
	 * Checks to see if the nonce parameter sent in the Authorization Request 
	 * is equal to the nonce parameter in the id token
	 * 
	 * @param jwt
	 * @param nonce
	 * 			the string representation of the Nonce
	 * @return true if both nonce parameters are equal, false if otherwise 
	 * @throws NoSuchAlgorithmException
	 */
	public void signJwt(Jwt jwt) throws NoSuchAlgorithmException;
	
	/**
	 * Sign a jwt using the selected algorithm. The algorithm is selected using the String parameter values specified
	 * in the JWT spec, section 6. I.E., "HS256" means HMAC with SHA-256 and corresponds to our HmacSigner class.
	 * 
	 * @param jwt the jwt to sign
	 * @param alg the name of the algorithm to use, as specified in JWS s.6
	 * @return the signed jwt
	 */
	//TODO: implement later; only need signJwt(Jwt jwt) for now
	//public Jwt signJwt(Jwt jwt, String alg);
	
	/**
	 * TODO: method to sign a jwt using a specified algorithm and a key id
	 */
}
