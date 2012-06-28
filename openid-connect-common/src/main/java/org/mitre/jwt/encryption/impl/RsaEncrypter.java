package org.mitre.jwt.encryption.impl;

import java.security.NoSuchAlgorithmException;

import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweEncrypter;
import org.mitre.jwt.signer.impl.RsaSigner;

public class RsaEncrypter extends AbstractJweEncrypter {
	
	public RsaEncrypter(Jwe jwe) {
		setJwe(jwe);
		setHeader(jwe.getHeader());
		setClaims(jwe.getClaims());
		setSignature(jwe.getSignature());
	}

	@Override
	public Jwe encryptAndSign(Jwe jwe) {
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			
			jwe.setCiphertext(encryptClaims(jwe));
			jwe.setEncryptedKey(encryptKey(jwe));
			
			RsaSigner rsaSigner = new RsaSigner(); //TODO: Add parameters to RsaSigner. ie: keys from keystore (null at the moment)
			try {
				jwe = (Jwe) rsaSigner.sign(jwe);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){
			
			throw new IllegalArgumentException("Cannot use Hmac for encryption");
			
		} else {
			throw new IllegalArgumentException("Not a valid signing algorithm");
		}
		
		return jwe;
	}

}
