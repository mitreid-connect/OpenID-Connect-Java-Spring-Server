package org.mitre.oauth2.service.impl;

import java.util.List;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.service.AuthenticationHolderEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

@Service("authenticationHolderEntityService")
public class DefaultAuthenticationHolderEntityService implements AuthenticationHolderEntityService {

  private final AuthenticationHolderRepository repo;

  @Autowired
  public DefaultAuthenticationHolderEntityService(AuthenticationHolderRepository repo) {
    this.repo = repo;
  }

  @Override
  public AuthenticationHolderEntity create(OAuth2Authentication authn) {
    AuthenticationHolderEntity holder = new AuthenticationHolderEntity();
    holder.setAuthentication(authn);

    return repo.save(holder);
  }

  @Override
  public void remove(AuthenticationHolderEntity holder) {
    repo.remove(holder);
  }

  @Override
  public List<AuthenticationHolderEntity> getOrphanedAuthenticationHolders() {
    
    return repo.getOrphanedAuthenticationHolders();
  }

}
