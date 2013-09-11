/**
 * 
 */
package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.PairwiseIdentifier;

/**
 * @author jricher
 *
 */
public interface PairwiseIdentifierRepository {

	/**
	 * Get a pairwise identifier by its associated user subject and sector identifier.
	 * 
	 * @param sub
	 * @param sectorIdentifierUri
	 * @return
	 */
    public PairwiseIdentifier getBySectorIdentifier(String sub, String sectorIdentifierUri);

    /**
     * Save a pairwise identifier to the database.
     * 
     * @param pairwise
     */
    public void save(PairwiseIdentifier pairwise);
    
}
