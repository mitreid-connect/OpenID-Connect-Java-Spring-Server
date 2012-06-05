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

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class JwtSigningAndValidationServiceDefault implements
		JwtSigningAndValidationService, InitializingBean {

	@Autowired
	private ConfigurationPropertiesBean configBean;
	
	// map of identifier to signer
	private Map<String, ? extends JwtSigner> signers = new HashMap<String, JwtSigner>();

	private static Log logger = LogFactory
			.getLog(JwtSigningAndValidationServiceDefault.class);

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
	public void afterPropertiesSet() throws Exception {
		// used for debugging...
		if (!signers.isEmpty()) {
			logger.info(this.toString());
		}

		logger.info("JwtSigningAndValidationServiceDefault is open for business");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.service.JwtSigningAndValidationService#getAllPublicKeys
	 * ()
	 */
	@Override
	public Map<String, PublicKey> getAllPublicKeys() {

		Map<String, PublicKey> map = new HashMap<String, PublicKey>();

		for (String signerId : signers.keySet()) {

			JwtSigner signer = signers.get(signerId);
			
			if (signer instanceof RsaSigner) {

				RsaSigner rsa = (RsaSigner)signer;
				
				PublicKey publicKey = rsa.getPublicKey();

				if (publicKey != null) {
					map.put(signerId, publicKey);
				}

			}
		}

		return map;
	}

	/**
	 * Return the JwtSigners associated with this service
	 * 
	 * @return
	 */
	public Map<String, ? extends JwtSigner> getSigners() {
		return signers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.service.JwtSigningAndValidationService#isJwtExpired
	 * (org.mitre.jwt.model.Jwt)
	 */
	@Override
	public boolean isJwtExpired(Jwt jwt) {

		Date expiration = jwt.getClaims().getExpiration();

		if (expiration != null)
			return new Date().after(expiration);
		else
			return false;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.service.JwtSigningAndValidationService#validateIssuedJwt
	 * (org.mitre.jwt.model.Jwt)
	 */
	@Override
	public boolean validateIssuedJwt(Jwt jwt, String expectedIssuer) {

		String iss = jwt.getClaims().getIssuer();
		
		if (iss.equals(expectedIssuer))
			return true;
		
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.service.JwtSigningAndValidationService#validateSignature
	 * (java.lang.String)
	 */
	@Override
	public boolean validateSignature(String jwtString) {

		for (JwtSigner signer : signers.values()) {
			if (signer.verify(jwtString))
				return true;
		}

		return false;
	}

	/**
	 * Sign a jwt in place using the configured default signer.
	 */
	@Override
	public void signJwt(Jwt jwt) {
		String signerId = configBean.getDefaultJwtSigner();
		
		JwtSigner signer = signers.get(signerId);
		
		signer.sign(jwt);

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
}
