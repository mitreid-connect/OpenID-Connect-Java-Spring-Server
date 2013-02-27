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

	private String jwkPublishUrl;
	private String x509PublishUrl;
	
	/* (non-Javadoc)
     * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#isHandler(java.lang.Class)
     */
    @Override
    protected boolean isHandler(Class<?> beanType) {
	    return beanType.equals(ClientKeyPublisher.class);
    }

	/**
	 * Map the "jwkKeyPublish" method to our jwkPublishUrl.
	 * Map the "x509KeyPublish" method to our x509PublishUrl.
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    	
    	if (method.getName().equals("publishClientJwk") && getJwkPublishUrl() != null) {
    		return new RequestMappingInfo(
    				new PatternsRequestCondition(new String[] {getJwkPublishUrl()}, getUrlPathHelper(), getPathMatcher(), false, false),
    				null,
    				null,
    				null,
    				null,
    				null, 
    				null);
    	} else if (method.getName().equals("publishClientx509") && getX509PublishUrl() != null) {
    		return new RequestMappingInfo(
    				new PatternsRequestCondition(new String[] {getX509PublishUrl()}, getUrlPathHelper(), getPathMatcher(), false, false),
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
     * @return the jwkPublishUrl
     */
    public String getJwkPublishUrl() {
	    return jwkPublishUrl;
    }

	/**
     * @param jwkPublishUrl the jwkPublishUrl to set
     */
    public void setJwkPublishUrl(String jwkPublishUrl) {
	    this.jwkPublishUrl = jwkPublishUrl;
    }

	/**
     * @return the x509PublishUrl
     */
    public String getX509PublishUrl() {
	    return x509PublishUrl;
    }

	/**
     * @param x509PublishUrl the x509PublishUrl to set
     */
    public void setX509PublishUrl(String x509PublishUrl) {
	    this.x509PublishUrl = x509PublishUrl;
    }

}
