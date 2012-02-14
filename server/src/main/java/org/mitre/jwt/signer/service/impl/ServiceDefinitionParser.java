package org.mitre.jwt.signer.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jwt.signer.impl.HmacSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Needed to parse and define just a single BeanDefinition for the
 * JwtSigningAndValidationServiceDefault
 * 
 * @author nemonik
 * 
 */
public class ServiceDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static Log logger = LogFactory.getLog(ServiceDefinitionParser.class);
	
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

		ManagedList<BeanMetadataElement> signers = new ManagedList<BeanMetadataElement>();

		List<Element> signerElements = DomUtils.getChildElementsByTagName(
				element, new String[] { "rsa", "hmac" });

		for (Element signerElement : signerElements) {
			
			if (signerElement.getTagName().contains("rsa")) {
				
				logger.debug("parsing rsa element");
				
				BeanDefinitionBuilder signer = BeanDefinitionBuilder
						.rootBeanDefinition(RsaSigner.class);

				String bits = signerElement.getAttribute("bits");
				if (StringUtils.hasText(bits)) {
					signer.addConstructorArgValue("RS".concat(bits));
				} else {
					signer.addConstructorArgValue(RsaSigner.Algorithm.DEFAULT);
				}
				
				String keystoreRef = signerElement.getAttribute("keystore-ref");
				if (!StringUtils.hasText(keystoreRef)) {
					parserContext
							.getReaderContext()
							.error("A keystore-ref must be supplied with the definition of a rsa.",
									signerElement);
				} else {
					signer.addConstructorArgReference(keystoreRef);				
				}

				String alias = signerElement.getAttribute("key-alias");
				if (!StringUtils.hasText(alias)) {
					parserContext
							.getReaderContext()
							.error("An key-alias must be supplied with the definition of a rsa.",
									signerElement);
				} else {
					signer.addConstructorArgValue(alias);
				}

				String password = signerElement.getAttribute("password");
				if (StringUtils.hasText(password)) {
					signer.addConstructorArgValue(password);
				} else {
					signer.addConstructorArgValue(RsaSigner.DEFAULT_PASSWORD);
				}

				signers.add(signer.getBeanDefinition());

			} else if (signerElement.getTagName().contains("hmac")) {
				
				logger.debug("parsing hmac element");

				BeanDefinitionBuilder signer = BeanDefinitionBuilder
						.rootBeanDefinition(HmacSigner.class);

				String bits = signerElement.getAttribute("bits");
				if (StringUtils.hasText(bits)) {
					signer.addConstructorArgValue("HS".concat(bits));
				} else {
					signer.addConstructorArgValue(HmacSigner.Algorithm.DEFAULT);
				}

				String passphrase = signerElement.getAttribute("passphrase");
				if (!StringUtils.hasText(passphrase)) {
					parserContext
					.getReaderContext()
					.error("A passphrase must be supplied with the definition of a hmac.",
							signerElement);
				} else {
					signer.addConstructorArgValue(passphrase);
				}

				signers.add(signer.getBeanDefinition());
			}
		}

		builder.addPropertyValue("signers", signers);
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
		return JwtSigningAndValidationServiceDefault.class;
	}
}
