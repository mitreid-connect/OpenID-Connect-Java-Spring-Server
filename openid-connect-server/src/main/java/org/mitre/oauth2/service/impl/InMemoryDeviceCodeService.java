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

package org.mitre.oauth2.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.DeviceCode;
import org.mitre.oauth2.service.DeviceCodeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

/**
 * @author jricher
 *
 */
@Service
public class InMemoryDeviceCodeService implements DeviceCodeService {
	
	private Set<DeviceCode> codes = new HashSet<>();
	
	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.DeviceCodeService#save(org.mitre.oauth2.model.DeviceCode)
	 */
	@Override
	public DeviceCode createNewDeviceCode(String deviceCode, String userCode, Set<String> requestedScopes, ClientDetailsEntity client, Map<String, String> parameters) {
		
		DeviceCode dc = new DeviceCode(deviceCode, userCode, requestedScopes, client.getClientId(), parameters);
		
		if (client.getDeviceCodeValiditySeconds() != null) {
			dc.setExpiration(new Date(System.currentTimeMillis() + client.getDeviceCodeValiditySeconds() * 1000L));
		}
		
		dc.setApproved(false);
		
		codes.add(dc);
		return dc;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.DeviceCodeService#lookUpByUserCode(java.lang.String)
	 */
	@Override
	public DeviceCode lookUpByUserCode(String userCode) {
		for (DeviceCode dc : codes) {
			if (dc.getUserCode().equals(userCode)) {
				return dc;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.DeviceCodeService#approveDeviceCode(org.mitre.oauth2.model.DeviceCode)
	 */
	@Override
	public DeviceCode approveDeviceCode(DeviceCode dc, OAuth2Authentication auth) {
		dc.setApproved(true);
		
		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setAuthentication(auth);
		dc.setAuthenticationHolder(authHolder);

		return dc;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.service.DeviceCodeService#consumeDeviceCode(java.lang.String, org.springframework.security.oauth2.provider.ClientDetails)
	 */
	@Override
	public DeviceCode consumeDeviceCode(String deviceCode, ClientDetails client) {
		for (DeviceCode dc : codes) {
			if (dc.getDeviceCode().equals(deviceCode) && dc.getClientId().equals(client.getClientId())) {
				codes.remove(dc);
				return dc;
			}
		}
		return null;
	}

}
