package org.mitre.oauth2.model;


public interface ClientDetailsEntityFactory {
	
	public ClientDetailsEntity createClient(String clientId, String clientSecret);

}
