/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.config;

import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ConfigurationPropertiesBean {

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
	private boolean allowCompleteDeviceCodeUri = false;

	public ConfigurationPropertiesBean() { }

	@PostConstruct
	public void checkConfigConsistency() {
		if (!StringUtils.startsWithIgnoreCase(issuer, "https")) {
			if (this.forceHttps) {
				log.error("Configured issuer url is not using https scheme. Server will be shut down!");
				throw new BeanCreationException("Issuer is not using https scheme as required: " + issuer);
			} else {
				log.warn("\n\n**\n** WARNING: Configured issuer url is not using https scheme.\n**\n\n");
			}
		}

		if (languageNamespaces == null || languageNamespaces.isEmpty()) {
			log.error("No configured language namespaces! Text rendering will fail!");
		}
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String iss) {
		issuer = iss;
	}

	public String getTopbarTitle() {
		return topbarTitle;
	}

	public void setTopbarTitle(String topbarTitle) {
		this.topbarTitle = topbarTitle;
	}

	public String getShortTopbarTitle() {
		return shortTopbarTitle == null ? topbarTitle : shortTopbarTitle;
	}

	public void setShortTopbarTitle(String shortTopbarTitle) {
		this.shortTopbarTitle = shortTopbarTitle;
	}

	public String getLogoImageUrl() {
		return logoImageUrl;
	}

	public void setLogoImageUrl(String logoImageUrl) {
		this.logoImageUrl = logoImageUrl;
	}

	public Long getRegTokenLifeTime() {
		return regTokenLifeTime;
	}

	public void setRegTokenLifeTime(Long regTokenLifeTime) {
		this.regTokenLifeTime = regTokenLifeTime;
	}

	public Long getRqpTokenLifeTime() {
		return rqpTokenLifeTime;
	}

	public void setRqpTokenLifeTime(Long rqpTokenLifeTime) {
		this.rqpTokenLifeTime = rqpTokenLifeTime;
	}

	public boolean isForceHttps() {
		return forceHttps;
	}

	public void setForceHttps(boolean forceHttps) {
		this.forceHttps = forceHttps;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public List<String> getLanguageNamespaces() {
		return languageNamespaces;
	}

	public void setLanguageNamespaces(List<String> languageNamespaces) {
		this.languageNamespaces = languageNamespaces;
	}

	public boolean isDualClient() {
		if (isHeartMode()) {
			return false; // HEART mode is incompatible with dual client mode
		} else {
			return dualClient;
		}
	}

	public void setDualClient(boolean dualClient) {
		this.dualClient = dualClient;
	}

	public String getLanguageNamespacesString() {
		return new Gson().toJson(getLanguageNamespaces());
	}

	public String getDefaultLanguageNamespace() {
		return getLanguageNamespaces().get(0);
	}

	public boolean isHeartMode() {
		return heartMode;
	}

	public void setHeartMode(boolean heartMode) {
		this.heartMode = heartMode;
	}

	public boolean isAllowCompleteDeviceCodeUri() {
		return allowCompleteDeviceCodeUri;
	}

	public void setAllowCompleteDeviceCodeUri(boolean allowCompleteDeviceCodeUri) {
		this.allowCompleteDeviceCodeUri = allowCompleteDeviceCodeUri;
	}

}
