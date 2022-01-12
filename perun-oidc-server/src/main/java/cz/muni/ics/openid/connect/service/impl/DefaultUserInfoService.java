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
package cz.muni.ics.openid.connect.service.impl;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.SavedUserAuthentication;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.model.enums.SubjectType;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.repository.UserInfoRepository;
import cz.muni.ics.openid.connect.service.PairwiseIdentiferService;
import cz.muni.ics.openid.connect.service.UserInfoService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;

/**
 * Implementation of the UserInfoService
 *
 * @author Michael Joseph Walsh, jricher
 *
 */
public class DefaultUserInfoService implements UserInfoService {

	@Autowired
	private UserInfoRepository userInfoRepository;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private PairwiseIdentiferService pairwiseIdentifierService;

	@Override
	public UserInfo get(String username, String clientId, Set<String> scope, SavedUserAuthentication userAuthentication) {
		return getByUsernameAndClientId(username, clientId);
	}

	@Override
	public UserInfo get(String username, String clientId, Set<String> scope, SAMLCredential samlCredential) {
		return getByUsernameAndClientId(username, clientId);
	}

	@Override
	public UserInfo get(String username, String clientId, Set<String> scope) {
		return getByUsernameAndClientId(username, clientId);
	}

	private UserInfo getByUsernameAndClientId(String username, String clientId) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
		if (client == null) {
			return null;
		}
		UserInfo userInfo = userInfoRepository.getByUsername(username);

		if (userInfo == null) {
			return  null;
		}

		if (SubjectType.PAIRWISE.equals(client.getSubjectType())) {
			String pairwiseSub = pairwiseIdentifierService.getIdentifier(userInfo, client);
			userInfo.setSub(pairwiseSub);
		}

		return userInfo;

	}

}
