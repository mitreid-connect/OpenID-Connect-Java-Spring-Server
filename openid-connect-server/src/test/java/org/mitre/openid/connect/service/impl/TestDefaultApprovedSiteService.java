package org.mitre.openid.connect.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.annotation.Rollback;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultApprovedSiteService {

	private ApprovedSite site1;
	private ApprovedSite site2;
	private ApprovedSite site3;
	
	private ClientDetailsEntity client;
	private final String clientId = "client";
	
	@Mock
	private ApprovedSiteRepository repository;
	
	@InjectMocks
	private ApprovedSiteService service = new DefaultApprovedSiteService();
	
	
	/**
	 * Initialize the service and repository mock. Initialize a client and
	 * several ApprovedSite objects for use in unit tests.
	 */
	@Before
	public void prepare() {
		
		client = new ClientDetailsEntity();
		client.setClientId(clientId);
		
		site1 = new ApprovedSite();
		site1.setId(1L);
		site1.setUserId("user1");
		site1.setClientId("other");
		
		site2 = new ApprovedSite();
		site2.setId(2L);
		site2.setUserId("user1");
		site2.setClientId(clientId);
		
		site3 = new ApprovedSite();
		site3.setId(3L);
		site3.setUserId("user2");
		site3.setClientId(clientId);
		
		Mockito.reset(repository);

	}
	
	/**
	 * Test clearing approved sites for a client that has 2 stored approved sites.
	 * Ensure that the repository's remove() method is called twice.
	 */
	@Test
	public void clearApprovedSitesForClient_success() {
		Set<ApprovedSite> setToReturn = Sets.newHashSet(site2, site3);
		Mockito.when(repository.getByClientId(client.getClientId())).thenReturn(setToReturn);
		
		service.clearApprovedSitesForClient(client);
		
		Mockito.verify(repository, times(2)).remove(any(ApprovedSite.class));
	}
	
	/**
	 * Test clearing approved sites for a client that doesn't have any stored approved
	 * sites. Ensure that the repository's remove() method is never called in this case.
	 */
	@Test
	@Rollback
	public void clearApprovedSitesForClient_null() {
		String otherId = "a different id";
		client.setClientId(otherId);
		service.clearApprovedSitesForClient(client);
		Mockito.when(repository.getByClientId(otherId)).thenReturn(new HashSet<ApprovedSite>());
		Mockito.verify(repository, never()).remove(any(ApprovedSite.class));
	}
	
	
}
