package org.mitre.jwt.signer.service.impl;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Support class for implementing custom jwt-signer namespace
 * 
 * @author nemonik
 *
 */
public class JwtSignerNamespaceHandler extends NamespaceHandlerSupport {

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	@Override
	public void init() {
		registerBeanDefinitionParser("keystore", new KeystoreDefinitionParser());
		registerBeanDefinitionParser("service", new ServiceDefinitionParser());	
	}

}
