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
@Component("clientEntityViewUsers")
public class ClientEntityViewForUsers extends AbstractClientEntityView {

	private Set<String> whitelistedFields = ImmutableSet.of("clientName", "clientId", "id", "clientDescription", "scope", "logoUri");

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
