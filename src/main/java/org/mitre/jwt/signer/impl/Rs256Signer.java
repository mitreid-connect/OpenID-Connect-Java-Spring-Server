package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwt.signer.AbstractJwtSigner;

/**
 * JWT Signer using RSA SHA-256 algorithm
 * @author AANGANES
 *
 */
public class Rs256Signer extends AbstractJwtSigner {

	//TODO: should this class generate a new private key or get one passed into the constructor?
	private PrivateKey privateKey;
	private Signature signer;
	
	public Rs256Signer() {
		this(null);
	}
	
	public Rs256Signer(PrivateKey privateKey) {
		super("RS256");
		
		setPrivateKey(privateKey);
		
		try {
			signer = Signature.getInstance("SHA256withRSA");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected String generateSignature(String signatureBase) {
		
		try {
			signer.initSign(privateKey);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			signer.update(signatureBase.getBytes("UTF-8"));
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] sigBytes;
		String sig = "";
		
		try {
			sigBytes = signer.sign();
			sig = new String(Base64.encodeBase64URLSafe(sigBytes));
			// strip off any padding
			sig = sig.replace("=", "");
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
	    return sig;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
}
