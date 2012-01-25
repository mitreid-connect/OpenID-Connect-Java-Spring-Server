package org.mitre.openid.connect.service.impl;

import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.impl.JpaWhitelistedSiteRepository;
import org.mitre.openid.connect.service.WhitelistedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the WhitelistedSiteService
 * 
 * @author Michael Joseph Walsh
 * 
 */
@Service
@Transactional
public class WhitelistedSiteServiceImpl implements WhitelistedSiteService {

	@Autowired
	private JpaWhitelistedSiteRepository whitelistedSiteRepository;

	/**
	 * Default constructor
	 */
	public WhitelistedSiteServiceImpl() {

	}

	/**
	 * Constructor for use in test harnesses.
	 * 
	 * @param repository
	 */
	public WhitelistedSiteServiceImpl(
			JpaWhitelistedSiteRepository whitelistedSiteRepository) {
		this.whitelistedSiteRepository = whitelistedSiteRepository;
	}	
	
	@Override
	public WhitelistedSite getById(Long id) {
		return whitelistedSiteRepository.getById(id);
	}

	@Override
	public void remove(WhitelistedSite whitelistedSite) {
		whitelistedSiteRepository.remove(whitelistedSite);
	}

	@Override
	public void removeById(Long id) {
	}

	@Override
	public WhitelistedSite save(WhitelistedSite whitelistedSite) {
		return whitelistedSiteRepository.save(whitelistedSite);
	}

}
