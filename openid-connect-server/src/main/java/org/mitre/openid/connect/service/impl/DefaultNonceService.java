/*******************************************************************************
 * Copyright 2013 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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

import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.mitre.openid.connect.model.Nonce;
import org.mitre.openid.connect.repository.NonceRepository;
import org.mitre.openid.connect.service.NonceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("defaultNonceService")
public class DefaultNonceService implements NonceService {

	private static Logger logger = LoggerFactory.getLogger(NonceService.class);

	@Autowired
	private NonceRepository repository;

	@Autowired
	private Period nonceStorageDuration;

	/**
	 * Make sure that the nonce storage duration was set
	 */
	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		if (nonceStorageDuration == null) {
			logger.error("Nonce storage duration must be set!");
		}
		logger.info("Nonce Service ready to go");
	}

	@Override
	public Nonce create(String clientId, String value) {
		//Store nonce
		Nonce nonce = new Nonce();
		nonce.setClientId(clientId);
		nonce.setValue(value);
		DateTime now = new DateTime(new Date());
		nonce.setUseDate(now.toDate());
		DateTime expDate = now.plus(nonceStorageDuration);
		Date expirationJdkDate = expDate.toDate();
		nonce.setExpireDate(expirationJdkDate);
		return nonce;
	}

	@Override
	public boolean alreadyUsed(String clientId, String value) {

		Collection<Nonce> clientNonces = getByClientId(clientId);
		for (Nonce nonce : clientNonces) {
			String nonceVal = nonce.getValue();
			if (nonceVal.equals(value)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Nonce getById(Long id) {
		return repository.getById(id);
	}

	@Override
	public void remove(Nonce nonce) {
		repository.remove(nonce);
	}

	@Override
	public Nonce save(Nonce nonce) {
		return repository.save(nonce);
	}

	@Override
	public Collection<Nonce> getAll() {
		return repository.getAll();
	}

	@Override
	public Collection<Nonce> getExpired() {
		return repository.getExpired();
	}

	@Override
	public Collection<Nonce> getByClientId(String clientId) {
		return repository.getByClientId(clientId);
	}

	@Override
	//We are eventually deleting this class, but if we weren't,
	//this would have been moved to application-context.xml for easier configuration.
	//@Scheduled(fixedRate = 5 * 60 * 1000) // schedule this task every five minutes
	public void clearExpiredNonces() {

		logger.info("Clearing expired nonces");

		Collection<Nonce> expired = repository.getExpired();
		logger.info("Found " + expired.size() + " expired nonces");

		for (Nonce nonce : expired) {
			remove(nonce);
		}
	}

	public NonceRepository getRepository() {
		return repository;
	}

	public void setRepository(NonceRepository repository) {
		this.repository = repository;
	}

	public Period getNonceStorageDuration() {
		return nonceStorageDuration;
	}

	public void setNonceStorageDuration(Period nonceStorageDuration) {
		this.nonceStorageDuration = nonceStorageDuration;
	}


}
