/**
 * 
 */
package org.mitre.jwt.signer.service.impl;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.mitre.jwt.encryption.impl.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;

/**
 * @author jricher
 *
 */
public class RSASSASignerVerifierBuilder {
	
	private static Logger log = LoggerFactory.getLogger(RSASSASignerVerifierBuilder.class);

	private String alias;
	private String password;
	private KeyStore keystore;
	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}
	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the keystore
	 */
	public KeyStore getKeystore() {
		return keystore;
	}
	/**
	 * @param keystore the keystore to set
	 */
	public void setKeystore(KeyStore keystore) {
		this.keystore = keystore;
	}
	
	/**
	 * Build the signer as configured from the given keystore, null if it can't be built for some reason
	 * @return
	 */
	public RSASSASigner buildSigner() {
		
		try {
	        KeyPair keyPair = keystore.getKeyPairForAlias(alias, password);
	        
	        PrivateKey privateKey = keyPair.getPrivate();
	        
	        if (privateKey instanceof RSAPrivateKey) {
	        	RSASSASigner signer = new RSASSASigner((RSAPrivateKey) privateKey);
	        	return signer;
	        } else {
	        	log.warn("Couldn't build signer, referenced key is not RSA");
	        	return null;
	        }
        } catch (GeneralSecurityException e) {
	        // TODO Auto-generated catch block
	        log.warn("Couldn't buld signer:", e);
	        
        }
		
		log.warn("Couldn't build signer");
		return null;
		
	}
	
	/**
	 * Build the signer as configured from the given keystore, null if it can't be built for some reason
	 * @return
	 */
	public RSASSAVerifier buildVerifier() {
		
		try {
	        KeyPair keyPair = keystore.getKeyPairForAlias(alias, password);
	        
	        PublicKey publicKey = keyPair.getPublic();
	        
	        if (publicKey instanceof RSAPublicKey) {
	        	RSASSAVerifier signer = new RSASSAVerifier((RSAPublicKey) publicKey);
	        	return signer;
	        } else {
	        	log.warn("Couldn't build verifier, referenced key is not RSA");
	        	return null;
	        }
        } catch (GeneralSecurityException e) {
	        // TODO Auto-generated catch block
	        log.warn("Couldn't buld verifier:", e);
	        
        }
		
		log.warn("Couldn't build verifier");
		return null;
		
	}
}
