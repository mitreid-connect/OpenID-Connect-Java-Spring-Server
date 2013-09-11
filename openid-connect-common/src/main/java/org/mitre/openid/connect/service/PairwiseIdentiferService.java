/**
 * 
 */
package org.mitre.openid.connect.service;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.UserInfo;

/**
 * @author jricher
 *
 */
public interface PairwiseIdentiferService {

	/**
	 * Calcualtes the pairwise identifier for the given userinfo object and client.
	 * 
	 * Returns 'null' if no identifer could be calculated.
	 * 
	 * @param userInfo
	 * @param client
	 * @return
	 */
    public String getIdentifier(UserInfo userInfo, ClientDetailsEntity client);

}
