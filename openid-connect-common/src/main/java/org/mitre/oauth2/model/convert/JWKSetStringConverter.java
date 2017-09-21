/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

package org.mitre.oauth2.model.convert;

import java.text.ParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.jwk.JWKSet;

/**
 * @author jricher
 *
 */
@Converter
public class JWKSetStringConverter implements AttributeConverter<JWKSet, String> {

	private static Logger logger = LoggerFactory.getLogger(JWKSetStringConverter.class);

	@Override
	public String convertToDatabaseColumn(JWKSet attribute) {
		if (attribute != null) {
			return attribute.toString();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
	 */
	@Override
	public JWKSet convertToEntityAttribute(String dbData) {
		if (dbData != null) {
			try {
				JWKSet jwks = JWKSet.parse(dbData);
				return jwks;
			} catch (ParseException e) {
				logger.error("Unable to parse JWK Set", e);
				return null;
			}
		} else {
			return null;
		}

	}

}
