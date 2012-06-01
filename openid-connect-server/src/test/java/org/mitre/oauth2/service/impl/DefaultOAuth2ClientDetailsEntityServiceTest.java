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

    private ClientDetailsEntity clienttest = new ClientDetailsEntity();
    
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
        
        //setup a basic client with details
        clienttest.setClientId("XVX42QQA9CA348S46TNJ00NP8MRO37FHO1UW748T59BAT74LN9");
        clienttest.setClientSecret("password");
        clienttest.setClientName("a test client service");
        clienttest.setOwner("some owner person"); // why not set/get ClientOwner? 
        clienttest.setClientDescription("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer aliquam adipiscing lacus. Ut nec urna");
        //TODO model question: should seconds be in LONG? Wouldn't INT be better
        clienttest.setAccessTokenTimeout((long) 10);
        clienttest.setRefreshTokenTimeout((long) 360);
        clienttest.setAllowRefresh(true); // db handles actual value
        //TODO model question: what are correct values for this field?
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        GrantedAuthority roleClient = new SimpleGrantedAuthority("ROLE_CLIENT");
        authorities.add(roleClient);
        clienttest.setAuthorities(authorities);
		Set<String> resourceIds = new HashSet<String>();
			resourceIds.add("https://www.whatg.com/some/"+ " # % & * { } : ' < > ? +" + "\\" + "\\/");
	        resourceIds.add("https://www.whatg.com/thing/");
	        resourceIds.add("htts://www.whatg.com/thing/");
	        resourceIds.add("htps://www.whatg.com/else/");
	        resourceIds.add("hts://www.someRedirectURI.com/redirectURI");
        clienttest.setResourceIds(resourceIds);
        Set<String> authorizedGrantTypes = new HashSet<String>();
        //TODO model question: what are correct values for this field?
    	authorizedGrantTypes.add("authorization_code");
		authorizedGrantTypes.add("refresh_token");
		clienttest.setAuthorizedGrantTypes(authorizedGrantTypes);
		Set<String> scope = new HashSet<String>();
		scope.add("openid"); //TODO model question: what are correct values for this field?
		clienttest.setScope(scope);
	        
	}

	@After
	public void tearDown() throws Exception {
		entityService = null;
		clienttest = null;
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
    	clienttest.setClientId("Idon'texist/client");
       						
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(null).once();
        replay(clientRepository);
        
        entityService.loadClientByClientId(clienttest.getClientId());
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
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(clienttest).once();
        replay(clientRepository);
        ClientDetailsEntity clientFromService = null;
        try {
			clientFromService = entityService.loadClientByClientId(clienttest.getClientId());
		} catch (InvalidClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuth2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        verify(clientRepository);
        
        //test that the returned object (clientbysrv) is not null
        assertThat(clientFromService, is(not(nullValue())));
        // test that the clientid is not null
        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        // TODO need make a test for all fields
        assertThat(clientFromService.getClientId(), equalTo(clienttest.getClientId()));
        // test objects are equal
        assertThat(clientFromService, CoreMatchers.equalTo(clienttest));		
	}
       
    @Test
    public void testCreateClient_withValidClientData() {
        ClientDetailsEntity clientFromService = null;
    	expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(clienttest).once();
        expect(clientFactory.createClient(clienttest.getClientId(), clienttest.getClientSecret())).andReturn(clienttest).once();
        replay(clientFactory);
        replay(clientRepository);
        verify(clientRepository);
        verify(clientFactory);
        expect(clientRepository.saveClient(clientFromService));
    	replay(clientRepository);
	    expectLastCall();	 	  	
	        
        try { 	        	
			clientFromService = entityService.createClient(
					clienttest.getClientId(), 
					clienttest.getClientSecret(), 
					clienttest.getScope(), 
					clienttest.getAuthorizedGrantTypes(), 
					"", //TODO the get method doesn't work for redirecturi back in
					clienttest.getAuthorities(), 
					clienttest.getResourceIds(), 
					clienttest.getClientName(), 
					clienttest.getClientDescription(), 
					clienttest.isAllowRefresh(), 
					clienttest.getAccessTokenTimeout(), 
					clienttest.getRefreshTokenTimeout(), 
					clienttest.getOwner()
			);

		 	           
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        verify(clientRepository);
        verify(clientFactory);

            
        //test that the returned object (clientbysrv) is not null
        assertThat(clientFromService, is(not(nullValue())));
        // test that the clientid is not null
        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        // TODO need make a test for all fields
        assertThat(clientFromService.getClientId(), equalTo(clienttest.getClientId()));
        // test objects are equal
        assertThat(clientFromService, CoreMatchers.equalTo(clienttest));
    }
        
	@Test
	public void testCreateClient_withINVALIDClientData() {
		// Test new client create with INVALID data
        logger.warn("What should we get if we submit invalid data?");
        
        clienttest.setClientId(" # % & * { } : ' < > ? +" + "\\" + "\\/");
        clienttest.setClientDescription("<script type=\"text/javascript\">alert(\"I am an alert box!\");</script>");
        clienttest.setAccessTokenTimeout(Long.MIN_VALUE);
        clienttest.setRefreshTokenTimeout(Long.MAX_VALUE);
		
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(clienttest).once();
        replay(clientRepository);
	    ClientDetailsEntity clientFromService = null;
		 try {
			clientFromService = entityService.createClient(
					clienttest.getClientId(), 
					clienttest.getClientSecret(), 
					clienttest.getScope(), 
					clienttest.getAuthorizedGrantTypes(), 
					"", 
					clienttest.getAuthorities(), 
					clienttest.getResourceIds(), 
					clienttest.getClientName(), 
					clienttest.getClientDescription(), 
					clienttest.isAllowRefresh(), 
					clienttest.getAccessTokenTimeout(), 
					clienttest.getRefreshTokenTimeout(), 
					clienttest.getOwner()
			);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    verify(clientRepository);
        
	    // test that the clientid is not null
        assertThat(clientFromService.getClientId(), is(not(nullValue())));
        // TODO need make a test for all fields
        assertThat(clientFromService.getClientId(), equalTo(clienttest.getClientId()));
        assertThat(clientFromService.getClientName(), equalTo(clienttest.getClientName()));   
        // test that the objects are equal
        assertThat(clientFromService, CoreMatchers.equalTo(clienttest));
        
	}	

    @Test(expected = InvalidClientException.class)
	public void testDeleteClient_throwInvalidClientException() {
		//TODO  delete existing entry
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(clienttest).once();
       //TODO tokenRepository.clearTokensForClient(clienttest);

        
        try {
			clientRepository.deleteClient(clienttest);
		} catch (Exception e) {
			e.printStackTrace();
		}
        expectLastCall();
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(null).once(); 
        replay(clientRepository);
       
		// this function is going to return void        
        entityService.deleteClient(clienttest);		
        verify(clientRepository);
        
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(null).once();
        replay(clientRepository);
        // test that client doesn't not exist 
        entityService.loadClientByClientId(clienttest.getClientId());
	}

    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_oldnull() {		
		entityService.updateClient(null, clienttest);		
	}
    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_newnull() {		
		entityService.updateClient(clienttest, null);
	}    
    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_nullnull() {		
		entityService.updateClient(null, null);
	}    
    
	@Test
	public void testUpdateClient() {
		// get an existing client, change a field, verify field changed from service	
		ClientDetailsEntity originalClient =  clienttest;
		ClientDetailsEntity newClient = originalClient;
		newClient.setOwner("");
		newClient.setClientId("TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");

		expect(clientRepository.getClientById(originalClient.getClientId())).andReturn(originalClient).once();
        replay(clientRepository);
		entityService.updateClient(originalClient, newClient);        
		ClientDetailsEntity currClient = entityService.loadClientByClientId(originalClient.getClientId());
        verify(clientRepository);
       
        assertThat(currClient.getOwner(), equalTo(newClient.getOwner()));   
        assertSame(newClient, currClient);
        assertThat(currClient.getOwner(), not(equalTo(originalClient.getOwner())));
        // TODO pass by ref behavior how to fix so it really is two diff objects??
     	assertNotSame(originalClient, currClient);
	}

	@Test
	public void testGetAllClients() {
		//instantiate and set all of them    	
    	ClientDetailsEntity currentClient[] = new ClientDetailsEntity[4];
    	currentClient[0] = new ClientDetailsEntity();
		currentClient[0].setClientId("TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1");
    	currentClient[1] = clienttest;
    	currentClient[1].setClientId("FRY83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L56EEB14000");
    	currentClient[2] = clienttest;
    	currentClient[2].setClientId("GST55YEA4CE763Z21WSE83NL0LGM65SKD0GN945L66EEB19999");
    	currentClient[3] = clienttest;
    	currentClient[3].setClientId("H57T8YEA4CE763Z21WSE83NL0LGM65SKD0GN945L76EEB19999");        
    	
    	int foundcnt = currentClient.length; // number of matches we need to 
    	
        expect(clientRepository.getClientById(currentClient[0].getClientId())).andReturn(currentClient[0]).once();
        expect(clientRepository.getClientById(currentClient[1].getClientId())).andReturn(currentClient[1]).once();
        expect(clientRepository.getClientById(currentClient[2].getClientId())).andReturn(currentClient[2]).once();
        expect(clientRepository.getClientById(currentClient[3].getClientId())).andReturn(currentClient[3]).once();
        expect(clientRepository.getAllClients()).andReturn(currentClient);
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
