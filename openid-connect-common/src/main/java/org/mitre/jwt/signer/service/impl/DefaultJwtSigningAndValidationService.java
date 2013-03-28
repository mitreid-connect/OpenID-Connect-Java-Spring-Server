/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.jwt.signer.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mitre.jose.keystore.JWKSetKeyStore;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Strings;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

public class DefaultJwtSigningAndValidationService implements JwtSigningAndValidationService, InitializingBean {

	// map of identifier to signer
	private Map<String, JWSSigner> signers = new HashMap<String, JWSSigner>();
	// map of identifier to verifier
	private Map<String, JWSVerifier> verifiers = new HashMap<String, JWSVerifier>();

	private static Logger logger = LoggerFactory.getLogger(DefaultJwtSigningAndValidationService.class);

	private String defaultSignerKeyId;
	
	private JWSAlgorithm defaultAlgorithm;
	
	private JWKSetKeyStore keyStore;

	/**
	 * default constructor
	 */
	public DefaultJwtSigningAndValidationService() {

	}

	/**
	 * Create a new validation service from the given set of verifiers (no signing)
	 */
	public DefaultJwtSigningAndValidationService(Map<String, JWSVerifier> verifiers) {
		this.verifiers = verifiers;
	}
	
	/**
	 * @return the defaultSignerKeyId
	 */
	public String getDefaultSignerKeyId() {
		return defaultSignerKeyId;
	}

	/**
	 * @param defaultSignerKeyId the defaultSignerKeyId to set
	 */
	public void setDefaultSignerKeyId(String defaultSignerId) {
		this.defaultSignerKeyId = defaultSignerId;
	}

	/**
	 * @return
	 */
	@Override
    public JWSAlgorithm getDefaultSigningAlgorithm() {
    	return defaultAlgorithm;
    }
    
    public void setDefaultSigningAlgorithmName(String algName) {
    	defaultAlgorithm = JWSAlgorithm.parse(algName);
    }
    
    public String getDefaultSigningAlgorithmName() {
    	if (defaultAlgorithm != null) {
    		return defaultAlgorithm.getName();
    	} else {
    		return null;
    	}
    }
	/**
	 * @return the keyStore
	 */
    public JWKSetKeyStore getKeyStore() {
	    return keyStore;
    }

	/**
	 * @param keyStore the keyStore to set
	 */
    public void setKeyStore(JWKSetKeyStore keyStore) {
	    this.keyStore = keyStore;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws NoSuchAlgorithmException, InvalidKeySpecException{
		
		if (keyStore != null) {
			// if we have a keystore, load it up into our signers
			
			List<JWK> keys = keyStore.getKeys();
			for (JWK jwk : keys) {
				
				if (!Strings.isNullOrEmpty(jwk.getKeyID())) {
				
		            if (jwk instanceof RSAKey) {
		            	// build RSA signers & verifiers
		            	RSASSASigner signer = new RSASSASigner(((RSAKey) jwk).toRSAPrivateKey());
		            	RSASSAVerifier verifier = new RSASSAVerifier(((RSAKey) jwk).toRSAPublicKey());
		            	
		            	signers.put(jwk.getKeyID(), signer);
		            	verifiers.put(jwk.getKeyID(), verifier);
		            	
		            } else if (jwk instanceof ECKey) {
		            	// build EC signers & verifiers
		            	
		            	// TODO: add support for EC keys
		            	logger.warn("EC Keys are not yet supported.");
		            	
		            } else if (jwk instanceof OctetSequenceKey) {
		            	// build HMAC signers & verifiers
		            	MACSigner signer = new MACSigner(((OctetSequenceKey) jwk).toByteArray());
		            	MACVerifier verifier = new MACVerifier(((OctetSequenceKey) jwk).toByteArray());

		            	signers.put(jwk.getKeyID(), signer);
		            	verifiers.put(jwk.getKeyID(), verifier);
		            } else {
		            	logger.warn("Unknown key type: " + jwk);
		            }
				} else {
					logger.warn("Found a key with no KeyId: " + jwk);
				}
            }
			
		}
		
		logger.info("DefaultJwtSigningAndValidationService is ready: " + this.toString());
	}

	/**
	 * Sign a jwt in place using the configured default signer.
	 */
	@Override
	public void signJwt(SignedJWT jwt) {
		if (getDefaultSignerKeyId() == null) {
			throw new IllegalStateException("Tried to call default signing with no default signer ID set");
		}
		
		JWSSigner signer = signers.get(getDefaultSignerKeyId());

		try {
	        jwt.sign(signer);
        } catch (JOSEException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		
	}
	
	@Override
	public boolean validateSignature(SignedJWT jwt) {

		for (JWSVerifier verifier : verifiers.values()) {
			try {
				if (jwt.verify(verifier)) {
					return true;
				}
			} catch (JOSEException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
		}
		return false;
	}

	@Override
	public Map<String, PublicKey> getAllPublicKeys() {
		Map<String, PublicKey> keys = new HashMap<String, PublicKey>();
		
		// pull all keys out of the verifiers if we know how
		for (String keyId : verifiers.keySet()) {
	        JWSVerifier verifier = verifiers.get(keyId);
	        if (verifier instanceof RSASSAVerifier) {
	        	// we know how to do RSA public keys
	        	keys.put(keyId, ((RSASSAVerifier) verifier).getPublicKey());
	        }
        }
		
		return keys;
	}
	
}
