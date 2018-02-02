/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.openid.connect.service.impl;

import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.PairwiseIdentifier;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.PairwiseIdentifierRepository;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author jricher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUUIDPairwiseIdentiferService {

	@Mock
	private PairwiseIdentifierRepository pairwiseIdentifierRepository;

	@InjectMocks
	private UUIDPairwiseIdentiferService service;

	private UserInfo userInfoRegular;

	private ClientDetailsEntity pairwiseClient1;
	private ClientDetailsEntity pairwiseClient2;
	private ClientDetailsEntity pairwiseClient3;
	private ClientDetailsEntity pairwiseClient4;
	private ClientDetailsEntity pairwiseClient5;

	private String regularUsername = "regular";
	private String regularSub = "regularSub652ha23b";
	private String pairwiseSub = "pairwise-12-regular-user";

	private String pairwiseClientId1 = "pairwiseClient-1-2312";
	private String pairwiseClientId2 = "pairwiseClient-2-324416";
	private String pairwiseClientId3 = "pairwiseClient-3-154157";
	private String pairwiseClientId4 = "pairwiseClient-4-4589723";
	private String pairwiseClientId5 = "pairwiseClient-5-34908713";

	private String sectorHost12 = "sector-identifier-12";
	private String sectorHost3 = "sector-identifier-3";
	private String clientHost4 = "client-redirect-4";
	private String clientHost5 = "client-redirect-5";

	private String sectorIdentifier1 = "https://" + sectorHost12 + "/url";
	private String sectorIdentifier2 = "https://" + sectorHost12 + "/url2";
	private String sectorIdentifier3 = "https://" + sectorHost3 + "/url";

	private Set<String> pairwiseClient3RedirectUris = ImmutableSet.of("https://" + sectorHost3 + "/oauth", "https://" + sectorHost3 + "/other");
	private Set<String> pairwiseClient4RedirectUris = ImmutableSet.of("https://" + clientHost4 + "/oauth");
	private Set<String> pairwiseClient5RedirectUris = ImmutableSet.of("https://" + clientHost5 + "/oauth", "https://" + clientHost5 + "/other");

	private PairwiseIdentifier savedPairwiseIdentifier;

	@Before
	public void prepare() {
		userInfoRegular = new DefaultUserInfo();
		userInfoRegular.setPreferredUsername(regularUsername);
		userInfoRegular.setSub(regularSub);

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
		pairwiseClient3.setRedirectUris(pairwiseClient3RedirectUris);

		// pairwise with null sector
		pairwiseClient4 = new ClientDetailsEntity();
		pairwiseClient4.setClientId(pairwiseClientId4);
		pairwiseClient4.setSubjectType(SubjectType.PAIRWISE);
		pairwiseClient4.setRedirectUris(pairwiseClient4RedirectUris);

		// pairwise with multiple redirects and no sector (error)
		pairwiseClient5 = new ClientDetailsEntity();
		pairwiseClient5.setClientId(pairwiseClientId5);
		pairwiseClient5.setSubjectType(SubjectType.PAIRWISE);
		pairwiseClient5.setRedirectUris(pairwiseClient5RedirectUris);

		// saved pairwise identifier from repository
		savedPairwiseIdentifier = new PairwiseIdentifier();
		savedPairwiseIdentifier.setUserSub(regularSub);
		savedPairwiseIdentifier.setIdentifier(pairwiseSub);
		savedPairwiseIdentifier.setSectorIdentifier(sectorHost12);

	}

	/**
	 * Test method for {@link org.mitre.openid.connect.service.impl.UUIDPairwiseIdentiferService#getIdentifier(org.mitre.openid.connect.model.UserInfo, org.mitre.oauth2.model.ClientDetailsEntity)}.
	 */
	@Test
	public void testGetIdentifier_existingEqual() {

		Mockito.when(pairwiseIdentifierRepository.getBySectorIdentifier(regularSub, sectorHost12)).thenReturn(savedPairwiseIdentifier);

		String pairwise1 = service.getIdentifier(userInfoRegular, pairwiseClient1);
		String pairwise2 = service.getIdentifier(userInfoRegular, pairwiseClient2);

		assertEquals(pairwiseSub, pairwise1);
		assertEquals(pairwiseSub, pairwise2);

	}

	@Test
	public void testGetIdentifier_newEqual() {

		String pairwise1 = service.getIdentifier(userInfoRegular, pairwiseClient1);
		Mockito.verify(pairwiseIdentifierRepository, Mockito.atLeast(1)).save(Matchers.any(PairwiseIdentifier.class));

		PairwiseIdentifier pairwiseId = new PairwiseIdentifier();
		pairwiseId.setUserSub(regularSub);
		pairwiseId.setIdentifier(pairwise1);
		pairwiseId.setSectorIdentifier(sectorHost12);

		Mockito.when(pairwiseIdentifierRepository.getBySectorIdentifier(regularSub, sectorHost12)).thenReturn(pairwiseId);

		String pairwise2 = service.getIdentifier(userInfoRegular, pairwiseClient2);

		assertNotSame(pairwiseSub, pairwise1);
		assertNotSame(pairwiseSub, pairwise2);

		assertEquals(pairwise1, pairwise2);

		// see if the pairwise id's are actual UUIDs
		UUID uudi1 = UUID.fromString(pairwise1);
		UUID uuid2 = UUID.fromString(pairwise2);

	}

	@Test
	public void testGetIdentifer_unique() {
		String pairwise1 = service.getIdentifier(userInfoRegular, pairwiseClient1);
		String pairwise3 = service.getIdentifier(userInfoRegular, pairwiseClient3);
		String pairwise4 = service.getIdentifier(userInfoRegular, pairwiseClient4);

		// make sure nothing's equal
		assertNotSame(pairwise1, pairwise3);
		assertNotSame(pairwise1, pairwise4);
		assertNotSame(pairwise3, pairwise4);

		// see if the pairwise id's are actual UUIDs
		UUID uudi1 = UUID.fromString(pairwise1);
		UUID uudi3 = UUID.fromString(pairwise3);
		UUID uudi4 = UUID.fromString(pairwise4);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetIdentifier_multipleRedirectError() {
		String pairwise5 = service.getIdentifier(userInfoRegular, pairwiseClient5);
	}

}
