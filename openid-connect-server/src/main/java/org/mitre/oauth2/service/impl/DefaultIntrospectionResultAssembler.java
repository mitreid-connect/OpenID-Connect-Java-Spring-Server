/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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
package org.mitre.oauth2.service.impl;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.text.ParseException;
import java.util.Map;


import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.IntrospectionResultAssembler;
import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

/**
 * Default implementation of the {@link IntrospectionResultAssembler} interface.
 */
@Service
public class DefaultIntrospectionResultAssembler implements IntrospectionResultAssembler {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(DefaultIntrospectionResultAssembler.class);

	@Override
	public Map<String, Object> assembleFrom(OAuth2AccessTokenEntity accessToken, UserInfo userInfo) {

		Map<String, Object> result = newLinkedHashMap();
		OAuth2Authentication authentication = accessToken.getAuthenticationHolder().getAuthentication();

		result.put(ACTIVE, true);

		result.put(SCOPE, Joiner.on(SCOPE_SEPARATOR).join(accessToken.getScope()));

		if (accessToken.getExpiration() != null) {
			try {
				result.put(EXPIRES_AT, dateFormat.valueToString(accessToken.getExpiration()));
				result.put(EXP, accessToken.getExpiration().getTime() / 1000L);
			} catch (ParseException e) {
				log.error("Parse exception in token introspection", e);
			}
		}

		if (userInfo != null) {
			// if we have a UserInfo, use that for the subject
			result.put(SUB, userInfo.getSub());
		} else {
			// otherwise, use the authentication's username
			result.put(SUB, authentication.getName());
		}

		result.put(USER_ID, authentication.getName());

		result.put(CLIENT_ID, authentication.getOAuth2Request().getClientId());

		result.put(TOKEN_TYPE, accessToken.getTokenType());

		return result;
	}

	@Override
	public Map<String, Object> assembleFrom(OAuth2RefreshTokenEntity refreshToken, UserInfo userInfo) {

		Map<String, Object> result = newLinkedHashMap();
		OAuth2Authentication authentication = refreshToken.getAuthenticationHolder().getAuthentication();

		result.put(ACTIVE, true);

		result.put(SCOPE, Joiner.on(SCOPE_SEPARATOR).join(authentication.getOAuth2Request().getScope()));

		if (refreshToken.getExpiration() != null) {
			try {
				result.put(EXPIRES_AT, dateFormat.valueToString(refreshToken.getExpiration()));
				result.put(EXP, refreshToken.getExpiration().getTime() / 1000L);
			} catch (ParseException e) {
				log.error("Parse exception in token introspection", e);
			}
		}


		if (userInfo != null) {
			// if we have a UserInfo, use that for the subject
			result.put(SUB, userInfo.getSub());
		} else {
			// otherwise, use the authentication's username
			result.put(SUB, authentication.getName());
		}

		result.put(USER_ID, authentication.getName());

		result.put(CLIENT_ID, authentication.getOAuth2Request().getClientId());

		return result;
	}
}
