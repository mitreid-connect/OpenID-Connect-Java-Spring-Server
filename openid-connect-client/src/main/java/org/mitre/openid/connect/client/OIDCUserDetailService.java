package org.mitre.openid.connect.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.mitre.openid.connect.model.IdToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class OIDCUserDetailService implements UserDetailsService,
		AuthenticationUserDetailsService<OpenIdConnectAuthenticationToken> {
	
	public IdToken retrieveToken(URL url) throws IOException{
		String str = new BufferedReader(new InputStreamReader(url.openStream())).toString();
		IdToken idToken = IdToken.parse(str);
		return idToken;
	}


	@Override
	public UserDetails loadUserDetails(OpenIdConnectAuthenticationToken token)
			throws UsernameNotFoundException {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}
