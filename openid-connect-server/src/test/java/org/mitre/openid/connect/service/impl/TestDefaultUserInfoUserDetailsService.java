package org.mitre.openid.connect.service.impl;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.collect.Lists;

public class TestDefaultUserInfoUserDetailsService {

	private DefaultUserInfoUserDetailsService service;
	private UserInfoRepository userInfoRepository;
	private UserInfo userInfoAdmin;
	private UserInfo userInfoRegular;
	private String adminUsername = "username";
	private String regularUsername = "regular";
	private String adminSub = "adminSub12d3a1f34a2";
	private String regularSub = "regularSub652ha23b";
	
	/**
	 * Initialize the service and the mocked repository.
	 * Initialize 2 users, one of them an admin, for use in unit tests.
	 */
	@Before
	public void prepare() {
		userInfoRepository = Mockito.mock(UserInfoRepository.class);
		service = new DefaultUserInfoUserDetailsService();
		service.setUserInfoRepository(userInfoRepository);
		service.setAdmins(Lists.newArrayList(adminUsername));
		
		userInfoAdmin = new DefaultUserInfo();
		userInfoAdmin.setPreferredUsername(adminUsername);
		userInfoAdmin.setSub(adminSub);
		
		userInfoRegular = new DefaultUserInfo();
		userInfoRegular.setPreferredUsername(regularUsername);
		userInfoRegular.setSub(regularSub);	
	}
	
	/**
	 * Test loading an admin user, ensuring that the UserDetails object returned
	 * has both the ROLE_USER and ROLE_ADMIN authorities.
	 */
	@Test
	public void loadByUsername_admin_success() {
		
		Mockito.when(userInfoRepository.getByUsername(adminUsername)).thenReturn(userInfoAdmin);
		UserDetails user = service.loadUserByUsername(adminUsername);
		ArrayList<GrantedAuthority> userAuthorities = Lists.newArrayList(user.getAuthorities());
		assertThat(userAuthorities, hasItem(DefaultUserInfoUserDetailsService.ROLE_ADMIN));
		assertThat(userAuthorities, hasItem(DefaultUserInfoUserDetailsService.ROLE_USER));
		assertEquals(user.getUsername(), adminSub);
		
	}
	
	/**
	 * Test loading a regular, non-admin user, ensuring that the returned UserDetails
	 * object has ROLE_USER but *not* ROLE_ADMIN.
	 */
	@Test
	public void loadByUsername_regular_success() {
		
		Mockito.when(userInfoRepository.getByUsername(regularUsername)).thenReturn(userInfoRegular);
		UserDetails user = service.loadUserByUsername(regularUsername);
		ArrayList<GrantedAuthority> userAuthorities = Lists.newArrayList(user.getAuthorities());
		assertThat(userAuthorities, not(hasItem(DefaultUserInfoUserDetailsService.ROLE_ADMIN)));
		assertThat(userAuthorities, hasItem(DefaultUserInfoUserDetailsService.ROLE_USER));
		assertEquals(user.getUsername(), regularSub);
		
	}	
	
	/**
	 * If a user is not found, the loadByUsername method should throw an exception.
	 */
	@Test(expected = UsernameNotFoundException.class)
	public void loadByUsername_nullUser() {
		
		Mockito.when(userInfoRepository.getByUsername(adminUsername)).thenReturn(null);
		service.loadUserByUsername(adminUsername);
		
	}
	
}
