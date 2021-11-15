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
package cz.muni.ics.openid.connect.repository.impl;

import cz.muni.ics.openid.connect.model.PairwiseIdentifier;
import cz.muni.ics.openid.connect.repository.PairwiseIdentifierRepository;
import cz.muni.ics.util.jpa.JpaUtil;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository
public class JpaPairwiseIdentifierRepository implements PairwiseIdentifierRepository {

	@PersistenceContext(unitName="defaultPersistenceUnit")
	private EntityManager manager;

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.repository.PairwiseIdentifierRepository#getBySectorIdentifier(java.lang.String, java.lang.String)
	 */
	@Override
	public PairwiseIdentifier getBySectorIdentifier(String sub, String sectorIdentifierUri) {
		TypedQuery<PairwiseIdentifier> query = manager.createNamedQuery(PairwiseIdentifier.QUERY_BY_SECTOR_IDENTIFIER, PairwiseIdentifier.class);
		query.setParameter(PairwiseIdentifier.PARAM_SUB, sub);
		query.setParameter(PairwiseIdentifier.PARAM_SECTOR_IDENTIFIER, sectorIdentifierUri);

		return JpaUtil.getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.repository.PairwiseIdentifierRepository#save(cz.muni.ics.openid.connect.model.PairwiseIdentifier)
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void save(PairwiseIdentifier pairwise) {
		JpaUtil.saveOrUpdate(manager, pairwise);
	}

}
