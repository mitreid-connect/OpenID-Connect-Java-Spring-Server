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

package cz.muni.ics.uma.service;

import cz.muni.ics.oauth2.model.RegisteredClient;
import cz.muni.ics.uma.model.SavedRegisteredClient;
import java.util.Collection;

/**
 * @author jricher
 */
public interface SavedRegisteredClientService {

	/**
	 * Get a list of all the registered clients that we know about.
	 *
	 * @return
	 */
	Collection<SavedRegisteredClient> getAll();

	/**
	 * @param issuer
	 * @param client
	 */
	void save(String issuer, RegisteredClient client);


}
