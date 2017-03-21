/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.openid.connect.service.impl;

import java.util.Collection;

import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the WhitelistedSiteService
 *
 * @author Michael Joseph Walsh, aanganes
 *
 */
@Service
@Transactional(value="defaultTransactionManager")
public class DefaultWhitelistedSiteService implements WhitelistedSiteService {

	@Autowired
	private WhitelistedSiteRepository repository;

	@Override
	public WhitelistedSite getById(Long id) {
		return repository.getById(id);
	}

	@Override
	public void remove(WhitelistedSite whitelistedSite) {
		repository.remove(whitelistedSite);
	}

	@Override
	public WhitelistedSite saveNew(WhitelistedSite whitelistedSite) {
		if (whitelistedSite.getId() != null) {
			throw new IllegalArgumentException("A new whitelisted site cannot be created with an id value already set: " + whitelistedSite.getId());
		}
		return repository.save(whitelistedSite);
	}

	@Override
	public Collection<WhitelistedSite> getAll() {
		return repository.getAll();
	}

	@Override
	public WhitelistedSite getByClientId(String clientId) {
		return repository.getByClientId(clientId);
	}

	@Override
	public WhitelistedSite update(WhitelistedSite oldWhitelistedSite, WhitelistedSite whitelistedSite) {
		if (oldWhitelistedSite == null || whitelistedSite == null) {
			throw new IllegalArgumentException("Neither the old or new sites may be null");
		}
		return repository.update(oldWhitelistedSite, whitelistedSite);
	}

}
