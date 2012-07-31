package org.mitre.jwt.encryption;

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

}
