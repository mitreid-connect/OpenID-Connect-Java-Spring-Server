package org.mitre.openid.connect.service.impl;

import org.mitre.openid.connect.repository.UserRepository;
import org.mitre.openid.connect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("defaultUserService")
public class DefaultUserService implements UserService {

	@Autowired
	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.getUserByUsername(username);
	}

}
