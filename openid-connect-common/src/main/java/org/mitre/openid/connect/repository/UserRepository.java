package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.openid.connect.model.DefaultUser;

public interface UserRepository {

	public DefaultUser getById(String uuid);

	public DefaultUser getUserByUsername(String username);

	public DefaultUser saveUser(DefaultUser user);

	public void deleteUser(DefaultUser user);

	public DefaultUser updateUser(String uuid, DefaultUser user);

	public Collection<DefaultUser> getAllUsers();
}
