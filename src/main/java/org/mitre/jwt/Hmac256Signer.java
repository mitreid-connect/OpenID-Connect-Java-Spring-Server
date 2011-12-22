package org.mitre.jwt;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class Hmac256Signer extends AbstractJwtSigner {

	private Mac mac;
	
	private byte[] passphrase;
	
	public Hmac256Signer() {
		this(null);
	}
	
	public Hmac256Signer(byte[] passphrase) {
		super(HS256);
		setPassphrase(passphrase);
		
		try {
	        mac = Mac.getInstance("HMACSHA256");
        } catch (NoSuchAlgorithmException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        
	}

	/* (non-Javadoc)
     * @see org.mitre.jwt.AbstractJwtSigner#sign(org.mitre.jwt.Jwt)
     */
    @Override
    public void sign(Jwt jwt) {
	    super.sign(jwt);
	    
	    if (passphrase == null) {
	    	return; // TODO: probably throw some kind of exception
	    }
	    
	    try {
	        mac.init(new SecretKeySpec(getPassphrase(), mac.getAlgorithm()));
        } catch (InvalidKeyException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        try {
	        mac.update(jwt.getSignatureBase().getBytes("UTF-8"));
        } catch (IllegalStateException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        byte[] sigBytes = mac.doFinal();
        
        String sig = new String(Base64.encodeBase64URLSafe(sigBytes));
	    
        // strip off any padding
        sig = sig.replace("=", "");
        
        jwt.setSignature(sig);
    }

	/**
     * @return the passphrase
     */
    public byte[] getPassphrase() {
    	return passphrase;
    }

	/**
     * @param passphrase the passphrase to set
     */
    public void setPassphrase(byte[] passphrase) {
    	this.passphrase = passphrase;
    }
	
	

}
