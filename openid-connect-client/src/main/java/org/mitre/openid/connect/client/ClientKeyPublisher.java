/**
 * 
 */
package org.mitre.openid.connect.client;

import java.security.PublicKey;
import java.util.Map;
import java.util.UUID;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.view.JwkKeyListView;
import org.mitre.openid.connect.view.X509CertificateView;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;

/**
 * @author jricher
 *
 */
public class ClientKeyPublisher implements BeanDefinitionRegistryPostProcessor {

	private JwtSigningAndValidationService signingAndValidationService;

	private String jwkPublishUrl;

	private String x509PublishUrl;

	private BeanDefinitionRegistry registry;

	private String jwkViewName = "jwkKeyList";

	private String x509ViewName;


	/**
	 * If either the jwkPublishUrl or x509PublishUrl fields are set on this bean, set up a listener on that URL to publish keys.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (!Strings.isNullOrEmpty(getJwkPublishUrl()) || !Strings.isNullOrEmpty(getX509PublishUrl())) {

			// add a mapping to this class
			BeanDefinitionBuilder clientKeyMapping = BeanDefinitionBuilder.rootBeanDefinition(ClientKeyPublisherMapping.class);
			// custom view resolver
			BeanDefinitionBuilder viewResolver = BeanDefinitionBuilder.rootBeanDefinition(JwkViewResolver.class);
			
			if (!Strings.isNullOrEmpty(getJwkPublishUrl())) {
				clientKeyMapping.addPropertyValue("jwkPublishUrl", getJwkPublishUrl());

				// randomize view name to make sure it doesn't conflict with local views
				jwkViewName = "jwkKeyList-" + UUID.randomUUID().toString();
				viewResolver.addPropertyValue("jwkViewName", jwkViewName);

				// view bean
				BeanDefinitionBuilder jwkView = BeanDefinitionBuilder.rootBeanDefinition(JwkKeyListView.class);
				registry.registerBeanDefinition("jwkKeyList", jwkView.getBeanDefinition());
				viewResolver.addPropertyReference("jwk", "jwkKeyList");
			}
			
			if (!Strings.isNullOrEmpty(getX509PublishUrl())) {
				clientKeyMapping.addPropertyValue("x509PublishUrl", getX509PublishUrl());
				
				// randomize view name to make sure it doesn't conflict with local views
				x509ViewName = "x509certs-" + UUID.randomUUID().toString();
				viewResolver.addPropertyValue("x509ViewName", x509ViewName);

				// view bean
				BeanDefinitionBuilder x509View = BeanDefinitionBuilder.rootBeanDefinition(X509CertificateView.class);
				registry.registerBeanDefinition("x509certs", x509View.getBeanDefinition());
				viewResolver.addPropertyReference("x509", "x509certs");
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
		Map<String, PublicKey> keys = signingAndValidationService.getAllPublicKeys();

		// TODO: check if keys are empty, return a 404 here or just an empty list?
		
		return new ModelAndView(jwkViewName, "keys", keys);
	}

	/**
	 * Return a view to publish all keys in x509 format. Only used if x509publishUrl is set.
	 * @return
	 */
	public ModelAndView publishClientx509() {
		// map from key id to key
		Map<String, PublicKey> keys = signingAndValidationService.getAllPublicKeys();

		// TODO: check if keys are empty, return a 404 here or just an empty list?
		
		return new ModelAndView(x509ViewName, "keys", keys);
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

	/**
	 * @return the signingAndValidationService
	 */
	public JwtSigningAndValidationService getSigningAndValidationService() {
		return signingAndValidationService;
	}

	/**
	 * @param signingAndValidationService the signingAndValidationService to set
	 */
	public void setSigningAndValidationService(JwtSigningAndValidationService signingAndValidationService) {
		this.signingAndValidationService = signingAndValidationService;
	}


}
