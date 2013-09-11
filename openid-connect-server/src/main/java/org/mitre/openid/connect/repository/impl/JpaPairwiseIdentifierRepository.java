/**
 * 
 */
package org.mitre.openid.connect.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.openid.connect.model.PairwiseIdentifier;
import org.mitre.openid.connect.repository.PairwiseIdentifierRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.mitre.util.jpa.JpaUtil.getSingleResult;
import static org.mitre.util.jpa.JpaUtil.saveOrUpdate;

/**
 * @author jricher
 *
 */
@Repository
public class JpaPairwiseIdentifierRepository implements PairwiseIdentifierRepository {

	@PersistenceContext
	private EntityManager manager;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.PairwiseIdentifierRepository#getBySectorIdentifier(java.lang.String, java.lang.String)
	 */
	@Override
	public PairwiseIdentifier getBySectorIdentifier(String sub, String sectorIdentifierUri) {
		TypedQuery<PairwiseIdentifier> query = manager.createNamedQuery("PairwiseIdentifier.getBySectorIdentifier", PairwiseIdentifier.class);
		query.setParameter("sub", sub);
		query.setParameter("sectorIdentifier", sectorIdentifierUri);

		return getSingleResult(query.getResultList());
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.repository.PairwiseIdentifierRepository#save(org.mitre.openid.connect.model.PairwiseIdentifier)
	 */
	@Override
	@Transactional
	public void save(PairwiseIdentifier pairwise) {
		saveOrUpdate(pairwise.getId(), manager, pairwise);
	}

}
