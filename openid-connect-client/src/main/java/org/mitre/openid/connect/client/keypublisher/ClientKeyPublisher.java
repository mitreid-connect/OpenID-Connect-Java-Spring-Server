/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.openid.connect.client.keypublisher;

import java.util.Map;
import java.util.UUID;

import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.openid.connect.view.JWKSetView;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;
import com.nimbusds.jose.jwk.JWK;

/**
 * @author jricher
 *
 */
public class ClientKeyPublisher implements BeanDefinitionRegistryPostProcessor {

	private JWTSigningAndValidationService signingAndValidationService;

	private String jwkPublishUrl;

	private BeanDefinitionRegistry registry;

	private String jwkViewName = JWKSetView.VIEWNAME;

	/**
	 * If the jwkPublishUrl field is set on this bean, set up a listener on that URL to publish keys.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (!Strings.isNullOrEmpty(getJwkPublishUrl())) {

			// add a mapping to this class
			BeanDefinitionBuilder clientKeyMapping = BeanDefinitionBuilder.rootBeanDefinition(ClientKeyPublisherMapping.class);
			// custom view resolver
			BeanDefinitionBuilder viewResolver = BeanDefinitionBuilder.rootBeanDefinition(JwkViewResolver.class);

			if (!Strings.isNullOrEmpty(getJwkPublishUrl())) {
				clientKeyMapping.addPropertyValue("jwkPublishUrl", getJwkPublishUrl());

				// randomize view name to make sure it doesn't conflict with local views
				jwkViewName = JWKSetView.VIEWNAME + "-" + UUID.randomUUID().toString();
				viewResolver.addPropertyValue("jwkViewName", jwkViewName);

				// view bean
				BeanDefinitionBuilder jwkView = BeanDefinitionBuilder.rootBeanDefinition(JWKSetView.class);
				registry.registerBeanDefinition(JWKSetView.VIEWNAME, jwkView.getBeanDefinition());
				viewResolver.addPropertyReference("jwk", JWKSetView.VIEWNAME);
			}

			registry.registerBeanDefinition("clientKeyMapping", clientKeyMapping.getBeanDefinition());
			registry.registerBeanDefinition("jwkViewResolver", viewResolver.getBeanDefinition());

		}

	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.registry = registry;
	}

	/**
	 * Return a view to publish all keys in JWK format. Only used if jwkPublishUrl is set.
	 * @return
	 */
	public ModelAndView publishClientJwk() {

		// map from key id to key
		Map<String, JWK> keys = signingAndValidationService.getAllPublicKeys();

		return new ModelAndView(jwkViewName, "keys", keys);
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
	 * @return the signingAndValidationService
	 */
	public JWTSigningAndValidationService getSigningAndValidationService() {
		return signingAndValidationService;
	}

	/**
	 * @param signingAndValidationService the signingAndValidationService to set
	 */
	public void setSigningAndValidationService(JWTSigningAndValidationService signingAndValidationService) {
		this.signingAndValidationService = signingAndValidationService;
	}


}
