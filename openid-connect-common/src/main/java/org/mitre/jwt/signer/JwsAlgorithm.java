package org.mitre.jwt.signer;

import org.apache.commons.lang.StringUtils;

/**
 * Enum to translate between the JWS defined algorithm names and the JSE algorithm names
 * 
 * @author jricher
 *
 */
public enum JwsAlgorithm {

	// HMAC
	HS256("HMACSHA256"), 
	HS384("HMACSHA384"), 
	HS512("HMACSHA512"),
	// RSA
	RS256("SHA256withRSA"), 
	RS384("SHA384withRSA"), 
	RS512("SHA512withRSA");
	
	/**
	 * Returns the Algorithm for the name
	 * 
	 * @param name
	 * @return
	 */
	public static JwsAlgorithm getByName(String name) {
		for (JwsAlgorithm correspondingType : JwsAlgorithm.values()) {
			if (correspondingType.toString().equals(name)) {
				return correspondingType;
			}
		}

		// corresponding type not found
		throw new IllegalArgumentException(
				"JwsAlgorithm name " + name + " does not have a corresponding JwsAlgorithm: expected one of [" + StringUtils.join(JwsAlgorithm.values(), ", ") + "]");
	}

	private final String standardName;

	/**
	 * Constructor of JwsAlgorithm
	 * 
	 * @param standardName
	 */
	JwsAlgorithm(String standardName) {
		this.standardName = standardName;
	}

	/**
	 * Return the Java standard JwsAlgorithm name
	 * 
	 * @return
	 */
	public String getStandardName() {
		return standardName;
	}
}
