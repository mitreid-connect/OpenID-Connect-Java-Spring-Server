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

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class JwtSigningAndValidationServiceDefault extends AbstractJwtSigningAndValidationService implements
		JwtSigningAndValidationService, InitializingBean {

	@Autowired 
	private ConfigurationPropertiesBean configBean;
	
	// map of identifier to signer
	private Map<String, ? extends JwtSigner> signers = new HashMap<String, JwtSigner>();

	private static Logger logger = LoggerFactory.getLogger(JwtSigningAndValidationServiceDefault.class);

	/**
	 * default constructor
	 */
	public JwtSigningAndValidationServiceDefault() {
	}

	/**
	 * Create JwtSigningAndValidationServiceDefault
	 * 
	 * @param signer
	 *            List of JwtSigners to associate with this service
	 */
	public JwtSigningAndValidationServiceDefault(
			Map<String, ? extends JwtSigner> signer) {
		setSigners(signer);
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

		logger.info("JwtSigningAndValidationServiceDefault is open for business");

	}

	/**
	 * 
	 * Returns a copy of the collection of signers.
	 * 
	 * @see
	 * org.mitre.jwt.signer.service.JwtSigningAndValidationService#getAllPublicKeys
	 * ()
	 */
	@Override
	public Map<String, JwtSigner> getAllSigners() {

		Map<String, JwtSigner> map = new HashMap<String, JwtSigner>();

		map.putAll(signers);

		return map;
	}

	/**
	 * Set the JwtSigners associated with this service
	 * 
	 * @param signers
	 *            List of JwtSigners to associate with this service
	 */
	public void setSigners(Map<String, ? extends JwtSigner> signers) {
		this.signers = signers;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JwtSigningAndValidationServiceDefault [signers=" + signers
				+ "]";
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
	 * @throws NoSuchAlgorithmException 
	 */
	@Override
	public void signJwt(Jwt jwt) throws NoSuchAlgorithmException {
		String signerId = configBean.getDefaultJwtSigner();
		
		JwtSigner signer = getSigners().get(signerId);
		
		// set the signing algorithm in the JWT
		jwt.getHeader().setAlgorithm(signer.getAlgorithm().getJwaName());
		
		signer.sign(jwt);
	
	}
	
	/**
	 * Return the JwtSigners associated with this service
	 * 
	 * @return
	 */
	public Map<String, ? extends JwtSigner> getSigners() {
		return signers;
	}

}
