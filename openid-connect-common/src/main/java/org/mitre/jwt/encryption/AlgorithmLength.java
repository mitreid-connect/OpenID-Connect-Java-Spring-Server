package org.mitre.jwt.encryption;

import org.apache.commons.lang.StringUtils;

public enum AlgorithmLength {
	//TODO:Fill in values for each standard name
	// RSA
	A128CBC("128"),
	A256CBC("256"),
	A128GCM("128"),
	A256GCM("256");
	
	
	/**
	 * Returns the Algorithm for the name
	 * 
	 * @param name
	 * @return
	 */
	public static AlgorithmLength getByName(String name) {
		for (AlgorithmLength correspondingType : AlgorithmLength.values()) {
			if (correspondingType.toString().equals(name)) {
				return correspondingType;
			}
		}

		// corresponding type not found
		throw new IllegalArgumentException(
				"AlgorithmLength name " + name + " does not have a corresponding AlgorithmLength: expected one of [" + StringUtils.join(AlgorithmLength.values(), ", ") + "]");
	}

	private final String standardName;

	/**
	 * Constructor of AlgorithmLength
	 * 
	 * @param standardName
	 */
	AlgorithmLength(String standardName) {
		this.standardName = standardName;
	}

	/**
	 * Return the Java standard Algorithm Length
	 * 
	 * @return
	 */
	public String getStandardName() {
		return standardName;
	}

}
