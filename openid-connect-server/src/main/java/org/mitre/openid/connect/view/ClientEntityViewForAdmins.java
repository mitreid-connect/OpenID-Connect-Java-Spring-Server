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
@Component("clientEntityViewAdmins")
public class ClientEntityViewForAdmins extends AbstractClientEntityView {

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
