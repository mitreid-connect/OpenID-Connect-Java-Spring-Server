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

import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Translates a Serializable object of certain primitive types
 * into a String for storage in the database, for use with the
 * OAuth2Request extensions map.
 *
 * This class does allow some extension data to be lost.
 *
 * @author jricher
 */
@Converter
@Slf4j
public class SamlAuthenticationDetailsStringConverter implements AttributeConverter<SamlAuthenticationDetails, String> {

	@Override
	public String convertToDatabaseColumn(SamlAuthenticationDetails attribute) {
		return SamlAuthenticationDetails.serialize(attribute);
	}

	@Override
	public SamlAuthenticationDetails convertToEntityAttribute(String dbData) {
		return SamlAuthenticationDetails.deserialize(dbData);
	}
}
