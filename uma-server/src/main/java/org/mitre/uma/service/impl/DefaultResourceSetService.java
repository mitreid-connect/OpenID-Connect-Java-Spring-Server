/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/

package org.mitre.uma.service.impl;

import java.util.Collection;

import org.mitre.uma.model.Policy;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.repository.ResourceSetRepository;
import org.mitre.uma.service.ResourceSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author jricher
 *
 */
@Service
@Primary
public class DefaultResourceSetService implements ResourceSetService {

	private static final Logger logger = LoggerFactory.getLogger(DefaultResourceSetService.class);
	
	@Autowired
	private ResourceSetRepository repository;

	@Override
	public ResourceSet saveNew(ResourceSet rs) {
		
		if (rs.getId() != null) {
			throw new IllegalArgumentException("Can't save a new resource set with an ID already set to it.");
		}
		
		if (!checkScopeConsistency(rs)) {
			throw new IllegalArgumentException("Can't save a resource set with inconsistent claims.");
		}
		
		ResourceSet saved = repository.save(rs);
		
		return saved;
		
	}

	@Override
	public ResourceSet getById(Long id) {
		return repository.getById(id);
	}

	@Override
	public ResourceSet update(ResourceSet oldRs, ResourceSet newRs) {

		if (oldRs.getId() == null || newRs.getId() == null
				|| !oldRs.getId().equals(newRs.getId())) {
			
			throw new IllegalArgumentException("Resource set IDs mismatched");
			
		}

		if (!checkScopeConsistency(newRs)) {
			throw new IllegalArgumentException("Can't save a resource set with inconsistent claims.");
		}
		
		newRs.setOwner(oldRs.getOwner()); // preserve the owner tag across updates
		newRs.setClientId(oldRs.getClientId()); // preserve the client id across updates
		
		ResourceSet saved = repository.save(newRs);
		
		return saved;
		
	}

	@Override
	public void remove(ResourceSet rs) {
		repository.remove(rs);
	}

	@Override
	public Collection<ResourceSet> getAllForOwner(String owner) {
		return repository.getAllForOwner(owner);
	}

	@Override
	public Collection<ResourceSet> getAllForOwnerAndClient(String owner, String clientId) {
		return repository.getAllForOwnerAndClient(owner, clientId);
	}
	
	private boolean checkScopeConsistency(ResourceSet rs) {
		if (rs.getPolicies() == null) {
			// nothing to check, no problem!
			return true;
		}
		for (Policy policy : rs.getPolicies()) {
			if (!rs.getScopes().containsAll(policy.getScopes())) {
				return false;
			}
		}
		// we've checked everything, we're good
		return true;
	}
	
}
