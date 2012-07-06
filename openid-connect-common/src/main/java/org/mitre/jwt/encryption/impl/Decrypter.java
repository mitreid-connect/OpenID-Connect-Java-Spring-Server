package org.mitre.jwt.encryption.impl;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwe.model.JweHeader;
import org.mitre.jwt.encryption.AbstractJweDecrypter;


public class Decrypter extends AbstractJweDecrypter {
	
	private Jwe jwe;
	
	public Decrypter(Jwe jwe) {
		setJwe(jwe);
	}
	
	public Jwe getJwe() {
		return jwe;
	}

	public void setJwe(Jwe jwe) {
		this.jwe = jwe;
	}
	
	@Override
	public Jwe decrypt(String encryptedJwe) {
		
		Jwe jwe = Jwe.parse(encryptedJwe);
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			
			//Base 64 decode each part of the jwe
			String decodedHeader = new String(Base64.decodeBase64(jwe.getHeader().toString()));
			JweHeader unencryptedHeader = new JweHeader(decodedHeader);
			
			String decodedEncryptionKey = new String(Base64.decodeBase64(jwe.getEncryptedKey().toString()));
			//sets decoded key on jwe so that it can be decrypted
			jwe.setEncryptedKey(decodedEncryptionKey.getBytes());
			
			String decodedCiphertext = new String(Base64.decodeBase64(jwe.getCiphertext().toString()));
			//sets decoded ciphertext on jwe so that it can be decrypted
			jwe.setCiphertext(decodedCiphertext.getBytes());
			
			String decodedSig = new String(Base64.decodeBase64(jwe.getSignature()));
			
			//create new jwe using the decoded header and signature, and decrypt the ciphertext and key
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
