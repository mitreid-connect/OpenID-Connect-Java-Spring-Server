/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
package org.mitre.openid.connect.client;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Utility class to fetch userinfo from the userinfo endpoint, if available. 
 * @author jricher
 *
 */
public class UserInfoFetcher {

	private Logger logger = LoggerFactory.getLogger(UserInfoFetcher.class);
	
	public UserInfo loadUserInfo(OIDCAuthenticationToken token) {

		ServerConfiguration serverConfiguration = token.getServerConfiguration();
		
		if (serverConfiguration == null) {
			logger.warn("No server configuration found.");
			return null;
		}
		
		if (Strings.isNullOrEmpty(serverConfiguration.getUserInfoUri())) {
			logger.warn("No userinfo endpoint, not fetching.");
			return null;
		}

		// if we got this far, try to actually get the userinfo
		
		HttpClient httpClient = new DefaultHttpClient();

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		RestTemplate restTemplate = new RestTemplate(factory);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.add("access_token", token.getAccessTokenValue());

		try {
			String userInfoString = restTemplate.postForObject(serverConfiguration.getUserInfoUri(), form, String.class);
	
			JsonObject userInfoJson = new JsonParser().parse(userInfoString).getAsJsonObject();
	
			UserInfo userInfo = DefaultUserInfo.fromJson(userInfoJson);
	
			return userInfo;
		} catch (Exception e) {
			logger.warn("Error fetching userinfo", e);
			return null;
		}

	}

}
