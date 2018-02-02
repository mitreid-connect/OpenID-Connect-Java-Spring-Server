/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.oauth2.service;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import javax.swing.text.DateFormatter;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.openid.connect.model.UserInfo;

/**
 * Strategy interface for assembling a token introspection result.
 */
public interface IntrospectionResultAssembler {

	public String TOKEN_TYPE = "token_type";
	public String CLIENT_ID = "client_id";
	public String USER_ID = "user_id";
	public String SUB = "sub";
	public String EXP = "exp";
	public String EXPIRES_AT = "expires_at";
	public String SCOPE_SEPARATOR = " ";
	public String SCOPE = "scope";
	public String ACTIVE = "active";
	public DateFormatter dateFormat = new DateFormatter(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));

	/**
	 * Assemble a token introspection result from the given access token and user info.
	 *
	 * @param accessToken the access token
	 * @param userInfo the user info
	 * @param authScopes the scopes the client is authorized for
	 * @return the token introspection result
	 */
	Map<String, Object> assembleFrom(OAuth2AccessTokenEntity accessToken, UserInfo userInfo, Set<String> authScopes);

	/**
	 * Assemble a token introspection result from the given refresh token and user info.
	 *
	 * @param refreshToken the refresh token
	 * @param userInfo the user info
	 * @param authScopes the scopes the client is authorized for
	 * @return the token introspection result
	 */
	Map<String, Object> assembleFrom(OAuth2RefreshTokenEntity refreshToken, UserInfo userInfo, Set<String> authScopes);

}
