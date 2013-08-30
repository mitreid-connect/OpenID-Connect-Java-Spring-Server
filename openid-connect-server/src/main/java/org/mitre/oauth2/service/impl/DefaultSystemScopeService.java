/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
 ******************************************************************************/
/**
 * 
 */
package org.mitre.oauth2.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author jricher
 *
 */
@Service("defaultSystemScopeService")
public class DefaultSystemScopeService implements SystemScopeService {

	@Autowired
	private SystemScopeRepository repository;

	private Predicate<SystemScope> isDefault = new Predicate<SystemScope>() {
		@Override
		public boolean apply(SystemScope input) {
			return (input != null && input.isDefaultScope());
		}
	};


	private Predicate<SystemScope> isDynReg = new Predicate<SystemScope>() {
		@Override
		public boolean apply(SystemScope input) {
			return (input != null && input.isAllowDynReg());
		}
	};

	private Function<String, SystemScope> stringToSystemScope = new Function<String, SystemScope>() {
		@Override
		public SystemScope apply(String input) {
			input = baseScopeString(input);
			if (input == null) {
				return null;
			} else {
				SystemScope s = getByValue(input);
				if (s != null) {
					// get the real scope if it's available
					return s;
				} else {
					// make a fake one otherwise
					return new SystemScope(input);
				}
			}
		}
	};

	private Function<SystemScope, String> systemScopeToString = new Function<SystemScope, String>() {
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
	 * @see org.mitre.oauth2.service.SystemScopeService#getAll()
	 */
	@Override
	public Set<SystemScope> getAll() {
		return repository.getAll();
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#getDefaults()
	 */
	@Override
	public Set<SystemScope> getDefaults() {
		return Sets.filter(getAll(), isDefault);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#getDynReg()
	 */
	@Override
	public Set<SystemScope> getDynReg() {
		return Sets.filter(getAll(), isDynReg);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#getById(java.lang.Long)
	 */
	@Override
	public SystemScope getById(Long id) {
		return repository.getById(id);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#getByValue(java.lang.String)
	 */
	@Override
	public SystemScope getByValue(String value) {
		return repository.getByValue(value);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#remove(org.mitre.oauth2.model.SystemScope)
	 */
	@Override
	public void remove(SystemScope scope) {
		repository.remove(scope);

	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#save(org.mitre.oauth2.model.SystemScope)
	 */
	@Override
	public SystemScope save(SystemScope scope) {
		return repository.save(scope);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#fromStrings(java.util.Set)
	 */
	@Override
	public Set<SystemScope> fromStrings(Set<String> scope) {
		if (scope == null) {
			return null;
		} else {
			return new LinkedHashSet<SystemScope>(Collections2.filter(Collections2.transform(scope, stringToSystemScope), Predicates.notNull()));
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.SystemScopeService#toStrings(java.util.Set)
	 */
	@Override
	public Set<String> toStrings(Set<SystemScope> scope) {
		if (scope == null) {
			return null;
		} else {
			return new LinkedHashSet<String>(Collections2.filter(Collections2.transform(scope, systemScopeToString), Predicates.notNull()));
		}
	}

	private String[] scopeParts(String value){
		return Iterables.toArray(
				Splitter.on(":").split(value), 
				String.class);
	}
	
	@Override
	public String baseScopeString(String value) {
		SystemScope s = toStructuredScope(value);
		if (s != null) {
			return s.getValue();
		}
		return value;
	}
	
	@Override
	public SystemScope toStructuredScope(String value) {
		String[] scopeParts = scopeParts(value);
		String baseScope = value;
		if (scopeParts.length == 2) {
			baseScope = scopeParts[0];
		}
		SystemScope s = repository.getByValue(baseScope);
		if (s != null && s.isStructured()) {
			return s;
		}			
		
		return null;
	}

	@Override
	public Map<String, String> structuredScopeParameters(Set<String> scopes) {
		HashMap<String, String> ret = new HashMap<String, String>();
		
		for (String s : scopes){
			SystemScope structured = toStructuredScope(s);
			if (structured != null){
				String[] scopeParts = scopeParts(s);
				if (scopeParts.length == 2){
					ret.put(scopeParts[0], scopeParts[1]);
				}
			}
		}		
		
		return ret;
	}



}
