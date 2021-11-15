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

package cz.muni.ics.oauth2.model.convert;

import com.nimbusds.jose.jwk.JWKSet;
import java.text.ParseException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jricher
 */
@Converter
@Slf4j
public class JWKSetStringConverter implements AttributeConverter<JWKSet, String> {

	@Override
	public String convertToDatabaseColumn(JWKSet attribute) {
		return attribute != null ? attribute.toString() : null;
	}

	@Override
	public JWKSet convertToEntityAttribute(String dbData) {
		if (dbData != null) {
			try {
				return JWKSet.parse(dbData);
			} catch (ParseException e) {
				log.error("Unable to parse JWK Set", e);
				return null;
			}
		} else {
			return null;
		}
	}

}
