package org.mitre.jwt.signer.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Creates and manages a JCE KeyStore
 * 
 * @author nemonik
 * 
 */
public class KeyStore implements InitializingBean {

	// TODO: Doesn't have a provider attribute, getter/setter. Not sure we need.

	private static Log logger = LogFactory.getLog(KeyStore.class);

	private static final String TYPE = java.security.KeyStore.getDefaultType();
	private static final String PASSWORD = "changeit";

	private String password;
	private Resource location;
	private String type;

	private java.security.KeyStore keystore;

	/**
	 * default constructor
	 */
	public KeyStore() {
		this(PASSWORD, null, TYPE);
	}

	/**
	 * @param password
	 * @param location
	 */
	public KeyStore(String password, Resource location) {
		this(password, location, TYPE);
	}

	/**
	 * KeyStore constructor
	 * 
	 * @param password
	 *            the password used to unlock the keystore
	 * @param location
	 *            the location of the keystore
	 * @param type
	 *            the type of keystore. See Appendix A in the Java Cryptography
	 *            Architecture API Specification & Reference for information
	 *            about standard keystore types.
	 */
	public KeyStore(String password, Resource location, String type) {
		setPassword(password);
		setLocation(location);
		setType(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws IOException,
			GeneralSecurityException {

		InputStream inputStream = null;

		try {
			keystore = java.security.KeyStore.getInstance(type);
			inputStream = location.getInputStream();
			keystore.load(inputStream, this.password.toCharArray());

			logger.info("Loaded keystore from " + location);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
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

	public Resource getLocation() {
		return location;
	}

	public String getPassword() {
		return password;
	}

	public String getType() {
		return type;
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

	public void setType(String type) {
		if (StringUtils.hasLength(type)) {
			this.type = type;
		} else {
			throw new IllegalArgumentException("type must not be empty");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KeyStore [password=" + password + ", location=" + location
				+ ", type=" + type + ", keystore=" + keystore + "]";
	}

}
