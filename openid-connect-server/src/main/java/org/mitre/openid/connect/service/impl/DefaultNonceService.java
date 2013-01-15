package org.mitre.openid.connect.service.impl;

import java.util.Collection;

import org.mitre.openid.connect.model.Nonce;
import org.mitre.openid.connect.repository.NonceRepository;
import org.mitre.openid.connect.service.NonceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("defaultNonceService")
public class DefaultNonceService implements NonceService {

	private static Logger logger = LoggerFactory.getLogger(NonceService.class);	
	
	@Autowired
	NonceRepository repository;
	
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
	@Scheduled(fixedRate = 5 * 60 * 1000) // schedule this task every five minutes
	public void clearExpiredNonces() {
		
		logger.info("Clearing expired nonces");
		
		Collection<Nonce> expired = repository.getExpired();
		logger.info("Found " + expired.size() + " expired nonces");
		
		for (Nonce nonce : expired) {
			remove(nonce);
		}
	}

}
