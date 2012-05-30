package org.mitre.openid.connect.service;

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

import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

//not testing getter and setter

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring-servlet.xml", "classpath:test-context.xml"})

public class ClientUserDetailsServiceTest {
	private Logger logger; 
	
	@Autowired
	private ClientUserDetailsService clientUserDetailsService;
	
	@Before
	public void setUp() throws Exception {
		logger = LoggerFactory.getLogger(this.getClass());
        logger.info("setUp of ClientUserDetailsServiceTest");
		clientUserDetailsService = createNiceMock(ClientUserDetailsService.class);
		
		
        
	}

	@After
	public void tearDown() throws Exception {
		clientUserDetailsService = null;
		logger = LoggerFactory.getLogger(this.getClass());
        logger.info("tearDown of ClientUserDetailsServiceTest");
	}

	@Test(expected = UsernameNotFoundException.class)
	public final void testLoadUserByUsername_throwsUsernameNotFoundException() {
		fail("Not yet implemented"); // TODO
		
	}
	@Test(expected = DataAccessException.class)
	public final void testLoadUserByUsername_throwsDataAccessException() {
		fail("Not yet implemented"); // TODO
		
	}
	@Test
	public final void testLoadUserByUsername_withValidUserName() {
		fail("Not yet implemented"); // TODO
		
	}	
	@Test
	public final void testLoadUserByUsername_withINVALIDUserName() {
		fail("Not yet implemented"); // TODO
		
	}	
}
