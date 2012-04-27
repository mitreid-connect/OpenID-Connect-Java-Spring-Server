package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.signer.AbstractJwtSigner;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * JWT Signer using either the HMAC SHA-256, SHA-384, SHA-512 hash algorithm
 * 
 * @author AANGANES, nemonik
 * 
 */
public class HmacSigner extends AbstractJwtSigner implements InitializingBean {

	public static final String DEFAULT_PASSPHRASE = "changeit";

	public static final String DEFAULT_ALGORITHM = JwsAlgorithm.HS256
			.toString();

	private static Log logger = LogFactory.getLog(HmacSigner.class);

	private Mac mac;

	private String passphrase = DEFAULT_PASSPHRASE;

	/**
	 * Default constructor
	 */
	public HmacSigner() {
		super(DEFAULT_ALGORITHM);
	}

	/**
	 * Create HMAC singer with default algorithm and passphrase as raw bytes
	 * 
	 * @param passphraseAsRawBytes
	 *            The passphrase as raw bytes
	 */
	public HmacSigner(byte[] passphraseAsRawBytes)
			throws NoSuchAlgorithmException {
		this(DEFAULT_ALGORITHM, new String(passphraseAsRawBytes,
				Charset.forName("UTF-8")));
	}

	/**
	 * Create HMAC singer with default algorithm and passphrase
	 * 
	 * @param passwordAsRawBytes
	 *            The passphrase as raw bytes
	 */
	public HmacSigner(String passphrase) throws NoSuchAlgorithmException {
		this(DEFAULT_ALGORITHM, passphrase);
	}

	/**
	 * Create HMAC singer with given algorithm and password as raw bytes
	 * 
	 * @param algorithmName
	 *            The Java standard name of the requested MAC algorithm
	 * @param passphraseAsRawBytes
	 *            The passphrase as raw bytes
	 */
	public HmacSigner(String algorithmName, byte[] passphraseAsRawBytes)
			throws NoSuchAlgorithmException {
		this(algorithmName, new String(passphraseAsRawBytes,
				Charset.forName("UTF-8")));
	}

	/**
	 * Create HMAC singer with given algorithm and passwords
	 * 
	 * @param algorithmName
	 *            The Java standard name of the requested MAC algorithm
	 * @param passphrase
	 *            the passphrase
	 */
	public HmacSigner(String algorithmName, String passphrase) {
		super(algorithmName);

		Assert.notNull(passphrase, "A passphrase must be supplied");

		setPassphrase(passphrase);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		mac = Mac.getInstance(JwsAlgorithm.getByName(super.getAlgorithm())
				.getStandardName());

		logger.debug(JwsAlgorithm.getByName(getAlgorithm()).getStandardName()
				+ " ECDSA Signer ready for business");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.AbstractJwtSigner#generateSignature(java.lang.String
	 * )
	 */
	@Override
	public String generateSignature(String signatureBase) {
		if (passphrase == null) {
			throw new IllegalArgumentException("Passphrase cannot be null");
		}

		try {
			mac.init(new SecretKeySpec(getPassphrase().getBytes(), mac
					.getAlgorithm()));

			mac.update(signatureBase.getBytes("UTF-8"));
		} catch (GeneralSecurityException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}

		byte[] sigBytes = mac.doFinal();

		String sig = new String(Base64.encodeBase64URLSafe(sigBytes));

		// strip off any padding
		sig = sig.replace("=", "");

		return sig;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(byte[] rawbytes) {
		this.setPassphrase(new String(rawbytes, Charset.forName("UTF-8")));
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HmacSigner [mac=" + mac + ", passphrase=" + passphrase + "]";
	}
}