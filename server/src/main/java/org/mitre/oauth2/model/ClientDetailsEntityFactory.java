package org.mitre.oauth2.model;

import org.mitre.oauth2.model.ClientDetailsEntity.ClientDetailsEntityBuilder;

public interface ClientDetailsEntityFactory {
	
	public ClientDetailsEntity createClient(String clientId, String clientSecret);

}
