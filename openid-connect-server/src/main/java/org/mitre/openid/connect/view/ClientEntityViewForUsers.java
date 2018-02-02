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
 * View bean for field-limited view of client entity, for regular users.
 *
 * @see AbstractClientEntityView
 * @see ClientEntityViewForAdmins
 * @author jricher
 *
 */
@Component(ClientEntityViewForUsers.VIEWNAME)
public class ClientEntityViewForUsers extends AbstractClientEntityView {

	private Set<String> whitelistedFields = ImmutableSet.of("clientName", "clientId", "id", "clientDescription", "scope", "logoUri");

	public static final String VIEWNAME = "clientEntityViewUsers";

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.view.AbstractClientEntityView#getExclusionStrategy()
	 */
	@Override
	protected ExclusionStrategy getExclusionStrategy() {
		return new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				// whitelist the handful of fields that are good
				if (whitelistedFields.contains(f.getName())) {
					return false;
				} else {
					return true;
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
