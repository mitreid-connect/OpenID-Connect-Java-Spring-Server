package org.mitre.oauth2.service.impl;

import static org.junit.Assert.*;


import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntityFactory;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntityFactory;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
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

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring-servlet.xml", "classpath:test-context.xml"})

public class DefaultOAuth2ProviderTokenServiceTest {
	private Logger logger; 
	
    private ClientDetailsEntity clienttest = new ClientDetailsEntity();
	private OAuth2TokenEntityService tokenService;
	private OAuth2AccessTokenEntity accessToken;
    private OAuth2RefreshTokenEntity refreshToken;
    private AuthorizationRequest authorizationRequest;
    private Authentication userAuthentication;
	
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
        
        clienttest.setClientId("XVX42QQA9CA348S46TNJ00NP8MRO37FHO1UW748T59BAT74LN9");
        clienttest.setClientSecret("password");
        clienttest.setClientName("a test client service");
        clienttest.setOwner("some owner person"); 
        clienttest.setClientDescription("Lorem ipsum dolor sit amet, er aliquam adipiscing lacus. Ut nec urna");
        clienttest.setAccessTokenTimeout((long) 10);
        clienttest.setRefreshTokenTimeout((long) 360);
        clienttest.setAllowRefresh(true); // db handles actual value

     
        //TODO model question: what are correct values for this field?
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        GrantedAuthority roleClient = new SimpleGrantedAuthority("ROLE_CLIENT");
        authorities.add(roleClient);
        clienttest.setAuthorities(authorities);
       
		Set<String> resourceIds = new HashSet<String>();
	        resourceIds.add("https://www.whatg.com/thing/");
	        resourceIds.add("https://www.whatg.com/else/");
	        resourceIds.add("https://www.whatg.com/some/");
        clienttest.setResourceIds(resourceIds);

        Set<String> authorizedGrantTypes = new HashSet<String>();
        //TODO model question: what are correct values for this field?
    	authorizedGrantTypes.add("authorization_code");
		authorizedGrantTypes.add("refresh_token");
		clienttest.setAuthorizedGrantTypes(authorizedGrantTypes);

		Set<String> scope = new HashSet<String>();
		scope.add(""); //TODO model question: what are correct values for this field?
		clienttest.setScope(scope);
		
		authorizationRequest =  new AuthorizationRequest(
				clienttest.getClientId(),clienttest.getScope(), authorities, clienttest.getResourceIds());

	    userAuthentication = null; // user authentication may be null
	    
        refreshToken = new OAuth2RefreshTokenEntity();
        refreshToken.setValue("REFRESH-TOKEN-VALUE");
        refreshToken.setClient(clienttest);
        refreshToken.setScope(clienttest.getScope());
        
        accessToken = new OAuth2AccessTokenEntity();
        accessToken.setValue("ACCESS-TOKEN-VALUE");
        accessToken.setClient(clienttest);
        accessToken.setScope(clienttest.getScope());
        accessToken.setRefreshToken(refreshToken);
        accessToken.setTokenType("bearer");
        
        tokenService =  DefaultOAuth2ProviderTokenService.makeBuilder()
                .setTokenRepository(tokenRepository)
                .setClientDetailsService(clientDetailsService)
                .setAccessTokenFactory(accessTokenFactory)
                .setRefreshTokenFactory(refreshTokenFactory)
                .finish();
	}

	@After
	public void tearDown() throws Exception {
		tokenService = null;
		clienttest = null;
		logger = LoggerFactory.getLogger(this.getClass());
        logger.info("teardown of DefaultOAuth2ProviderTokenServiceTest");
        
	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public final void testCreateAccessToken_throwAuthenticationCredentialsNotFoundException_withnull() {
		tokenService.createAccessToken(null);
	}
	
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public final void testCreateAccessToken_throwAuthenticationCredentialsNotFoundException() {

		OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest, userAuthentication);
        accessToken.setAuthentication(authentication);

        expect(clientDetailsService.loadClientByClientId(clienttest.getClientId())).andReturn(clienttest).once();            
        replay(clientDetailsService);
         
        expect(accessTokenFactory.createNewAccessToken()).andReturn(accessToken).once();
        replay(accessTokenFactory);
         
        expect(refreshTokenFactory.createNewRefreshToken()).andReturn(refreshToken).once();
        replay(refreshTokenFactory);
         
        expect(tokenRepository.saveRefreshToken(refreshToken)).andReturn(refreshToken).once();
        expect(tokenRepository.saveAccessToken(accessToken)).andReturn(accessToken).once();
        replay(tokenRepository);

		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) tokenService.createAccessToken(authentication);
        
        verify(tokenRepository);
        verify(clientDetailsService);
        verify(accessTokenFactory);
        verify(refreshTokenFactory);
        
        assertThat(token, is(accessToken));
	}
		
	@Test(expected = InvalidClientException.class)
	public final void testCreateAccessToken_throwInvalidClientException_withNullAuthReq() {	
		AuthorizationRequest authorizationRequest = null;
		OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest, null);
		tokenService.createAccessToken(authentication);
	}	
	@Test(expected = AuthenticationException.class)
	public final void testCreateAccessToken_throwAuthenticationException() {
		OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest, userAuthentication);		
		tokenService.createAccessToken(authentication);
	}	
	@Test
	public final void testCreateAccessToken_valid() {
		AuthorizationRequest authorizationRequest = null;
		Authentication userAuthentication = null;
		OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest, userAuthentication);		
		tokenService.createAccessToken(authentication);
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
