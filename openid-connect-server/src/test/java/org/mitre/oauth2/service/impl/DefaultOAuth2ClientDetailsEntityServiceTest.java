/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
 ******************************************************************************/

package org.mitre.oauth2.service.impl;

import static org.junit.Assert.*;

import java.awt.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntityFactory;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring-servlet.xml", "classpath:test-context.xml"})

public class DefaultOAuth2ClientDetailsEntityServiceTest {

	private Logger logger; 
    
	@Autowired
	private DefaultOAuth2ClientDetailsEntityService entityService;
	
	@Autowired
	private OAuth2ClientRepository clientRepository;
	
	@Autowired
	private OAuth2TokenRepository tokenRepository;
	
	@Autowired
	private ClientDetailsEntityFactory clientFactory;

	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(this.getClass());
        logger.info("setUp of DefaultOAuth2ClientDetailsEntityServiceTest");

        clientRepository = createNiceMock(OAuth2ClientRepository.class);
        tokenRepository = createNiceMock(OAuth2TokenRepository.class);
        clientFactory = createNiceMock(ClientDetailsEntityFactory.class);
        
        entityService = new DefaultOAuth2ClientDetailsEntityService(
        		clientRepository, 
    			tokenRepository, 
    			clientFactory);     
	}

	@After
	public void tearDown() throws Exception {
		entityService = null;
		logger = LoggerFactory.getLogger(this.getClass());
        logger.info("tearDown of DefaultOAuth2ClientDetailsEntityServiceTest");
        
	}

    @Test(expected = IllegalArgumentException.class)
	public void testLoadClientByClientId_throwIllegalArgumentException() {
		//Try NULL clientID, should get:("Client id must not be empty!");
		entityService.loadClientByClientId(null);
	}
    
    @Test(expected = IllegalArgumentException.class)
	public void testLoadClientByClientId_throwInvalidClientException() {
		entityService.loadClientByClientId("");
	}  
    
    @Test(expected = InvalidClientException.class)
    public void testLoadClientByClientId_throwInvalidClientException_withNonExistantClient() {
        expect(clientRepository.getClientById("Idon'texist/client")).andReturn(null).once();
        replay(clientRepository);
        entityService.loadClientByClientId("Idon'texist/client");
        verify(clientRepository);
    }
    	
    @Test(expected = OAuth2Exception.class)
	public void testLoadClientByClientId_throwsOAuth2Exception() {
	    //TODO loadClientByClientId throws OAuth2Exception how to trigger?
    	logger.warn("throws OAuth2Exception How to trigger?");
		fail("Not yet implemented -throws OAuth2Exception How to trigger?");
	}
    
    @Test
	public void testLoadClientByClientId_withValidID() {
        ClientDetailsEntity testClient = new ClientDetailsEntity();
	        testClient.setClientId("111222333");
	        testClient.setClientSecret("password");
    	expect(clientRepository.getClientById(testClient.getClientId())).andReturn(testClient).once();
        replay(clientRepository);
        
        ClientDetailsEntity clientFromService = null;
        try {
			clientFromService = entityService.loadClientByClientId(testClient.getClientId());
		} catch (Exception e) {
			e.printStackTrace();
		}
        verify(clientRepository);
        
        assertThat(clientFromService, is(not(nullValue())));
        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        assertThat(clientFromService, CoreMatchers.equalTo(testClient));		
        assertThat(clientFromService.getClientId(), equalTo(testClient.getClientId()));
        testClient = null;
	}
       
    @Test
    public void testCreateClient_withValidClientData() {
    	ClientDetailsEntity testClient = new ClientDetailsEntity();
    	testClient.setClientId("111222333");
        testClient.setClientName("a test client service");
        testClient.setOwner("some owner person"); 
        testClient.setClientDescription("Lorem ipsum dolor sit ametr aliquam adipiscing lacus. Ut nec urna");
        testClient.setAccessTokenTimeout((long) 10);
        testClient.setRefreshTokenTimeout((long) 360);
        testClient.setAllowRefresh(true); 
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        GrantedAuthority roleClient = new SimpleGrantedAuthority("ROLE_CLIENT");
        authorities.add(roleClient);
        testClient.setAuthorities(authorities);
		Set<String> resourceIds = new HashSet<String>();
	        resourceIds.add("http://127.0.0.1/whatg/thing/");
	        resourceIds.add("https://www.someRedirectURI.com/redirectURI");
	        testClient.setResourceIds(resourceIds);
        Set<String> authorizedGrantTypes = new HashSet<String>();
    	authorizedGrantTypes.add("authorization_code");
		authorizedGrantTypes.add("refresh_token");
		testClient.setAuthorizedGrantTypes(authorizedGrantTypes);
		Set<String> scope = new HashSet<String>();
		scope.add("openid");
		testClient.setScope(scope);
		
        ClientDetailsEntity clientFromService = null;
    	expect(clientRepository.getClientById(testClient.getClientId())).andReturn(testClient).once();
        expect(clientFactory.createClient(testClient.getClientId(), testClient.getClientSecret())).andReturn(testClient).once();
        expect(clientRepository.saveClient(testClient));
    	replay(clientRepository);
        replay(clientFactory);
        verify(clientRepository);
        verify(clientFactory);
	    expectLastCall();	 	  	
	        
        try { 	        	
			clientFromService = entityService.createClient(
					testClient.getClientId(), 
					testClient.getClientSecret(), 
					testClient.getScope(), 
					testClient.getAuthorizedGrantTypes(), 
					"", //TODO the get method doesn't work for redirecturi back in
					testClient.getAuthorities(), 
					testClient.getResourceIds(), 
					testClient.getClientName(), 
					testClient.getClientDescription(), 
					testClient.isAllowRefresh(), 
					testClient.getAccessTokenTimeout(), 
					testClient.getRefreshTokenTimeout(), 
					testClient.getOwner()
			);
		 	          
		} catch (Exception e) {
			e.printStackTrace();
		}
        verify(clientRepository);
        verify(clientFactory);

        assertThat(clientFromService, is(not(nullValue())));
        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        // TODO need make a test for all fields
        assertThat(clientFromService.getClientId(), equalTo(testClient.getClientId()));
        assertThat(clientFromService, CoreMatchers.equalTo(testClient));
    }
        
	@Test
	public void testCreateClient_withINVALIDClientData() {
		// Test new client create with INVALID data
        logger.warn("What should we get if we submit invalid data?");
    	ClientDetailsEntity testClient = new ClientDetailsEntity();
    	testClient.setClientId(" # % & * { } : ' < > ? +" + "\\" + "\\/");
        testClient.setClientName("a test client service");
        testClient.setOwner("some owner person"); 
        testClient.setClientDescription("<script type=\"text/javascript\">alert(\"I am an alert box!\");</script>");
        testClient.setAccessTokenTimeout(Long.MIN_VALUE);
        testClient.setRefreshTokenTimeout(Long.MIN_VALUE);
        testClient.setAllowRefresh(false); 
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        GrantedAuthority roleClient = new SimpleGrantedAuthority("ADMIN");
        authorities.add(roleClient);
        testClient.setAuthorities(authorities);
		Set<String> resourceIds = new HashSet<String>();
			resourceIds.add("https://www.whatg.com/some/ # % & * { } : ' < > ? + \\ \\/");
	        resourceIds.add("http://127.0.0.1/whatg/thing/");
	        resourceIds.add("htts://www.whatg.com/thing/");
	        resourceIds.add("htps://www.whatg.com/else/");
	        resourceIds.add("https://www.someRedirectURI.com/redirectURI");
	        testClient.setResourceIds(resourceIds);
        Set<String> authorizedGrantTypes = new HashSet<String>();
    	authorizedGrantTypes.add("authorization_code");
		authorizedGrantTypes.add("refresh_token");
		testClient.setAuthorizedGrantTypes(authorizedGrantTypes);
		Set<String> scope = new HashSet<String>();
		scope.add("openid");
		testClient.setScope(scope);
		        
		
    	expect(clientRepository.getClientById(testClient.getClientId())).andReturn(testClient).once();
        expect(clientFactory.createClient(testClient.getClientId(), testClient.getClientSecret())).andReturn(testClient).once();
        expect(clientRepository.saveClient(testClient));
        replay(clientFactory);
        replay(clientRepository);
        verify(clientFactory);
	    expectLastCall();	 	
	    
	    ClientDetailsEntity clientFromService = null;
		 try {
			clientFromService = entityService.createClient(
					testClient.getClientId(), 
					testClient.getClientSecret(), 
					testClient.getScope(), 
					testClient.getAuthorizedGrantTypes(), 
					"", 
					testClient.getAuthorities(), 
					testClient.getResourceIds(), 
					testClient.getClientName(), 
					testClient.getClientDescription(), 
					testClient.isAllowRefresh(), 
					testClient.getAccessTokenTimeout(), 
					testClient.getRefreshTokenTimeout(), 
					testClient.getOwner()
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    verify(clientRepository);

        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        // TODO need make a test for all fields
        assertThat(clientFromService.getClientId(), equalTo(testClient.getClientId()));
        assertThat(clientFromService.getClientName(), equalTo(testClient.getClientName()));   
        assertThat(clientFromService, CoreMatchers.equalTo(testClient));
        
	}	

    @Test(expected = InvalidClientException.class)
	public void testDeleteClient_throwInvalidClientException() {

    	ClientDetailsEntity testClient = new ClientDetailsEntity();
    	testClient.setClientId("111222333");
    	expect(clientRepository.getClientById(testClient.getClientId())).andReturn(testClient).once();
        expect(clientRepository.getClientById(testClient.getClientId())).andReturn(null).once(); 
        replay(clientRepository);
    	
    	try {
    		// this function is going to return void      
    		entityService.deleteClient(testClient);
		} catch (Exception e) {
			e.printStackTrace();
		}
        verify(clientRepository);
               
       // can't assert that the object is gone because its a mock throughout
       // has to be done in repository tests
	}

    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_oldnull() {		
    	ClientDetailsEntity testClient = new ClientDetailsEntity();
		entityService.updateClient(null, testClient);		
	}
    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_newnull() {		
    	ClientDetailsEntity testClient = new ClientDetailsEntity();
		entityService.updateClient(testClient, null);
	}    
    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_nullnull() {		
		entityService.updateClient(null, null);
	}    
    
	@Test
	public void testUpdateClient() {
		// get an existing client, change a field, verify field changed from service	
		ClientDetailsEntity originalClient =  new ClientDetailsEntity();
		originalClient.setOwner("first client owner");
		originalClient.setClientId("TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");
		ClientDetailsEntity newClient = new ClientDetailsEntity();
		newClient.setOwner("new client order");
		newClient.setClientId("TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");

		expect(clientRepository.getClientById(originalClient.getClientId())).andReturn(originalClient).once();
        replay(clientRepository);
        
        expect(clientRepository.updateClient(originalClient.getClientId(), newClient));
        replay(clientRepository);
		entityService.updateClient(originalClient, newClient);      
		verify(clientRepository);
		ClientDetailsEntity currClient = entityService.loadClientByClientId(originalClient.getClientId());
        verify(clientRepository);
       
        assertThat(currClient.getOwner(), equalTo(newClient.getOwner()));   
        assertSame(newClient, currClient);
        assertThat(currClient.getOwner(), not(equalTo(originalClient.getOwner())));
     	assertNotSame(originalClient, currClient);
     	
	}

	@Test
	public void testGetAllClients() { 	
    	ClientDetailsEntity currentClient[] = new ClientDetailsEntity[4];
    	currentClient[0] = new ClientDetailsEntity();
		currentClient[0].setClientId("TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");
    	currentClient[1] = new ClientDetailsEntity();
    	currentClient[1].setClientId("FRY83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L56EEB14000");
    	currentClient[2] = new ClientDetailsEntity();
    	currentClient[2].setClientId("GST55YEA4CE763Z21WSE83NL0LGM65SKD0GN945L66EEB19999");
    	currentClient[3] = new ClientDetailsEntity();
    	currentClient[3].setClientId("H57T8YEA4CE763Z21WSE83NL0LGM65SKD0GN945L76EEB19999");        
    	
    	
    	Collection<ClientDetailsEntity> cdeColl = Arrays.asList(currentClient);
    	
    	int foundcnt = currentClient.length; // number of matches we need to 
    	
        expect(clientRepository.getClientById(currentClient[0].getClientId())).andReturn(currentClient[0]).once();
        expect(clientRepository.getClientById(currentClient[1].getClientId())).andReturn(currentClient[1]).once();
        expect(clientRepository.getClientById(currentClient[2].getClientId())).andReturn(currentClient[2]).once();
        expect(clientRepository.getClientById(currentClient[3].getClientId())).andReturn(currentClient[3]).once();
        expect(clientRepository.getAllClients()).andReturn(cdeColl);
        replay(clientRepository);		
	    verify(clientRepository);
        //check that each one exists in this loop only once
	    Collection<ClientDetailsEntity> clientCollection =  entityService.getAllClients();
	    for (ClientDetailsEntity clientDetailsEntity : clientCollection) {
	    	for (int i = 0;i<currentClient.length;i++){
	    		if (currentClient[i].getClientId() == clientDetailsEntity.getClientId()){
	    			foundcnt--;
	    		}
	    	}
		}
	    assertSame((int) 0, foundcnt);
//		fail("Not yet implemented - Have to create orignal objects and check entityservice results against them"); //TODO 
	}
	
}
