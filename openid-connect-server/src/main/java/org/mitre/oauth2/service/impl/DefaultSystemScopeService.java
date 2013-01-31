/**
 * 
 */
package org.mitre.oauth2.service.impl;

import java.util.Set;

import javax.annotation.Nullable;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
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

	
	
}
