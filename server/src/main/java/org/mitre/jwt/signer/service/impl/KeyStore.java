package org.mitre.jwt.signer.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * Creates and manages a JCE KeyStore
 * 
 * @author nemonik
 * 
 */
@SuppressWarnings("deprecation")
public class KeyStore implements InitializingBean {

	private static Log logger = LogFactory.getLog(KeyStore.class);

	public static final String TYPE = "BKS";
	public static final String PASSWORD = "changeit";

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Creates a certificate.
	 * 
	 * @param commonName
	 * @param daysNotValidBefore
	 * @param daysNotValidAfter
	 * @return
	 */
	private static X509V3CertificateGenerator createCertificate(
			String commonName, int daysNotValidBefore, int daysNotValidAfter) {
		// BC docs say to use another, but it seemingly isn't included...
		X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();	

		v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		v3CertGen.setIssuerDN(new X509Principal("CN=" + commonName
				+ ", OU=None, O=None L=None, C=None"));
		v3CertGen.setNotBefore(new Date(System.currentTimeMillis()
				- (1000L * 60 * 60 * 24 * daysNotValidBefore)));
		v3CertGen.setNotAfter(new Date(System.currentTimeMillis()
				+ (1000L * 60 * 60 * 24 * daysNotValidAfter)));
		v3CertGen.setSubjectDN(new X509Principal("CN=" + commonName
				+ ", OU=None, O=None L=None, C=None"));
		return v3CertGen;
	}

	/**
	 * Create an RSA KeyPair and insert into specified KeyStore
	 * 
	 * @param location
	 * @param domainName
	 * @param alias
	 * @param keystorePassword
	 * @param aliasPassword
	 * @param daysNotValidBefore
	 * @param daysNotValidAfter
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static java.security.KeyStore generateRsaKeyPair(String location,
			String domainName, String alias, String keystorePassword,
			String aliasPassword, int daysNotValidBefore, int daysNotValidAfter)
			throws GeneralSecurityException, IOException {

		java.security.KeyStore ks = loadJceKeyStore(location, keystorePassword);

		KeyPairGenerator rsaKeyPairGenerator = KeyPairGenerator
				.getInstance("RSA", "BC");
		rsaKeyPairGenerator.initialize(2048);
		KeyPair rsaKeyPair = rsaKeyPairGenerator.generateKeyPair();

		X509V3CertificateGenerator v3CertGen = createCertificate(domainName,
				daysNotValidBefore, daysNotValidAfter);

		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();

		v3CertGen.setPublicKey(rsaKeyPair.getPublic());
		v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption"); // "MD5WithRSAEncryption");

		// BC docs say to use another, but it seemingly isn't included...		
		X509Certificate certificate = v3CertGen
				.generateX509Certificate(rsaPrivateKey);

		// if exist, overwrite
		ks.setKeyEntry(alias, rsaPrivateKey, aliasPassword.toCharArray(),
				new java.security.cert.Certificate[] { certificate });

		storeJceKeyStore(location, keystorePassword, ks);

		return ks;

	}
	
	/**
	 * Creates or loads a JCE KeyStore
	 * @param location
	 * @param keystorePassword
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private static java.security.KeyStore loadJceKeyStore(String location, String keystorePassword) throws GeneralSecurityException, IOException {
		java.security.KeyStore ks = java.security.KeyStore.getInstance(TYPE);

		File keystoreFile = new File(location);
		if (!keystoreFile.exists()) {
			ks.load(null, null);
		} else {
			InputStream ios = new FileInputStream(keystoreFile);
			try {
				ks.load(ios, keystorePassword.toCharArray());
				logger.info("Loaded keystore from " + location);
			} finally {
				ios.close();
			}
		}
	
		return ks;
	}

	public static void main(String[] args) {
		
		//TODO create a cmd-line to create the KeyStore?
		
		try {
			KeyStore.generateRsaKeyPair("/tmp/keystore.jks",
					"OpenID Connect Server", "test", KeyStore.PASSWORD,
					KeyStore.PASSWORD, 30, 365);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Store the JCE KeyStore
	 * 
	 * @param location
	 * @param keystorePassword
	 * @param ks
	 * @throws FileNotFoundException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	private static void storeJceKeyStore(String location,
			String keystorePassword, java.security.KeyStore ks)
			throws FileNotFoundException, KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {
		File keystoreFile = new File(location);
		FileOutputStream fos = new FileOutputStream(keystoreFile);
		try {
			ks.store(fos, keystorePassword.toCharArray());
		} finally {
			fos.close();
		}

		logger.info("Keystore created here:  " + keystoreFile.getAbsolutePath());
	}
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

			return new KeyPair(publicKey, (RSAPrivateKey) key);
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
