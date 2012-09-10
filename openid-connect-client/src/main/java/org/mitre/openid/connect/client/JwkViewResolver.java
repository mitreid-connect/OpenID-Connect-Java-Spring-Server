/**
 * 
 */
package org.mitre.openid.connect.client;

import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * 
 * Simple view resolver to map JWK view names to appropriate beans
 * 
 * @author jricher
 *
 */
public class JwkViewResolver implements ViewResolver, Ordered {

	private String jwkViewName = "jwkKeyList";
	private View jwk;
	
	private String x509ViewName = "x509certs";
	private View x509;
	
	private int order = HIGHEST_PRECEDENCE; // highest precedence, most specific -- avoids hitting the catch-all view resolvers
	
	/**
	 * Map "jwkKeyList" to the jwk property and "x509certs" to the x509 property on this bean.
	 * Everything else returns null
	 */
	@Override
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		if (viewName != null) {
			if (viewName.equals(getJwkViewName())) {
				return getJwk();
			} else if (viewName.equals(getX509ViewName())) {
				return getX509();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
     * @return the x509
     */
    public View getX509() {
    	return x509;
    }

	/**
     * @param x509 the x509 to set
     */
    public void setX509(View x509) {
    	this.x509 = x509;
    }

	/**
     * @return the jwk
     */
    public View getJwk() {
    	return jwk;
    }

	/**
     * @param jwk the jwk to set
     */
    public void setJwk(View jwk) {
    	this.jwk = jwk;
    }

	/**
     * @return the order
     */
    @Override
    public int getOrder() {
	    return order;
    }

	/**
     * @param order the order to set
     */
    public void setOrder(int order) {
	    this.order = order;
    }

	/**
     * @return the jwkViewName
     */
    public String getJwkViewName() {
	    return jwkViewName;
    }

	/**
     * @param jwkViewName the jwkViewName to set
     */
    public void setJwkViewName(String jwkViewName) {
	    this.jwkViewName = jwkViewName;
    }

	/**
     * @return the x509ViewName
     */
    public String getX509ViewName() {
	    return x509ViewName;
    }

	/**
     * @param x509ViewName the x509ViewName to set
     */
    public void setX509ViewName(String x509ViewName) {
	    this.x509ViewName = x509ViewName;
    }

}
