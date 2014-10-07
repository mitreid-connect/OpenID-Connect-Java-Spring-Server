/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
 ******************************************************************************/
package org.mitre.oauth2.service.impl;

import com.google.common.base.Joiner;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.IntrospectionResultAssembler;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 * Default implementation of the {@link IntrospectionResultAssembler} interface.
 */
@Service
public class DefaultIntrospectionResultAssembler implements IntrospectionResultAssembler {

    @Override
    public Map<String, Object> assembleFrom(OAuth2AccessTokenEntity accessToken, UserInfo userInfo) {

        Map<String, Object> result = newLinkedHashMap();
        OAuth2Authentication authentication = accessToken.getAuthenticationHolder().getAuthentication();

        result.put("active", true);

        result.put("scope", Joiner.on(" ").join(accessToken.getScope()));

        if (accessToken.getExpiration() != null) {
            result.put("exp", accessToken.getExpiration());
        }

        if (userInfo != null) {
            // if we have a UserInfo, use that for the subject
            result.put("sub", userInfo.getSub());
        } else {
            // otherwise, use the authentication's username
            result.put("sub", authentication.getName());
        }

        result.put("user_id", authentication.getName());

        result.put("client_id", authentication.getOAuth2Request().getClientId());

        result.put("token_type", accessToken.getTokenType());

        return result;
    }

    @Override
    public Map<String, Object> assembleFrom(OAuth2RefreshTokenEntity refreshToken, UserInfo userInfo) {

        Map<String, Object> result = newLinkedHashMap();
        OAuth2Authentication authentication = refreshToken.getAuthenticationHolder().getAuthentication();

        result.put("active", true);

        result.put("scope", Joiner.on(" ").join(authentication.getOAuth2Request().getScope()));

        if (refreshToken.getExpiration() != null) {
            result.put("exp", refreshToken.getExpiration());
        }

        if (userInfo != null) {
            // if we have a UserInfo, use that for the subject
            result.put("sub", userInfo.getSub());
        } else {
            // otherwise, use the authentication's username
            result.put("sub", authentication.getName());
        }

        result.put("user_id", authentication.getName());

        result.put("client_id", authentication.getOAuth2Request().getClientId());

        return result;
    }
}
