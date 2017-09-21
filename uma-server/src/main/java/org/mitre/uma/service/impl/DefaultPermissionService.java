/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

import java.sql.Date;
import java.util.Set;
import java.util.UUID;

import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.uma.model.Permission;
import org.mitre.uma.model.PermissionTicket;
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

	private Long permissionExpirationSeconds = 60L * 60L; // 1 hr

	/* (non-Javadoc)
	 * @see org.mitre.uma.service.PermissionService#create(org.mitre.uma.model.ResourceSet, java.util.Set)
	 */
	@Override
	public PermissionTicket createTicket(ResourceSet resourceSet, Set<String> scopes) {

		// check to ensure that the scopes requested are a subset of those in the resource set

		if (!scopeService.scopesMatch(resourceSet.getScopes(), scopes)) {
			throw new InsufficientScopeException("Scopes of resource set are not enough for requested permission.");
		}

		Permission perm = new Permission();
		perm.setResourceSet(resourceSet);
		perm.setScopes(scopes);

		PermissionTicket ticket = new PermissionTicket();
		ticket.setPermission(perm);
		ticket.setTicket(UUID.randomUUID().toString());
		ticket.setExpiration(new Date(System.currentTimeMillis() + permissionExpirationSeconds * 1000L));

		return repository.save(ticket);

	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.service.PermissionService#getByTicket(java.lang.String)
	 */
	@Override
	public PermissionTicket getByTicket(String ticket) {
		return repository.getByTicket(ticket);
	}

	/* (non-Javadoc)
	 * @see org.mitre.uma.service.PermissionService#updateTicket(org.mitre.uma.model.PermissionTicket)
	 */
	@Override
	public PermissionTicket updateTicket(PermissionTicket ticket) {
		if (ticket.getId() != null) {
			return repository.save(ticket);
		} else {
			return null;
		}

	}



}
