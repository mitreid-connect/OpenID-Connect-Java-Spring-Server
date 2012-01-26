package org.mitre.openid.connect.service.impl;

import org.mitre.openid.connect.model.IdTokenClaims;
import org.mitre.openid.connect.repository.impl.JpaIdTokenClaimsRepository;
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
	private JpaIdTokenClaimsRepository idTokenClaimsRepository;

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
	public IdTokenClaimsServiceImpl(JpaIdTokenClaimsRepository idTokenClaimsRepository) {
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
