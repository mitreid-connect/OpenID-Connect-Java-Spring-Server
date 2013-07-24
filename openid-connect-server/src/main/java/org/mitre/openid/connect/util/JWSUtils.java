package org.mitre.openid.connect.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;

/**
 * Utility class for JWS processing. 
 * 
 * @author Amanda Anganes
 *
 */
public class JWSUtils {

	private static Logger logger = LoggerFactory.getLogger(JWSUtils.class);
	
	/**
	 * Compute the HMAC hash of an authorization code
	 * 
	 * @param signingAlg
	 * @param code
	 * @return
	 */
	public static Base64URL getCodeHash(JWSAlgorithm signingAlg, String code) {
		return getHash(signingAlg, code.getBytes());
	}
	
	/**
	 * Compute the HMAC hash of a token
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
		
		//Switch based on the given signing algorithm - use HMAC with the same bitnumber
		//as the JWSAlgorithm to hash the token.
		String hashAlg = null;
		
		if (signingAlg.equals(JWSAlgorithm.HS256) || signingAlg.equals(JWSAlgorithm.ES256) || signingAlg.equals(JWSAlgorithm.RS256)) {
			hashAlg = "HMACSHA256";
		}
		
		else if (signingAlg.equals(JWSAlgorithm.ES384) || signingAlg.equals(JWSAlgorithm.HS384) || signingAlg.equals(JWSAlgorithm.RS384)) {
			hashAlg = "HMACSHA384";
		}
		
		else if (signingAlg.equals(JWSAlgorithm.ES512) || signingAlg.equals(JWSAlgorithm.HS512) || signingAlg.equals(JWSAlgorithm.RS512)) {
			hashAlg = "HMACSHA512";
		}
		
		if (hashAlg != null) {

			try {
				Mac mac = Mac.getInstance(hashAlg);
				mac.init(new SecretKeySpec(bytes, hashAlg));

				byte[] at_hash_bytes = mac.doFinal();
				byte[] at_hash_bytes_left = Arrays.copyOf(at_hash_bytes, at_hash_bytes.length / 2);
				Base64URL at_hash = Base64URL.encode(at_hash_bytes_left);

				return at_hash;

			} catch (NoSuchAlgorithmException e) {
				
				logger.error("No such algorithm error: ", e);
				
			} catch (InvalidKeyException e) {
				
				logger.error("Invalid key error: ", e);
			}

		}
		
		return null;
	}
	
}
