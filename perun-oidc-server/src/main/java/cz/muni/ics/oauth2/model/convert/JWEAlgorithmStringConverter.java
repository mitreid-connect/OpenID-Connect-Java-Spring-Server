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

import com.nimbusds.jose.JWEAlgorithm;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class JWEAlgorithmStringConverter implements AttributeConverter<JWEAlgorithm, String> {

	@Override
	public String convertToDatabaseColumn(JWEAlgorithm attribute) {
		return attribute != null ? attribute.getName() : null;
	}

	@Override
	public JWEAlgorithm convertToEntityAttribute(String dbData) {
		return dbData != null ? JWEAlgorithm.parse(dbData) : null;
	}

}
