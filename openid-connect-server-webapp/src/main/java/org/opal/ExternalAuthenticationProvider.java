package org.opal;

import java.util.ArrayList;

import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.opal.config.ExternalAuthenticationManager;
import org.opal.data.CrudRepository;
import org.opal.data.model.FIAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.gson.JsonObject;

public class ExternalAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(ExternalAuthenticationProvider.class);
	
	@Autowired
	private ExternalAuthenticationManager authManager;
	
	@Autowired
	private UserInfoService userService;

	@Autowired
	private CrudRepository crudRepository;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		ExternalAuthenticationToken auth = null;
		try {
			ExternalAuthenticationToken t = (ExternalAuthenticationToken)authentication;
		
			if(t == null || t.getCode() == null) {
				return auth;
			}
			// Get necessary config to do authentication
			String code = t.getCode();
			String state = t.getState();
			String issuer = t.getIssuer();
			
			// 1. Get Access Token
			JsonObject respParams = authManager.getAccessToken(issuer, code, state);
			if(respParams == null) {
				throw new Exception("No access token.");
			}
			String access_token = respParams.getAsJsonPrimitive("access_token").getAsString();
		
			// 2. Get user info
			JsonObject json = authManager.getUserInfo(issuer, access_token);
			if(json==null) {
				throw new Exception("No user info.");
			}
			String username = json.getAsJsonPrimitive("login").getAsString();
			String id = json.getAsJsonPrimitive("id").getAsString();
			String name = json.getAsJsonPrimitive("name").getAsString();
						
			// 3. Load user data from local database
			UserInfo user = userService.getByUsername(username);
			if(user == null) {
				// User does not exist in local db.
				DefaultUserInfo newUser = new DefaultUserInfo();
				newUser.setPreferredUsername(username);
				newUser.setSub(id);
				newUser.setName(name);
				newUser.setGivenName(name);
				crudRepository.saveUserInfo(newUser);
				user = newUser;
			}
			
			// 4. Store FIAccess
			FIAccess fAccess = new FIAccess();
			fAccess.setClientId(issuer);
			fAccess.setCreatorUserId(user.getSub());
			fAccess.setSessionInfo(respParams.toString());
			crudRepository.saveFIAccess(fAccess);
			
			ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
			if(user.getPreferredUsername().matches("admin")) {
				grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			}
			auth = new ExternalAuthenticationToken(username, null, grantedAuthorities);
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.isAssignableFrom(ExternalAuthenticationToken.class);
	}

}
