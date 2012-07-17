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
package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.signer.AbstractJwtSigner;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	public void afterPropertiesSet(){
		initializeMac();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.AbstractJwtSigner#generateSignature(java.lang.String
	 * )
	 */
	@Override
	public String generateSignature(String signatureBase) throws NoSuchAlgorithmException {
		
		afterPropertiesSet();
		
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
	
	public void initializeMac() {
		// TODO: check if it's already been done
		try {
			mac = Mac.getInstance(JwsAlgorithm.getByName(super.getAlgorithm()).getStandardName());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// TODO: nuke and clean up
	public void initializeMacJwe(String signatureBase) {
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(signatureBase));
		String header = parts.get(0);
		JsonParser parser = new JsonParser();
		JsonObject object = (JsonObject) parser.parse(header);
		
		try {
			mac = Mac.getInstance(JwsAlgorithm.getByName(object.get("int").getAsString())
					.getStandardName());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
