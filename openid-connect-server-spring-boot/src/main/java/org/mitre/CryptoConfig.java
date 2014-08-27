package org.mitre;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.mitre.jose.keystore.JWKSetKeyStore;
import org.mitre.jwt.encryption.service.impl.DefaultJwtEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.impl.DefaultJwtSigningAndValidationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;

@Configuration
public class CryptoConfig {
	@Bean(name = "defaultKeyStore")
	public JWKSetKeyStore defaultKeyStore(@Value("classpath:keystore.jwks") Resource location) {
		JWKSetKeyStore keystore = new JWKSetKeyStore();
		keystore.setLocation(location);
		return keystore;
	}
	
	@Bean(name = "defaultsignerService")
	public DefaultJwtSigningAndValidationService defaultJwtSigningAndValidationService(
			@Qualifier("defaultKeyStore") JWKSetKeyStore keystore,
			@Value("rsa1") String defaultSignerId,
			@Value("RS256") String defaultSigningAlgorithmName) throws NoSuchAlgorithmException, InvalidKeySpecException {
		DefaultJwtSigningAndValidationService s = new DefaultJwtSigningAndValidationService(keystore);
		s.setDefaultSignerKeyId(defaultSignerId);
		s.setDefaultSigningAlgorithmName(defaultSigningAlgorithmName);
		return s;
	}
	
	@Bean(name = "defaultEncryptionService")
	public DefaultJwtEncryptionAndDecryptionService defaultJwtEncryptionAndDecryptionService(
			@Qualifier("defaultKeyStore") JWKSetKeyStore keystore, 
			@Value("RSA1_5") JWEAlgorithm defaultAlgorithm,
			@Value("rsa1") String defaultDecryptionKeyId,
			@Value("rsa1") String defaultEncryptionKeyId
			) throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
		DefaultJwtEncryptionAndDecryptionService s = new DefaultJwtEncryptionAndDecryptionService(keystore);
		s.setDefaultAlgorithm(defaultAlgorithm);
		s.setDefaultDecryptionKeyId(defaultDecryptionKeyId);
		s.setDefaultEncryptionKeyId(defaultEncryptionKeyId);
		return s;
	}
}
