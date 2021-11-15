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

package cz.muni.ics.openid.connect.config;

import com.google.common.base.Strings;
import com.nimbusds.jose.jwk.JWKSet;
import java.beans.PropertyEditorSupport;
import java.text.ParseException;

/**
 * Allows JWK Set strings to be used in XML configurations.
 *
 * @author jricher
 */
public class JWKSetEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (!Strings.isNullOrEmpty(text)) {
			try {
				setValue(JWKSet.parse(text));
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			setValue(null);
		}
	}

}
