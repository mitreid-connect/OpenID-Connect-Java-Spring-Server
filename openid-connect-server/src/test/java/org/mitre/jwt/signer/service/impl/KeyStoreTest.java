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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class KeyStoreTest {

	@Autowired
	@Qualifier("testKeystore")
	KeyStore keystore;

	static final String PROVIDER = "BC";
	
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
	public static X509V3CertificateGenerator createCertificate(
			String commonName, int daysNotValidBefore, int daysNotValidAfter) {
		// BC sez X509V3CertificateGenerator is deprecated and the docs say to
		// use another, but it seemingly isn't included jar...
		X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();

		v3CertGen
				.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
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
	public static java.security.KeyStore generateKeyPair(KeyStore keystore,
			String keyPairAlgorithm, int keySize, String signatureAlgorithm,
			String domainName, String alias, String aliasPassword,
			int daysNotValidBefore, int daysNotValidAfter)
			throws GeneralSecurityException, IOException {

		java.security.KeyStore ks;
		
		if (keystore != null ) {
			ks = keystore.getKeystore();
		} else {
			ks = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
			ks.load(null, null);
		}

		KeyPairGenerator rsaKeyPairGenerator = null;

		rsaKeyPairGenerator = KeyPairGenerator.getInstance(keyPairAlgorithm);

		rsaKeyPairGenerator.initialize(keySize);
		KeyPair rsaKeyPair = rsaKeyPairGenerator.generateKeyPair();

		// BC sez X509V3CertificateGenerator is deprecated and the docs say to
		// use another, but it seemingly isn't included jar...
		X509V3CertificateGenerator v3CertGen = createCertificate(domainName,
				daysNotValidBefore, daysNotValidAfter);

		PrivateKey privateKey = rsaKeyPair.getPrivate();

		v3CertGen.setPublicKey(rsaKeyPair.getPublic());
		v3CertGen.setSignatureAlgorithm(signatureAlgorithm);

		// BC docs say to use another, but it seemingly isn't included...
		X509Certificate certificate = v3CertGen
				.generateX509Certificate(privateKey);

		// if exist, overwrite
		ks.setKeyEntry(alias, privateKey, aliasPassword.toCharArray(),
				new java.security.cert.Certificate[] { certificate });

		if (keystore != null) {
			keystore.setKeystore(ks);
		}

		return ks;
	}

	@Test
	public void storeRsaKeyPair() throws GeneralSecurityException, IOException {

		java.security.KeyStore ks = null;

		try {
			ks = KeyStoreTest.generateKeyPair(keystore,
					RsaSigner.KEYPAIR_ALGORITHM, 2048,
					"SHA256WithRSAEncryption", "OpenID Connect Server",
					"rsa", RsaSigner.DEFAULT_PASSWORD, 30, 365);

		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertThat(ks, not(nullValue()));
	}

	@Test
	public void readKey() throws GeneralSecurityException {

		Key key = keystore.getKeystore().getKey("rsa",
				KeyStore.PASSWORD.toCharArray());

		assertThat(key, not(nullValue()));
	}

	/**
	 * Saves the keystore for future use.
	 * 
	 * @param keystore
	 * @param path
	 * @param password
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public static void persistKeystoreToFile(final java.security.KeyStore keystore,
			final String path, final String password) throws GeneralSecurityException,
			IOException {

		FileOutputStream fos = new FileOutputStream(new File(path));
		try {
			keystore.store(fos, password.toCharArray());
			System.out.println("Wrote keystore to " + path);
		} finally {
			fos.close();
		}
	}

}
