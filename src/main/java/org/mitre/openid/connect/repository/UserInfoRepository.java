package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.UserInfo;

public interface UserInfoRepository {

	public UserInfo getByUserId(String user_id);
	
}
