package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.signer.AbstractJwtSigner;

/**
 * JWT Signer using either the HMAC SHA-256, SHA-384, SHA-512 hash algorithm
 * 
 * @author AANGANES, nemonik
 * 
 */
public class HmacSigner extends AbstractJwtSigner {

	/**
	 * an enum for mapping a JWS name to standard algorithm name
	 * 
	 * @author nemonik
	 * 
	 */
	public enum Algorithm {

		// Algorithm constants
		HS256("HMACSHA256"), HS384("HMACSHA384"), HS512("HMACSHA512");

		public static final String DEFAULT = Algorithm.HS256.toString();

		/**
		 * Returns the Algorithm for the name
		 * 
		 * @param name
		 * @return
		 */
		public static Algorithm getByName(String name) {
			for (Algorithm correspondingType : Algorithm.values()) {
				if (correspondingType.toString().equals(name)) {
					return correspondingType;
				}
			}

			// corresponding type not found
			throw new IllegalArgumentException(
					"Algorithm name does not have a corresponding Algorithm");
		}

		private final String standardName;

		/**
		 * Constructor of Algorithm
		 * 
		 * @param standardName
		 */
		Algorithm(String standardName) {
			this.standardName = standardName;
		}

		/**
		 * Return the Java standard algorithm name
		 * 
		 * @return
		 */
		public String getStandardName() {
			return standardName;
		}
	};

	private static Log logger = LogFactory.getLog(HmacSigner.class);	
	
	private Mac mac;

	private String passphrase;

	/**
	 * Create a signer with no passphrase
	 */
	public HmacSigner() {
		super(Algorithm.DEFAULT);
	}

	/**
	 * Create HMAC singer with default algorithm and passphrase as raw bytes
	 * 
	 * @param passphraseAsRawBytes
	 */
	public HmacSigner(byte[] passphraseAsRawBytes) {
		this(Algorithm.DEFAULT, new String(passphraseAsRawBytes,
				Charset.forName("UTF-8")));
	}
	
	/**
	 * Create HMAC singer with default algorithm and passphrase
	 * 
	 * @param passwordAsRawBytes
	 */
	public HmacSigner(String passphrase) {
		this(Algorithm.DEFAULT, passphrase);
	}	

	/**
	 * Create HMAC singer with given algorithm and password as raw bytes
	 * 
	 * @param algorithmName
	 *            the JWS name for the standard name of the requested MAC
	 *            algorithm
	 * @param passphraseAsRawBytes
	 */
	public HmacSigner(String algorithmName, byte[] passphraseAsRawBytes) {
		this(algorithmName, new String(passphraseAsRawBytes,
				Charset.forName("UTF-8")));
	}

	/**
	 * Create HMAC singer with given algorithm and passwords
	 * 
	 * @param algorithmName
	 *            the JWS name for the standard name of the requested MAC
	 *            algorithm
	 * @param passphrase
	 *            the passphrase
	 */
	public HmacSigner(String algorithmName, String passphrase) {
		super(algorithmName);

		setPassphrase(passphrase);

		try {
			mac = Mac.getInstance(Algorithm.getByName(algorithmName)
					.getStandardName());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.AbstractJwtSigner#generateSignature(java.lang.String
	 * )
	 */
	@Override
	protected String generateSignature(String signatureBase) {
		if (passphrase == null) {
			return null; // TODO: probably throw some kind of exception
		}

		try {
			mac.init(new SecretKeySpec(getPassphrase().getBytes(), mac
					.getAlgorithm()));
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			mac.update(signatureBase.getBytes("UTF-8"));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		if (passphrase.isEmpty())
			throw new IllegalArgumentException("passphrase must be set");

		this.passphrase = passphrase;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HmacSigner [mac=" + mac + ", passphrase=" + passphrase + "]";
	}
}
