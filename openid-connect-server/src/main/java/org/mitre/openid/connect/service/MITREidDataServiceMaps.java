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
 */
public class MITREidDataServiceMaps {

	private Map<Long, Long> accessTokenOldToNewIdMap = new HashMap<>();
	private Map<Long, Long> accessTokenToAuthHolderRefs = new HashMap<>();
	private Map<Long, String> accessTokenToClientRefs = new HashMap<>();
	private Map<Long, Long> accessTokenToRefreshTokenRefs = new HashMap<>();
	private Map<Long, Long> authHolderOldToNewIdMap = new HashMap<>();
	private Map<Long, Long> grantOldToNewIdMap = new HashMap<>();
	private Map<Long, Set<Long>> grantToAccessTokensRefs = new HashMap<>();
	private Map<Long, Long> refreshTokenOldToNewIdMap = new HashMap<>();
	private Map<Long, Long> refreshTokenToAuthHolderRefs = new HashMap<>();
	private Map<Long, String> refreshTokenToClientRefs = new HashMap<>();
	private Map<Long, Long> whitelistedSiteOldToNewIdMap = new HashMap<>();

	public Map<Long, Long> getAccessTokenOldToNewIdMap() {
		return accessTokenOldToNewIdMap;
	}

	public Map<Long, Long> getAccessTokenToAuthHolderRefs() {
		return accessTokenToAuthHolderRefs;
	}

	public Map<Long, String> getAccessTokenToClientRefs() {
		return accessTokenToClientRefs;
	}

	public Map<Long, Long> getAccessTokenToRefreshTokenRefs() {
		return accessTokenToRefreshTokenRefs;
	}

	public Map<Long, Long> getAuthHolderOldToNewIdMap() {
		return authHolderOldToNewIdMap;
	}

	public Map<Long, Long> getGrantOldToNewIdMap() {
		return grantOldToNewIdMap;
	}

	public Map<Long, Set<Long>> getGrantToAccessTokensRefs() {
		return grantToAccessTokensRefs;
	}

	public Map<Long, Long> getRefreshTokenOldToNewIdMap() {
		return refreshTokenOldToNewIdMap;
	}

	public Map<Long, Long> getRefreshTokenToAuthHolderRefs() {
		return refreshTokenToAuthHolderRefs;
	}

	public Map<Long, String> getRefreshTokenToClientRefs() {
		return refreshTokenToClientRefs;
	}

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
