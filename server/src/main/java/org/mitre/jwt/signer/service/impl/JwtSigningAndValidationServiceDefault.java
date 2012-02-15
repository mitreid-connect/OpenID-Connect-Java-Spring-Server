package org.mitre.jwt.signer.service.impl;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.EcdsaSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.springframework.beans.factory.InitializingBean;

public class JwtSigningAndValidationServiceDefault implements
		JwtSigningAndValidationService, InitializingBean {

	private List<? extends JwtSigner> signers = new ArrayList<JwtSigner>();

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
	 * @param signer List of JwtSigners to associate with this service
	 */
	public JwtSigningAndValidationServiceDefault(List<? extends JwtSigner> signer) {	
		setSigners(signer);
	}
	
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// used for debugging...
		if (!signers.isEmpty()) {
			logger.info(this.toString());	
		}
		
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> JwtSigningAndValidationServiceDefault is open for business");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.service.JwtSigningAndValidationService#getAllPublicKeys
	 * ()
	 */
	@Override
	public List<PublicKey> getAllPublicKeys() {
		// TODO Iterate through the signers, gather up, and return all the PublicKeys
		
		List<PublicKey> publicKeys = new ArrayList<PublicKey>(); 
		PublicKey publicKey;		
		
		for (JwtSigner signer: signers) {
			
			if (signer instanceof RsaSigner) {
			
				publicKey = ((RsaSigner) signer).getPublicKey();
				
				if (publicKey != null)
					publicKeys.add(((RsaSigner) signer).getPublicKey());
			
			} else if (signer instanceof EcdsaSigner) {
				
				publicKey = ((EcdsaSigner) signer).getPublicKey();
				
				if (publicKey != null)
					publicKeys.add(publicKey);
			}
		}
		
		return publicKeys;
	}

	/**
	 * Return the JwtSigners associated with this service
	 * 
	 * @return
	 */
	public List<? extends JwtSigner> getSigners() {
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
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Set the JwtSigners associated with this service
	 * 
	 * @param signers
	 *            List of JwtSigners to associate with this service
	 */
	public void setSigners(List<? extends JwtSigner> signers) {
		this.signers = signers;
	}

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
	public boolean validateIssuedJwt(Jwt jwt) {
		
		// TODO Verify this is correct...

		for (JwtSigner signer: signers) {
			if (signer.verify(jwt.toString()))
				return true;
		}
		
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

		for (JwtSigner signer: signers) {
			if (signer.verify(jwtString))
				return true;
		}
		
		return false;
	}
}
