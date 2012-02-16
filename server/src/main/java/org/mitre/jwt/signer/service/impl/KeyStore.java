package org.mitre.jwt.signer.service.impl;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * Creates and manages a JCE KeyStore
 * 
 * @author nemonik
 * 
 */
public class KeyStore implements InitializingBean {

	private static Log logger = LogFactory.getLog(KeyStore.class);

	public static final String TYPE = java.security.KeyStore.getDefaultType();
	public static final String PASSWORD = "changeit";

	private String password;

	private Resource location;

	private java.security.KeyStore keystore;

	/**
	 * default constructor
	 */
	public KeyStore() {
		this(PASSWORD, null);
	}

	/**
	 * KeyStore constructor
	 * 
	 * @param password
	 *            the password used to unlock the keystore
	 * @param location
	 *            the location of the keystore
	 */
	public KeyStore(String password, Resource location) {
		setPassword(password);
		setLocation(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		InputStream inputStream = null;

		try {
			keystore = java.security.KeyStore.getInstance(TYPE);
			inputStream = location.getInputStream();
			keystore.load(inputStream, this.password.toCharArray());

			logger.info("Loaded keystore from " + location);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		if (keystore.size() == 0) {
			throw new Exception("Keystore is empty; it has no entries");
		}
	}

	/**
	 * Returns a KeyPair for the alias given the password
	 * 
	 * @param alias
	 *            the alias name
	 * @param password
	 *            the password for recovering the key pair
	 * @return the key pair
	 * @throws GeneralSecurityException
	 */
	public KeyPair getKeyPairForAlias(String alias, String password)
			throws GeneralSecurityException {

		Key key = keystore.getKey(alias, password.toCharArray());

		if (key instanceof PrivateKey) {

			// Get certificate of public key
			java.security.cert.Certificate cert = keystore
					.getCertificate(alias);

			// Get public key
			PublicKey publicKey = cert.getPublicKey();

			return new KeyPair(publicKey, (PrivateKey) key);
		}

		return null;
	}

	public java.security.KeyStore getKeystore() {
		return keystore;
	}

	public Resource getLocation() {
		return location;
	}

	public String getPassword() {
		return password;
	}

	public Provider getProvider() {
		return keystore.getProvider();
	}

	public void setKeystore(java.security.KeyStore keystore) {
		this.keystore = keystore;
	}

	public void setLocation(Resource location) {
		if (location != null && location.exists()) {
			this.location = location;
		} else {
			throw new IllegalArgumentException("location must exist");
		}
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KeyStore [password=" + password + ", location=" + location
				+ ", keystore=" + keystore + "]";
	}

}
