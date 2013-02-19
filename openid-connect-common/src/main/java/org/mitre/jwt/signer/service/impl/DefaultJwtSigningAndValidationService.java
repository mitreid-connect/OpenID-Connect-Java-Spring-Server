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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;

public class DefaultJwtSigningAndValidationService implements JwtSigningAndValidationService, InitializingBean {

	@Autowired 
	private ConfigurationPropertiesBean configBean;
	
	// map of identifier to signer
	private Map<String, JWSSigner> signers = new HashMap<String, JWSSigner>();
	// map of identifier to verifier
	private Map<String, JWSVerifier> verifiers = new HashMap<String, JWSVerifier>();

	private static Logger logger = LoggerFactory.getLogger(DefaultJwtSigningAndValidationService.class);

	/**
	 * default constructor
	 */
	public DefaultJwtSigningAndValidationService() {

	}
	
	public DefaultJwtSigningAndValidationService(Map<String, RSASSASignerVerifierBuilder> builders) {
		
		for (Entry<String, RSASSASignerVerifierBuilder> e : builders.entrySet()) {
	        
			JWSSigner signer = e.getValue().buildSigner();
			signers.put(e.getKey(), signer);
			
	        JWSVerifier verifier = e.getValue().buildVerifier();
	        verifiers.put(e.getKey(), verifier);
	        
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
		// used for debugging...
		if (!signers.isEmpty()) {
			logger.info(this.toString());
		}

		logger.info("DefaultJwtSigningAndValidationService is open for business");

	}

	/**
	 * @return the configBean
	 */
	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	/**
	 * @param configBean the configBean to set
	 */
	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}
	

	/**
	 * Sign a jwt in place using the configured default signer.
	 * @throws JOSEException 
	 * @throws NoSuchAlgorithmException 
	 */
	@Override
	public void signJwt(SignedJWT jwt) {
		String signerId = configBean.getDefaultJwtSigner();
		
		JWSSigner signer = signers.get(signerId);
		
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

}
