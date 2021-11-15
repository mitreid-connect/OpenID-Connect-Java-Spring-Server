/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.service.impl;

import com.google.common.base.Strings;
import cz.muni.ics.openid.connect.model.BlacklistedSite;
import cz.muni.ics.openid.connect.repository.BlacklistedSiteRepository;
import cz.muni.ics.openid.connect.service.BlacklistedSiteService;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Service
@Transactional(value="defaultTransactionManager")
public class DefaultBlacklistedSiteService implements BlacklistedSiteService {

	@Autowired
	private BlacklistedSiteRepository repository;

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.BlacklistedSiteService#getAll()
	 */
	@Override
	public Collection<BlacklistedSite> getAll() {
		return repository.getAll();
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.BlacklistedSiteService#getById(java.lang.Long)
	 */
	@Override
	public BlacklistedSite getById(Long id) {
		return repository.getById(id);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.BlacklistedSiteService#remove(cz.muni.ics.openid.connect.model.BlacklistedSite)
	 */
	@Override
	public void remove(BlacklistedSite blacklistedSite) {
		repository.remove(blacklistedSite);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.BlacklistedSiteService#saveNew(cz.muni.ics.openid.connect.model.BlacklistedSite)
	 */
	@Override
	public BlacklistedSite saveNew(BlacklistedSite blacklistedSite) {
		return repository.save(blacklistedSite);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.BlacklistedSiteService#update(cz.muni.ics.openid.connect.model.BlacklistedSite, cz.muni.ics.openid.connect.model.BlacklistedSite)
	 */
	@Override
	public BlacklistedSite update(BlacklistedSite oldBlacklistedSite, BlacklistedSite blacklistedSite) {
		return repository.update(oldBlacklistedSite, blacklistedSite);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.BlacklistedSiteService#isBlacklisted(java.lang.String)
	 */
	@Override
	public boolean isBlacklisted(String uri) {

		if (Strings.isNullOrEmpty(uri)) {
			return false; // can't be blacklisted if you don't exist
		}

		Collection<BlacklistedSite> sites = getAll();

		// TODO: rewrite this to do regex matching and use the Guava predicates collection

		for (BlacklistedSite blacklistedSite : sites) {
			if (Strings.nullToEmpty(blacklistedSite.getUri()).equals(uri)) {
				return true;
			}
		}

		return false;
	}

}
