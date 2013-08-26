/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
 ******************************************************************************/
package org.bbplus;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

/**
 * ApprovedSite repository interface
 *
 * @author Josh Mandel
 *
 */
public interface TrustedRegistryRepository extends JpaRepository<TrustedRegistry, String> {

	/**
	 * Return a collection of all TrustedRegistries managed by this repository
	 * 
	 * @return the TrustedRegistry collection, or null
	 */
	public Collection<TrustedRegistry> getAll();

}
