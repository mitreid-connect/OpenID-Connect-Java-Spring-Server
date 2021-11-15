/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.oauth2.service.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.repository.SystemScopeRepository;
import cz.muni.ics.oauth2.service.SystemScopeService;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jricher
 *
 */
@Service("defaultSystemScopeService")
public class DefaultSystemScopeService implements SystemScopeService {

	@Autowired
	private SystemScopeRepository repository;

	private final Predicate<SystemScope> isDefault = new Predicate<SystemScope>() {
		@Override
		public boolean apply(SystemScope input) {
			return (input != null && input.isDefaultScope());
		}
	};

	private final Predicate<SystemScope> isRestricted = new Predicate<SystemScope>() {
		@Override
		public boolean apply(SystemScope input) {
			return (input != null && input.isRestricted());
		}
	};

	private final Predicate<SystemScope> isReserved = new Predicate<SystemScope>() {
		@Override
		public boolean apply(SystemScope input) {
			return (input != null && getReserved().contains(input));
		}
	};

	private final Function<String, SystemScope> stringToSystemScope = new Function<String, SystemScope>() {
		@Override
		public SystemScope apply(String input) {
			if (Strings.isNullOrEmpty(input)) {
				return null;
			} else {
				// get the real scope if it's available
				SystemScope s = getByValue(input);
				if (s == null) {
					// make a fake one otherwise
					s = new SystemScope(input);
				}

				return s;
			}
		}
	};

	private final Function<SystemScope, String> systemScopeToString = new Function<SystemScope, String>() {
		@Override
		public String apply(SystemScope input) {
			if (input == null) {
				return null;
			} else {
				return input.getValue();
			}
		}
	};

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#getAll()
	 */
	@Override
	public Set<SystemScope> getAll() {
		return repository.getAll();
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#getById(java.lang.Long)
	 */
	@Override
	public SystemScope getById(Long id) {
		return repository.getById(id);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#getByValue(java.lang.String)
	 */
	@Override
	public SystemScope getByValue(String value) {
		return repository.getByValue(value);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#remove(cz.muni.ics.oauth2.model.SystemScope)
	 */
	@Override
	public void remove(SystemScope scope) {
		repository.remove(scope);

	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#save(cz.muni.ics.oauth2.model.SystemScope)
	 */
	@Override
	public SystemScope save(SystemScope scope) {
		if (!isReserved.apply(scope)) { // don't allow saving of reserved scopes
			return repository.save(scope);
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#fromStrings(java.util.Set)
	 */
	@Override
	public Set<SystemScope> fromStrings(Set<String> scope) {
		if (scope == null) {
			return null;
		} else {
			return new LinkedHashSet<>(Collections2.filter(Collections2.transform(scope, stringToSystemScope), Predicates.notNull()));
		}
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#toStrings(java.util.Set)
	 */
	@Override
	public Set<String> toStrings(Set<SystemScope> scope) {
		if (scope == null) {
			return null;
		} else {
			return new LinkedHashSet<>(Collections2.filter(Collections2.transform(scope, systemScopeToString), Predicates.notNull()));
		}
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.SystemScopeService#scopesMatch(java.util.Set, java.util.Set)
	 */
	@Override
	public boolean scopesMatch(Set<String> expected, Set<String> actual) {

		Set<SystemScope> ex = fromStrings(expected);
		Set<SystemScope> act = fromStrings(actual);

		for (SystemScope actScope : act) {
			// first check to see if there's an exact match
			if (!ex.contains(actScope)) {
				return false;
			} else {
				// if we did find an exact match, we need to check the rest
			}
		}

		// if we got all the way down here, the setup passed
		return true;

	}

	@Override
	public Set<SystemScope> getDefaults() {
		return Sets.filter(getAll(), isDefault);
	}


	@Override
	public Set<SystemScope> getReserved() {
		return reservedScopes;
	}

	@Override
	public Set<SystemScope> getRestricted() {
		return Sets.filter(getAll(), isRestricted);
	}

	@Override
	public Set<SystemScope> getUnrestricted() {
		return Sets.filter(getAll(), Predicates.not(isRestricted));
	}

	@Override
	public Set<SystemScope> removeRestrictedAndReservedScopes(Set<SystemScope> scopes) {
		return Sets.filter(scopes, Predicates.not(Predicates.or(isRestricted, isReserved)));
	}

	@Override
	public Set<SystemScope> removeReservedScopes(Set<SystemScope> scopes) {
		return Sets.filter(scopes, Predicates.not(isReserved));
	}

}
