/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.IssuerService;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Strings;

/**
 * @author jricher
 *
 */
public class StaticSingleIssuerService implements IssuerService, InitializingBean {

	private String issuer;

	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}

	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * Always returns the configured issuer URL
	 * 
	 * @see org.mitre.openid.connect.client.service.IssuerService#getIssuer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public IssuerServiceResponse getIssuer(HttpServletRequest request) {
		return new IssuerServiceResponse(getIssuer(), null, null);
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		if (Strings.isNullOrEmpty(issuer)) {
			throw new IllegalArgumentException("Issuer must not be null or empty.");
		}

	}

}
