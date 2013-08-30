/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
 ******************************************************************************/
package org.mitre.openid.connect.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;



/**
 * Bean to hold configuration information that must be injected into various parts
 * of our application. Set all of the properties here, and autowire a reference
 * to this bean if you need access to any configuration properties.
 * 
 * @author AANGANES
 *
 */
public class ConfigurationPropertiesBean {

	private static Logger logger = LoggerFactory.getLogger(ConfigurationPropertiesBean.class);

	private String issuer;

	private String topbarTitle;

	private String logoImageUrl;

	public ConfigurationPropertiesBean() {

	}

	/**
	 * Endpoints protected by TLS must have https scheme in the URI.
	 */
	@PostConstruct
	public void checkForHttps() {
		if (!StringUtils.startsWithIgnoreCase(issuer, "https")) {
			logger.warn("Configured issuer url is not using https scheme.");
		}
	}

	/**
	 * @return the issuer baseUrl
	 */
	public String getIssuer() {
		String ret = java.lang.System.getenv("baseUrl");
		if (ret != null) return ret;
		return issuer;
	}

	/**
	 * @param iss the issuer to set
	 */
	public void setIssuer(String iss) {
		issuer = iss;
	}

	/**
	 * @return the topbarTitle
	 */
	public String getTopbarTitle() {
		return topbarTitle;
	}

	/**
	 * @param topbarTitle the topbarTitle to set
	 */
	public void setTopbarTitle(String topbarTitle) {
		this.topbarTitle = topbarTitle;
	}

	/**
	 * @return the logoImageUrl
	 */
	public String getLogoImageUrl() {
		return logoImageUrl;
	}

	/**
	 * @param logoImageUrl the logoImageUrl to set
	 */
	public void setLogoImageUrl(String logoImageUrl) {
		this.logoImageUrl = logoImageUrl;
	}

}
