package org.mitre.oauth2.service;

import java.util.List;

import org.mitre.data.PageCriteria;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface AuthenticationHolderEntityService {
  
  AuthenticationHolderEntity create(OAuth2Authentication authn);
  
  void remove(AuthenticationHolderEntity holder);

  List<AuthenticationHolderEntity> getOrphanedAuthenticationHolders();
  
  List<AuthenticationHolderEntity> getOrphanedAuthenticationHolders(PageCriteria page);
 
}
