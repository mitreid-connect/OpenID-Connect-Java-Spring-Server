package org.mitre.oauth2.repository;

import org.mitre.oauth2.model.AuthenticationHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface AuthenticationHolderRepository {

	public AuthenticationHolder getById(Long id);
	
	public AuthenticationHolder getByAuthentication(OAuth2Authentication a);
	
	public void removeById(Long id);
	
	public void remove(AuthenticationHolder a);
	
	public AuthenticationHolder save(AuthenticationHolder a);
	
}
