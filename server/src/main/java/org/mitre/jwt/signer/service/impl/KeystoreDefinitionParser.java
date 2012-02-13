package org.mitre.jwt.signer.service.impl;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Needed to parse and define just a single BeanDefinition for the KeyStore
 * 
 * @author nemonik
 * 
 */
public class KeystoreDefinitionParser extends
		AbstractSingleBeanDefinitionParser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser
	 * #doParse(org.w3c.dom.Element,
	 * org.springframework.beans.factory.xml.ParserContext,
	 * org.springframework.beans.factory.support.BeanDefinitionBuilder)
	 */
	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {

		String password = element.getAttribute("password");
		if (StringUtils.hasText(password)) {
			builder.addConstructorArgValue(password);
		}

		String location = element.getAttribute("location");
		
		if (!StringUtils.hasText(location)) {
			parserContext.getReaderContext().error(
					"A location must be supplied on a keystore element.",
					element);
		} else {
			
			Resource resource = parserContext.getReaderContext().getResourceLoader().getResource(location);
			
			if (!resource.exists()) {
				parserContext.getReaderContext().error(
						"The location supplied on the keystore element must exist.",
						element);				
			} else {
				builder.addConstructorArgValue(resource);
			}
		}

		String type = element.getAttribute("type");
		if (StringUtils.hasText(type)) {
			builder.addConstructorArgValue(type);
		}	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser
	 * #getBeanClass(org.w3c.dom.Element)
	 */
	@Override
	protected Class<?> getBeanClass(Element element) {
		return KeyStore.class;
	}
}