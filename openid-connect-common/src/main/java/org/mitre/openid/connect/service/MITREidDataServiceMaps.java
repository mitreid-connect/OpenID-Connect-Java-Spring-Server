/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author jricher
 *
 */
public class MITREidDataServiceMaps {

	private Map<String, String> accessTokenOldToNewIdMap = new HashMap<String, String>();
	private Map<String, String> accessTokenToAuthHolderRefs = new HashMap<String, String>();
	private Map<String, String> accessTokenToClientRefs = new HashMap<String, String>();
	private Map<String, String> accessTokenToRefreshTokenRefs = new HashMap<String, String>();
	private Map<String, String> authHolderOldToNewIdMap = new HashMap<String, String>();
	private Map<String, String> grantOldToNewIdMap = new HashMap<>();
	private Map<String, Set<String>> grantToAccessTokensRefs = new HashMap<>();
	private Map<String, String> refreshTokenOldToNewIdMap = new HashMap<String, String>();
	private Map<String, String> refreshTokenToAuthHolderRefs = new HashMap<String, String>();
	private Map<String, String> refreshTokenToClientRefs = new HashMap<String, String>();
	private Map<String, String> whitelistedSiteOldToNewIdMap = new HashMap<String, String>();
	/**
	 * @return the accessTokenOldToNewIdMap
	 */
	public Map<String, String> getAccessTokenOldToNewIdMap() {
		return accessTokenOldToNewIdMap;
	}
	/**
	 * @return the accessTokenToAuthHolderRefs
	 */
	public Map<String, String> getAccessTokenToAuthHolderRefs() {
		return accessTokenToAuthHolderRefs;
	}
	/**
	 * @return the accessTokenToClientRefs
	 */
	public Map<String, String> getAccessTokenToClientRefs() {
		return accessTokenToClientRefs;
	}
	/**
	 * @return the accessTokenToRefreshTokenRefs
	 */
	public Map<String, String> getAccessTokenToRefreshTokenRefs() {
		return accessTokenToRefreshTokenRefs;
	}
	/**
	 * @return the authHolderOldToNewIdMap
	 */
	public Map<String, String> getAuthHolderOldToNewIdMap() {
		return authHolderOldToNewIdMap;
	}
	/**
	 * @return the grantOldToNewIdMap
	 */
	public Map<String, String> getGrantOldToNewIdMap() {
		return grantOldToNewIdMap;
	}
	/**
	 * @return the grantToAccessTokensRefs
	 */
	public Map<String, Set<String>> getGrantToAccessTokensRefs() {
		return grantToAccessTokensRefs;
	}
	/**
	 * @return the refreshTokenOldToNewIdMap
	 */
	public Map<String, String> getRefreshTokenOldToNewIdMap() {
		return refreshTokenOldToNewIdMap;
	}
	/**
	 * @return the refreshTokenToAuthHolderRefs
	 */
	public Map<String, String> getRefreshTokenToAuthHolderRefs() {
		return refreshTokenToAuthHolderRefs;
	}
	/**
	 * @return the refreshTokenToClientRefs
	 */
	public Map<String, String> getRefreshTokenToClientRefs() {
		return refreshTokenToClientRefs;
	}
	/**
	 * @return the whitelistedSiteOldToNewIdMap
	 */
	public Map<String, String> getWhitelistedSiteOldToNewIdMap() {
		return whitelistedSiteOldToNewIdMap;
	}

	public void clearAll() {
		refreshTokenToClientRefs.clear();
		refreshTokenToAuthHolderRefs.clear();
		accessTokenToClientRefs.clear();
		accessTokenToAuthHolderRefs.clear();
		accessTokenToRefreshTokenRefs.clear();
		refreshTokenOldToNewIdMap.clear();
		accessTokenOldToNewIdMap.clear();
		grantOldToNewIdMap.clear();
	}

}
