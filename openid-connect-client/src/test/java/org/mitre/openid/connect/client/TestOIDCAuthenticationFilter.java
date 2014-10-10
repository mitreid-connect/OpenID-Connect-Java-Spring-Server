package org.mitre.openid.connect.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.OIDCAuthenticationFilter;
import org.mitre.openid.connect.client.OIDCAuthenticationProvider;
import org.mitre.openid.connect.client.service.impl.PlainAuthRequestUrlBuilder;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class TestOIDCAuthenticationFilter {
	
	private OIDCAuthenticationFilter authenticationFilter= new OIDCAuthenticationFilter();
	private ServerConfiguration serverConfig;
	private RegisteredClient clientConfig;
	private PlainAuthRequestUrlBuilder authRequestBuilder;
	private Map<String, String> options = ImmutableMap.of("foo", "bar");
	private String redirectUri = "https://client.example.org/",
			       nonce = "34fasf3ds",
			       state = "af0ifjsldkj";
			       
	@Before
	public void prepare() {
		serverConfig = Mockito.mock(ServerConfiguration.class);
		Mockito.when(serverConfig.getAuthorizationEndpointUri()).thenReturn("https://server.example.com/authorize");
		
		
		clientConfig = Mockito.mock(RegisteredClient.class);
		Mockito.when(clientConfig.getClientId()).thenReturn("s6BhdRkqt3");
		Mockito.when(clientConfig.getScope()).thenReturn(Sets.newHashSet("openid", "profile"));
		
		authRequestBuilder = Mockito.mock(PlainAuthRequestUrlBuilder.class);
		Mockito.when(authRequestBuilder.buildAuthRequestUrl(serverConfig, clientConfig, redirectUri, state, options)).thenReturn("WithoutNonce");
		Mockito.when(authRequestBuilder.buildAuthRequestUrl(serverConfig, clientConfig, redirectUri,nonce, state, options)).thenReturn("WithNonce");
		authenticationFilter.setAuthRequestUrlBuilder(authRequestBuilder);
	}

	@Test
	public void testGetAuthRequestUrlWithNonce(){
		testGetAuthRequestUrl(true);
	}
	@Test
	public void testGetAuthRequestUrlWithoutNonce(){
		testGetAuthRequestUrl(false);
	}
	
	private boolean containsNonce(){
		return authenticationFilter.getAuthRequestUrl(serverConfig, clientConfig, redirectUri, nonce, state, options).equals("WithNonce");
	}
	
	private void testGetAuthRequestUrl(boolean useNonce){			
		Mockito.when(serverConfig.isUseNonce()).thenReturn(useNonce);		
		assertThat(containsNonce(), equalTo(serverConfig.isUseNonce())); 	      
	}
	
}
