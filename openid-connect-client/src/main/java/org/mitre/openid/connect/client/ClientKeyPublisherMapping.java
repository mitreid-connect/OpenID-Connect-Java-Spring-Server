/**
 * 
 */
package org.mitre.openid.connect.client;

import java.lang.reflect.Method;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

/**
 * @author jricher
 *
 */
@Component
public class ClientKeyPublisherMapping extends RequestMappingInfoHandlerMapping {

	private String url;
	
	/* (non-Javadoc)
     * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#isHandler(java.lang.Class)
     */
    @Override
    protected boolean isHandler(Class<?> beanType) {
	    return beanType.equals(OIDCSignedRequestFilter.class);
    }

	/**
	 * Map the "jwkKeyPublish" method to our given URL
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    	
    	if (method.getName().equals("publishClientJwk")) {
    		return new RequestMappingInfo(
    				new PatternsRequestCondition(new String[] {url}, getUrlPathHelper(), getPathMatcher(), false, false),
    				null,
    				null,
    				null,
    				null,
    				null, 
    				null);
    	} else {
    		return null;
    	}
    	
    }

	/**
     * @return the url
     */
    public String getUrl() {
    	return url;
    }

	/**
     * @param url the url to set
     */
    public void setUrl(String url) {
    	this.url = url;
    }

	
}
