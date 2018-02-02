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

import com.nimbusds.jose.EncryptionMethod;

@Converter
public class JWEEncryptionMethodStringConverter implements AttributeConverter<EncryptionMethod, String> {

	@Override
	public String convertToDatabaseColumn(EncryptionMethod attribute) {
		if (attribute != null) {
			return attribute.getName();
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
	 */
	@Override
	public EncryptionMethod convertToEntityAttribute(String dbData) {
		if (dbData != null) {
			return EncryptionMethod.parse(dbData);
		} else {
			return null;
		}
	}
}