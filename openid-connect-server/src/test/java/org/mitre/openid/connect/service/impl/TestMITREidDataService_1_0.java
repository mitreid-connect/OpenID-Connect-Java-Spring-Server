package org.mitre.openid.connect.service.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
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
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.service.MITREidDataService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

		Set<ClientDetailsEntity> allClients = ImmutableSet.of(client1, client2);
		
		Mockito.when(clientRepository.getAllClients()).thenReturn(allClients);
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
		JsonObject root = elem.getAsJsonObject();
		
		// make sure the root is there
		assertThat(root.has(MITREidDataService.MITREID_CONNECT_1_0), is(true));
		
		JsonObject config = root.get(MITREidDataService.MITREID_CONNECT_1_0).getAsJsonObject();
		
		// make sure all the root elements are there
		assertThat(config.has(MITREidDataService.CLIENTS), is(true));
		assertThat(config.has(MITREidDataService.GRANTS), is(true));
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
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
		assertThat(config.has(MITREidDataService.REFRESHTOKENS), is(true));
		assertThat(config.has(MITREidDataService.ACCESSTOKENS), is(true));
		assertThat(config.has(MITREidDataService.SYSTEMSCOPES), is(true));
		assertThat(config.has(MITREidDataService.AUTHENTICATIONHOLDERS), is(true));
		
		// make sure the root elements are all arrays
		assertThat(config.get(MITREidDataService.CLIENTS).isJsonArray(), is(true));
		assertThat(config.get(MITREidDataService.GRANTS).isJsonArray(), is(true));
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

	private Set<String> jsonArrayToStringSet(JsonArray a) {
		Set<String> s = new HashSet<String>();
		for (JsonElement jsonElement : a) {
			s.add(jsonElement.getAsString());
		}
		return s;
	}

}
