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
package org.mitre.openid.connect.config;

import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;



/**
 * Bean to hold configuration information that must be injected into various parts
 * of our application. Set all of the properties here, and autowire a reference
 * to this bean if you need access to any configuration properties.
 *
 * @author AANGANES
 *
 */
public class ConfigurationPropertiesBean {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationPropertiesBean.class);

	private String issuer;

	private String topbarTitle;

	private String shortTopbarTitle;

	private String logoImageUrl;

	private Long regTokenLifeTime;

	private Long rqpTokenLifeTime;

	private boolean forceHttps = false; // by default we just log a warning for HTTPS deployment

	private Locale locale = Locale.ENGLISH; // we default to the english translation

	private List<String> languageNamespaces = Lists.newArrayList("messages");

	private boolean dualClient = false;

	private boolean heartMode = false;

	public ConfigurationPropertiesBean() {

	}

	/**
	 * Endpoints protected by TLS must have https scheme in the URI.
	 * @throws HttpsUrlRequiredException
	 */
	@PostConstruct
	public void checkConfigConsistency() {
		if (!StringUtils.startsWithIgnoreCase(issuer, "https")) {
			if (this.forceHttps) {
				logger.error("Configured issuer url is not using https scheme. Server will be shut down!");
				throw new BeanCreationException("Issuer is not using https scheme as required: " + issuer);
			}
			else {
				logger.warn("\n\n**\n** WARNING: Configured issuer url is not using https scheme.\n**\n\n");
			}
		}

		if (languageNamespaces == null || languageNamespaces.isEmpty()) {
			logger.error("No configured language namespaces! Text rendering will fail!");
		}
	}

	/**
	 * @return the issuer baseUrl
	 */
	public String getIssuer() {
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
	 * @return If shortTopbarTitle is undefined, returns topbarTitle.
	 */
	public String getShortTopbarTitle() {
		return shortTopbarTitle == null ? topbarTitle : shortTopbarTitle;
	}

	public void setShortTopbarTitle(String shortTopbarTitle) {
		this.shortTopbarTitle = shortTopbarTitle;
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

	/**
	 * @return the regTokenLifeTime
	 */
	public Long getRegTokenLifeTime() {
		return regTokenLifeTime;
	}

	/**
	 * @param regTokenLifeTime the registration token lifetime to set in seconds
	 */
	public void setRegTokenLifeTime(Long regTokenLifeTime) {
		this.regTokenLifeTime = regTokenLifeTime;
	}

	/**
	 * @return the rqpTokenLifeTime
	 */
	public Long getRqpTokenLifeTime() {
		return rqpTokenLifeTime;
	}

	/**
	 * @param rqpTokenLifeTime the rqpTokenLifeTime to set
	 */
	public void setRqpTokenLifeTime(Long rqpTokenLifeTime) {
		this.rqpTokenLifeTime = rqpTokenLifeTime;
	}

	public boolean isForceHttps() {
		return forceHttps;
	}

	public void setForceHttps(boolean forceHttps) {
		this.forceHttps = forceHttps;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the languageNamespaces
	 */
	public List<String> getLanguageNamespaces() {
		return languageNamespaces;
	}

	/**
	 * @param languageNamespaces the languageNamespaces to set
	 */
	public void setLanguageNamespaces(List<String> languageNamespaces) {
		this.languageNamespaces = languageNamespaces;
	}

	/**
	 * @return true if dual client is configured, otherwise false
	 */
	public boolean isDualClient() {
		if (isHeartMode()) {
			return false; // HEART mode is incompatible with dual client mode
		} else {
			return dualClient;
		}
	}

	/**
	 * @param dualClient the dual client configuration
	 */
	public void setDualClient(boolean dualClient) {
		this.dualClient = dualClient;
	}

	/**
	 * Get the list of namespaces as a JSON string, for injection into the JavaScript UI
	 * @return
	 */
	public String getLanguageNamespacesString() {
		return new Gson().toJson(getLanguageNamespaces());
	}

	/**
	 * Get the default namespace (first in the nonempty list)
	 */
	public String getDefaultLanguageNamespace() {
		return getLanguageNamespaces().get(0);
	}

	/**
	 * @return the heartMode
	 */
	public boolean isHeartMode() {
		return heartMode;
	}

	/**
	 * @param heartMode the heartMode to set
	 */
	public void setHeartMode(boolean heartMode) {
		this.heartMode = heartMode;
	}
}
