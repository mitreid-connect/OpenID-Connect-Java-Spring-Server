/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/

package org.mitre.openid.connect.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author jricher
 *
 */
public class JsonMessageSource extends AbstractMessageSource {
	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(JsonMessageSource.class);

	private Resource baseDirectory;

	private Locale fallbackLocale = new Locale("en"); // US English is the fallback language

	private Map<Locale, JsonObject> languageMaps = new HashMap<>();

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {

		JsonObject lang = getLanguageMap(locale);

		String value = getValue(code, lang);

		if (value == null) {
			// if we haven't found anything, try the default locale
			lang = getLanguageMap(fallbackLocale);
			value = getValue(code, lang);
		}

		if (value == null) {
			// if it's still null, return null
			return null;
		} else {
			// otherwise format the message
			return new MessageFormat(value, locale);
		}
	
	}

	/**
	 * @param code
	 * @param locale
	 * @param lang
	 * @return
	 */
	private String getValue(String code, JsonObject lang) {

		// if there's no language map, nothing to look up
		if (lang == null) {
			return null;
		}

		JsonElement e = lang;

		Iterable<String> parts = Splitter.on('.').split(code);
		Iterator<String> it = parts.iterator();

		String value = null;

		while (it.hasNext()) {
			String p = it.next();
			if (e.isJsonObject()) {
				JsonObject o = e.getAsJsonObject();
				if (o.has(p)) {
					e = o.get(p); // found the next level
					if (!it.hasNext()) {
						// we've reached a leaf, grab it
						if (e.isJsonPrimitive()) {
							value = e.getAsString();
						}
					}
				} else {
					// didn't find it, stop processing
					break;
				}
			} else {
				// didn't find it, stop processing
				break;
			}
		}


		return value;

	}

	/**
	 * @param locale
	 * @return
	 */
	private JsonObject getLanguageMap(Locale locale) {

		if (!languageMaps.containsKey(locale)) {
			try {
				String filename = locale.getLanguage() + File.separator + "messages.json";

				Resource r = getBaseDirectory().createRelative(filename);

				logger.info("No locale loaded, trying to load from " + r);

				JsonParser parser = new JsonParser();
				JsonObject obj = (JsonObject) parser.parse(new InputStreamReader(r.getInputStream(), "UTF-8"));

				languageMaps.put(locale, obj);
			} catch (JsonIOException | JsonSyntaxException | IOException e) {
				logger.error("Unable to load locale", e);
			}
		}

		return languageMaps.get(locale);



	}

	/**
	 * @return the baseDirectory
	 */
	public Resource getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(Resource baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

}
