package org.mitre.openid.connect.service.impl;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
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
import org.mitre.openid.connect.util.DateUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@RunWith(MockitoJUnitRunner.class)
public class TestMITREidDataService_1_0 {

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
	private MITREidDataService_1_0 dataService;

	@Before
	public void prepare() {
		Mockito.reset(clientRepository, approvedSiteRepository, authHolderRepository, tokenRepository, sysScopeRepository, wlSiteRepository, blSiteRepository);
	}
	
    @Test
    public void testExportRefreshTokens() throws IOException, ParseException {
        String expiration1 = "2014-09-10T22:49:44.090+0000";
        Date expirationDate1 = DateUtil.utcToDate(expiration1);
        
        ClientDetailsEntity mockedClient1 = mock(ClientDetailsEntity.class);
        when(mockedClient1.getClientId()).thenReturn("mocked_client_1");
        
        AuthenticationHolderEntity mockedAuthHolder1 = mock(AuthenticationHolderEntity.class);
        when(mockedAuthHolder1.getId()).thenReturn(1L);
        
        OAuth2RefreshTokenEntity token1 = new OAuth2RefreshTokenEntity();
        token1.setId(1L);
        token1.setClient(mockedClient1);
        token1.setExpiration(expirationDate1);
        token1.setValue("eyJhbGciOiJub25lIn0.eyJqdGkiOiJmOTg4OWQyOS0xMTk1LTQ4ODEtODgwZC1lZjVlYzAwY2Y4NDIifQ.");
        token1.setAuthenticationHolder(mockedAuthHolder1);
        
        String expiration2 = "2015-01-07T18:31:50.079+0000";
        Date expirationDate2 = DateUtil.utcToDate(expiration2);
        
        ClientDetailsEntity mockedClient2 = mock(ClientDetailsEntity.class);
        when(mockedClient2.getClientId()).thenReturn("mocked_client_2");
        
        AuthenticationHolderEntity mockedAuthHolder2 = mock(AuthenticationHolderEntity.class);
        when(mockedAuthHolder2.getId()).thenReturn(2L);
        
        OAuth2RefreshTokenEntity token2 = new OAuth2RefreshTokenEntity();
        token2.setId(2L);
        token2.setClient(mockedClient2);
        token2.setExpiration(expirationDate2);
        token2.setValue("eyJhbGciOiJub25lIn0.eyJqdGkiOiJlYmEyYjc3My0xNjAzLTRmNDAtOWQ3MS1hMGIxZDg1OWE2MDAifQ.");
        token2.setAuthenticationHolder(mockedAuthHolder2);
        
        Set<OAuth2RefreshTokenEntity> allRefreshTokens = ImmutableSet.of(token1, token2);
        
		Mockito.when(clientRepository.getAllClients()).thenReturn(new HashSet<ClientDetailsEntity>());
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(new HashSet<WhitelistedSite>());
		Mockito.when(blSiteRepository.getAll()).thenReturn(new HashSet<BlacklistedSite>());
		Mockito.when(authHolderRepository.getAll()).thenReturn(new HashSet<AuthenticationHolderEntity>());
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(new HashSet<OAuth2AccessTokenEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(allRefreshTokens);
		Mockito.when(sysScopeRepository.getAll()).thenReturn(new HashSet<SystemScope>());
        
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();
		
		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));

		
		// check our refresh token list (this test)
		JsonArray refreshTokens = config.get(MITREidDataService.REFRESHTOKENS).getAsJsonArray();

		assertThat(refreshTokens.size(), is(2));
		// check for both of our refresh tokens in turn
		Set<OAuth2RefreshTokenEntity> checked = new HashSet<OAuth2RefreshTokenEntity>();
		for (JsonElement e : refreshTokens) {
			assertThat(e.isJsonObject(), is(true));
			JsonObject token = e.getAsJsonObject();

			OAuth2RefreshTokenEntity compare = null;
			if (token.get("id").getAsLong() == token1.getId()) {
				compare = token1;
			} else if (token.get("id").getAsLong() == token2.getId()) {
				compare = token2;
			}
			
			if (compare == null) {
				fail("Could not find matching id: " + token.get("id").getAsString());
			} else {
				assertThat(token.get("id").getAsLong(), equalTo(compare.getId()));
				assertThat(token.get("clientId").getAsString(), equalTo(compare.getClient().getClientId()));
                assertThat(token.get("expiration").getAsString(), equalTo(DateUtil.toUTCString(compare.getExpiration())));
				assertThat(token.get("value").getAsString(), equalTo(compare.getValue()));
				assertThat(token.get("authenticationHolderId").getAsLong(), equalTo(compare.getAuthenticationHolder().getId()));
				checked.add(compare);
			}
		}
		// make sure all of our refresh tokens were found
		assertThat(checked.containsAll(allRefreshTokens), is(true));	
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
        Date expirationDate1 = DateUtil.utcToDate(expiration1);
        
        ClientDetailsEntity mockedClient1 = mock(ClientDetailsEntity.class);
        when(mockedClient1.getClientId()).thenReturn("mocked_client_1");
        
        AuthenticationHolderEntity mockedAuthHolder1 = mock(AuthenticationHolderEntity.class);
        when(mockedAuthHolder1.getId()).thenReturn(1L);
        
        OAuth2RefreshTokenEntity token1 = new OAuth2RefreshTokenEntity();
        token1.setId(1L);
        token1.setClient(mockedClient1);
        token1.setExpiration(expirationDate1);
        token1.setValue("eyJhbGciOiJub25lIn0.eyJqdGkiOiJmOTg4OWQyOS0xMTk1LTQ4ODEtODgwZC1lZjVlYzAwY2Y4NDIifQ.");
        token1.setAuthenticationHolder(mockedAuthHolder1);
        
        String expiration2 = "2015-01-07T18:31:50.079+0000";
        Date expirationDate2 = DateUtil.utcToDate(expiration2);
        
        ClientDetailsEntity mockedClient2 = mock(ClientDetailsEntity.class);
        when(mockedClient2.getClientId()).thenReturn("mocked_client_2");
        
        AuthenticationHolderEntity mockedAuthHolder2 = mock(AuthenticationHolderEntity.class);
        when(mockedAuthHolder2.getId()).thenReturn(2L);
        
        OAuth2RefreshTokenEntity token2 = new OAuth2RefreshTokenEntity();
        token2.setId(2L);
        token2.setClient(mockedClient2);
        token2.setExpiration(expirationDate2);
        token2.setValue("eyJhbGciOiJub25lIn0.eyJqdGkiOiJlYmEyYjc3My0xNjAzLTRmNDAtOWQ3MS1hMGIxZDg1OWE2MDAifQ.");
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
			
		System.err.println(configJson);
        JsonReader reader = new JsonReader(new StringReader(configJson));
        
        final Map<Long, OAuth2RefreshTokenEntity> fakeDb = new HashMap<Long, OAuth2RefreshTokenEntity>();
        when(tokenRepository.saveRefreshToken(isA(OAuth2RefreshTokenEntity.class))).thenAnswer(new Answer<OAuth2RefreshTokenEntity>() {
            Long id = 3L;
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
           Long id = 1L;
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
		assertThat(savedRefreshTokens.get(0).getAuthenticationHolder().getId(), equalTo(token1.getAuthenticationHolder().getId()));
		assertThat(savedRefreshTokens.get(0).getValue(), equalTo(token1.getValue()));

		assertThat(savedRefreshTokens.get(1).getClient().getClientId(), equalTo(token2.getClient().getClientId()));
        assertThat(savedRefreshTokens.get(1).getExpiration(), equalTo(token2.getExpiration()));
		assertThat(savedRefreshTokens.get(1).getAuthenticationHolder().getId(), equalTo(token2.getAuthenticationHolder().getId()));
		assertThat(savedRefreshTokens.get(1).getValue(), equalTo(token2.getValue()));
    }
    
    @Test
    public void testExportAccessTokens() throws IOException, ParseException {
        String expiration1 = "2014-09-10T22:49:44.090+0000";
        Date expirationDate1 = DateUtil.utcToDate(expiration1);
        
        ClientDetailsEntity mockedClient1 = mock(ClientDetailsEntity.class);
        when(mockedClient1.getClientId()).thenReturn("mocked_client_1");

        AuthenticationHolderEntity mockedAuthHolder1 = mock(AuthenticationHolderEntity.class);
        when(mockedAuthHolder1.getId()).thenReturn(1L);
        
        OAuth2AccessTokenEntity token1 = new OAuth2AccessTokenEntity();
        token1.setId(1L);
        token1.setClient(mockedClient1);
        token1.setExpiration(expirationDate1);
        token1.setValue("eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3ODk5NjgsInN1YiI6IjkwMzQyLkFTREZKV0ZBIiwiYXRfaGFzaCI6InptTmt1QmNRSmNYQktNaVpFODZqY0EiLCJhdWQiOlsiY2xpZW50Il0sImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDgwXC9vcGVuaWQtY29ubmVjdC1zZXJ2ZXItd2ViYXBwXC8iLCJpYXQiOjE0MTI3ODkzNjh9.xkEJ9IMXpH7qybWXomfq9WOOlpGYnrvGPgey9UQ4GLzbQx7JC0XgJK83PmrmBZosvFPCmota7FzI_BtwoZLgAZfFiH6w3WIlxuogoH-TxmYbxEpTHoTsszZppkq9mNgOlArV4jrR9y3TPo4MovsH71dDhS_ck-CvAlJunHlqhs0");
        token1.setAuthenticationHolder(mockedAuthHolder1);
        token1.setScope(ImmutableSet.of("id-token"));
        token1.setTokenType("Bearer");
        
        String expiration2 = "2015-01-07T18:31:50.079+0000";
        Date expirationDate2 = DateUtil.utcToDate(expiration2);
        
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
        token2.setValue("eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3OTI5NjgsImF1ZCI6WyJjbGllbnQiXSwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODBcL29wZW5pZC1jb25uZWN0LXNlcnZlci13ZWJhcHBcLyIsImp0aSI6IjBmZGE5ZmRiLTYyYzItNGIzZS05OTdiLWU0M2VhMDUwMzNiOSIsImlhdCI6MTQxMjc4OTM2OH0.xgaVpRLYE5MzbgXfE0tZt823tjAm6Oh3_kdR1P2I9jRLR6gnTlBQFlYi3Y_0pWNnZSerbAE8Tn6SJHZ9k-curVG0-ByKichV7CNvgsE5X_2wpEaUzejvKf8eZ-BammRY-ie6yxSkAarcUGMvGGOLbkFcz5CtrBpZhfd75J49BIQ");
        token2.setAuthenticationHolder(mockedAuthHolder2);
        token2.setIdToken(token1);
        token2.setRefreshToken(mockRefreshToken2);
        token2.setScope(ImmutableSet.of("openid", "offline_access", "email", "profile"));
        token2.setTokenType("Bearer");
        
        Set<OAuth2AccessTokenEntity> allAccessTokens = ImmutableSet.of(token1, token2);
        
		Mockito.when(clientRepository.getAllClients()).thenReturn(new HashSet<ClientDetailsEntity>());
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(new HashSet<WhitelistedSite>());
		Mockito.when(blSiteRepository.getAll()).thenReturn(new HashSet<BlacklistedSite>());
		Mockito.when(authHolderRepository.getAll()).thenReturn(new HashSet<AuthenticationHolderEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(new HashSet<OAuth2RefreshTokenEntity>());
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(allAccessTokens);
		Mockito.when(sysScopeRepository.getAll()).thenReturn(new HashSet<SystemScope>());
        
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();
		
		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));

		
		// check our access token list (this test)
		JsonArray accessTokens = config.get(MITREidDataService.ACCESSTOKENS).getAsJsonArray();

		assertThat(accessTokens.size(), is(2));
		// check for both of our access tokens in turn
		Set<OAuth2AccessTokenEntity> checked = new HashSet<OAuth2AccessTokenEntity>();
		for (JsonElement e : accessTokens) {
			assertTrue(e.isJsonObject());
			JsonObject token = e.getAsJsonObject();

			OAuth2AccessTokenEntity compare = null;
			if (token.get("id").getAsLong() == token1.getId().longValue()) {
				compare = token1;
			} else if (token.get("id").getAsLong() == token2.getId().longValue()) {
				compare = token2;
			}
			
			if (compare == null) {
				fail("Could not find matching id: " + token.get("id").getAsString());
			} else {
				assertThat(token.get("id").getAsLong(), equalTo(compare.getId()));
				assertThat(token.get("clientId").getAsString(), equalTo(compare.getClient().getClientId()));
                assertThat(token.get("expiration").getAsString(), equalTo(DateUtil.toUTCString(compare.getExpiration())));
				assertThat(token.get("value").getAsString(), equalTo(compare.getValue()));
                assertThat(token.get("type").getAsString(), equalTo(compare.getTokenType()));
				assertThat(token.get("authenticationHolderId").getAsLong(), equalTo(compare.getAuthenticationHolder().getId()));
                assertTrue(token.get("scope").isJsonArray());
                assertThat(jsonArrayToStringSet(token.getAsJsonArray("scope")), equalTo(compare.getScope()));
                if(token.get("idTokenId").isJsonNull()) {
                    assertNull(compare.getIdToken());
                } else {
                    assertThat(token.get("idTokenId").getAsLong(), equalTo(compare.getIdToken().getId()));
                }
                if(token.get("refreshTokenId").isJsonNull()) {
                    assertNull(compare.getIdToken());
                } else {
                    assertThat(token.get("refreshTokenId").getAsLong(), equalTo(compare.getRefreshToken().getId()));
                }
				checked.add(compare);
			}
		}
		// make sure all of our access tokens were found
		assertThat(checked.containsAll(allAccessTokens), is(true));	
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
        Date expirationDate1 = DateUtil.utcToDate(expiration1);
        
        ClientDetailsEntity mockedClient1 = mock(ClientDetailsEntity.class);
        when(mockedClient1.getClientId()).thenReturn("mocked_client_1");

        AuthenticationHolderEntity mockedAuthHolder1 = mock(AuthenticationHolderEntity.class);
        when(mockedAuthHolder1.getId()).thenReturn(1L);
        
        OAuth2AccessTokenEntity token1 = new OAuth2AccessTokenEntity();
        token1.setId(1L);
        token1.setClient(mockedClient1);
        token1.setExpiration(expirationDate1);
        token1.setValue("eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3ODk5NjgsInN1YiI6IjkwMzQyLkFTREZKV0ZBIiwiYXRfaGFzaCI6InptTmt1QmNRSmNYQktNaVpFODZqY0EiLCJhdWQiOlsiY2xpZW50Il0sImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDgwXC9vcGVuaWQtY29ubmVjdC1zZXJ2ZXItd2ViYXBwXC8iLCJpYXQiOjE0MTI3ODkzNjh9.xkEJ9IMXpH7qybWXomfq9WOOlpGYnrvGPgey9UQ4GLzbQx7JC0XgJK83PmrmBZosvFPCmota7FzI_BtwoZLgAZfFiH6w3WIlxuogoH-TxmYbxEpTHoTsszZppkq9mNgOlArV4jrR9y3TPo4MovsH71dDhS_ck-CvAlJunHlqhs0");
        token1.setAuthenticationHolder(mockedAuthHolder1);
        token1.setScope(ImmutableSet.of("id-token"));
        token1.setTokenType("Bearer");
        
        String expiration2 = "2015-01-07T18:31:50.079+0000";
        Date expirationDate2 = DateUtil.utcToDate(expiration2);
        
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
        token2.setValue("eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0MTI3OTI5NjgsImF1ZCI6WyJjbGllbnQiXSwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODBcL29wZW5pZC1jb25uZWN0LXNlcnZlci13ZWJhcHBcLyIsImp0aSI6IjBmZGE5ZmRiLTYyYzItNGIzZS05OTdiLWU0M2VhMDUwMzNiOSIsImlhdCI6MTQxMjc4OTM2OH0.xgaVpRLYE5MzbgXfE0tZt823tjAm6Oh3_kdR1P2I9jRLR6gnTlBQFlYi3Y_0pWNnZSerbAE8Tn6SJHZ9k-curVG0-ByKichV7CNvgsE5X_2wpEaUzejvKf8eZ-BammRY-ie6yxSkAarcUGMvGGOLbkFcz5CtrBpZhfd75J49BIQ");
        token2.setAuthenticationHolder(mockedAuthHolder2);
        token2.setIdToken(token1);
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
		
		
		System.err.println(configJson);
		
		JsonReader reader = new JsonReader(new StringReader(configJson));

        final Map<Long, OAuth2AccessTokenEntity> fakeDb = new HashMap<Long, OAuth2AccessTokenEntity>();
        when(tokenRepository.saveAccessToken(isA(OAuth2AccessTokenEntity.class))).thenAnswer(new Answer<OAuth2AccessTokenEntity>() {
            Long id = 3L;
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
           Long id = 1L;
           @Override
           public AuthenticationHolderEntity answer(InvocationOnMock invocation) throws Throwable {
               AuthenticationHolderEntity _auth = mock(AuthenticationHolderEntity.class);
               when(_auth.getId()).thenReturn(id);
               id++;
               return _auth;
           }
        });
		dataService.importData(reader);
        //2 times for token, 2 times to update client, 2 times to update authHolder, 2 times to update id token, 2 times to update refresh token
		verify(tokenRepository, times(8)).saveAccessToken(capturedAccessTokens.capture());
		
		List<OAuth2AccessTokenEntity> savedAccessTokens = new ArrayList(fakeDb.values()); //capturedAccessTokens.getAllValues();
        Collections.sort(savedAccessTokens, new accessTokenIdComparator());
        
		assertThat(savedAccessTokens.size(), is(2));
        
		assertThat(savedAccessTokens.get(0).getClient().getClientId(), equalTo(token1.getClient().getClientId()));
        assertThat(savedAccessTokens.get(0).getExpiration(), equalTo(token1.getExpiration()));
		assertThat(savedAccessTokens.get(0).getAuthenticationHolder().getId(), equalTo(token1.getAuthenticationHolder().getId()));
		assertThat(savedAccessTokens.get(0).getValue(), equalTo(token1.getValue()));

		assertThat(savedAccessTokens.get(1).getClient().getClientId(), equalTo(token2.getClient().getClientId()));
        assertThat(savedAccessTokens.get(1).getExpiration(), equalTo(token2.getExpiration()));
		assertThat(savedAccessTokens.get(1).getAuthenticationHolder().getId(), equalTo(token2.getAuthenticationHolder().getId()));
		assertThat(savedAccessTokens.get(1).getValue(), equalTo(token2.getValue()));
    }    
    
	@Test
	public void testExportClients() throws IOException {
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

        Set<ClientDetailsEntity> allClients = ImmutableSet.of(client1, client2);
        
		Mockito.when(clientRepository.getAllClients()).thenReturn(allClients);
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(new HashSet<WhitelistedSite>());
		Mockito.when(blSiteRepository.getAll()).thenReturn(new HashSet<BlacklistedSite>());
		Mockito.when(authHolderRepository.getAll()).thenReturn(new HashSet<AuthenticationHolderEntity>());
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(new HashSet<OAuth2AccessTokenEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(new HashSet<OAuth2RefreshTokenEntity>());
		Mockito.when(sysScopeRepository.getAll()).thenReturn(new HashSet<SystemScope>());
		
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();
		
		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));

		
		// check our client list (this test)
		JsonArray clients = config.get(MITREidDataService.CLIENTS).getAsJsonArray();

		assertThat(clients.size(), is(2));
		// check for both of our clients in turn
		Set<ClientDetailsEntity> checked = new HashSet<ClientDetailsEntity>();
		for (JsonElement e : clients) {
			assertThat(e.isJsonObject(), is(true));
			JsonObject client = e.getAsJsonObject();

			ClientDetailsEntity compare = null;
			if (client.get("clientId").getAsString().equals(client1.getClientId())) {
				compare = client1;
			} else if (client.get("clientId").getAsString().equals(client2.getClientId())) {
				compare = client2;
			}
			
			if (compare == null) {
				fail("Could not find matching clientId: " + client.get("clientId").getAsString());
			} else {
				assertThat(client.get("clientId").getAsString(), equalTo(compare.getClientId()));
				assertThat(client.get("secret").getAsString(), equalTo(compare.getClientSecret()));
				assertThat(client.get("accessTokenValiditySeconds").getAsInt(), equalTo(compare.getAccessTokenValiditySeconds()));
				assertThat(client.get("allowIntrospection").getAsBoolean(), equalTo(compare.isAllowIntrospection()));
				assertThat(jsonArrayToStringSet(client.get("redirectUris").getAsJsonArray()), equalTo(compare.getRedirectUris()));
				assertThat(jsonArrayToStringSet(client.get("scope").getAsJsonArray()), equalTo(compare.getScope()));
				assertThat(jsonArrayToStringSet(client.get("grantTypes").getAsJsonArray()), equalTo(compare.getGrantTypes()));
				checked.add(compare);
			}
		}
		// make sure all of our clients were found
		assertThat(checked.containsAll(allClients), is(true));	
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
		
		System.err.println(configJson);
		
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
	public void testExportBlacklistedSites() throws IOException {	
		BlacklistedSite site1 = new BlacklistedSite();
        site1.setId(1L);
        site1.setUri("http://foo.com");

        BlacklistedSite site2 = new BlacklistedSite();
        site2.setId(2L);
        site2.setUri("http://bar.com");
        
        BlacklistedSite site3 = new BlacklistedSite();
        site3.setId(3L);
        site3.setUri("http://baz.com");

		Set<BlacklistedSite> allBlacklistedSites = ImmutableSet.of(site1, site2, site3);
		
		Mockito.when(clientRepository.getAllClients()).thenReturn(new HashSet<ClientDetailsEntity>());
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(new HashSet<WhitelistedSite>());
		Mockito.when(blSiteRepository.getAll()).thenReturn(allBlacklistedSites);
		Mockito.when(authHolderRepository.getAll()).thenReturn(new HashSet<AuthenticationHolderEntity>());
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(new HashSet<OAuth2AccessTokenEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(new HashSet<OAuth2RefreshTokenEntity>());
		Mockito.when(sysScopeRepository.getAll()).thenReturn(new HashSet<SystemScope>());
		
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();

		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));
		
		// check our scope list (this test)
		JsonArray sites = config.get(MITREidDataService.BLACKLISTEDSITES).getAsJsonArray();

		assertThat(sites.size(), is(3));
		// check for both of our sites in turn
		Set<BlacklistedSite> checked = new HashSet<BlacklistedSite>();
		for (JsonElement e : sites) {
			assertThat(e.isJsonObject(), is(true));
			JsonObject site = e.getAsJsonObject();

			BlacklistedSite compare = null;
			if (site.get("id").getAsLong() == site1.getId().longValue()) {
				compare = site1;
			} else if (site.get("id").getAsLong() == site2.getId().longValue()) {
				compare = site2;
			} else if (site.get("id").getAsLong() == site3.getId().longValue()) {
				compare = site3;
			}
			
			if (compare == null) {
				fail("Could not find matching blacklisted site id: " + site.get("id").getAsString());
			} else {
				assertThat(site.get("uri").getAsString(), equalTo(compare.getUri()));
				checked.add(compare);
			}
		}
		// make sure all of our clients were found
		assertThat(checked.containsAll(allBlacklistedSites), is(true));
		
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
		
		
		System.err.println(configJson);
		
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
	public void testExportWhitelistedSites() throws IOException {		
		WhitelistedSite site1 = new WhitelistedSite();
        site1.setId(1L);
        site1.setClientId("foo");

        WhitelistedSite site2 = new WhitelistedSite();
        site2.setId(2L);
        site2.setClientId("bar");
        
        WhitelistedSite site3 = new WhitelistedSite();
        site3.setId(3L);
        site3.setClientId("baz");

		Set<WhitelistedSite> allWhitelistedSites = ImmutableSet.of(site1, site2, site3);
		
		Mockito.when(clientRepository.getAllClients()).thenReturn(new HashSet<ClientDetailsEntity>());
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
		Mockito.when(blSiteRepository.getAll()).thenReturn(new HashSet<BlacklistedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(allWhitelistedSites);
		Mockito.when(authHolderRepository.getAll()).thenReturn(new HashSet<AuthenticationHolderEntity>());
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(new HashSet<OAuth2AccessTokenEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(new HashSet<OAuth2RefreshTokenEntity>());
		Mockito.when(sysScopeRepository.getAll()).thenReturn(new HashSet<SystemScope>());
		
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();

		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));
		
		// check our scope list (this test)
		JsonArray sites = config.get(MITREidDataService.WHITELISTEDSITES).getAsJsonArray();

		assertThat(sites.size(), is(3));
		// check for both of our sites in turn
		Set<WhitelistedSite> checked = new HashSet<WhitelistedSite>();
		for (JsonElement e : sites) {
			assertThat(e.isJsonObject(), is(true));
			JsonObject site = e.getAsJsonObject();

			WhitelistedSite compare = null;
			if (site.get("id").getAsLong() == site1.getId().longValue()) {
				compare = site1;
			} else if (site.get("id").getAsLong() == site2.getId().longValue()) {
				compare = site2;
			} else if (site.get("id").getAsLong() == site3.getId().longValue()) {
				compare = site3;
			}
			
			if (compare == null) {
				fail("Could not find matching whitelisted site id: " + site.get("id").getAsString());
			} else {
				assertThat(site.get("clientId").getAsString(), equalTo(compare.getClientId()));
				checked.add(compare);
			}
		}
		// make sure all of our clients were found
		assertThat(checked.containsAll(allWhitelistedSites), is(true));
		
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
		
		System.err.println(configJson);
		
		JsonReader reader = new JsonReader(new StringReader(configJson));
		
        final Map<Long, WhitelistedSite> fakeDb = new HashMap<Long, WhitelistedSite>();
        when(wlSiteRepository.save(isA(WhitelistedSite.class))).thenAnswer(new Answer<WhitelistedSite>() {
            Long id = 3L;
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
    public void testExportGrants() throws IOException {
        Date creationDate1 = DateUtil.utcToDate("2014-09-10T22:49:44.090+0000");
        Date accessDate1 = DateUtil.utcToDate("2014-09-10T23:49:44.090+0000");
        
        WhitelistedSite mockWlSite1 = mock(WhitelistedSite.class);
        when(mockWlSite1.getId()).thenReturn(1L);
        
		ApprovedSite site1 = new ApprovedSite();
        site1.setId(1L);
        site1.setClientId("foo");
        site1.setCreationDate(creationDate1);
        site1.setAccessDate(accessDate1);
        site1.setUserId("user1");
        site1.setWhitelistedSite(mockWlSite1);
        site1.setAllowedScopes(ImmutableSet.of("openid", "phone"));

        Date creationDate2 = DateUtil.utcToDate("2014-09-11T18:49:44.090+0000");
        Date accessDate2 = DateUtil.utcToDate("2014-09-11T20:49:44.090+0000");
        Date timeoutDate2 = DateUtil.utcToDate("2014-10-01T20:49:44.090+0000");
        
		ApprovedSite site2 = new ApprovedSite();
        site2.setId(2L);
        site2.setClientId("bar");
        site2.setCreationDate(creationDate2);
        site2.setAccessDate(accessDate2);
        site2.setUserId("user2");
        site2.setAllowedScopes(ImmutableSet.of("openid", "offline_access", "email", "profile"));
        site2.setTimeoutDate(timeoutDate2);

		Set<ApprovedSite> allApprovedSites = ImmutableSet.of(site1, site2);
		
		Mockito.when(clientRepository.getAllClients()).thenReturn(new HashSet<ClientDetailsEntity>());
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(allApprovedSites);
		Mockito.when(blSiteRepository.getAll()).thenReturn(new HashSet<BlacklistedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(new HashSet<WhitelistedSite>());
		Mockito.when(authHolderRepository.getAll()).thenReturn(new HashSet<AuthenticationHolderEntity>());
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(new HashSet<OAuth2AccessTokenEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(new HashSet<OAuth2RefreshTokenEntity>());
		Mockito.when(sysScopeRepository.getAll()).thenReturn(new HashSet<SystemScope>());
		
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();

		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));
		
		// check our scope list (this test)
		JsonArray sites = config.get(MITREidDataService.GRANTS).getAsJsonArray();

		assertThat(sites.size(), is(2));
		// check for both of our sites in turn
		Set<ApprovedSite> checked = new HashSet<ApprovedSite>();
		for (JsonElement e : sites) {
			assertThat(e.isJsonObject(), is(true));
			JsonObject site = e.getAsJsonObject();

			ApprovedSite compare = null;
			if (site.get("id").getAsLong() == site1.getId().longValue()) {
				compare = site1;
			} else if (site.get("id").getAsLong() == site2.getId().longValue()) {
				compare = site2;
			}
			
			if (compare == null) {
				fail("Could not find matching whitelisted site id: " + site.get("id").getAsString());
			} else {
				assertThat(site.get("clientId").getAsString(), equalTo(compare.getClientId()));
                assertThat(site.get("creationDate").getAsString(), equalTo(DateUtil.toUTCString(compare.getCreationDate())));
                assertThat(site.get("accessDate").getAsString(), equalTo(DateUtil.toUTCString(compare.getAccessDate())));
                if(site.get("timeoutDate").isJsonNull()) {
                    assertNull(compare.getTimeoutDate());
                } else {
                    assertThat(site.get("timeoutDate").getAsString(), equalTo(DateUtil.toUTCString(compare.getTimeoutDate())));
                }
                assertThat(site.get("userId").getAsString(), equalTo(compare.getUserId()));
                assertThat(jsonArrayToStringSet(site.getAsJsonArray("allowedScopes")), equalTo(compare.getAllowedScopes()));
                if (site.get("whitelistedSiteId").isJsonNull()) {
                    assertNull(compare.getWhitelistedSite());
                } else {
                    assertThat(site.get("whitelistedSiteId").getAsLong(), equalTo(compare.getWhitelistedSite().getId()));
                }
				checked.add(compare);
			}
		}
		// make sure all of our clients were found
		assertThat(checked.containsAll(allApprovedSites), is(true));
    }
    
    @Test
    public void testImportGrants() throws IOException {
        Date creationDate1 = DateUtil.utcToDate("2014-09-10T22:49:44.090+0000");
        Date accessDate1 = DateUtil.utcToDate("2014-09-10T23:49:44.090+0000");
        
        WhitelistedSite mockWlSite1 = mock(WhitelistedSite.class);
        when(mockWlSite1.getId()).thenReturn(1L);
        
		ApprovedSite site1 = new ApprovedSite();
        site1.setId(1L);
        site1.setClientId("foo");
        site1.setCreationDate(creationDate1);
        site1.setAccessDate(accessDate1);
        site1.setUserId("user1");
        site1.setWhitelistedSite(mockWlSite1);
        site1.setAllowedScopes(ImmutableSet.of("openid", "phone"));

        Date creationDate2 = DateUtil.utcToDate("2014-09-11T18:49:44.090+0000");
        Date accessDate2 = DateUtil.utcToDate("2014-09-11T20:49:44.090+0000");
        Date timeoutDate2 = DateUtil.utcToDate("2014-10-01T20:49:44.090+0000");
        
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
                + "\"userId\":\"user1\",\"whitelistedSiteId\":null,\"allowedScopes\":[\"openid\",\"phone\"], \"whitelistedSiteId\":1}," +
				"{\"id\":2,\"clientId\":\"bar\",\"creationDate\":\"2014-09-11T18:49:44.090+0000\",\"accessDate\":\"2014-09-11T20:49:44.090+0000\","
                + "\"timeoutDate\":\"2014-10-01T20:49:44.090+0000\",\"userId\":\"user2\","
                + "\"allowedScopes\":[\"openid\",\"offline_access\",\"email\",\"profile\"]}" +
                
				"  ]" +
				"}";	
		
		System.err.println(configJson);
		
		JsonReader reader = new JsonReader(new StringReader(configJson));
		
        final Map<Long, ApprovedSite> fakeDb = new HashMap<Long, ApprovedSite>();
        when(approvedSiteRepository.save(isA(ApprovedSite.class))).thenAnswer(new Answer<ApprovedSite>() {
            Long id = 3L;
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
            Long id = 2L;
            @Override
            public WhitelistedSite answer(InvocationOnMock invocation) throws Throwable {
                WhitelistedSite _site = mock(WhitelistedSite.class);
                when(_site.getId()).thenReturn(id++);
                return _site;
            }
        });
        
		dataService.importData(reader);
        //2 for sites, 1 more for updating whitelistedSite ref on #2
		verify(approvedSiteRepository, times(3)).save(capturedApprovedSites.capture());
		
		List<ApprovedSite> savedSites = new ArrayList(fakeDb.values());
		
		assertThat(savedSites.size(), is(2));
                
		assertThat(savedSites.get(0).getClientId(), equalTo(site1.getClientId()));
        assertThat(savedSites.get(0).getAccessDate(), equalTo(site1.getAccessDate()));
        assertThat(savedSites.get(0).getCreationDate(), equalTo(site1.getCreationDate()));
        assertThat(savedSites.get(0).getAllowedScopes(), equalTo(site1.getAllowedScopes()));
        assertThat(savedSites.get(0).getIsWhitelisted(), equalTo(site1.getIsWhitelisted()));
        assertThat(savedSites.get(0).getTimeoutDate(), equalTo(site1.getTimeoutDate()));
        
		assertThat(savedSites.get(1).getClientId(), equalTo(site2.getClientId()));
        assertThat(savedSites.get(1).getAccessDate(), equalTo(site2.getAccessDate()));
        assertThat(savedSites.get(1).getCreationDate(), equalTo(site2.getCreationDate()));
        assertThat(savedSites.get(1).getAllowedScopes(), equalTo(site2.getAllowedScopes()));
		assertThat(savedSites.get(1).getTimeoutDate(), equalTo(site2.getTimeoutDate()));
        assertThat(savedSites.get(1).getIsWhitelisted(), equalTo(site2.getIsWhitelisted()));
    }
    
    @Test
    public void testExportAuthenticationHolders() throws IOException {
        AuthorizationRequest mockRequest1 = mock(AuthorizationRequest.class); 
        when(mockRequest1.getAuthorizationParameters()).thenReturn(new HashMap<String, String>());
        Authentication mockAuth1 = null;
        OAuth2Authentication auth1 = new OAuth2Authentication(mockRequest1, mockAuth1);
        
        AuthenticationHolderEntity holder1 = new AuthenticationHolderEntity();
        holder1.setId(1L);
        holder1.setAuthentication(auth1);
        
        AuthorizationRequest mockRequest2 = mock(AuthorizationRequest.class);
        when(mockRequest2.getAuthorizationParameters()).thenReturn(new HashMap<String, String>());
        Authentication mockAuth2 = null;
        OAuth2Authentication auth2 = new OAuth2Authentication(mockRequest2, mockAuth2);
        
        AuthenticationHolderEntity holder2 = new AuthenticationHolderEntity();
        holder2.setId(2L);
        holder2.setAuthentication(auth2);
        
		Set<AuthenticationHolderEntity> allAuthHolders = ImmutableSet.of(holder1, holder2);
		
		Mockito.when(clientRepository.getAllClients()).thenReturn(new HashSet<ClientDetailsEntity>());
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(new HashSet<WhitelistedSite>());
		Mockito.when(blSiteRepository.getAll()).thenReturn(new HashSet<BlacklistedSite>());
		Mockito.when(authHolderRepository.getAll()).thenReturn(allAuthHolders);
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(new HashSet<OAuth2AccessTokenEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(new HashSet<OAuth2RefreshTokenEntity>());
		Mockito.when(sysScopeRepository.getAll()).thenReturn(new HashSet<SystemScope>());
		
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();

		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));

		
		// check our holder list (this test)
		JsonArray holders = config.get(MITREidDataService.AUTHENTICATIONHOLDERS).getAsJsonArray();

		assertThat(holders.size(), is(2));
		// check for both of our clients in turn
		Set<AuthenticationHolderEntity> checked = new HashSet<AuthenticationHolderEntity>();
		for (JsonElement e : holders) {
			assertThat(e.isJsonObject(), is(true));
			JsonObject holder = e.getAsJsonObject();

			AuthenticationHolderEntity compare = null;
			if (holder.get("id").getAsLong() == holder1.getId()) {
				compare = holder1;
			} else if (holder.get("id").getAsLong() == holder2.getId()) {
				compare = holder2;
			}
			
			if (compare == null) {
				fail("Could not find matching authentication holder id: " + holder.get("id").getAsString());
			} else {
				assertTrue(holder.get("authentication").isJsonObject());
				checked.add(compare);
			}
		}
		// make sure all of our clients were found
		assertThat(checked.containsAll(allAuthHolders), is(true));
    }
    
    @Test
    public void testImportAuthenticationHolders() throws IOException {
        AuthorizationRequest mockRequest1 = mock(AuthorizationRequest.class); 
        when(mockRequest1.getAuthorizationParameters()).thenReturn(new HashMap<String, String>());
        Authentication mockAuth1 = null;
        OAuth2Authentication auth1 = new OAuth2Authentication(mockRequest1, mockAuth1);
        
        AuthenticationHolderEntity holder1 = new AuthenticationHolderEntity();
        holder1.setId(1L);
        holder1.setAuthentication(auth1);
        
        AuthorizationRequest mockRequest2 = mock(AuthorizationRequest.class);
        when(mockRequest2.getAuthorizationParameters()).thenReturn(new HashMap<String, String>());
        Authentication mockAuth2 = null;
        OAuth2Authentication auth2 = new OAuth2Authentication(mockRequest2, mockAuth2);
        
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
				
				"{\"id\":1,\"authentication\":{\"clientAuthorization\":{},\"userAuthentication\":\"\"}}," +
				"{\"id\":2,\"authentication\":{\"clientAuthorization\":{},\"userAuthentication\":\"\"}}" +
				
				"  ]" +
				"}";
			
		System.err.println(configJson);
		
		JsonReader reader = new JsonReader(new StringReader(configJson));
		
        final Map<Long, AuthenticationHolderEntity> fakeDb = new HashMap<Long, AuthenticationHolderEntity>();
        when(authHolderRepository.save(isA(AuthenticationHolderEntity.class))).thenAnswer(new Answer<AuthenticationHolderEntity>() {
            Long id = 3L;
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
		assertThat(savedAuthHolders.get(0).getAuthentication().getName(), equalTo(holder1.getAuthentication().getName()));
        assertThat(savedAuthHolders.get(1).getAuthentication().getName(), equalTo(holder2.getAuthentication().getName()));
    }
    
	@Test
	public void testExportSystemScopes() throws IOException {		
		SystemScope scope1 = new SystemScope();
		scope1.setId(1L);
		scope1.setValue("scope1");
		scope1.setDescription("Scope 1");
		scope1.setAllowDynReg(false);
		scope1.setDefaultScope(false);
		scope1.setIcon("glass");

		SystemScope scope2 = new SystemScope();
		scope2.setId(2L);
		scope2.setValue("scope2");
		scope2.setDescription("Scope 2");
		scope2.setAllowDynReg(true);
		scope2.setDefaultScope(false);
		scope2.setIcon("ball");

		SystemScope scope3 = new SystemScope();
		scope3.setId(3L);
		scope3.setValue("scope3");
		scope3.setDescription("Scope 3");
		scope3.setAllowDynReg(true);
		scope3.setDefaultScope(true);
		scope3.setIcon("road");

		Set<SystemScope> allScopes = ImmutableSet.of(scope1, scope2, scope3);
		
		Mockito.when(clientRepository.getAllClients()).thenReturn(new HashSet<ClientDetailsEntity>());
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
		Mockito.when(wlSiteRepository.getAll()).thenReturn(new HashSet<WhitelistedSite>());
		Mockito.when(blSiteRepository.getAll()).thenReturn(new HashSet<BlacklistedSite>());
		Mockito.when(authHolderRepository.getAll()).thenReturn(new HashSet<AuthenticationHolderEntity>());
		Mockito.when(tokenRepository.getAllAccessTokens()).thenReturn(new HashSet<OAuth2AccessTokenEntity>());
		Mockito.when(tokenRepository.getAllRefreshTokens()).thenReturn(new HashSet<OAuth2RefreshTokenEntity>());
		Mockito.when(sysScopeRepository.getAll()).thenReturn(allScopes);
		
		// do the data export
		StringWriter stringWriter = new StringWriter();
		JsonWriter writer = new JsonWriter(stringWriter);
		writer.beginObject();
		dataService.exportData(writer);
		writer.endObject();
		writer.close();
		
		// parse the output as a JSON object for testing
		JsonElement elem = new JsonParser().parse(stringWriter.toString());
		JsonObject root = elem.getAsJsonObject();

		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
        assertThat(config.has(MITREidDataService.WHITELISTEDSITES), is(true));
        assertThat(config.has(MITREidDataService.BLACKLISTEDSITES), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.WHITELISTEDSITES).isJsonArray(), is(true));
        assertThat(config.get(MITREidDataService.BLACKLISTEDSITES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.REFRESHTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.ACCESSTOKENS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.SYSTEMSCOPES).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.AUTHENTICATIONHOLDERS).isJsonArray(), is(true));

		
		// check our scope list (this test)
		JsonArray scopes = config.get(MITREidDataService.SYSTEMSCOPES).getAsJsonArray();

		assertThat(scopes.size(), is(3));
		// check for both of our clients in turn
		Set<SystemScope> checked = new HashSet<SystemScope>();
		for (JsonElement e : scopes) {
			assertThat(e.isJsonObject(), is(true));
			JsonObject scope = e.getAsJsonObject();

			SystemScope compare = null;
			if (scope.get("value").getAsString().equals(scope1.getValue())) {
				compare = scope1;
			} else if (scope.get("value").getAsString().equals(scope2.getValue())) {
				compare = scope2;
			} else if (scope.get("value").getAsString().equals(scope3.getValue())) {
				compare = scope3;
			}
			
			if (compare == null) {
				fail("Could not find matching scope value: " + scope.get("value").getAsString());
			} else {
				assertThat(scope.get("value").getAsString(), equalTo(compare.getValue()));
				assertThat(scope.get("description").getAsString(), equalTo(compare.getDescription()));
				assertThat(scope.get("icon").getAsString(), equalTo(compare.getIcon()));
				assertThat(scope.get("allowDynReg").getAsBoolean(), equalTo(compare.isAllowDynReg()));
				assertThat(scope.get("defaultScope").getAsBoolean(), equalTo(compare.isDefaultScope()));
				checked.add(compare);
			}
		}
		// make sure all of our clients were found
		assertThat(checked.containsAll(allScopes), is(true));
		
	}

	@Test
	public void testImportSystemScopes() throws IOException {
		SystemScope scope1 = new SystemScope();
		scope1.setId(1L);
		scope1.setValue("scope1");
		scope1.setDescription("Scope 1");
		scope1.setAllowDynReg(false);
		scope1.setDefaultScope(false);
		scope1.setIcon("glass");

		SystemScope scope2 = new SystemScope();
		scope2.setId(2L);
		scope2.setValue("scope2");
		scope2.setDescription("Scope 2");
		scope2.setAllowDynReg(true);
		scope2.setDefaultScope(false);
		scope2.setIcon("ball");

		SystemScope scope3 = new SystemScope();
		scope3.setId(3L);
		scope3.setValue("scope3");
		scope3.setDescription("Scope 3");
		scope3.setAllowDynReg(true);
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
				
				"{\"id\":1,\"description\":\"Scope 1\",\"icon\":\"glass\",\"value\":\"scope1\",\"allowDynReg\":false,\"defaultScope\":false}," +
				"{\"id\":2,\"description\":\"Scope 2\",\"icon\":\"ball\",\"value\":\"scope2\",\"allowDynReg\":true,\"defaultScope\":false}," +
				"{\"id\":3,\"description\":\"Scope 3\",\"icon\":\"road\",\"value\":\"scope3\",\"allowDynReg\":true,\"defaultScope\":true}" +
				
				"  ]" +
				"}";	
		
		System.err.println(configJson);
		
		JsonReader reader = new JsonReader(new StringReader(configJson));
		
		dataService.importData(reader);
		verify(sysScopeRepository, times(3)).save(capturedScope.capture());
		
		List<SystemScope> savedScopes = capturedScope.getAllValues();
		
		assertThat(savedScopes.size(), is(3));
		assertThat(savedScopes.get(0).getValue(), equalTo(scope1.getValue()));
		assertThat(savedScopes.get(0).getDescription(), equalTo(scope1.getDescription()));
		assertThat(savedScopes.get(0).getIcon(), equalTo(scope1.getIcon()));
		assertThat(savedScopes.get(0).isDefaultScope(), equalTo(scope1.isDefaultScope()));
		assertThat(savedScopes.get(0).isAllowDynReg(), equalTo(scope1.isAllowDynReg()));

		assertThat(savedScopes.get(1).getValue(), equalTo(scope2.getValue()));
		assertThat(savedScopes.get(1).getDescription(), equalTo(scope2.getDescription()));
		assertThat(savedScopes.get(1).getIcon(), equalTo(scope2.getIcon()));
		assertThat(savedScopes.get(1).isDefaultScope(), equalTo(scope2.isDefaultScope()));
		assertThat(savedScopes.get(1).isAllowDynReg(), equalTo(scope2.isAllowDynReg()));

		assertThat(savedScopes.get(2).getValue(), equalTo(scope3.getValue()));
		assertThat(savedScopes.get(2).getDescription(), equalTo(scope3.getDescription()));
		assertThat(savedScopes.get(2).getIcon(), equalTo(scope3.getIcon()));
		assertThat(savedScopes.get(2).isDefaultScope(), equalTo(scope3.isDefaultScope()));
		assertThat(savedScopes.get(2).isAllowDynReg(), equalTo(scope3.isAllowDynReg()));
		
	}
	
	private Set<String> jsonArrayToStringSet(JsonArray a) {
		Set<String> s = new HashSet<String>();
		for (JsonElement jsonElement : a) {
			s.add(jsonElement.getAsString());
		}
		return s;
	}

}
