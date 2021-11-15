/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

package cz.muni.ics.openid.connect.service.impl;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.uma.model.ResourceSet;
import cz.muni.ics.uma.service.ResourceSetService;
import java.util.Collection;
import java.util.Collections;
import org.springframework.stereotype.Service;

/**
 * Dummy resource set service that doesn't do anything; acts as a stub for the
 * introspection service when the UMA functionality is disabled.
 *
 * @author jricher
 *
 */
@Service
public class DummyResourceSetService implements ResourceSetService {

	@Override
	public ResourceSet saveNew(ResourceSet rs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceSet getById(Long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceSet update(ResourceSet oldRs, ResourceSet newRs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(ResourceSet rs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ResourceSet> getAllForOwner(String owner) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<ResourceSet> getAllForOwnerAndClient(String owner, String authClientId) {
		return Collections.emptySet();
	}

	@Override
	public Collection<ResourceSet> getAllForClient(ClientDetailsEntity client) {
		return Collections.emptySet();
	}

}
