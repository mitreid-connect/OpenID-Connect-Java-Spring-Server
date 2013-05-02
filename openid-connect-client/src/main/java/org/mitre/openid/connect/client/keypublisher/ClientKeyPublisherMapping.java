/**
 * 
 */
package org.mitre.openid.connect.client.keypublisher;

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

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#isHandler(java.lang.Class)
	 */
	@Override
	protected boolean isHandler(Class<?> beanType) {
		return beanType.equals(ClientKeyPublisher.class);
	}

	/**
	 * Map the "jwkKeyPublish" method to our jwkPublishUrl.
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

}
