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

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.model.DefaultUserInfo;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.repository.UserInfoRepository;
import cz.muni.ics.openid.connect.service.PairwiseIdentiferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
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

	private String adminUsername = "username";
	private String regularUsername = "regular";
	private String adminSub = "adminSub12d3a1f34a2";
	private String regularSub = "regularSub652ha23b";

	private String pairwiseSub12 = "regularPairwise-12-31ijoef";
	private String pairwiseSub3 = "regularPairwise-3-1ojadsio";
	private String pairwiseSub4 = "regularPairwise-4-1ojadsio";

	private String publicClientId1 = "publicClient-1-313124";
	private String publicClientId2 = "publicClient-2-4109312";
	private String pairwiseClientId1 = "pairwiseClient-1-2312";
	private String pairwiseClientId2 = "pairwiseClient-2-324416";
	private String pairwiseClientId3 = "pairwiseClient-3-154157";
	private String pairwiseClientId4 = "pairwiseClient-4-4589723";

	private String sectorIdentifier1 = "https://sector-identifier-12/url";
	private String sectorIdentifier2 = "https://sector-identifier-12/url2";
	private String sectorIdentifier3 = "https://sector-identifier-3/url";




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
		publicClient2.setSubjectType(ClientDetailsEntity.SubjectType.PUBLIC);

		// pairwise set 1
		pairwiseClient1 = new ClientDetailsEntity();
		pairwiseClient1.setClientId(pairwiseClientId1);
		pairwiseClient1.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		pairwiseClient1.setSectorIdentifierUri(sectorIdentifier1);

		pairwiseClient2 = new ClientDetailsEntity();
		pairwiseClient2.setClientId(pairwiseClientId2);
		pairwiseClient2.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		pairwiseClient2.setSectorIdentifierUri(sectorIdentifier2);

		// pairwise set 2
		pairwiseClient3 = new ClientDetailsEntity();
		pairwiseClient3.setClientId(pairwiseClientId3);
		pairwiseClient3.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);
		pairwiseClient3.setSectorIdentifierUri(sectorIdentifier3);

		// pairwise with null sector
		pairwiseClient4 = new ClientDetailsEntity();
		pairwiseClient4.setClientId(pairwiseClientId4);
		pairwiseClient4.setSubjectType(ClientDetailsEntity.SubjectType.PAIRWISE);




	}

	/**
	 * Test loading an admin user, ensuring that the UserDetails object returned
	 * has both the ROLE_USER and ROLE_ADMIN authorities.
	 */
	@Test
	public void loadByUsername_admin_success() {
		Mockito.when(userInfoRepository.getByUsername(adminUsername)).thenReturn(userInfoAdmin);
		UserInfo user = service.getByUsername(adminUsername);
		assertEquals(user.getSub(), adminSub);
	}

	/**
	 * Test loading a regular, non-admin user, ensuring that the returned UserDetails
	 * object has ROLE_USER but *not* ROLE_ADMIN.
	 */
	@Test
	public void loadByUsername_regular_success() {

		Mockito.when(userInfoRepository.getByUsername(regularUsername)).thenReturn(userInfoRegular);
		UserInfo user = service.getByUsername(regularUsername);
		assertEquals(user.getSub(), regularSub);

	}

	/**
	 * If a user is not found, the loadByUsername method should throw an exception.
	 */
	@Test()
	public void loadByUsername_nullUser() {

		Mockito.when(userInfoRepository.getByUsername(adminUsername)).thenReturn(null);
		UserInfo user = service.getByUsername(adminUsername);

		assertNull(user);
	}

	/**
	 * Clients with public subs should always return the same sub
	 */
	@Test
	public void getByUsernameAndClientId_publicClients() {

		Mockito.when(clientDetailsEntityService.loadClientByClientId(publicClientId1)).thenReturn(publicClient1);
		Mockito.when(clientDetailsEntityService.loadClientByClientId(publicClientId2)).thenReturn(publicClient2);

		Mockito.when(userInfoRepository.getByUsername(regularUsername)).thenReturn(userInfoRegular);

		Mockito.verify(pairwiseIdentiferService, Mockito.never()).getIdentifier(Matchers.any(UserInfo.class), Matchers.any(ClientDetailsEntity.class));

		UserInfo user1 = service.getByUsernameAndClientId(regularUsername, publicClientId1);
		UserInfo user2 = service.getByUsernameAndClientId(regularUsername, publicClientId2);

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

		UserInfo user1 = service.getByUsernameAndClientId(regularUsername, pairwiseClientId1);
		UserInfo user2 = service.getByUsernameAndClientId(regularUsername, pairwiseClientId2);
		UserInfo user3 = service.getByUsernameAndClientId(regularUsername, pairwiseClientId3);
		UserInfo user4 = service.getByUsernameAndClientId(regularUsername, pairwiseClientId4);

		assertEquals(pairwiseSub12, user1.getSub());
		assertEquals(pairwiseSub12, user2.getSub());
		assertEquals(pairwiseSub3, user3.getSub());
		assertEquals(pairwiseSub4, user4.getSub());

	}



}
