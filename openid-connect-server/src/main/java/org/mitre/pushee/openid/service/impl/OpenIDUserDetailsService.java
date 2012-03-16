package org.mitre.pushee.openid.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class OpenIDUserDetailsService implements UserDetailsService {

    private String openIdRoot;
    private String admins;
    private List<String> adminList = new ArrayList<String>();

    private GrantedAuthority roleUser = new GrantedAuthorityImpl("ROLE_USER");
	private GrantedAuthority roleAdmin = new GrantedAuthorityImpl("ROLE_ADMIN");

    /**
     * @return the roleUser
     */
    public GrantedAuthority getRoleUser() {
    	return roleUser;
    }

	/**
     * @param roleUser the roleUser to set
     */
    public void setRoleUser(GrantedAuthority roleUser) {
    	this.roleUser = roleUser;
    }

	/**
     * @return the roleAdmin
     */
    public GrantedAuthority getRoleAdmin() {
    	return roleAdmin;
    }

	/**
     * @param roleAdmin the roleAdmin to set
     */
    public void setRoleAdmin(GrantedAuthority roleAdmin) {
    	this.roleAdmin = roleAdmin;
    }

    public String getOpenIdRoot() {
        return openIdRoot;
    }

    public void setOpenIdRoot(String openIdRoot) {
        this.openIdRoot = openIdRoot;
    }

    public String getAdmins() {
        return admins;
    }

    public void setAdmins(String admins) {
        this.admins = admins;
        adminList.clear();
        Iterables.addAll(adminList, Splitter.on(',').omitEmptyStrings().split(admins));
    }

    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException, DataAccessException {

        if (identifier != null && identifier.startsWith(openIdRoot)) {
            String username = identifier;
            //String username = identifier.replace(openIdRoot, ""); // strip off the OpenID root
            String password = "notused";
            boolean enabled = true;
            boolean accountNonExpired = true;
            boolean credentialsNonExpired = true;
            boolean accountNonLocked = true;
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            authorities.add(roleUser);

            // calculate raw user id (SUI)
            // TODO: make this more generic, right now only works with postfix names
            String userid = identifier.replace(openIdRoot, "");

            if (adminList.contains(userid)) {
                authorities.add(roleAdmin);
            }

            return new User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        } else {
            throw new UsernameNotFoundException("Identifier " + identifier + " did not match OpenID root " + openIdRoot);
        }
    }
}
