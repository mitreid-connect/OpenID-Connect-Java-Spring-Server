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

import org.mitre.openid.connect.model.IdToken;
import org.mitre.openid.connect.repository.IdTokenRepository;
import org.mitre.openid.connect.service.IdTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the IdTokenService
 * 
 * @author Michael Joseph Walsh
 *
 */
@Service
@Transactional
public class IdTokenServiceImpl implements IdTokenService {

	@Autowired
	private IdTokenRepository idTokenRepository;

	/**
	 * Default constructor
	 */	
	public IdTokenServiceImpl() {

	}

    /**
     * Constructor for use in test harnesses. 
     * 
     * @param repository
     */	
	public IdTokenServiceImpl(IdTokenRepository idTokenRepository) {
		this.idTokenRepository = idTokenRepository;
	}	
	
	@Override
	public void save(IdToken idToken) {
		idTokenRepository.save(idToken);
	}

	@Override
	public IdToken getById(Long id) {
		return idTokenRepository.getById(id);
	}

	@Override
	public void remove(IdToken idToken) {
		idTokenRepository.remove(idToken);
	}

	@Override
	public void removeById(Long id) {
		idTokenRepository.removeById(id);
	}

}
