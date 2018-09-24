package org.mitre.openid.connect.model;

import java.util.Collection;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.mitre.oauth2.model.convert.SimpleGrantedAuthorityStringConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity(name = "User")
@Table(name = "user")
@NamedQueries({
		@NamedQuery(name = DefaultUser.QUERY_ALL, query = "select u from User u where u.hostUuid = :"
				+ DefaultUser.PARAM_HOST_UUID),
		@NamedQuery(name = DefaultUser.QUERY_BY_USER_NAME, query = "select u from User u where u.hostUuid = :"
				+ DefaultUser.PARAM_HOST_UUID + " and u.username = :" + DefaultUser.PARAM_USER_NAME) })
public class DefaultUser implements UserDetails {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_ALL = "DefaultUser.queryAll";
	public static final String QUERY_BY_USER_NAME = "DefaultUser.getByUsername";

	public static final String PARAM_HOST_UUID = "hostUuid";
	public static final String PARAM_USER_NAME = "username";

	private String uuid;
	private String hostUuid;
	private String username;
	private String password;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	private Collection<GrantedAuthority> authorities;

	public DefaultUser() {
		this.uuid = UUID.randomUUID().toString();
	}

	public DefaultUser(String uuid) {
		super();
		this.uuid = uuid;
	}

	@Id
	@Column(name = "uuid")
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Basic
	@Column(name = "host_uuid")
	public String getHostUuid() {
		return hostUuid;
	}

	public void setHostUuid(String hostUuid) {
		this.hostUuid = hostUuid;
	}

	@Basic
	@Column(name = "username")
	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Basic
	@Column(name = "password")
	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Basic
	@Column(name = "account_non_expired")
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	@Basic
	@Column(name = "account_non_locked")
	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	@Basic
	@Column(name = "credentials_non_expired")
	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	@Basic
	@Column(name = "enabled")
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_authority", joinColumns = @JoinColumn(name = "user_uuid"))
	@Convert(converter = SimpleGrantedAuthorityStringConverter.class)
	@Column(name = "authority")
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

}
