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

import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.repository.AddressRepository;
import org.mitre.openid.connect.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the AddressService
 * 
 * @author Michael Joseph Walsh
 *
 */
@Service
@Transactional
public class AddressServiceImpl implements AddressService {

		@Autowired
		private AddressRepository addressRepository;

		/**
		 * Default constructor
		 */
		public AddressServiceImpl() {

		}
		
        /**
         * Constructor for use in test harnesses. 
         * 
         * @param repository
         */		
		public AddressServiceImpl(AddressRepository addressRepository) {
			this.addressRepository = addressRepository;
		}

		@Override
		public void save(Address address) {
			this.addressRepository.save(address);
		}

		@Override
		public Address getById(Long id) {
			return addressRepository.getById(id);
		}

		@Override
		public void remove(Address address) {
			this.addressRepository.remove(address);
		}

		@Override
		public void removeById(Long id) {
			this.addressRepository.removeById(id);
		}		
}
