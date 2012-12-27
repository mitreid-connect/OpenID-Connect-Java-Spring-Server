package org.mitre.openid.connect.repository.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class JpaNonceRepositoryTest {
	
	@Test
	public void getById_valid() {
		
	}
	
	@Test
	public void getById_invalid() {
		
	}
	
	@Test
	public void remove_valid() {
		
	}
	
	@Test
	public void remove_invalid() {
		
	
	}

	@Test
	public void getExpired() {
		
	}
	
	@Test
	public void getByClientId_valid() {
		
	}
	
	@Test
	public void getByClientId_invalid() {
		
	}
	
}
