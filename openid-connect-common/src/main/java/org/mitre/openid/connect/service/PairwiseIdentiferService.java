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
	 * @param userInfo
	 * @param client
	 * @return
	 */
    public String getIdentifier(UserInfo userInfo, ClientDetailsEntity client);

}
