/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
package org.mitre.jwt.encryption.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.EncryptedJWT;

/**
 * @author wkim
 *
 */
public class DefaultJwtEncryptionAndDecryptionService implements JwtEncryptionAndDecryptionService {

	private static Logger logger = LoggerFactory.getLogger(DefaultJwtEncryptionAndDecryptionService.class);
	
	
	// map of identifier to encrypter
	private Map<String, JWEEncrypter> encrypters = new HashMap<String, JWEEncrypter>();

	// map of identifier to decrypter
	private Map<String, JWEDecrypter> decrypters = new HashMap<String, JWEDecrypter>();


	private String defaultEncryptionKeyId;

	private JWSAlgorithm defaultAlgorithm;

	// map of identifier to key
	private Map<String, JWK> keys = new HashMap<String, JWK>();

	/* (non-Javadoc)
	 * @see org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService#encryptJwt(com.nimbusds.jwt.EncryptedJWT)
	 */
	@Override
	public void encryptJwt(EncryptedJWT jwt) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService#decryptJwt(com.nimbusds.jwt.EncryptedJWT)
	 */
	@Override
	public void decryptJwt(EncryptedJWT jwt) {
		// TODO Auto-generated method stub

	}

}
