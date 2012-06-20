package org.mitre.key.fetch;

import static org.junit.Assert.assertEquals;


import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwk.model.EC;
import org.mitre.jwk.model.Jwk;
import org.mitre.jwk.model.Rsa;
import org.mitre.openid.connect.client.OIDCServerConfiguration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class KeyFetcherTest {
	
	private KeyFetcher keyFetch;
	
	@Before
	public void setUp(){
		keyFetch = EasyMock.createMock(KeyFetcher.class);
	}
	
	@After
	public void tearDown(){
	}
	
	@Test
	public void retrieveJwkTest(){
		//EasyMock.expect(keyFetch.retrieveJwk()).andReturn(Rsa(new JsonObject(object))).once();
	}
	
	@Test
	public void retrieveX509Key(){
		
	}
	
	@Test
	public void retriveJwkKey(){
		
	}

}
