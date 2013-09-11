/**
 * 
 */
package org.mitre.openid.connect.service.impl;

import java.util.Set;
import java.util.UUID;

import org.apache.http.client.utils.URIBuilder;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.PairwiseIdentifier;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.PairwiseIdentifierRepository;
import org.mitre.openid.connect.service.PairwiseIdentiferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

/**
 * @author jricher
 *
 */
@Service("uuidPairwiseIdentiferService")
public class UUIDPairwiseIdentiferService implements PairwiseIdentiferService {

	private static Logger logger = LoggerFactory.getLogger(UUIDPairwiseIdentiferService.class);
	
	@Autowired
	private PairwiseIdentifierRepository pairwiseIdentifierRepository;
	
	@Override
    public String getIdentifier(UserInfo userInfo, ClientDetailsEntity client) {
		
		String sectorIdentifier = null;
		
		if (!Strings.isNullOrEmpty(client.getSectorIdentifierUri())) {
			UriComponents uri = UriComponentsBuilder.fromUriString(client.getSectorIdentifierUri()).build();
			sectorIdentifier = uri.getHost(); // calculate based on the host component only
		} else {
			Set<String> redirectUris = client.getRedirectUris();			
			UriComponents uri = UriComponentsBuilder.fromUriString(Iterables.getOnlyElement(redirectUris)).build();
			sectorIdentifier = uri.getHost(); // calculate based on the host of the only redirect URI
		}
		
		if (sectorIdentifier != null) {
			// if there's a sector identifier, use that for the lookup
			PairwiseIdentifier pairwise = pairwiseIdentifierRepository.getBySectorIdentifier(userInfo.getSub(), sectorIdentifier);

			if (pairwise == null) {
				// we don't have an identifier, need to make and save one
				
				pairwise = new PairwiseIdentifier();
				pairwise.setIdentifier(UUID.randomUUID().toString());
				pairwise.setUserSub(userInfo.getSub());
				pairwise.setSectorIdentifier(sectorIdentifier);
				
				pairwiseIdentifierRepository.save(pairwise);
			}
			
			return pairwise.getIdentifier();
		} else {
			
			return null;
		}
    }

}
