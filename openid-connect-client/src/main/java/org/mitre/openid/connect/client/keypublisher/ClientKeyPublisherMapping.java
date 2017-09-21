/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
