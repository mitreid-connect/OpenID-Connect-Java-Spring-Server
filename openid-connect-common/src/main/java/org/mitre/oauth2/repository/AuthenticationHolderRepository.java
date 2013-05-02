package org.mitre.oauth2.repository;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface AuthenticationHolderRepository {

	public AuthenticationHolderEntity getById(Long id);

	public AuthenticationHolderEntity getByAuthentication(OAuth2Authentication a);

	public void removeById(Long id);

	public void remove(AuthenticationHolderEntity a);

	public AuthenticationHolderEntity save(AuthenticationHolderEntity a);

}
