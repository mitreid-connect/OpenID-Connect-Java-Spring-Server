/**
 * 
 */
package org.mitre.openid.connect.service.impl;

import java.util.UUID;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.PairwiseIdentifier;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.PairwiseIdentifierRepository;
import org.mitre.openid.connect.service.PairwiseIdentiferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

/**
 * @author jricher
 *
 */
@Service("uuidPairwiseIdentiferService")
public class UUIDPairwiseIdentiferService implements PairwiseIdentiferService {

	@Autowired
	private PairwiseIdentifierRepository pairwiseIdentifierRepository;
	
	@Override
    public String getIdentifier(UserInfo userInfo, ClientDetailsEntity client) {
		
		if (!Strings.isNullOrEmpty(client.getSectorIdentifierUri())) {
			// if there's a sector identifier, use that for the lookup
			PairwiseIdentifier pairwise = pairwiseIdentifierRepository.getBySectorIdentifier(userInfo.getSub(), client.getSectorIdentifierUri());

			if (pairwise == null) {
				// we don't have an identifier, need to make and save one
				
				pairwise = new PairwiseIdentifier();
				pairwise.setIdentifier(UUID.randomUUID().toString());
				pairwise.setUserSub(userInfo.getSub());
				pairwise.setSectorIdentifier(client.getSectorIdentifierUri());
				
				pairwiseIdentifierRepository.save(pairwise);
			}
			
			return pairwise.getIdentifier();
		} else {
			// if there's no sector identifier, use the client ID
			PairwiseIdentifier pairwise = pairwiseIdentifierRepository.getByClientId(userInfo.getSub(), client.getClientId());
			
			if (pairwise == null) {
				// we don't have an identifier, need to make and save one
				
				pairwise = new PairwiseIdentifier();
				pairwise.setIdentifier(UUID.randomUUID().toString());
				pairwise.setUserSub(userInfo.getSub());
				pairwise.setClientId(client.getClientId());
				
				pairwiseIdentifierRepository.save(pairwise);
			}
			
			return pairwise.getIdentifier();
		}
		
		
    }

}
