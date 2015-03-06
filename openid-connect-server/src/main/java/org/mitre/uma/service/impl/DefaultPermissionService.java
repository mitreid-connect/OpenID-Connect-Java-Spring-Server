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

import java.util.Set;
import java.util.UUID;

import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.uma.model.Permission;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.repository.PermissionRepository;
import org.mitre.uma.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;
import org.springframework.stereotype.Service;

/**
 * @author jricher
 *
 */
@Service
public class DefaultPermissionService implements PermissionService {

	@Autowired
	private PermissionRepository repository;
	
	@Autowired
	private SystemScopeService scopeService;
	
	/* (non-Javadoc)
	 * @see org.mitre.uma.service.PermissionService#create(org.mitre.uma.model.ResourceSet, java.util.Set)
	 */
	@Override
	public Permission create(ResourceSet resourceSet, Set<String> scopes) {
		
		// check to ensure that the scopes requested are a subset of those in the resource set
		
		if (!scopeService.scopesMatch(resourceSet.getScopes(), scopes)) {
			throw new InsufficientScopeException("Scopes of resource set are not enough for requested permission.");
		}
		
		Permission p = new Permission();
		p.setResourceSet(resourceSet);
		p.setScopes(scopes);
		p.setTicket(UUID.randomUUID().toString());
		
		return repository.save(p);
		
	}

}
