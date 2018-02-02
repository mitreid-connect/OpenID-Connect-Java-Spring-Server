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
package org.mitre.uma.service;

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.uma.model.ResourceSet;

/**
 *
 * Manage registered resource sets at this authorization server.
 *
 * @author jricher
 *
 */
public interface ResourceSetService {

	public ResourceSet saveNew(ResourceSet rs);

	public ResourceSet getById(Long id);

	public ResourceSet update(ResourceSet oldRs, ResourceSet newRs);

	public void remove(ResourceSet rs);

	public Collection<ResourceSet> getAllForOwner(String owner);

	public Collection<ResourceSet> getAllForOwnerAndClient(String owner, String authClientId);

	public Collection<ResourceSet> getAllForClient(ClientDetailsEntity client);

}
