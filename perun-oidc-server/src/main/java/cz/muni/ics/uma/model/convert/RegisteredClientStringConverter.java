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

package cz.muni.ics.uma.model.convert;

import cz.muni.ics.oauth2.model.RegisteredClient;
import cz.muni.ics.openid.connect.ClientDetailsEntityJsonProcessor;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.springframework.util.StringUtils;

/**
 * @author jricher
 */
@Converter
public class RegisteredClientStringConverter implements AttributeConverter<RegisteredClient, String>{

	@Override
	public String convertToDatabaseColumn(RegisteredClient attribute) {
		return attribute == null || attribute.getSource() == null ? null : attribute.getSource().toString();
	}

	@Override
	public RegisteredClient convertToEntityAttribute(String dbData) {
		return StringUtils.isEmpty(dbData) ? null : ClientDetailsEntityJsonProcessor.parseRegistered(dbData);
	}

}
