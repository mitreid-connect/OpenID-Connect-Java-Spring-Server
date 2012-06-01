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
        		clientRepository, tokenRepository, clientFactory);
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
        ClientDetailsEntity clientFromService = null;
        
		ClientDetailsEntity testClient =  new ClientDetailsEntity();
		testClient.setClientId("123client");
		testClient.setOwner("Test Entity Owner");
    	
    	expect(clientRepository.getClientById(testClient.getClientId())).andReturn(testClient).once();
        replay(clientRepository);

        try {
			clientFromService = entityService.loadClientByClientId(testClient.getClientId());
		} catch (Exception e) {
			e.printStackTrace();
		}
        verify(clientRepository);
        
        //test that the returned object (clientbysrv) is not null
        assertThat(clientFromService, is(not(nullValue())));
        // test that the clientid is not null
        assertThat(clientFromService.getClientId(), not(nullValue()));
        // TODO should make a test for all fields?
        assertThat(clientFromService.getClientId(), equalTo(testClient.getClientId()));
        // test objects are equal
        assertThat(clientFromService, CoreMatchers.equalTo(testClient));		
	}
       
    @Test
    public void testCreateClient_withValidClientData() {
    	// Create a testClient with Valid Data        
		ClientDetailsEntity testClient =  new ClientDetailsEntity();
		testClient.setClientId("123client");
		testClient.setOwner("Test Entity Owner");
		testClient.setClientSecret("WWW83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");
		testClient.setClientName("a test client service");
		testClient.setOwner("some owner person");
		testClient.setClientDescription("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer aliquam adipiscing lacus. Ut nec urna");
		testClient.setAccessTokenTimeout((long) 10);
		testClient.setRefreshTokenTimeout((long) 360);
		testClient.setAllowRefresh(true);
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		    GrantedAuthority roleClient = new SimpleGrantedAuthority("ROLE_CLIENT");
		    authorities.add(roleClient);
		testClient.setAuthorities(authorities);
		Set<String> resourceIds = new HashSet<String>();
			        resourceIds.add("https://www.whatg.com/thing/");
			        resourceIds.add("https://www.whatg.com/else/");
			        resourceIds.add("https://www.someRedirectURI.com/redirectURI");
	    testClient.setResourceIds(resourceIds);
		Set<String> authorizedGrantTypes = new HashSet<String>();
					authorizedGrantTypes.add("authorization_code");
					authorizedGrantTypes.add("refresh_token");
		testClient.setAuthorizedGrantTypes(authorizedGrantTypes);
		Set<String> scope = new HashSet<String>();
					scope.add("openid");
		testClient.setScope(scope);
	
    	expect(clientFactory.createClient(testClient.getClientId(), testClient.getClientSecret())).andReturn(testClient).once();
        replay(clientFactory);
			        
    	expect(clientRepository.saveClient(testClient)).andReturn(testClient).once();
        replay(clientRepository);					
        
        ClientDetailsEntity clientFromService = null;							
		try { 	        	
			clientFromService = entityService.createClient(
					//change to strings that match the one above
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

            
        //test that the returned object (clientbysrv) is not null
        assertThat(clientFromService, is(not(nullValue())));
        // test that the clientid is not null
        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        // TODO should make a test for all fields?
        assertThat(clientFromService.getClientId(), equalTo(testClient.getClientId()));
        // test objects are equal
        assertThat(clientFromService, CoreMatchers.equalTo(testClient));
    }
        
	@Test
	public void testCreateClient_withINVALIDClientData() {
		// Test new client create with INVALID data
        logger.warn("What should we get if we submit invalid data?");
        
    	// Create a testClient with INValid Data        
		ClientDetailsEntity testClient =  new ClientDetailsEntity();
		testClient.setClientId(" # % & * { } : ' < > ? +" + "\\" + "\\/");
		testClient.setOwner("Test Entity Owner");
		testClient.setClientSecret("WWW83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");
		testClient.setClientName("a test client service");
		testClient.setOwner("some owner person");
		testClient.setClientDescription("<script type=\"text/javascript\">alert(\"I am an alert box!\");</script>");
		testClient.setAccessTokenTimeout(Long.MIN_VALUE);
		testClient.setRefreshTokenTimeout(Long.MIN_VALUE);
		testClient.setAllowRefresh(true);
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		    GrantedAuthority roleClient = new SimpleGrantedAuthority("ROLE_CLIENT");
		    authorities.add(roleClient);
		testClient.setAuthorities(authorities);
		Set<String> resourceIds = new HashSet<String>();
					resourceIds.add("https://www.whatg.com/some/"+ " # % & * { } : ' < > ? +" + "\\" + "\\/");
			        resourceIds.add("https://www.whatg.com/thing/");
			        resourceIds.add("htts://www.whatg.com/thing/");
			        resourceIds.add("htps://www.whatg.com/else/");
			        resourceIds.add("hts://www.someRedirectURI.com/redirectURI");
	    testClient.setResourceIds(resourceIds);
		Set<String> authorizedGrantTypes = new HashSet<String>();
					authorizedGrantTypes.add("authorization_code");
					authorizedGrantTypes.add("refresh_token");
		testClient.setAuthorizedGrantTypes(authorizedGrantTypes);
		Set<String> scope = new HashSet<String>();
					scope.add("openid");
		testClient.setScope(scope);
	    
    	expect(clientFactory.createClient(testClient.getClientId(), testClient.getClientSecret())).andReturn(testClient).once();
        replay(clientFactory);
			        
    	expect(clientRepository.saveClient(testClient)).andReturn(testClient).once();
        replay(clientRepository);					
        
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
        
	    // test that the clientid is not null
        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        // TODO should make a test for all fields?
        assertThat(clientFromService.getClientId(), equalTo(testClient.getClientId()));
        assertThat(clientFromService.getClientName(), equalTo(testClient.getClientName()));   
        // test that the objects are equal
        assertThat(clientFromService, CoreMatchers.equalTo(testClient));
        
	}	

    @Test(expected = InvalidClientException.class)
	public void testDeleteClient_throwInvalidClientException() {
		//delete existing entry
		ClientDetailsEntity originalClient =  new ClientDetailsEntity();
		originalClient.setClientId("123client");
		originalClient.setOwner("Test Entity Owner");
		originalClient.setClientSecret("WWW83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");
		
        expect(clientRepository.getClientById(originalClient.getClientId())).andReturn(originalClient).once();
        
        tokenRepository.clearTokensForClient(originalClient);
        expectLastCall();

		clientRepository.deleteClient(originalClient);
        expectLastCall();
        
        replay(clientRepository);
        replay(tokenRepository);
      	
        
        // this function is going to return void        
      	entityService.deleteClient(originalClient);

      	verify(tokenRepository);
      	verify(clientRepository);
        
        // test that client doesn't not exist 
        entityService.loadClientByClientId(originalClient.getClientId());
    }

    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_oldnull() {		
    	ClientDetailsEntity originalClient =  new ClientDetailsEntity();    	
		entityService.updateClient(null, originalClient);		
	}
    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_newnull() {		
    	ClientDetailsEntity originalClient =  new ClientDetailsEntity();
		entityService.updateClient(originalClient, null);
	}    
    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_nullnull() {		
		entityService.updateClient(null, null);
	}    
 
	@Test
	public void testUpdateClient() {
		// get an existing client, change a field, verify field changed from service	
		ClientDetailsEntity originalClient =  new ClientDetailsEntity();
							originalClient.setClientId("123client");
							originalClient.setOwner("Test Entity Owner");
							originalClient.setClientSecret("WWW83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");

		ClientDetailsEntity newClient = new ClientDetailsEntity();
							newClient.setClientId("123client");
							newClient.setOwner("new owner");
							newClient.setClientSecret("TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");

		expect(clientRepository.updateClient(originalClient.getClientId(), newClient)).andReturn(newClient).once();
        expect(clientRepository.getClientById(newClient.getClientId())).andReturn(newClient).once();
		replay(clientRepository);

		ClientDetailsEntity currClient = new ClientDetailsEntity();
		try {
			
			entityService.updateClient(originalClient, newClient);   
	    	currClient = entityService.loadClientByClientId(newClient.getClientId());
	    
		} catch (Exception e) {
			e.printStackTrace();
		}

		verify(clientRepository);
		
        assertThat(currClient.getOwner(), equalTo(newClient.getOwner()));   
        assertThat(currClient.getClientSecret(), not(equalTo(originalClient.getClientSecret())));   
        assertSame(newClient, currClient);
        assertThat(currClient.getOwner(), not(equalTo(originalClient.getOwner())));
     	assertNotSame(originalClient, currClient);
	}

	@Test
	public void testGetAllClients() {
		//instantiate and set all of them    			
    	ClientDetailsEntity currentClient[] = new ClientDetailsEntity[4];
    	
    	currentClient[0] = new ClientDetailsEntity();
		currentClient[0].setClientId("TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");
		currentClient[0].setOwner("T P H");
		currentClient[0].setClientSecret("password1");
        expect(clientRepository.getClientById(currentClient[0].getClientId())).andReturn(currentClient[0]).once();
        //replay(clientRepository);		
        
		currentClient[1] = new ClientDetailsEntity();
    	currentClient[1].setClientId("FRY83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L56EEB14000");
    	currentClient[1].setOwner("F R Y");
    	currentClient[1].setClientSecret("password2");
        expect(clientRepository.getClientById(currentClient[1].getClientId())).andReturn(currentClient[1]).once();
        //replay(clientRepository);	
        
    	currentClient[2] = new ClientDetailsEntity();
    	currentClient[2].setClientId("GST55YEA4CE763Z21WSE83NL0LGM65SKD0GN945L66EEB19999");
    	currentClient[2].setOwner("G S T");
    	currentClient[2].setClientSecret("password3");    	
        expect(clientRepository.getClientById(currentClient[2].getClientId())).andReturn(currentClient[2]).once();
        //replay(clientRepository);		
        
    	currentClient[3] = new ClientDetailsEntity();
    	currentClient[3].setClientId("H57T8YEA4CE763Z21WSE83NL0LGM65SKD0GN945L76EEB19999");        
    	currentClient[3].setOwner("H 5 7");
    	currentClient[3].setClientSecret("password4");    	
        expect(clientRepository.getClientById(currentClient[3].getClientId())).andReturn(currentClient[3]).once();
        //replay(clientRepository);		
        
    	int foundcnt = currentClient.length; // number of matches we need 4 in this test
    	
    	//TODO Need to mock the clientRepository with these 4 objects
    	
    	Collection<ClientDetailsEntity> clientCollection = null;
		expect(clientRepository.getAllClients()).andReturn(clientCollection).once();
        expectLastCall();
        replay(clientRepository);		

        //check that each currentClient exists in this clientCollection only once
	    clientCollection =  entityService.getAllClients();
	    for (ClientDetailsEntity cde : clientCollection) {
	    	for (int i = 0;i<currentClient.length;i++){
	    		if (currentClient[i].getClientId() == cde.getClientId()){
	    			foundcnt--;
	    		}
	    	}
		}
	    verify(clientRepository);
	    assertSame((int) 0, foundcnt);
	}
	
}
