/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.config;

import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;

/**
 * Resolve the server's locale from the injected ConfigurationPropertiesBean.
 *
 * @author jricher
 */
public class ConfigurationBeanLocaleResolver extends AbstractLocaleContextResolver {

	private ConfigurationPropertiesBean config;

	@Autowired
	public ConfigurationBeanLocaleResolver(ConfigurationPropertiesBean config) {
		this.config = config;
	}

	@Override
	protected Locale getDefaultLocale() {
		if (config.getLocale() != null) {
			return config.getLocale();
		} else {
			return super.getDefaultLocale();
		}
	}

	@Override
	public LocaleContext resolveLocaleContext(HttpServletRequest request) {
		return new TimeZoneAwareLocaleContext() {
			@Override
			public Locale getLocale() {
				return getDefaultLocale();
			}
			@Override
			public TimeZone getTimeZone() {
				return getDefaultTimeZone();
			}
		};
	}

	@Override
	public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
		throw new UnsupportedOperationException("Cannot change fixed locale - use a different locale resolution strategy");
	}

}
