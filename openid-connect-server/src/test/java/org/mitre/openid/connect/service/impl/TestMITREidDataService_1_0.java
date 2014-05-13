package org.mitre.openid.connect.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;

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
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

@RunWith(MockitoJUnitRunner.class)
public class TestMITREidDataService_1_0 {

	@Mock
    private OAuth2ClientRepository clientRepository;
	@Mock
    private ApprovedSiteRepository approvedSiteRepository;
	@Mock
    private AuthenticationHolderRepository authHolderRepository;
	@Mock
    private OAuth2TokenRepository tokenRepository;
	@Mock
    private SystemScopeRepository sysScopeRepository;

	@InjectMocks
	private MITREidDataService_1_0 dataService;

	@Before
	public void prepare() {
		Mockito.reset(clientRepository, approvedSiteRepository, authHolderRepository, tokenRepository, sysScopeRepository);
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
		client2.setScope(ImmutableSet.of("foo", "dolphin", "electric-wombat"));
		client2.setGrantTypes(ImmutableSet.of("client_credentials", "urn:ietf:params:oauth:grant_type:redelegate"));
		client2.setAllowIntrospection(false);

		Mockito.when(clientRepository.getAllClients()).thenReturn(ImmutableSet.of(client1, client2));
		Mockito.when(approvedSiteRepository.getAll()).thenReturn(new HashSet<ApprovedSite>());
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
		
		
		
		
	}

}
