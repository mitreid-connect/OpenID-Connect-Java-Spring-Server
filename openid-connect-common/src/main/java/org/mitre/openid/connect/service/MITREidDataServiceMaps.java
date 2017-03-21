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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author jricher
 *
 */
public class MITREidDataServiceMaps {

	private Map<Long, Long> accessTokenOldToNewIdMap = new HashMap<Long, Long>();
	private Map<Long, Long> accessTokenToAuthHolderRefs = new HashMap<Long, Long>();
	private Map<Long, String> accessTokenToClientRefs = new HashMap<Long, String>();
	private Map<Long, Long> accessTokenToRefreshTokenRefs = new HashMap<Long, Long>();
	private Map<Long, Long> authHolderOldToNewIdMap = new HashMap<Long, Long>();
	private Map<Long, Long> grantOldToNewIdMap = new HashMap<>();
	private Map<Long, Set<Long>> grantToAccessTokensRefs = new HashMap<>();
	private Map<Long, Long> refreshTokenOldToNewIdMap = new HashMap<Long, Long>();
	private Map<Long, Long> refreshTokenToAuthHolderRefs = new HashMap<Long, Long>();
	private Map<Long, String> refreshTokenToClientRefs = new HashMap<Long, String>();
	private Map<Long, Long> whitelistedSiteOldToNewIdMap = new HashMap<Long, Long>();
	/**
	 * @return the accessTokenOldToNewIdMap
	 */
	public Map<Long, Long> getAccessTokenOldToNewIdMap() {
		return accessTokenOldToNewIdMap;
	}
	/**
	 * @return the accessTokenToAuthHolderRefs
	 */
	public Map<Long, Long> getAccessTokenToAuthHolderRefs() {
		return accessTokenToAuthHolderRefs;
	}
	/**
	 * @return the accessTokenToClientRefs
	 */
	public Map<Long, String> getAccessTokenToClientRefs() {
		return accessTokenToClientRefs;
	}
	/**
	 * @return the accessTokenToRefreshTokenRefs
	 */
	public Map<Long, Long> getAccessTokenToRefreshTokenRefs() {
		return accessTokenToRefreshTokenRefs;
	}
	/**
	 * @return the authHolderOldToNewIdMap
	 */
	public Map<Long, Long> getAuthHolderOldToNewIdMap() {
		return authHolderOldToNewIdMap;
	}
	/**
	 * @return the grantOldToNewIdMap
	 */
	public Map<Long, Long> getGrantOldToNewIdMap() {
		return grantOldToNewIdMap;
	}
	/**
	 * @return the grantToAccessTokensRefs
	 */
	public Map<Long, Set<Long>> getGrantToAccessTokensRefs() {
		return grantToAccessTokensRefs;
	}
	/**
	 * @return the refreshTokenOldToNewIdMap
	 */
	public Map<Long, Long> getRefreshTokenOldToNewIdMap() {
		return refreshTokenOldToNewIdMap;
	}
	/**
	 * @return the refreshTokenToAuthHolderRefs
	 */
	public Map<Long, Long> getRefreshTokenToAuthHolderRefs() {
		return refreshTokenToAuthHolderRefs;
	}
	/**
	 * @return the refreshTokenToClientRefs
	 */
	public Map<Long, String> getRefreshTokenToClientRefs() {
		return refreshTokenToClientRefs;
	}
	/**
	 * @return the whitelistedSiteOldToNewIdMap
	 */
	public Map<Long, Long> getWhitelistedSiteOldToNewIdMap() {
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
