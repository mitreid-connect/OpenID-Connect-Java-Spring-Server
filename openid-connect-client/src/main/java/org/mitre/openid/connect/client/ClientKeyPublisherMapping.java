/**
 * 
 */
package org.mitre.openid.connect.client;

import javax.servlet.http.HttpServletRequest;

import org.mitre.openid.connect.web.JsonWebKeyEndpoint;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

/**
 * @author jricher
 *
 */
@Component
public class ClientKeyPublisherMapping extends AbstractUrlHandlerMapping implements InitializingBean {

	private JsonWebKeyEndpoint controller;
	
	private String url;
	
	public void setKeyUrl(String url, JsonWebKeyEndpoint controller) {
		this.url = url;
		this.controller = controller;
	}

	/* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {

    	if (url != null && controller != null) {
    		super.registerHandler(url, controller);
    	}
	    
    }
	
}
