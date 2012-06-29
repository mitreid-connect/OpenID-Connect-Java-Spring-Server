package org.mitre.jwt.encryption.impl;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweDecrypter;
import org.mitre.jwt.model.JwtHeader;

public class RsaDecrypter extends AbstractJweDecrypter {
	
	public RsaDecrypter(Jwe jwe) {
		setJwe(jwe);
	}
	
	@Override
	public Jwe decrypt(String encryptedJwe) {
		
		Jwe jwe = Jwe.parse(encryptedJwe);
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			
			String decodedHeader = new String(Base64.decodeBase64(jwe.getHeader().toString()));
			JwtHeader unencryptedHeader = new JwtHeader(decodedHeader);
			String decodedSig = new String(Base64.decodeBase64(jwe.getSignature()));
			
			jwe.setHeader(unencryptedHeader);
			jwe.setCiphertext(decryptCipherText(jwe).getBytes());
			jwe.setEncryptedKey(decryptEncryptionKey(jwe));
			jwe.setSignature(decodedSig);
			
		} else if(alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){
			
			throw new IllegalArgumentException("Cannot use Hmac for decryption");
			
		} else {
			throw new IllegalArgumentException("Not a valid decrypting algorithm");
		}
		return jwe;
	}

}
