package org.mitre.openid.connect.web;

import java.util.Arrays;
import java.util.HashSet;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.SavedUserAuthentication;

public class EndSessionTestHelper {

	public static OAuth2AccessTokenEntity createClientDetailsWithPostLogoutRedirectUri(String registeredRedirect, String username) {
			OAuth2AccessTokenEntity value = new OAuth2AccessTokenEntity();
			value.setClient(createClient(registeredRedirect));
			value.setAuthenticationHolder(createAuthenticationHolder(username));
			return value;
	}

	private static AuthenticationHolderEntity createAuthenticationHolder(String username) {
		AuthenticationHolderEntity authenticationHolder = new AuthenticationHolderEntity();
		SavedUserAuthentication userAuth = new SavedUserAuthentication();
		userAuth.setName(username);
		authenticationHolder.setUserAuth(userAuth);
		return authenticationHolder;
	}

	private static ClientDetailsEntity createClient(String registeredRedirect) {
		ClientDetailsEntity client = new ClientDetailsEntity();
		client.setPostLogoutRedirectUris(new HashSet<String>(Arrays.asList(registeredRedirect)));
		return client;
	}

}
