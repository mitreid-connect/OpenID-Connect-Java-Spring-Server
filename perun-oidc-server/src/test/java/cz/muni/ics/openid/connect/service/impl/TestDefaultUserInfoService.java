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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.enums.SubjectType;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.model.DefaultUserInfo;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.repository.UserInfoRepository;
import cz.muni.ics.openid.connect.service.PairwiseIdentiferService;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author jricher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultUserInfoService {
	@InjectMocks
	private DefaultUserInfoService service = new DefaultUserInfoService();

	@Mock
	private UserInfoRepository userInfoRepository;

	@Mock
	private ClientDetailsEntityService clientDetailsEntityService;

	@Mock
	private PairwiseIdentiferService pairwiseIdentiferService;

	private UserInfo userInfoAdmin;
	private UserInfo userInfoRegular;

	private ClientDetailsEntity publicClient1;
	private ClientDetailsEntity publicClient2;
	private ClientDetailsEntity pairwiseClient1;
	private ClientDetailsEntity pairwiseClient2;
	private ClientDetailsEntity pairwiseClient3;
	private ClientDetailsEntity pairwiseClient4;

	private final String adminUsername = "username";
	private final String regularUsername = "regular";
	private final String adminSub = "adminSub12d3a1f34a2";
	private final String regularSub = "regularSub652ha23b";

	private final String pairwiseSub12 = "regularPairwise-12-31ijoef";
	private final String pairwiseSub3 = "regularPairwise-3-1ojadsio";
	private final String pairwiseSub4 = "regularPairwise-4-1ojadsio";

	private final String publicClientId1 = "publicClient-1-313124";
	private final String publicClientId2 = "publicClient-2-4109312";
	private final String pairwiseClientId1 = "pairwiseClient-1-2312";
	private final String pairwiseClientId2 = "pairwiseClient-2-324416";
	private final String pairwiseClientId3 = "pairwiseClient-3-154157";
	private final String pairwiseClientId4 = "pairwiseClient-4-4589723";

	private final String sectorIdentifier1 = "https://sector-identifier-12/url";
	private final String sectorIdentifier2 = "https://sector-identifier-12/url2";
	private final String sectorIdentifier3 = "https://sector-identifier-3/url";




	/**
	 * Initialize the service and the mocked repository.
	 * Initialize 2 users, one of them an admin, for use in unit tests.
	 */
	@Before
	public void prepare() {


		userInfoAdmin = new DefaultUserInfo();
		userInfoAdmin.setPreferredUsername(adminUsername);
		userInfoAdmin.setSub(adminSub);

		userInfoRegular = new DefaultUserInfo();
		userInfoRegular.setPreferredUsername(regularUsername);
		userInfoRegular.setSub(regularSub);

		publicClient1 = new ClientDetailsEntity();
		publicClient1.setClientId(publicClientId1);

		publicClient2 = new ClientDetailsEntity();
		publicClient2.setClientId(publicClientId2);
		publicClient2.setSubjectType(SubjectType.PUBLIC);

		// pairwise set 1
		pairwiseClient1 = new ClientDetailsEntity();
		pairwiseClient1.setClientId(pairwiseClientId1);
		pairwiseClient1.setSubjectType(SubjectType.PAIRWISE);
		pairwiseClient1.setSectorIdentifierUri(sectorIdentifier1);

		pairwiseClient2 = new ClientDetailsEntity();
		pairwiseClient2.setClientId(pairwiseClientId2);
		pairwiseClient2.setSubjectType(SubjectType.PAIRWISE);
		pairwiseClient2.setSectorIdentifierUri(sectorIdentifier2);

		// pairwise set 2
		pairwiseClient3 = new ClientDetailsEntity();
		pairwiseClient3.setClientId(pairwiseClientId3);
		pairwiseClient3.setSubjectType(SubjectType.PAIRWISE);
		pairwiseClient3.setSectorIdentifierUri(sectorIdentifier3);

		// pairwise with null sector
		pairwiseClient4 = new ClientDetailsEntity();
		pairwiseClient4.setClientId(pairwiseClientId4);
		pairwiseClient4.setSubjectType(SubjectType.PAIRWISE);




	}

	/**
	 * Clients with public subs should always return the same sub
	 */
	@Test
	public void getByUsernameAndClientId_publicClients() {

		Mockito.when(clientDetailsEntityService.loadClientByClientId(publicClientId1)).thenReturn(publicClient1);
		Mockito.when(clientDetailsEntityService.loadClientByClientId(publicClientId2)).thenReturn(publicClient2);

		Mockito.when(userInfoRepository.getByUsername(regularUsername)).thenReturn(userInfoRegular);

		Mockito.verify(pairwiseIdentiferService, Mockito.never()).getIdentifier(any(UserInfo.class), any(ClientDetailsEntity.class));

		UserInfo user1 = service.get(regularUsername, publicClientId1, new HashSet<>());
		UserInfo user2 = service.get(regularUsername, publicClientId2, new HashSet<>());

		assertEquals(regularSub, user1.getSub());
		assertEquals(regularSub, user2.getSub());
	}

	/**
	 * Clients with pairwise subs should be grouped by the sector URI
	 */
	@Test
	public void getByUsernameAndClientId_pairwiseClients() {

		Mockito.when(clientDetailsEntityService.loadClientByClientId(pairwiseClientId1)).thenReturn(pairwiseClient1);
		Mockito.when(clientDetailsEntityService.loadClientByClientId(pairwiseClientId2)).thenReturn(pairwiseClient2);
		Mockito.when(clientDetailsEntityService.loadClientByClientId(pairwiseClientId3)).thenReturn(pairwiseClient3);
		Mockito.when(clientDetailsEntityService.loadClientByClientId(pairwiseClientId4)).thenReturn(pairwiseClient4);

		Mockito.when(userInfoRepository.getByUsername(regularUsername)).thenAnswer(new Answer<UserInfo>() {
			@Override
			public UserInfo answer(InvocationOnMock invocation) throws Throwable {
				UserInfo userInfo = new DefaultUserInfo();
				userInfo.setPreferredUsername(regularUsername);
				userInfo.setSub(regularSub);

				return userInfo;
			}
		});

		Mockito.when(pairwiseIdentiferService.getIdentifier(userInfoRegular, pairwiseClient1)).thenReturn(pairwiseSub12);
		Mockito.when(pairwiseIdentiferService.getIdentifier(userInfoRegular, pairwiseClient2)).thenReturn(pairwiseSub12);
		Mockito.when(pairwiseIdentiferService.getIdentifier(userInfoRegular, pairwiseClient3)).thenReturn(pairwiseSub3);
		Mockito.when(pairwiseIdentiferService.getIdentifier(userInfoRegular, pairwiseClient4)).thenReturn(pairwiseSub4);

		UserInfo user1 = service.get(regularUsername, pairwiseClientId1, new HashSet<>());
		UserInfo user2 = service.get(regularUsername, pairwiseClientId2, new HashSet<>());
		UserInfo user3 = service.get(regularUsername, pairwiseClientId3, new HashSet<>());
		UserInfo user4 = service.get(regularUsername, pairwiseClientId4, new HashSet<>());

		assertEquals(pairwiseSub12, user1.getSub());
		assertEquals(pairwiseSub12, user2.getSub());
		assertEquals(pairwiseSub3, user3.getSub());
		assertEquals(pairwiseSub4, user4.getSub());

	}



}
