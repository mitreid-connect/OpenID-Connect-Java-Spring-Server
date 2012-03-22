package org.mitre.jwt.signer.service.impl;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.EcdsaSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class JwtSigningAndValidationServiceDefault implements
		JwtSigningAndValidationService, InitializingBean {

	@Autowired
	private ConfigurationPropertiesBean configBean;
	
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
	 * @param signer
	 *            List of JwtSigners to associate with this service
	 */
	public JwtSigningAndValidationServiceDefault(
			List<? extends JwtSigner> signer) {
		setSigners(signer);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// used for debugging...
		if (!signers.isEmpty()) {
			logger.info(this.toString());
		}

		logger.info("JwtSigningAndValidationServiceDefault is open for business");

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

		Map<String, PublicKey> map = new HashMap<String, PublicKey>();

		PublicKey publicKey;

		for (JwtSigner signer : signers) {

			if (signer instanceof RsaSigner) {

				publicKey = ((RsaSigner) signer).getPublicKey();

				if (publicKey != null)
					map.put(((RSAPublicKey) publicKey).getModulus()
							.toString(16).toUpperCase()
							+ ((RSAPublicKey) publicKey).getPublicExponent()
									.toString(16).toUpperCase(), publicKey);

			} else if (signer instanceof EcdsaSigner) {

				// TODO
			}
		}

		return new ArrayList<PublicKey>(map.values());

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

		Date expiration = jwt.getClaims().getExpiration();

		if (expiration != null)
			return new Date().after(expiration);
		else
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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
	public boolean validateIssuedJwt(Jwt jwt, String expectedIssuer) {

		String iss = jwt.getClaims().getIssuer();
		
		if (iss.equals(expectedIssuer))
			return true;
		
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

		for (JwtSigner signer : signers) {
			if (signer.verify(jwtString))
				return true;
		}

		return false;
	}

	@Override
	public Jwt signJwt(Jwt jwt) {
		String signerId = configBean.getDefaultJwtSigner();
		
		//JwtSigner signer = map.get(signerId);
		
		//signer.sign(jwt);
		
		return null;
	}

	/**
	 * @return the configBean
	 */
	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	/**
	 * @param configBean the configBean to set
	 */
	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}
}
