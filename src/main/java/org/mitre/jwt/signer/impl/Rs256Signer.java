package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwt.signer.AbstractJwtSigner;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * JWT Signer using RSA SHA-256 algorithm
 * @author AANGANES
 *
 */
public class Rs256Signer extends AbstractJwtSigner {

	//TODO: should this class generate a new private key or get one passed into the constructor?
	private PrivateKey privateKey;
	//TODO: where does the publicKey come from?
	private PublicKey publicKey;
	
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

	@Override
	public boolean verify(String jwtString) {
		
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(jwtString));
		
		if (parts.size() != 3) {
			throw new IllegalArgumentException("Invalid JWT format.");
		}
		
		String h64 = parts.get(0);
		String c64 = parts.get(1);
		String s64 = parts.get(2);
    	
		String signingInput = h64 + "." + c64 + ".";
		
		try {
			signer.initVerify(publicKey);
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		
		try {
			signer.update(signingInput.getBytes("UTF-8"));
		} catch (SignatureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			signer.verify(s64.getBytes("UTF-8"));
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
}
