package org.mitre.jwt.encryption;

import org.apache.commons.lang.StringUtils;

public enum JwtAlgorithm {
	
	//TODO:Fill in values for each standard name
	// RSA
	RSA1_5(""), 
	RSA_OAEP(""), 
	//EC
	ECDH_ES(""),
	//AES
	A128KW(""),
	A256KW(""),
	A128GCM(""),
	A256GCM("");
	
	
	/**
	 * Returns the Algorithm for the name
	 * 
	 * @param name
	 * @return
	 */
	public static JwtAlgorithm getByName(String name) {
		for (JwtAlgorithm correspondingType : JwtAlgorithm.values()) {
			if (correspondingType.toString().equals(name)) {
				return correspondingType;
			}
		}

		// corresponding type not found
		throw new IllegalArgumentException(
				"JwtAlgorithm name " + name + " does not have a corresponding JwtAlgorithm: expected one of [" + StringUtils.join(JwtAlgorithm.values(), ", ") + "]");
	}

	private final String standardName;

	/**
	 * Constructor of JwtAlgorithm
	 * 
	 * @param standardName
	 */
	JwtAlgorithm(String standardName) {
		this.standardName = standardName;
	}

	/**
	 * Return the Java standard JwtAlgorithm name
	 * 
	 * @return
	 */
	public String getStandardName() {
		return standardName;
	}
}
