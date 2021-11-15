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

package cz.muni.ics.uma.repository;

import cz.muni.ics.uma.model.ResourceSet;
import java.util.Collection;

/**
 * @author jricher
 */
public interface ResourceSetRepository {

	ResourceSet save(ResourceSet rs);

	ResourceSet getById(Long id);

	void remove(ResourceSet rs);

	Collection<ResourceSet> getAllForOwner(String owner);

	Collection<ResourceSet> getAllForOwnerAndClient(String owner, String clientId);

	Collection<ResourceSet> getAll();

	Collection<ResourceSet> getAllForClient(String clientId);

}
