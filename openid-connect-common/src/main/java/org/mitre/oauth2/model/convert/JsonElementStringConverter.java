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

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author jricher
 *
 */
@Converter
public class JsonElementStringConverter implements AttributeConverter<JsonElement, String> {

	private JsonParser parser = new JsonParser();

	@Override
	public String convertToDatabaseColumn(JsonElement attribute) {
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
	public JsonElement convertToEntityAttribute(String dbData) {
		if (!Strings.isNullOrEmpty(dbData)) {
			return parser.parse(dbData);
		} else {
			return null;
		}
	}

}
