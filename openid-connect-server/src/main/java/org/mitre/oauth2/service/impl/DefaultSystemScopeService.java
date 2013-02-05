/**
 * 
 */
package org.mitre.oauth2.service.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
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
        public boolean apply(@Nullable SystemScope input) {
			return (input != null && input.isDefaultScope());
        }
	};
	
	
	private Predicate<SystemScope> isDynReg = new Predicate<SystemScope>() {
		@Override
        public boolean apply(@Nullable SystemScope input) {
			return (input != null && input.isAllowDynReg());
        }
	};
	
	private Function<String, SystemScope> stringToSystemScope = new Function<String, SystemScope>() {
		@Override
        public SystemScope apply(@Nullable String input) {
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
        public String apply(@Nullable SystemScope input) {
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

	
	
}
