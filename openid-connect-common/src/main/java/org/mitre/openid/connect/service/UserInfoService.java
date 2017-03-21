/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.mitre.openid.connect.service;

import org.mitre.openid.connect.model.UserInfo;

/**
 * Interface for UserInfo service
 *
 * @author Michael Joseph Walsh
 *
 */
public interface UserInfoService {

	/**
	 * Get the UserInfo for the given username (usually maps to the
	 * preferredUsername field).
	 * @param username
	 * @return
	 */
	public UserInfo getByUsername(String username);

	/**
	 * Get the UserInfo for the given username (usually maps to the
	 * preferredUsername field) and clientId. This allows pairwise
	 * client identifiers where appropriate.
	 * @param username
	 * @param clientId
	 * @return
	 */
	public UserInfo getByUsernameAndClientId(String username, String clientId);

	/**
	 * Get the user registered at this server with the given email address.
	 *
	 * @param email
	 * @return
	 */
	public UserInfo getByEmailAddress(String email);

}
