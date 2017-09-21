/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
/**
 *
 */
package org.mitre.openid.connect.view;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;

import com.google.common.collect.ImmutableSet;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 *
 * View bean for full view of client entity, for admins.
 *
 * @see ClientEntityViewForUsers
 * @author jricher
 *
 */
@Component(ClientEntityViewForAdmins.VIEWNAME)
public class ClientEntityViewForAdmins extends AbstractClientEntityView {

	public static final String VIEWNAME = "clientEntityViewAdmins";
	private Set<String> blacklistedFields = ImmutableSet.of("additionalInformation");

	/**
	 * @return
	 */
	@Override
	protected ExclusionStrategy getExclusionStrategy() {
		return new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				if (blacklistedFields.contains(f.getName())) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				// skip the JPA binding wrapper
				if (clazz.equals(BeanPropertyBindingResult.class)) {
					return true;
				}
				return false;
			}

		};
	}
}
