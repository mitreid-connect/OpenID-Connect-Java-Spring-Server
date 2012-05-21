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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
	public void testLoadClientByClientI1d1() {
		//Try NULL clientID
		String clientId = null;
		// We should get: throw new IllegalArgumentException("Client id must not be empty!");
		entityService.loadClientByClientId(clientId);
	}
    
    @Test(expected = IllegalArgumentException.class)
	public void testLoadClientByClientId2() {
		//Try Empty clientID
		String clientId = "";
		// We should get: throw new InvalidClientException
		entityService.loadClientByClientId(clientId);
	}  
    
    @Test(expected = InvalidClientException.class)
    public void testLoadClientByClientId_InvalidClientException() {
    	// test that an invalid clientID throws InvalidClientException
       	ClientDetailsEntity clienttest = new ClientDetailsEntity();
         clienttest.setClientId("1doesn'texist/");
            expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(null).once();
            replay(clientRepository);
            entityService.loadClientByClientId(clienttest.getClientId());
            verify(clientRepository);
    }
    	
    @Test(expected = OAuth2Exception.class)
	public void testLoadClientByClientId_Oauth2excep() {
	    //TODO loadClientByClientId throws OAuth2Exception how to trigger?
		fail("Not yet implemented -throws OAuth2Exception How to trigger?");
	}
       
    @Test
    public void testLoadClientByClientId_validid1() {
    	// test that the data for a valid client is returned		
            ClientDetailsEntity clienttest = new ClientDetailsEntity();
            clienttest.setClientId("XVX42QQA9CA348S46TNJ00NP8MRO37FHO1UW748T59BAT74LN9");
            clienttest.setClientName("C2C");
            // TODO need to test all fields
            expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(clienttest).once();
            replay(clientRepository);
            ClientDetailsEntity clientbysrv = entityService.loadClientByClientId(clienttest.getClientId());
            verify(clientRepository);
            
            // test that the clientid is not null
            assertThat(clientbysrv.getClientId(), is(not(nullValue())));
            // todo check all the fields            
            assertThat(clientbysrv.getClientId(), equalTo(clienttest.getClientId()));
            assertThat(clientbysrv.getClientName(), equalTo(clienttest.getClientName()));   
            // test that the objects are equal
            assertThat(clientbysrv, CoreMatchers.equalTo(clienttest));
    }
        
	@Test
	public void testCreateClient_baddata() {
		// Test new client create use bad data
        ClientDetailsEntity clienttest = makeFakeClient(2);                
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(clienttest).once();
        replay(clientRepository);
	    ClientDetailsEntity clientbysrv = new ClientDetailsEntity();   
	    clientbysrv = entityService.createClient(
	    		clienttest.getClientId(), 
	    		clienttest.getClientSecret(), 
	    		clienttest.getScope(), 
	    		clienttest.getAuthorizedGrantTypes(), 
	    		clienttest.getRegisteredRedirectUri().toString(), 
	    		clienttest.getAuthorities(), 
	    		clienttest.getResourceIds(), 
				clienttest.getClientName(), 
				clienttest.getClientDescription(), 
				clienttest.isAllowRefresh(), 
				clienttest.getAccessTokenTimeout(), 
				clienttest.getRefreshTokenTimeout(), 
				clienttest.getOwner()
		);
	    verify(clientRepository);
        
	    // test that the clientid is not null
        assertThat(clientbysrv.getClientId(), is(not(nullValue())));
        // todo check all the fields            
        assertThat(clientbysrv.getClientId(), equalTo(clienttest.getClientId()));
        assertThat(clientbysrv.getClientName(), equalTo(clienttest.getClientName()));   
        // test that the objects are equal
        assertThat(clientbysrv, CoreMatchers.equalTo(clienttest));
        
        
		//TODO should validate data on create client?	
		fail("Not yet implemented - What should we get if we submit invalid field data?");
	}	

    @Test(expected = InvalidClientException.class)
	public void testDeleteClient() {
		// delete existing entry
        ClientDetailsEntity clienttest = new ClientDetailsEntity();
        clienttest.setClientId("XVX42QQA9CA348S46TNJ00NP8MRO37FHO1UW748T59BAT74LN9");
        expect(clientRepository.getClientById(clienttest.getClientId())).andReturn(clienttest).once();
        replay(clientRepository);
		// this function is going to return void        
        entityService.deleteClient(clienttest);		
        verify(clientRepository);
        // test that client doesn't not exist 
        ClientDetailsEntity clientbysrv = entityService.loadClientByClientId(clienttest.getClientId());
	}

    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_oldnull() {		
		ClientDetailsEntity newClient =  makeFakeClient(3);
		entityService.updateClient(null, newClient);		
	}
    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_newnull() {		
		ClientDetailsEntity oldClient =  makeFakeClient(3);
		entityService.updateClient(oldClient, null);
	}    

    @Test(expected = IllegalArgumentException.class)
	public void testUpdateClient_nullnull() {		
		entityService.updateClient(null, null);
	}    
    
	@Test
	public void testUpdateClient() {
		// get an existing client 
		ClientDetailsEntity originalClient =  makeFakeClient(3);
		ClientDetailsEntity newClient = originalClient;
		// change a field
		newClient.setOwner("");
		
        expect(clientRepository.getClientById(originalClient.getClientId())).andReturn(originalClient).once();
        replay(clientRepository);
		entityService.updateClient(originalClient, newClient);        
		// verify that it is changed in repository	
		ClientDetailsEntity currClient = entityService.loadClientByClientId(originalClient.getClientId());
        verify(clientRepository);
        assertThat(currClient.getOwner(), equalTo(newClient.getOwner()));   
        assertSame(newClient, currClient);
        // TODO I don't know why these don't work.... did it really pass test?
		// assertNotSame(originalClient, currClient);
        // assertThat(currClient.getOwner(), not(equalTo(originalClient.getOwner())));
	}

	@Test
	public void testGetAllClients() {
		
	    Collection<ClientDetailsEntity> itr =  entityService.getAllClients();
	    //iterate through the ClientDetailsEntityCollection values using Iterator's hasNext and next methods
	    Iterator<ClientDetailsEntity> iterator = itr.iterator();

		 // while loop
		 while (iterator.hasNext()) {
		    // System.out.println("value= " + iterator.next());
		 }
	    
	    
		fail("Not yet implemented - Have to create iterator, and the orignal objects to check against"); //TODO 
	}
	
	private ClientDetailsEntity makeFakeClient(int whichFake) {
		String clientID = null;
		String clientName = null;
		String clientSecret = null;
		String clientOwner = null;
		String clientDesc = null;
		//TODO model question: should seconds be in LONG? Wouldn't INT be better
		Long clientAccessTokenTimeout = (long) 0;
		Long clientRefresheTokenTimeout = (long) 0;
		Boolean clientAllowRefresh = false;
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		Set<String> RedirectURIs = new HashSet<String>();
		Set<String> authorizedGrantTypes = new HashSet<String>();
		Set<String> scope = new HashSet<String>();
		Set<String> resourceIds = new HashSet<String>();
		
		
		if (whichFake==1){
			//new good data doesn't exist
			clientID = "ZZZZZZZZ9CA348S46TNJ00NP8MRO37FHO1UW748T59BAT74LN9";
			clientName = "a test service";
			clientSecret = "fdjakfljrljwefioejwiofjsvklajioi938947238uujiofslkdnv";
			clientOwner = "William B. Davis";
			clientDesc = "description test Client Create";
			clientAccessTokenTimeout = (long) 10;
			clientRefresheTokenTimeout = (long) 360;
			clientAllowRefresh = false;
			
			authorities.addAll(authorities); //"ROLE_CLIENT"
	        
			RedirectURIs.add("https://www.whatg.com/some/");
	    	RedirectURIs.add("https://www.whatg.com/thing/");
	    	RedirectURIs.add("https://www.whatg.com/else/");
		
	    	authorizedGrantTypes.add("authorization_code");
			authorizedGrantTypes.add("refresh_token");
		
			scope.add("");
			resourceIds.add("");
			
		} else if (whichFake==2) {
			//TODO bad data 1 what mechanism is doing data validation? db layer?
			clientID = " # % & * { } : ' < > ? +" + "\\" + "\\/";
			clientName = "a test service";
			clientSecret = "fdjakfljrljwefioejwiofjsvklajioi938947238uujiofslkdnv";
			clientOwner = "William B. Davis";
			clientDesc = "<script type=\"text/javascript\">alert(\"I am an alert box!\");</script>";
			clientAccessTokenTimeout = Long.MIN_VALUE;
			clientRefresheTokenTimeout = Long.MAX_VALUE;
			clientAllowRefresh = true;
			
			authorities.addAll(authorities); //"ROLE_CLIENT"
	        
			RedirectURIs.add("https://www.whatg.com/some/"+ " # % & * { } : ' < > ? +" + "\\" + "\\/");
	    	RedirectURIs.add("https://www.whatg.com/thing/");
	    	RedirectURIs.add("https://www.whatg.com/else/");
		
	    	authorizedGrantTypes.add("authorization_code");
			authorizedGrantTypes.add("refresh_token");
		
			scope.add("");
			resourceIds.add("");			
			
		} else if (whichFake==3) {
			//existing client in the test database
			clientID = "TPH83YEA4CE763Z21WSE83NL0LGM65SKD0GN945L46EEB14EA1";
			clientName = "Customer Relations";
			clientSecret = "JXL93MIP9ZD63GRQ76BHC5YH60LCE42OKR4TD83CUN71TLU5DV82JTN07ZEY4KN8BZP27ZIO5CSHON86JPX7JK713Y08JPR45HQ3HZX70PBQ1WH441E85ZYY90UH5";
			clientOwner = "Tasha Q. Beard";
			clientDesc = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Curabitur sed tortor. Integer aliquam adipiscing lacus. Ut nec urna";
			clientAccessTokenTimeout = (long) 37;
			clientRefresheTokenTimeout = (long) 221;
			clientAllowRefresh = true; // in the database its "1"
			
			authorities.addAll(authorities); //"ROLE_CLIENT"
	        
			RedirectURIs.add("https://www.someRedirectURI.com/redirectURI");

	    	authorizedGrantTypes.add("authorization_code");
			authorizedGrantTypes.add("refresh_token");
		
			scope.add("");
			resourceIds.add("");			

		}
		
		ClientDetailsEntity fakeTestclient = new ClientDetailsEntity();
			fakeTestclient.setClientId(clientID);
			fakeTestclient.setClientName(clientName);
			fakeTestclient.setClientSecret(clientSecret);
			fakeTestclient.setOwner(clientOwner);
			fakeTestclient.setClientDescription(clientDesc);
			fakeTestclient.setAccessTokenTimeout(clientAccessTokenTimeout);
			fakeTestclient.setRefreshTokenTimeout(clientRefresheTokenTimeout);
			fakeTestclient.setAllowRefresh(clientAllowRefresh);
	        fakeTestclient.setAuthorities(authorities);
	    	fakeTestclient.setRegisteredRedirectUri(RedirectURIs);
	        fakeTestclient.setAuthorizedGrantTypes(authorizedGrantTypes);
			fakeTestclient.setScope(scope);
			fakeTestclient.setResourceIds(resourceIds);
		
		return fakeTestclient;
		
	}

}
