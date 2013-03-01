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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;

public class DefaultJwtSigningAndValidationService implements JwtSigningAndValidationService, InitializingBean {

	// map of identifier to signer
	private Map<String, JWSSigner> signers = new HashMap<String, JWSSigner>();
	// map of identifier to verifier
	private Map<String, JWSVerifier> verifiers = new HashMap<String, JWSVerifier>();

	private static Logger logger = LoggerFactory.getLogger(DefaultJwtSigningAndValidationService.class);

	private String defaultSignerId;
	
	private JWSAlgorithm defaultAlgorithm;

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
	 * Load this signing and validation service from the given builders (which load keys from keystores) 
	 * @param builders
	 */
	public void setBuilders(Map<String, RSASSASignerVerifierBuilder> builders) {
		
		for (Entry<String, RSASSASignerVerifierBuilder> e : builders.entrySet()) {
	        
			JWSSigner signer = e.getValue().buildSigner();
			signers.put(e.getKey(), signer);
			if (e.getValue().isDefault()) {
				defaultSignerId = e.getKey();
			}
			
	        JWSVerifier verifier = e.getValue().buildVerifier();
	        verifiers.put(e.getKey(), verifier);
	        
        }
		
	}

	/**
	 * @return the defaultSignerId
	 */
	public String getDefaultSignerId() {
		return defaultSignerId;
	}

	/**
	 * @param defaultSignerId the defaultSignerId to set
	 */
	public void setDefaultSignerId(String defaultSignerId) {
		this.defaultSignerId = defaultSignerId;
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
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet(){
		logger.info("DefaultJwtSigningAndValidationService is ready: " + this.toString());
	}

	/**
	 * Sign a jwt in place using the configured default signer.
	 */
	@Override
	public void signJwt(SignedJWT jwt) {
		if (getDefaultSignerId() == null) {
			throw new IllegalStateException("Tried to call default signing with no default signer ID set");
		}
		
		JWSSigner signer = signers.get(getDefaultSignerId());

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
