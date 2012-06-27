package org.mitre.jwt.encryption;

import java.security.NoSuchAlgorithmException;

import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.signer.impl.HmacSigner;
import org.mitre.jwt.signer.impl.RsaSigner;

public abstract class AbstractJweEncrypter implements JwtEncrypter {

	@Override
	public Jwe encrypt(Jwe jwe) {
		
		jwe.setCiphertext(encryptClaims(jwe));
		jwe.setEncryptedKey(encryptKey(jwe));
		
		String alg = jwe.getHeader().getAlgorithm();
		if(alg.equals("RS256") || alg.equals("RS384") || alg.equals("RS512")) {
			RsaSigner rsaSigner = new RsaSigner();
			try {
				jwe = (Jwe) rsaSigner.sign(jwe);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(alg.equals("HS256") || alg.equals("HS384") || alg.equals("HS512")){
			HmacSigner hmacSigner = new HmacSigner();
			try {
				jwe = (Jwe) hmacSigner.sign(jwe);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("Not a valid signing algorithm");
		}
		
		return jwe;
	}

}
