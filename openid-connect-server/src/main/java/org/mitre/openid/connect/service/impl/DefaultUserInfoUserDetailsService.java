/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
package org.mitre.openid.connect.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * A UserDetailsService backed by a UserInfoRepository.
 * 
 * @author jricher
 *
 */
@Service("userInfoUserDetailsService")
public class DefaultUserInfoUserDetailsService implements UserDetailsService {

	@Autowired
	private UserInfoRepository repository;

	public static final GrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");
	public static final GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

	private List<String> admins = new ArrayList<String>();

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserInfo userInfo = repository.getByUsername(username);

		if (userInfo != null) {

			// TODO: make passwords configurable? part of object?
			String password = "password";

			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			authorities.add(ROLE_USER);

			if (admins != null && admins.contains(username)) {
				authorities.add(ROLE_ADMIN);
			}

			// TODO: this should really be our own UserDetails wrapper class, shouldn't it?
			User user = new User(userInfo.getSub(), password, authorities);
			return user;
		} else {
			throw new UsernameNotFoundException("Could not find username: " + username);
		}
	}

	/**
	 * @return the admins
	 */
	public List<String> getAdmins() {
		return admins;
	}

	/**
	 * @param admins the admins to set
	 */
	public void setAdmins(List<String> admins) {
		this.admins = admins;
	}

}
