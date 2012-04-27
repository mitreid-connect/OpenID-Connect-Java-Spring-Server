package org.mitre.openid.connect.config;


/**
 * Bean to hold configuration information that must be injected into various parts
 * of our application. Set all of the properties here, and autowire a reference
 * to this bean if you need access to any configuration properties. 
 * 
 * @author AANGANES
 *
 */
public class ConfigurationPropertiesBean {

	private String issuer;
	
	private String defaultJwtSigner;

	public ConfigurationPropertiesBean() {
	}
	
	/**
	 * @return the defaultJwtSigner
	 */
	public String getDefaultJwtSigner() {
		return defaultJwtSigner;
	}
	
	public void setDefaultJwtSigner(String signer) {
		defaultJwtSigner = signer;
	}

	/**
	 * @return the baseUrl
	 */
	public String getIssuer() {
		return issuer;
	}
	
	/**
	 * @param iss the issuer to set
	 */
	public void setIssuer(String iss) {
		issuer = iss;
	}
}
