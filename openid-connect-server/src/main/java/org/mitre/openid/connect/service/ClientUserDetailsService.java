package org.mitre.openid.connect.service;

import java.util.ArrayList;
import java.util.List;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

/**
 * Shim layer to convert a ClientDetails service into a UserDetails service
 * 
 * @author AANGANES
 *
 */
@Service
public class ClientUserDetailsService implements UserDetailsService {

	@Autowired
	ClientDetailsService clientDetailsService;

	@Override
    public UserDetails loadUserByUsername(String clientId) throws  UsernameNotFoundException, DataAccessException {

		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
		
		
        String password = client.getClientSecret();
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority roleClient = new SimpleGrantedAuthority("ROLE_CLIENT");
        authorities.add(roleClient);

        return new User(clientId, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);

    }

	public ClientDetailsService getClientDetailsService() {
		return clientDetailsService;
	}

	public void setClientDetailsService(ClientDetailsService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}
	
}
