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

package org.mitre.uma.model.convert;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;

import com.google.common.base.Strings;

/**
 * @author jricher
 *
 */
@Converter
public class RegisteredClientStringConverter implements AttributeConverter<RegisteredClient, String>{

	/* (non-Javadoc)
	 * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
	 */
	@Override
	public String convertToDatabaseColumn(RegisteredClient attribute) {
		if (attribute == null || attribute.getSource() == null) {
			return null;
		} else {
			return attribute.getSource().toString();
		}

	}

	/* (non-Javadoc)
	 * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
	 */
	@Override
	public RegisteredClient convertToEntityAttribute(String dbData) {
		if (Strings.isNullOrEmpty(dbData)) {
			return null;
		} else {
			return ClientDetailsEntityJsonProcessor.parseRegistered(dbData);
		}
	}

}
