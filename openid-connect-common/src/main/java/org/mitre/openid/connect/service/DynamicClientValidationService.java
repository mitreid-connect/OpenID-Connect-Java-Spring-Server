package org.mitre.openid.connect.service;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.exception.ValidationException;

public interface DynamicClientValidationService {

  public ClientDetailsEntity validateClient(ClientDetailsEntity client) throws ValidationException;

}
