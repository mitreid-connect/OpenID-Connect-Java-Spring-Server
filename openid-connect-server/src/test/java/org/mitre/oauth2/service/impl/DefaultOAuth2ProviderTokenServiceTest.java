package org.mitre.oauth2.service.impl;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.OAuth2AccessTokenEntityFactory;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntityFactory;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.impl.DefaultOAuth2ProviderTokenService.DefaultOAuth2ProviderTokenServicesBuilder;


import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.equalTo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring-servlet.xml", "classpath:test-context.xml"})

public class DefaultOAuth2ProviderTokenServiceTest {


	private DefaultOAuth2ProviderTokenService tokenService;
	private Logger logger; 
	
	@Autowired
	private OAuth2TokenRepository tokenRepository;
	
	@Autowired
	private ClientDetailsEntityService clientDetailsService;
	
	@Autowired
	private OAuth2AccessTokenEntityFactory accessTokenFactory;
	
	@Autowired
	private OAuth2RefreshTokenEntityFactory refreshTokenFactory;
	
	
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(this.getClass());
        logger.info("setUp of DefaultOAuth2ProviderTokenServiceTest");
        
        tokenRepository = createNiceMock(OAuth2TokenRepository.class);
        clientDetailsService = createNiceMock(ClientDetailsEntityService.class);
        accessTokenFactory = createNiceMock(OAuth2AccessTokenEntityFactory.class);
        refreshTokenFactory = createNiceMock(OAuth2RefreshTokenEntityFactory.class);
        
        //TODO is this the right constructor?
        //tokenService = new DefaultOAuth2ProviderTokenServicesBuilder();
        tokenService = new DefaultOAuth2ProviderTokenService();
	}

	@After
	public void tearDown() throws Exception {
		tokenService = null;
		
		logger = LoggerFactory.getLogger(this.getClass());
        logger.info("teardown of DefaultOAuth2ProviderTokenServiceTest");
        
	}

	@Test(expected = AuthenticationException.class)
	public final void testCreateAccessToken_AuthExp() {
		fail("Not yet implemented"); // TODO
	}
	
	@Test(expected = InvalidClientException.class)
	public final void testCreateAccessToken_InvalidclientExp() {	
		fail("Not yet implemented"); // TODO
	}	
	
	

	
	@Test
	public final void testRefreshAccessToken() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testLoadAuthentication() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetAccessTokenString() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetAccessTokenOAuth2Authentication() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetRefreshToken() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testRevokeRefreshToken() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testRevokeAccessToken() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetAccessTokensForClient() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetRefreshTokensForClient() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testClearExpiredTokens() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testMakeBuilder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testReadAccessToken() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testSaveAccessToken() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testSaveRefreshToken() {
		fail("Not yet implemented"); // TODO
	}

}
