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
import com.google.common.collect.Iterables;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.openid.connect.model.PairwiseIdentifier;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.repository.PairwiseIdentifierRepository;
import cz.muni.ics.openid.connect.service.PairwiseIdentiferService;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author jricher
 *
 */
@Service("uuidPairwiseIdentiferService")
@Slf4j
public class UUIDPairwiseIdentiferService implements PairwiseIdentiferService {

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
