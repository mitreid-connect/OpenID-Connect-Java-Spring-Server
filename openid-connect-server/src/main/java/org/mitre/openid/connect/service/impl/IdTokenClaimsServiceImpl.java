/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
package org.mitre.openid.connect.service.impl;

import org.mitre.openid.connect.model.IdTokenClaims;
import org.mitre.openid.connect.repository.IdTokenClaimsRepository;
import org.mitre.openid.connect.service.IdTokenClaimsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the IdTokenClaimsService
 * 
 * @author Michael Joseph Walsh
 *
 */
@Service
@Transactional
public class IdTokenClaimsServiceImpl implements IdTokenClaimsService {

	@Autowired
	private IdTokenClaimsRepository idTokenClaimsRepository;

	/**
	 * Default constructor
	 */	
	public IdTokenClaimsServiceImpl() {

	}

    /**
     * Constructor for use in test harnesses. 
     * 
     * @param repository
     */	
	public IdTokenClaimsServiceImpl(IdTokenClaimsRepository idTokenClaimsRepository) {
		this.idTokenClaimsRepository = idTokenClaimsRepository;
	}	
	
	@Override
	public void save(IdTokenClaims idTokenClaims) {
		idTokenClaimsRepository.save(idTokenClaims);
	}

	@Override
	public IdTokenClaims getById(Long id) {
		return idTokenClaimsRepository.getById(id);
	}

	@Override
	public void remove(IdTokenClaims idTokenClaims) {
		idTokenClaimsRepository.remove(idTokenClaims);
	}

	@Override
	public void removeById(Long id) {
		idTokenClaimsRepository.removeById(id);
	}

}
