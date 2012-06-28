package org.mitre.jwt.encryption;

import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.impl.RsaDecrypter;


public abstract class AbstractJweDecrypter implements JwtDecrypter {
	
	@Override
	public Jwe decrypt(Jwe jwe) {
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			
			RsaDecrypter decrypter = new RsaDecrypter(jwe);
			jwe.setCiphertext(decrypter.decryptCipherText(jwe).getBytes()); //TODO: When decrypting, should it return a jwe or jwt?
			jwe.setEncryptedKey(decrypter.decryptEncryptionKey(jwe));
			
		} else if(alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){
			
			throw new IllegalArgumentException("Cannot use Hmac for decryption");
			
		} else {
			throw new IllegalArgumentException("Not a valid decrypting algorithm");
		}
		
		return jwe;
	}

}
