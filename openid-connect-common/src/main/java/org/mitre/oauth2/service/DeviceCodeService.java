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

import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.exception.DeviceCodeCreationException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.DeviceCode;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * @author jricher
 *
 */
public interface DeviceCodeService {

	/**
	 * @param userCode
	 * @return
	 */
	public DeviceCode lookUpByUserCode(String userCode);

	/**
	 * @param dc
	 * @param o2Auth
	 */
	public DeviceCode approveDeviceCode(DeviceCode dc, OAuth2Authentication o2Auth);

	/**
	 * @param deviceCode
	 * @param client
	 * @return
	 */
	public DeviceCode findDeviceCode(String deviceCode, ClientDetails client);


	/**
	 * 
	 * @param deviceCode
	 * @param client
	 */
	public void clearDeviceCode(String deviceCode, ClientDetails client);
	
	/**
	 * @param deviceCode
	 * @param userCode
	 * @param requestedScopes
	 * @param client
	 * @param parameters
	 * @return
	 */
	public DeviceCode createNewDeviceCode(Set<String> requestedScopes, ClientDetailsEntity client, Map<String, String> parameters) throws DeviceCodeCreationException;


	public void clearExpiredDeviceCodes();
}
