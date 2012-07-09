package org.mitre.jwt.encryption.impl;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwe.model.JweHeader;
import org.mitre.jwt.encryption.AbstractJweDecrypter;
import org.mitre.jwt.signer.impl.HmacSigner;


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
			jwe.setHeader(unencryptedHeader);
			
			String decodedEncryptionKey = new String(Base64.decodeBase64(jwe.getEncryptedKey().toString()));
			//sets decoded key on jwe so that it can be decrypted
			jwe.setEncryptedKey(decodedEncryptionKey.getBytes());
			
			String decodedCiphertext = new String(Base64.decodeBase64(jwe.getCiphertext().toString()));
			//sets decoded ciphertext on jwe so that it can be decrypted
			jwe.setCiphertext(decodedCiphertext.getBytes());
			
			//decode signature, but don't set it on jwe yet. first has to be verified (see below)
			String decodedSig = new String(Base64.decodeBase64(jwe.getSignature()));

			
			PrivateKey contentEncryptionKey = null;
			PublicKey contentIntegrityKey = null;
			
			try {
				
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance(jwe.getHeader().getKeyDerivationFunction());
				KeyPair keyPair = keyGen.genKeyPair();
				contentEncryptionKey = keyPair.getPrivate();
				contentIntegrityKey = keyPair.getPublic();
				
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			jwe.setCiphertext(decryptCipherText(jwe, contentEncryptionKey).getBytes());
			jwe.setEncryptedKey(decryptEncryptionKey(jwe));
			
			//generate signature for decrypted signature base in order to verify that decryption worked
			String signature = null;
			try {
				HmacSigner hmacSigner = new HmacSigner(contentIntegrityKey.getEncoded());
				signature = hmacSigner.generateSignature(jwe.getSignatureBase());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//verifys that the signature base was decrypted correctly
			if(signature != decodedSig){
				throw new IllegalArgumentException("Didn't decrypt correctly. Decoded Sig and generated Sig do not match");
			}
			
			jwe.setSignature(decodedSig);
			
		} else if(alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){
			
			throw new IllegalArgumentException("Cannot use Hmac for decryption");
			
		} else {
			throw new IllegalArgumentException("Not a valid decrypting algorithm");
		}
		return jwe;
	}

}
