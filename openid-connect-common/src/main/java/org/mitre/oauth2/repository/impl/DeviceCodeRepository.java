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

package org.mitre.oauth2.repository.impl;

import java.util.Collection;

import org.mitre.oauth2.model.DeviceCode;

/**
 * @author jricher
 *
 */
public interface DeviceCodeRepository {

	/**
	 * @param id
	 * @return
	 */
	public DeviceCode getById(Long id);

	/**
	 * @param deviceCode
	 * @return
	 */
	public DeviceCode getByDeviceCode(String deviceCode);

	/**
	 * @param scope
	 */
	public void remove(DeviceCode scope);

	/**
	 * @param scope
	 * @return
	 */
	public DeviceCode save(DeviceCode scope);

	/**
	 * @param userCode
	 * @return
	 */
	public DeviceCode getByUserCode(String userCode);

	/**
	 * @return
	 */
	public Collection<DeviceCode> getExpiredCodes();

}
