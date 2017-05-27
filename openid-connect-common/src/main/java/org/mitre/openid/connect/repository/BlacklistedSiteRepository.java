/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
/**
 *
 */
package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.openid.connect.model.BlacklistedSite;

/**
 * @author jricher
 *
 */
public interface BlacklistedSiteRepository {

	public Collection<BlacklistedSite> getAll();

	public BlacklistedSite getById(Long id);

	public void remove(BlacklistedSite blacklistedSite);

	public BlacklistedSite save(BlacklistedSite blacklistedSite);

	public BlacklistedSite update(BlacklistedSite oldBlacklistedSite, BlacklistedSite blacklistedSite);

}
