package org.mitre.openid.connect.service.impl;

import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.repository.impl.JpaAddressRepository;
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
		private JpaAddressRepository addressRepository;

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
		public AddressServiceImpl(JpaAddressRepository addressRepository) {
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
