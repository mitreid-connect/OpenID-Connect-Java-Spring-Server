package org.mitre.jwt.signer.impl;

import org.mitre.jwt.signer.AbstractJwtSigner;

public class Es256Signer extends AbstractJwtSigner {

	public Es256Signer() {
		this(null);
	}
	
	public Es256Signer(String algorithm) {
		super("ES256");
	}

	@Override
	protected String generateSignature(String signatureBase) {
		
		/*
		1) Generate a digital signature of the UTF-8 representation of the JWS Signing Input 
			using ECDSA P-256 SHA-256 with the desired private key. The output will be the 
			EC point (R, S), where R and S are unsigned integers.
		2) Turn R and S into byte arrays in big endian order. Each array will be 32 bytes long.
		3) Concatenate the two byte arrays in the order R and then S.
		4) Base64url encode the resulting 64 byte array.
		 */
		
		return null;
	}
	
	@Override
	public boolean verify(String jwtString) {
		
		/*
		1) Take the Encoded JWS Signature and base64url decode it into a byte array. 
			If decoding fails, the signed content MUST be rejected.
		2) The output of the base64url decoding MUST be a 64 byte array.
		3) Split the 64 byte array into two 32 byte arrays. The first array will be R and 
			the second S. Remember that the byte arrays are in big endian byte order; 
			please check the ECDSA validator in use to see what byte order it requires.
		4) Submit the UTF-8 representation of the JWS Signing Input, R, S and the public 
			key (x, y) to the ECDSA P-256 SHA-256 validator.
		5) If the validation fails, the signed content MUST be rejected.
		*/
		
		return false;
	}

}
