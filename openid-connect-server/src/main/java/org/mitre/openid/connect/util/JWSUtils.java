package org.mitre.openid.connect.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
	
	public static Base64URL getAccessTokenHash(JWSAlgorithm signingAlg, byte[] tokenBytes) {
		
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
				mac.init(new SecretKeySpec(tokenBytes, hashAlg));

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
