package org.mitre.jwt.encryption;

import org.apache.commons.lang.StringUtils;

public enum JweAlgorithms {
	
	//Key Derivation Function Values
	CS256("256"),
	CS384("384"),
	CS512("512"),
	//Encryption Method Values
	A128GCM("GCM"),
	A256GCM("GCM"),
	A128CBC("CBC"),
	A256CBC("CBC");
	
	
	
	private final String value;
	
	JweAlgorithms(String value) {
		this.value = value;
	}
	
	public static String getByName(String name) {
		for (JweAlgorithms correspondingType : JweAlgorithms.values()) {
			if (correspondingType.toString().equals(name)) {
				return correspondingType.value;
			}
		}
		throw new IllegalArgumentException(
				"JweAlgorithm name " + name + " does not have a corresponding JweAlgorithm: expected one of [" + StringUtils.join(JweAlgorithms.values(), ", ") + "]");
	}

	public String getValue() {
		return value;
	}

}
