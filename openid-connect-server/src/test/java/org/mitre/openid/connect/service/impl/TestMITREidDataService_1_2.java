/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.openid.connect.service.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.BlacklistedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.repository.BlacklistedSiteRepository;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.mitre.openid.connect.service.MITREidDataService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.nimbusds.jwt.JWTParser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class TestMITREidDataService_1_2 {

	private static Logger logger = LoggerFactory.getLogger(TestMITREidDataService_1_2.class);

	@Mock
	private OAuth2ClientRepository clientRepository;
	@Mock
	private ApprovedSiteRepository approvedSiteRepository;
	@Mock
	private WhitelistedSiteRepository wlSiteRepository;
	@Mock
	private BlacklistedSiteRepository blSiteRepository;
	@Mock
	private AuthenticationHolderRepository authHolderRepository;
	@Mock
	private OAuth2TokenRepository tokenRepository;
	@Mock
	private SystemScopeRepository sysScopeRepository;

	@Captor
	private ArgumentCaptor<OAuth2RefreshTokenEntity> capturedRefreshTokens;
	@Captor
	private ArgumentCaptor<OAuth2AccessTokenEntity> capturedAccessTokens;
	@Captor
	private ArgumentCaptor<ClientDetailsEntity> capturedClients;
	@Captor
	private ArgumentCaptor<BlacklistedSite> capturedBlacklistedSites;
	@Captor
	private ArgumentCaptor<WhitelistedSite> capturedWhitelistedSites;
	@Captor
	private ArgumentCaptor<ApprovedSite> capturedApprovedSites;
	@Captor
	private ArgumentCaptor<AuthenticationHolderEntity> capturedAuthHolders;
	@Captor
	private ArgumentCaptor<SystemScope> capturedScope;

	@InjectMocks
	private MITREidDataService_1_2 dataService;
	private DateFormatter formatter;

	@Before
	public void prepare() {
		formatter = new DateFormatter();
		formatter.setIso(ISO.DATE_TIME);

		Mockito.reset(clientRepository, approvedSiteRepository, authHolderRepository, tokenRepository, sysScopeRepository, wlSiteRepository, blSiteRepository);
	}

	private class refreshTokenIdComparator implements Comparator<OAuth2RefreshTokenEntity>  {
		@Override
		public int compare(OAuth2RefreshTokenEntity entity1, OAuth2RefreshTokenEntity entity2) {
			return entity1.getId().compareTo(entity2.getId());
		}
	}


	@Test
	public void testImportRefreshTokens() throws IOException, ParseException {
		String expiration1 = "2014-09-10T22:49:44.090+0000";
		Date expirationDate1 = formatter.parse(expiration1, Locale.ENGLISH);

		ClientDetailsEntity mockedClient1 = mock(ClientDetailsEntity.class);
		when(mockedClient1.getClientId()).thenReturn("mocked_client_1");

		AuthenticationHolderEntity mockedAuthHolder1 = mock(AuthenticationHolderEntity.class);
		when(mockedAuthHolder1.getId()).thenReturn(1L);

		OAuth2RefreshTokenEntity token1 = new OAuth2RefreshTokenEntity();
		token1.setId(1L);
		token1.setClient(mockedClient1);
		token1.setExpiration(expirationDate1);
		token1.setJwt(JWTParser.parse("eyJhbGciOiJub25lIn0.eyJqdGkiOiJmOTg4OWQyOS0xMTk1LTQ4ODEtODgwZC1lZjVlYzAwY2Y4NDIifQ."));
		token1.setAuthenticationHolder(mockedAuthHolder1);

		String expiration2 = "2015-01-07T18:31:50.079+0000";
		Date expirationDate2 = formatter.parse(expiration2, Locale.ENGLISH);

		ClientDetailsEntity mockedClient2 = mock(ClientDetailsEntity.class);
		when(mockedClient2.getClientId()).thenReturn("mocked_client_2");

		AuthenticationHolderEntity mockedAuthHolder2 = mock(AuthenticationHolderEntity.class);
		when(mockedAuthHolder2.getId()).thenReturn(2L);

		OAuth2RefreshTokenEntity token2 = new OAuth2RefreshTokenEntity();
		token2.setId(2L);
		token2.setClient(mockedClient2);
		token2.setExpiration(expirationDate2);
		token2.setJwt(JWTParser.parse("eyJhbGciOiJub25lIn0.eyJqdGkiOiJlYmEyYjc3My0xNjAzLTRmNDAtOWQ3MS1hMGIxZDg1OWE2MDAifQ."));
		token2.setAuthenticationHolder(mockedAuthHolder2);

		String configJson = "{" +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [" +

				"{\"id\":1,\"clientId\":\"mocked_client_1\",\"expiration\":\"2014-09-10T22:49:44.090+0000\","
				+ "\"authenticationHolderId\":1,\"value\":\"eyJhbGciOiJub25lIn0.eyJqdGkiOiJmOTg4OWQyOS0xMTk1LTQ4ODEtODgwZC1lZjVlYzAwY2Y4NDIifQ.\"}," +
				"{\"id\":2,\"clientId\":\"mocked_client_2\",\"expiration\":\"2015-01-07T18:31:50.079+0000\","
				+ "\"authenticationHolderId\":2,\"value\":\"eyJhbGciOiJub25lIn0.eyJqdGkiOiJlYmEyYjc3My0xNjAzLTRmNDAtOWQ3MS1hMGIxZDg1OWE2MDAifQ.\"}" +

				"  ]" +
				"}";

		logger.debug(configJson);
		JsonReader reader = new JsonReader(new StringReader(configJson));

		final Map<Long, OAuth2RefreshTokenEntity> fakeDb = new HashMap<>();
		when(tokenRepository.saveRefreshToken(isA(OAuth2RefreshTokenEntity.class))).thenAnswer(new Answer<OAuth2RefreshTokenEntity>() {
			Long id = 332L;
			@Override
			public OAuth2RefreshTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				OAuth2RefreshTokenEntity _token = (OAuth2RefreshTokenEntity) invocation.getArguments()[0];
				if(_token.getId() == null) {
					_token.setId(id++);
				}
				fakeDb.put(_token.getId(), _token);
				return _token;
			}
		});
		when(tokenRepository.getRefreshTokenById(anyLong())).thenAnswer(new Answer<OAuth2RefreshTokenEntity>() {
			@Override
			public OAuth2RefreshTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				Long _id = (Long) invocation.getArguments()[0];
				return fakeDb.get(_id);
			}
		});
		when(clientRepository.getClientByClientId(anyString())).thenAnswer(new Answer<ClientDetailsEntity>() {
			@Override
			public ClientDetailsEntity answer(InvocationOnMock invocation) throws Throwable {
				String _clientId = (String) invocation.getArguments()[0];
				ClientDetailsEntity _client = mock(ClientDetailsEntity.class);
				when(_client.getClientId()).thenReturn(_clientId);
				return _client;
			}
		});
		when(authHolderRepository.getById(isNull(Long.class))).thenAnswer(new Answer<AuthenticationHolderEntity>() {
			Long id = 131L;
			@Override
			public AuthenticationHolderEntity answer(InvocationOnMock invocation) throws Throwable {
				AuthenticationHolderEntity _auth = mock(AuthenticationHolderEntity.class);
				when(_auth.getId()).thenReturn(id);
				id++;
				return _auth;
			}
		});
		dataService.importData(reader);
		//2 times for token, 2 times to update client, 2 times to update authHolder
		verify(tokenRepository, times(6)).saveRefreshToken(capturedRefreshTokens.capture());

		List<OAuth2RefreshTokenEntity> savedRefreshTokens = new ArrayList(fakeDb.values()); //capturedRefreshTokens.getAllValues();
		Collections.sort(savedRefreshTokens, new refreshTokenIdComparator());

		assertThat(savedRefreshTokens.size(), is(2));

		assertThat(savedRefreshTokens.get(0).getClient().getClientId(), equalTo(token1.getClient().getClientId()));
		assertThat(savedRefreshTokens.get(0).getExpiration(), equalTo(token1.getExpiration()));
		assertThat(savedRefreshTokens.get(0).getValue(), equalTo(token1.getValue()));

		assertThat(savedRefreshTokens.get(1).getClient().getClientId(), equalTo(token2.getClient().getClientId()));
		assertThat(savedRefreshTokens.get(1).getExpiration(), equalTo(token2.getExpiration()));
		assertThat(savedRefreshTokens.get(1).getValue(), equalTo(token2.getValue()));
	}

	private class accessTokenIdComparator implements Comparator<OAuth2AccessTokenEntity>  {
		@Override
		public int compare(OAuth2AccessTokenEntity entity1, OAuth2AccessTokenEntity entity2) {
			return entity1.getId().compareTo(entity2.getId());
		}
	}

	@Test
	public void testImportAccessTokens() throws IOException, ParseException {
		String expiration1 = "2014-09-10T22:49:44.090+0000";
		Date expirationDate1 = formatter.parse(expiration1, Locale.ENGLISH);

		ClientDetailsEntity mockedClient1 = mock(ClientDetailsEntity.class);
		when(mockedClient1.getClientId()).thenReturn("mocked_client_1");

		AuthenticationHolderEntity mockedAuthHolder1 = mock(AuthenticationHolderEntity.class);
		when(mockedAuthHolder1.getId()).thenReturn(1L);

		OAuth2AccessTokenEntity token1 = new OAuth2AccessTokenEntity();
		token1.setId(1L);
		token1.setClient(mockedClient1);
		token1.setExpiration(expirationDate1);
		token1.setJwt(JWTParser.parse("eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3ODk5NjgsInN1YiI6IjkwMzQyLkFTREZKV0ZBIiwiYXRfaGFzaCI6InptTmt1QmNRSmNYQktNaVpFODZqY0EiLCJhdWQiOlsiY2xpZW50Il0sImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDgwXC9vcGVuaWQtY29ubmVjdC1zZXJ2ZXItd2ViYXBwXC8iLCJpYXQiOjE0MTI3ODkzNjh9.xkEJ9IMXpH7qybWXomfq9WOOlpGYnrvGPgey9UQ4GLzbQx7JC0XgJK83PmrmBZosvFPCmota7FzI_BtwoZLgAZfFiH6w3WIlxuogoH-TxmYbxEpTHoTsszZppkq9mNgOlArV4jrR9y3TPo4MovsH71dDhS_ck-CvAlJunHlqhs0"));
		token1.setAuthenticationHolder(mockedAuthHolder1);
		token1.setScope(ImmutableSet.of("id-token"));
		token1.setTokenType("Bearer");

		String expiration2 = "2015-01-07T18:31:50.079+0000";
		Date expirationDate2 = formatter.parse(expiration2, Locale.ENGLISH);

		ClientDetailsEntity mockedClient2 = mock(ClientDetailsEntity.class);
		when(mockedClient2.getClientId()).thenReturn("mocked_client_2");

		AuthenticationHolderEntity mockedAuthHolder2 = mock(AuthenticationHolderEntity.class);
		when(mockedAuthHolder2.getId()).thenReturn(2L);

		OAuth2RefreshTokenEntity mockRefreshToken2 = mock(OAuth2RefreshTokenEntity.class);
		when(mockRefreshToken2.getId()).thenReturn(1L);

		OAuth2AccessTokenEntity token2 = new OAuth2AccessTokenEntity();
		token2.setId(2L);
		token2.setClient(mockedClient2);
		token2.setExpiration(expirationDate2);
		token2.setJwt(JWTParser.parse("eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3OTI5NjgsImF1ZCI6WyJjbGllbnQiXSwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODBcL29wZW5pZC1jb25uZWN0LXNlcnZlci13ZWJhcHBcLyIsImp0aSI6IjBmZGE5ZmRiLTYyYzItNGIzZS05OTdiLWU0M2VhMDUwMzNiOSIsImlhdCI6MTQxMjc4OTM2OH0.xgaVpRLYE5MzbgXfE0tZt823tjAm6Oh3_kdR1P2I9jRLR6gnTlBQFlYi3Y_0pWNnZSerbAE8Tn6SJHZ9k-curVG0-ByKichV7CNvgsE5X_2wpEaUzejvKf8eZ-BammRY-ie6yxSkAarcUGMvGGOLbkFcz5CtrBpZhfd75J49BIQ"));
		token2.setAuthenticationHolder(mockedAuthHolder2);
		token2.setRefreshToken(mockRefreshToken2);
		token2.setScope(ImmutableSet.of("openid", "offline_access", "email", "profile"));
		token2.setTokenType("Bearer");

		String configJson = "{" +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [], " +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [" +

				"{\"id\":1,\"clientId\":\"mocked_client_1\",\"expiration\":\"2014-09-10T22:49:44.090+0000\","
				+ "\"refreshTokenId\":null,\"idTokenId\":null,\"scope\":[\"id-token\"],\"type\":\"Bearer\","
				+ "\"authenticationHolderId\":1,\"value\":\"eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3ODk5NjgsInN1YiI6IjkwMzQyLkFTREZKV0ZBIiwiYXRfaGFzaCI6InptTmt1QmNRSmNYQktNaVpFODZqY0EiLCJhdWQiOlsiY2xpZW50Il0sImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDgwXC9vcGVuaWQtY29ubmVjdC1zZXJ2ZXItd2ViYXBwXC8iLCJpYXQiOjE0MTI3ODkzNjh9.xkEJ9IMXpH7qybWXomfq9WOOlpGYnrvGPgey9UQ4GLzbQx7JC0XgJK83PmrmBZosvFPCmota7FzI_BtwoZLgAZfFiH6w3WIlxuogoH-TxmYbxEpTHoTsszZppkq9mNgOlArV4jrR9y3TPo4MovsH71dDhS_ck-CvAlJunHlqhs0\"}," +
				"{\"id\":2,\"clientId\":\"mocked_client_2\",\"expiration\":\"2015-01-07T18:31:50.079+0000\","
				+ "\"refreshTokenId\":1,\"idTokenId\":1,\"scope\":[\"openid\",\"offline_access\",\"email\",\"profile\"],\"type\":\"Bearer\","
				+ "\"authenticationHolderId\":2,\"value\":\"eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3OTI5NjgsImF1ZCI6WyJjbGllbnQiXSwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODBcL29wZW5pZC1jb25uZWN0LXNlcnZlci13ZWJhcHBcLyIsImp0aSI6IjBmZGE5ZmRiLTYyYzItNGIzZS05OTdiLWU0M2VhMDUwMzNiOSIsImlhdCI6MTQxMjc4OTM2OH0.xgaVpRLYE5MzbgXfE0tZt823tjAm6Oh3_kdR1P2I9jRLR6gnTlBQFlYi3Y_0pWNnZSerbAE8Tn6SJHZ9k-curVG0-ByKichV7CNvgsE5X_2wpEaUzejvKf8eZ-BammRY-ie6yxSkAarcUGMvGGOLbkFcz5CtrBpZhfd75J49BIQ\"}" +

				"  ]" +
				"}";


		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));

		final Map<Long, OAuth2AccessTokenEntity> fakeDb = new HashMap<>();
		when(tokenRepository.saveAccessToken(isA(OAuth2AccessTokenEntity.class))).thenAnswer(new Answer<OAuth2AccessTokenEntity>() {
			Long id = 324L;
			@Override
			public OAuth2AccessTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				OAuth2AccessTokenEntity _token = (OAuth2AccessTokenEntity) invocation.getArguments()[0];
				if(_token.getId() == null) {
					_token.setId(id++);
				}
				fakeDb.put(_token.getId(), _token);
				return _token;
			}
		});
		when(tokenRepository.getAccessTokenById(anyLong())).thenAnswer(new Answer<OAuth2AccessTokenEntity>() {
			@Override
			public OAuth2AccessTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				Long _id = (Long) invocation.getArguments()[0];
				return fakeDb.get(_id);
			}
		});
		when(clientRepository.getClientByClientId(anyString())).thenAnswer(new Answer<ClientDetailsEntity>() {
			@Override
			public ClientDetailsEntity answer(InvocationOnMock invocation) throws Throwable {
				String _clientId = (String) invocation.getArguments()[0];
				ClientDetailsEntity _client = mock(ClientDetailsEntity.class);
				when(_client.getClientId()).thenReturn(_clientId);
				return _client;
			}
		});
		when(authHolderRepository.getById(isNull(Long.class))).thenAnswer(new Answer<AuthenticationHolderEntity>() {
			Long id = 133L;
			@Override
			public AuthenticationHolderEntity answer(InvocationOnMock invocation) throws Throwable {
				AuthenticationHolderEntity _auth = mock(AuthenticationHolderEntity.class);
				when(_auth.getId()).thenReturn(id);
				id++;
				return _auth;
			}
		});
		dataService.importData(reader);
		//2 times for token, 2 times to update client, 2 times to update authHolder, 1 times to update refresh token
		verify(tokenRepository, times(7)).saveAccessToken(capturedAccessTokens.capture());

		List<OAuth2AccessTokenEntity> savedAccessTokens = new ArrayList(fakeDb.values()); //capturedAccessTokens.getAllValues();
		Collections.sort(savedAccessTokens, new accessTokenIdComparator());

		assertThat(savedAccessTokens.size(), is(2));

		assertThat(savedAccessTokens.get(0).getClient().getClientId(), equalTo(token1.getClient().getClientId()));
		assertThat(savedAccessTokens.get(0).getExpiration(), equalTo(token1.getExpiration()));
		assertThat(savedAccessTokens.get(0).getValue(), equalTo(token1.getValue()));

		assertThat(savedAccessTokens.get(1).getClient().getClientId(), equalTo(token2.getClient().getClientId()));
		assertThat(savedAccessTokens.get(1).getExpiration(), equalTo(token2.getExpiration()));
		assertThat(savedAccessTokens.get(1).getValue(), equalTo(token2.getValue()));
	}

	@Test
	public void testImportClients() throws IOException {
		ClientDetailsEntity client1 = new ClientDetailsEntity();
		client1.setId(1L);
		client1.setAccessTokenValiditySeconds(3600);
		client1.setClientId("client1");
		client1.setClientSecret("clientsecret1");
		client1.setRedirectUris(ImmutableSet.of("http://foo.com/"));
		client1.setScope(ImmutableSet.of("foo", "bar", "baz", "dolphin"));
		client1.setGrantTypes(ImmutableSet.of("implicit", "authorization_code", "urn:ietf:params:oauth:grant_type:redelegate", "refresh_token"));
		client1.setAllowIntrospection(true);

		ClientDetailsEntity client2 = new ClientDetailsEntity();
		client2.setId(2L);
		client2.setAccessTokenValiditySeconds(3600);
		client2.setClientId("client2");
		client2.setClientSecret("clientsecret2");
		client2.setRedirectUris(ImmutableSet.of("http://bar.baz.com/"));
		client2.setScope(ImmutableSet.of("foo", "dolphin", "electric-wombat"));
		client2.setGrantTypes(ImmutableSet.of("client_credentials", "urn:ietf:params:oauth:grant_type:redelegate"));
		client2.setAllowIntrospection(false);

		String configJson = "{" +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [], " +
				"\"" + MITREidDataService.CLIENTS + "\": [" +

				"{\"id\":1,\"accessTokenValiditySeconds\":3600,\"clientId\":\"client1\",\"secret\":\"clientsecret1\","
				+ "\"redirectUris\":[\"http://foo.com/\"],"
				+ "\"scope\":[\"foo\",\"bar\",\"baz\",\"dolphin\"],"
				+ "\"grantTypes\":[\"implicit\",\"authorization_code\",\"urn:ietf:params:oauth:grant_type:redelegate\",\"refresh_token\"],"
				+ "\"allowIntrospection\":true}," +
				"{\"id\":2,\"accessTokenValiditySeconds\":3600,\"clientId\":\"client2\",\"secret\":\"clientsecret2\","
				+ "\"redirectUris\":[\"http://bar.baz.com/\"],"
				+ "\"scope\":[\"foo\",\"dolphin\",\"electric-wombat\"],"
				+ "\"grantTypes\":[\"client_credentials\",\"urn:ietf:params:oauth:grant_type:redelegate\"],"
				+ "\"allowIntrospection\":false}" +

				"  ]" +
				"}";

		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));

		dataService.importData(reader);
		verify(clientRepository, times(2)).saveClient(capturedClients.capture());

		List<ClientDetailsEntity> savedClients = capturedClients.getAllValues();

		assertThat(savedClients.size(), is(2));

		assertThat(savedClients.get(0).getAccessTokenValiditySeconds(), equalTo(client1.getAccessTokenValiditySeconds()));
		assertThat(savedClients.get(0).getClientId(), equalTo(client1.getClientId()));
		assertThat(savedClients.get(0).getClientSecret(), equalTo(client1.getClientSecret()));
		assertThat(savedClients.get(0).getRedirectUris(), equalTo(client1.getRedirectUris()));
		assertThat(savedClients.get(0).getScope(), equalTo(client1.getScope()));
		assertThat(savedClients.get(0).getGrantTypes(), equalTo(client1.getGrantTypes()));
		assertThat(savedClients.get(0).isAllowIntrospection(), equalTo(client1.isAllowIntrospection()));

		assertThat(savedClients.get(1).getAccessTokenValiditySeconds(), equalTo(client2.getAccessTokenValiditySeconds()));
		assertThat(savedClients.get(1).getClientId(), equalTo(client2.getClientId()));
		assertThat(savedClients.get(1).getClientSecret(), equalTo(client2.getClientSecret()));
		assertThat(savedClients.get(1).getRedirectUris(), equalTo(client2.getRedirectUris()));
		assertThat(savedClients.get(1).getScope(), equalTo(client2.getScope()));
		assertThat(savedClients.get(1).getGrantTypes(), equalTo(client2.getGrantTypes()));
		assertThat(savedClients.get(1).isAllowIntrospection(), equalTo(client2.isAllowIntrospection()));
	}

	@Test
	public void testImportBlacklistedSites() throws IOException {
		BlacklistedSite site1 = new BlacklistedSite();
		site1.setId(1L);
		site1.setUri("http://foo.com");

		BlacklistedSite site2 = new BlacklistedSite();
		site2.setId(2L);
		site2.setUri("http://bar.com");

		BlacklistedSite site3 = new BlacklistedSite();
		site3.setId(3L);
		site3.setUri("http://baz.com");

		String configJson = "{" +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [" +

				"{\"id\":1,\"uri\":\"http://foo.com\"}," +
				"{\"id\":2,\"uri\":\"http://bar.com\"}," +
				"{\"id\":3,\"uri\":\"http://baz.com\"}" +

				"  ]" +
				"}";


		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));

		dataService.importData(reader);
		verify(blSiteRepository, times(3)).save(capturedBlacklistedSites.capture());

		List<BlacklistedSite> savedSites = capturedBlacklistedSites.getAllValues();

		assertThat(savedSites.size(), is(3));

		assertThat(savedSites.get(0).getUri(), equalTo(site1.getUri()));
		assertThat(savedSites.get(1).getUri(), equalTo(site2.getUri()));
		assertThat(savedSites.get(2).getUri(), equalTo(site3.getUri()));
	}

	@Test
	public void testImportWhitelistedSites() throws IOException {
		WhitelistedSite site1 = new WhitelistedSite();
		site1.setId(1L);
		site1.setClientId("foo");

		WhitelistedSite site2 = new WhitelistedSite();
		site2.setId(2L);
		site2.setClientId("bar");

		WhitelistedSite site3 = new WhitelistedSite();
		site3.setId(3L);
		site3.setClientId("baz");
		//site3.setAllowedScopes(null);

		String configJson = "{" +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [" +

				"{\"id\":1,\"clientId\":\"foo\"}," +
				"{\"id\":2,\"clientId\":\"bar\"}," +
				"{\"id\":3,\"clientId\":\"baz\"}" +

				"  ]" +
				"}";

		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));

		final Map<Long, WhitelistedSite> fakeDb = new HashMap<>();
		when(wlSiteRepository.save(isA(WhitelistedSite.class))).thenAnswer(new Answer<WhitelistedSite>() {
			Long id = 333L;
			@Override
			public WhitelistedSite answer(InvocationOnMock invocation) throws Throwable {
				WhitelistedSite _site = (WhitelistedSite) invocation.getArguments()[0];
				if(_site.getId() == null) {
					_site.setId(id++);
				}
				fakeDb.put(_site.getId(), _site);
				return _site;
			}
		});
		when(wlSiteRepository.getById(anyLong())).thenAnswer(new Answer<WhitelistedSite>() {
			@Override
			public WhitelistedSite answer(InvocationOnMock invocation) throws Throwable {
				Long _id = (Long) invocation.getArguments()[0];
				return fakeDb.get(_id);
			}
		});

		dataService.importData(reader);
		verify(wlSiteRepository, times(3)).save(capturedWhitelistedSites.capture());

		List<WhitelistedSite> savedSites = capturedWhitelistedSites.getAllValues();

		assertThat(savedSites.size(), is(3));

		assertThat(savedSites.get(0).getClientId(), equalTo(site1.getClientId()));
		assertThat(savedSites.get(1).getClientId(), equalTo(site2.getClientId()));
		assertThat(savedSites.get(2).getClientId(), equalTo(site3.getClientId()));
	}

	@Test
	public void testImportGrants() throws IOException, ParseException {
		Date creationDate1 = formatter.parse("2014-09-10T22:49:44.090+0000", Locale.ENGLISH);
		Date accessDate1 = formatter.parse("2014-09-10T23:49:44.090+0000", Locale.ENGLISH);

		OAuth2AccessTokenEntity mockToken1 = mock(OAuth2AccessTokenEntity.class);
		when(mockToken1.getId()).thenReturn(1L);

		ApprovedSite site1 = new ApprovedSite();
		site1.setId(1L);
		site1.setClientId("foo");
		site1.setCreationDate(creationDate1);
		site1.setAccessDate(accessDate1);
		site1.setUserId("user1");
		site1.setAllowedScopes(ImmutableSet.of("openid", "phone"));
		when(mockToken1.getApprovedSite()).thenReturn(site1);

		Date creationDate2 = formatter.parse("2014-09-11T18:49:44.090+0000", Locale.ENGLISH);
		Date accessDate2 = formatter.parse("2014-09-11T20:49:44.090+0000", Locale.ENGLISH);
		Date timeoutDate2 = formatter.parse("2014-10-01T20:49:44.090+0000", Locale.ENGLISH);

		ApprovedSite site2 = new ApprovedSite();
		site2.setId(2L);
		site2.setClientId("bar");
		site2.setCreationDate(creationDate2);
		site2.setAccessDate(accessDate2);
		site2.setUserId("user2");
		site2.setAllowedScopes(ImmutableSet.of("openid", "offline_access", "email", "profile"));
		site2.setTimeoutDate(timeoutDate2);

		String configJson = "{" +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [" +

				"{\"id\":1,\"clientId\":\"foo\",\"creationDate\":\"2014-09-10T22:49:44.090+0000\",\"accessDate\":\"2014-09-10T23:49:44.090+0000\","
				+ "\"userId\":\"user1\",\"whitelistedSiteId\":null,\"allowedScopes\":[\"openid\",\"phone\"], \"whitelistedSiteId\":1,"
				+ "\"approvedAccessTokens\":[1]}," +
				"{\"id\":2,\"clientId\":\"bar\",\"creationDate\":\"2014-09-11T18:49:44.090+0000\",\"accessDate\":\"2014-09-11T20:49:44.090+0000\","
				+ "\"timeoutDate\":\"2014-10-01T20:49:44.090+0000\",\"userId\":\"user2\","
				+ "\"allowedScopes\":[\"openid\",\"offline_access\",\"email\",\"profile\"]}" +

				"  ]" +
				"}";

		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));

		final Map<Long, ApprovedSite> fakeDb = new HashMap<>();
		when(approvedSiteRepository.save(isA(ApprovedSite.class))).thenAnswer(new Answer<ApprovedSite>() {
			Long id = 364L;
			@Override
			public ApprovedSite answer(InvocationOnMock invocation) throws Throwable {
				ApprovedSite _site = (ApprovedSite) invocation.getArguments()[0];
				if(_site.getId() == null) {
					_site.setId(id++);
				}
				fakeDb.put(_site.getId(), _site);
				return _site;
			}
		});
		when(approvedSiteRepository.getById(anyLong())).thenAnswer(new Answer<ApprovedSite>() {
			@Override
			public ApprovedSite answer(InvocationOnMock invocation) throws Throwable {
				Long _id = (Long) invocation.getArguments()[0];
				return fakeDb.get(_id);
			}
		});
		when(wlSiteRepository.getById(isNull(Long.class))).thenAnswer(new Answer<WhitelistedSite>() {
			Long id = 432L;
			@Override
			public WhitelistedSite answer(InvocationOnMock invocation) throws Throwable {
				WhitelistedSite _site = mock(WhitelistedSite.class);
				when(_site.getId()).thenReturn(id++);
				return _site;
			}
		});
		when(tokenRepository.getAccessTokenById(isNull(Long.class))).thenAnswer(new Answer<OAuth2AccessTokenEntity>() {
			Long id = 245L;
			@Override
			public OAuth2AccessTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				OAuth2AccessTokenEntity _token = mock(OAuth2AccessTokenEntity.class);
				when(_token.getId()).thenReturn(id++);
				return _token;
			}
		});

		dataService.importData(reader);
		//2 for sites, 1 for updating access token ref on #1
		verify(approvedSiteRepository, times(3)).save(capturedApprovedSites.capture());

		List<ApprovedSite> savedSites = new ArrayList(fakeDb.values());

		assertThat(savedSites.size(), is(2));

		assertThat(savedSites.get(0).getClientId(), equalTo(site1.getClientId()));
		assertThat(savedSites.get(0).getAccessDate(), equalTo(site1.getAccessDate()));
		assertThat(savedSites.get(0).getCreationDate(), equalTo(site1.getCreationDate()));
		assertThat(savedSites.get(0).getAllowedScopes(), equalTo(site1.getAllowedScopes()));
		assertThat(savedSites.get(0).getTimeoutDate(), equalTo(site1.getTimeoutDate()));

		assertThat(savedSites.get(1).getClientId(), equalTo(site2.getClientId()));
		assertThat(savedSites.get(1).getAccessDate(), equalTo(site2.getAccessDate()));
		assertThat(savedSites.get(1).getCreationDate(), equalTo(site2.getCreationDate()));
		assertThat(savedSites.get(1).getAllowedScopes(), equalTo(site2.getAllowedScopes()));
		assertThat(savedSites.get(1).getTimeoutDate(), equalTo(site2.getTimeoutDate()));
	}

	@Test
	public void testImportAuthenticationHolders() throws IOException {
		OAuth2Request req1 = new OAuth2Request(new HashMap<String, String>(), "client1", new ArrayList<GrantedAuthority>(),
				true, new HashSet<String>(), new HashSet<String>(), "http://foo.com",
				new HashSet<String>(), null);
		Authentication mockAuth1 = mock(Authentication.class, withSettings().serializable());
		OAuth2Authentication auth1 = new OAuth2Authentication(req1, mockAuth1);

		AuthenticationHolderEntity holder1 = new AuthenticationHolderEntity();
		holder1.setId(1L);
		holder1.setAuthentication(auth1);

		OAuth2Request req2 = new OAuth2Request(new HashMap<String, String>(), "client2", new ArrayList<GrantedAuthority>(),
				true, new HashSet<String>(), new HashSet<String>(), "http://bar.com",
				new HashSet<String>(), null);
		Authentication mockAuth2 = mock(Authentication.class, withSettings().serializable());
		OAuth2Authentication auth2 = new OAuth2Authentication(req2, mockAuth2);

		AuthenticationHolderEntity holder2 = new AuthenticationHolderEntity();
		holder2.setId(2L);
		holder2.setAuthentication(auth2);

		String configJson = "{" +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [" +

				"{\"id\":1,\"clientId\":\"client1\",\"redirectUri\":\"http://foo.com\","
				+ "\"savedUserAuthentication\":null}," +
				"{\"id\":2,\"clientId\":\"client2\",\"redirectUri\":\"http://bar.com\","
				+ "\"savedUserAuthentication\":null}" +
				"  ]" +
				"}";

		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));

		final Map<Long, AuthenticationHolderEntity> fakeDb = new HashMap<>();
		when(authHolderRepository.save(isA(AuthenticationHolderEntity.class))).thenAnswer(new Answer<AuthenticationHolderEntity>() {
			Long id = 243L;
			@Override
			public AuthenticationHolderEntity answer(InvocationOnMock invocation) throws Throwable {
				AuthenticationHolderEntity _site = (AuthenticationHolderEntity) invocation.getArguments()[0];
				if(_site.getId() == null) {
					_site.setId(id++);
				}
				fakeDb.put(_site.getId(), _site);
				return _site;
			}
		});

		dataService.importData(reader);
		verify(authHolderRepository, times(2)).save(capturedAuthHolders.capture());

		List<AuthenticationHolderEntity> savedAuthHolders = capturedAuthHolders.getAllValues();

		assertThat(savedAuthHolders.size(), is(2));
		assertThat(savedAuthHolders.get(0).getAuthentication().getOAuth2Request().getClientId(), equalTo(holder1.getAuthentication().getOAuth2Request().getClientId()));
		assertThat(savedAuthHolders.get(1).getAuthentication().getOAuth2Request().getClientId(), equalTo(holder2.getAuthentication().getOAuth2Request().getClientId()));
	}

	@Test
	public void testImportSystemScopes() throws IOException {
		SystemScope scope1 = new SystemScope();
		scope1.setId(1L);
		scope1.setValue("scope1");
		scope1.setDescription("Scope 1");
		scope1.setRestricted(true);
		scope1.setDefaultScope(false);
		scope1.setIcon("glass");

		SystemScope scope2 = new SystemScope();
		scope2.setId(2L);
		scope2.setValue("scope2");
		scope2.setDescription("Scope 2");
		scope2.setRestricted(false);
		scope2.setDefaultScope(false);
		scope2.setIcon("ball");

		SystemScope scope3 = new SystemScope();
		scope3.setId(3L);
		scope3.setValue("scope3");
		scope3.setDescription("Scope 3");
		scope3.setRestricted(false);
		scope3.setDefaultScope(true);
		scope3.setIcon("road");

		String configJson = "{" +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [], " +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [" +

				"{\"id\":1,\"description\":\"Scope 1\",\"icon\":\"glass\",\"value\":\"scope1\",\"restricted\":true,\"defaultScope\":false}," +
				"{\"id\":2,\"description\":\"Scope 2\",\"icon\":\"ball\",\"value\":\"scope2\",\"restricted\":false,\"defaultScope\":false}," +
				"{\"id\":3,\"description\":\"Scope 3\",\"icon\":\"road\",\"value\":\"scope3\",\"restricted\":false,\"defaultScope\":true}" +

				"  ]" +
				"}";

		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));

		dataService.importData(reader);
		verify(sysScopeRepository, times(3)).save(capturedScope.capture());

		List<SystemScope> savedScopes = capturedScope.getAllValues();

		assertThat(savedScopes.size(), is(3));
		assertThat(savedScopes.get(0).getValue(), equalTo(scope1.getValue()));
		assertThat(savedScopes.get(0).getDescription(), equalTo(scope1.getDescription()));
		assertThat(savedScopes.get(0).getIcon(), equalTo(scope1.getIcon()));
		assertThat(savedScopes.get(0).isDefaultScope(), equalTo(scope1.isDefaultScope()));
		assertThat(savedScopes.get(0).isRestricted(), equalTo(scope1.isRestricted()));

		assertThat(savedScopes.get(1).getValue(), equalTo(scope2.getValue()));
		assertThat(savedScopes.get(1).getDescription(), equalTo(scope2.getDescription()));
		assertThat(savedScopes.get(1).getIcon(), equalTo(scope2.getIcon()));
		assertThat(savedScopes.get(1).isDefaultScope(), equalTo(scope2.isDefaultScope()));
		assertThat(savedScopes.get(1).isRestricted(), equalTo(scope2.isRestricted()));

		assertThat(savedScopes.get(2).getValue(), equalTo(scope3.getValue()));
		assertThat(savedScopes.get(2).getDescription(), equalTo(scope3.getDescription()));
		assertThat(savedScopes.get(2).getIcon(), equalTo(scope3.getIcon()));
		assertThat(savedScopes.get(2).isDefaultScope(), equalTo(scope3.isDefaultScope()));
		assertThat(savedScopes.get(2).isRestricted(), equalTo(scope3.isRestricted()));

	}

	@Test
	public void testFixRefreshTokenAuthHolderReferencesOnImport() throws IOException, ParseException {
		String expiration1 = "2014-09-10T22:49:44.090+0000";
		Date expirationDate1 = formatter.parse(expiration1, Locale.ENGLISH);

		ClientDetailsEntity mockedClient1 = mock(ClientDetailsEntity.class);
		when(mockedClient1.getClientId()).thenReturn("mocked_client_1");

		OAuth2Request req1 = new OAuth2Request(new HashMap<String, String>(), "client1", new ArrayList<GrantedAuthority>(),
				true, new HashSet<String>(), new HashSet<String>(), "http://foo.com",
				new HashSet<String>(), null);
		Authentication mockAuth1 = mock(Authentication.class, withSettings().serializable());
		OAuth2Authentication auth1 = new OAuth2Authentication(req1, mockAuth1);

		AuthenticationHolderEntity holder1 = new AuthenticationHolderEntity();
		holder1.setId(1L);
		holder1.setAuthentication(auth1);

		OAuth2RefreshTokenEntity token1 = new OAuth2RefreshTokenEntity();
		token1.setId(1L);
		token1.setClient(mockedClient1);
		token1.setExpiration(expirationDate1);
		token1.setJwt(JWTParser.parse("eyJhbGciOiJub25lIn0.eyJqdGkiOiJmOTg4OWQyOS0xMTk1LTQ4ODEtODgwZC1lZjVlYzAwY2Y4NDIifQ."));
		token1.setAuthenticationHolder(holder1);

		String expiration2 = "2015-01-07T18:31:50.079+0000";
		Date expirationDate2 = formatter.parse(expiration2, Locale.ENGLISH);

		ClientDetailsEntity mockedClient2 = mock(ClientDetailsEntity.class);
		when(mockedClient2.getClientId()).thenReturn("mocked_client_2");

		OAuth2Request req2 = new OAuth2Request(new HashMap<String, String>(), "client2", new ArrayList<GrantedAuthority>(),
				true, new HashSet<String>(), new HashSet<String>(), "http://bar.com",
				new HashSet<String>(), null);
		Authentication mockAuth2 = mock(Authentication.class, withSettings().serializable());
		OAuth2Authentication auth2 = new OAuth2Authentication(req2, mockAuth2);

		AuthenticationHolderEntity holder2 = new AuthenticationHolderEntity();
		holder2.setId(2L);
		holder2.setAuthentication(auth2);

		OAuth2RefreshTokenEntity token2 = new OAuth2RefreshTokenEntity();
		token2.setId(2L);
		token2.setClient(mockedClient2);
		token2.setExpiration(expirationDate2);
		token2.setJwt(JWTParser.parse("eyJhbGciOiJub25lIn0.eyJqdGkiOiJlYmEyYjc3My0xNjAzLTRmNDAtOWQ3MS1hMGIxZDg1OWE2MDAifQ."));
		token2.setAuthenticationHolder(holder2);

		String configJson = "{" +
				"\"" + MITREidDataService.SYSTEMSCOPES + "\": [], " +
				"\"" + MITREidDataService.ACCESSTOKENS + "\": [], " +
				"\"" + MITREidDataService.CLIENTS + "\": [], " +
				"\"" + MITREidDataService.GRANTS + "\": [], " +
				"\"" + MITREidDataService.WHITELISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.BLACKLISTEDSITES + "\": [], " +
				"\"" + MITREidDataService.AUTHENTICATIONHOLDERS + "\": [" +

				"{\"id\":1,\"authentication\":{\"authorizationRequest\":{\"clientId\":\"client1\",\"redirectUri\":\"http://foo.com\"},"
				+ "\"userAuthentication\":null}}," +
				"{\"id\":2,\"authentication\":{\"authorizationRequest\":{\"clientId\":\"client2\",\"redirectUri\":\"http://bar.com\"},"
				+ "\"userAuthentication\":null}}" +
				"  ]," +
				"\"" + MITREidDataService.REFRESHTOKENS + "\": [" +

				"{\"id\":1,\"clientId\":\"mocked_client_1\",\"expiration\":\"2014-09-10T22:49:44.090+0000\","
				+ "\"authenticationHolderId\":1,\"value\":\"eyJhbGciOiJub25lIn0.eyJqdGkiOiJmOTg4OWQyOS0xMTk1LTQ4ODEtODgwZC1lZjVlYzAwY2Y4NDIifQ.\"}," +
				"{\"id\":2,\"clientId\":\"mocked_client_2\",\"expiration\":\"2015-01-07T18:31:50.079+0000\","
				+ "\"authenticationHolderId\":2,\"value\":\"eyJhbGciOiJub25lIn0.eyJqdGkiOiJlYmEyYjc3My0xNjAzLTRmNDAtOWQ3MS1hMGIxZDg1OWE2MDAifQ.\"}" +

				"  ]" +
				"}";
		logger.debug(configJson);

		JsonReader reader = new JsonReader(new StringReader(configJson));
		final Map<Long, OAuth2RefreshTokenEntity> fakeRefreshTokenTable = new HashMap<>();
		final Map<Long, AuthenticationHolderEntity> fakeAuthHolderTable = new HashMap<>();
		when(tokenRepository.saveRefreshToken(isA(OAuth2RefreshTokenEntity.class))).thenAnswer(new Answer<OAuth2RefreshTokenEntity>() {
			Long id = 343L;
			@Override
			public OAuth2RefreshTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				OAuth2RefreshTokenEntity _token = (OAuth2RefreshTokenEntity) invocation.getArguments()[0];
				if(_token.getId() == null) {
					_token.setId(id++);
				}
				fakeRefreshTokenTable.put(_token.getId(), _token);
				return _token;
			}
		});
		when(tokenRepository.getRefreshTokenById(anyLong())).thenAnswer(new Answer<OAuth2RefreshTokenEntity>() {
			@Override
			public OAuth2RefreshTokenEntity answer(InvocationOnMock invocation) throws Throwable {
				Long _id = (Long) invocation.getArguments()[0];
				return fakeRefreshTokenTable.get(_id);
			}
		});
		when(clientRepository.getClientByClientId(anyString())).thenAnswer(new Answer<ClientDetailsEntity>() {
			@Override
			public ClientDetailsEntity answer(InvocationOnMock invocation) throws Throwable {
				String _clientId = (String) invocation.getArguments()[0];
				ClientDetailsEntity _client = mock(ClientDetailsEntity.class);
				when(_client.getClientId()).thenReturn(_clientId);
				return _client;
			}
		});
		when(authHolderRepository.save(isA(AuthenticationHolderEntity.class))).thenAnswer(new Answer<AuthenticationHolderEntity>() {
			Long id = 356L;
			@Override
			public AuthenticationHolderEntity answer(InvocationOnMock invocation) throws Throwable {
				AuthenticationHolderEntity _holder = (AuthenticationHolderEntity) invocation.getArguments()[0];
				if(_holder.getId() == null) {
					_holder.setId(id++);
				}
				fakeAuthHolderTable.put(_holder.getId(), _holder);
				return _holder;
			}
		});
		when(authHolderRepository.getById(anyLong())).thenAnswer(new Answer<AuthenticationHolderEntity>() {
			@Override
			public AuthenticationHolderEntity answer(InvocationOnMock invocation) throws Throwable {
				Long _id = (Long) invocation.getArguments()[0];
				return fakeAuthHolderTable.get(_id);
			}
		});
		dataService.importData(reader);

		List<OAuth2RefreshTokenEntity> savedRefreshTokens = new ArrayList(fakeRefreshTokenTable.values()); //capturedRefreshTokens.getAllValues();
		Collections.sort(savedRefreshTokens, new refreshTokenIdComparator());

		assertThat(savedRefreshTokens.get(0).getAuthenticationHolder().getId(), equalTo(356L));
		assertThat(savedRefreshTokens.get(1).getAuthenticationHolder().getId(), equalTo(357L));
	}

	private Set<String> jsonArrayToStringSet(JsonArray a) {
		Set<String> s = new HashSet<>();
		for (JsonElement jsonElement : a) {
			s.add(jsonElement.getAsString());
		}
		return s;
	}

}
