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

package cz.muni.ics.openid.connect.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating hashes for access tokens and authorization codes
 * to be included in an ID Token.
 *
 * @author Amanda Anganes
 *
 */
@Slf4j
public class IdTokenHashUtils {

	/**
	 * Compute the SHA hash of an authorization code
	 *
	 * @param signingAlg
	 * @param code
	 * @return
	 */
	public static Base64URL getCodeHash(JWSAlgorithm signingAlg, String code) {
		return getHash(signingAlg, code.getBytes());
	}

	/**
	 * Compute the SHA hash of a token
	 *
	 * @param signingAlg
	 * @param token
	 * @return
	 */
	public static Base64URL getAccessTokenHash(JWSAlgorithm signingAlg, OAuth2AccessTokenEntity token) {

		byte[] tokenBytes = token.getJwt().serialize().getBytes();

		return getHash(signingAlg, tokenBytes);

	}

	public static Base64URL getHash(JWSAlgorithm signingAlg, byte[] bytes) {

		//Switch based on the given signing algorithm - use SHA-xxx with the same 'xxx' bitnumber
		//as the JWSAlgorithm to hash the token.
		String hashAlg = null;

		if (signingAlg.equals(JWSAlgorithm.HS256) || signingAlg.equals(JWSAlgorithm.ES256) || signingAlg.equals(JWSAlgorithm.RS256) || signingAlg.equals(JWSAlgorithm.PS256)) {
			hashAlg = "SHA-256";
		}

		else if (signingAlg.equals(JWSAlgorithm.ES384) || signingAlg.equals(JWSAlgorithm.HS384) || signingAlg.equals(JWSAlgorithm.RS384) || signingAlg.equals(JWSAlgorithm.PS384)) {
			hashAlg = "SHA-384";
		}

		else if (signingAlg.equals(JWSAlgorithm.ES512) || signingAlg.equals(JWSAlgorithm.HS512) || signingAlg.equals(JWSAlgorithm.RS512) || signingAlg.equals(JWSAlgorithm.PS512)) {
			hashAlg = "SHA-512";
		}

		if (hashAlg != null) {

			try {
				MessageDigest hasher = MessageDigest.getInstance(hashAlg);
				hasher.reset();
				hasher.update(bytes);

				byte[] hashBytes = hasher.digest();
				byte[] hashBytesLeftHalf = Arrays.copyOf(hashBytes, hashBytes.length / 2);
				Base64URL encodedHash = Base64URL.encode(hashBytesLeftHalf);

				return encodedHash;

			} catch (NoSuchAlgorithmException e) {

				log.error("No such algorithm error: ", e);

			}

		}

		return null;
	}

}
