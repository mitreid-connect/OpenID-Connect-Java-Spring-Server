/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.IssuerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.base.Strings;

/**
 * 
 * Determines the issuer using an account chooser or other third-party-initiated login
 * 
 * @author jricher
 *
 */
public class ThirdPartyIssuerService implements IssuerService, InitializingBean {

	private String accountChooserUrl;
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.IssuerService#getIssuer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public IssuerServiceResponse getIssuer(HttpServletRequest request) {
		
		// if the issuer is passed in, return that
		if (!Strings.isNullOrEmpty(request.getParameter("iss"))) {
			return new IssuerServiceResponse(request.getParameter("iss"), request.getParameter("login_hint"), request.getParameter("target_link_uri"));
		} else {
			
			try {
				// otherwise, need to forward to the account chooser
				String redirectUri = request.getRequestURL().toString();
	            URIBuilder builder = new URIBuilder(accountChooserUrl);
	            
	            builder.addParameter("redirect_uri", redirectUri);
	            
	            return new IssuerServiceResponse(builder.build().toString());
	            
            } catch (URISyntaxException e) {
            	throw new AuthenticationServiceException("Account Chooser URL is not valid", e);
            }
			
			
		}
		
	}

	/**
	 * @return the accountChooserUrl
	 */
	public String getAccountChooserUrl() {
		return accountChooserUrl;
	}

	/**
	 * @param accountChooserUrl the accountChooserUrl to set
	 */
	public void setAccountChooserUrl(String accountChooserUrl) {
		this.accountChooserUrl = accountChooserUrl;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
    @Override
    public void afterPropertiesSet() throws Exception {
    	if (Strings.isNullOrEmpty(this.accountChooserUrl)) {
    		throw new IllegalArgumentException("Account Chooser URL cannot be null or empty");
    	}
	    
    }

}
