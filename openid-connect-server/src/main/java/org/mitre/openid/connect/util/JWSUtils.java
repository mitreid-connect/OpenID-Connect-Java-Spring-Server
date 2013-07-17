package org.mitre.openid.connect.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		
		//Switch based on the given signing algorithm
		
		String algName = signingAlg.getName();
		
		if (algName.equals(JWSAlgorithm.HS256)) {
			
		}
		
		Pattern re = Pattern.compile("^[HRE]S(\\d+)$");
		Matcher match = re.matcher(algName);
		if (match.matches()) {
			String bits = match.group(1);
			String hmacAlg = "HMACSHA" + bits;
			try {
				Mac mac = Mac.getInstance(hmacAlg);
				mac.init(new SecretKeySpec(tokenBytes, hmacAlg));

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
