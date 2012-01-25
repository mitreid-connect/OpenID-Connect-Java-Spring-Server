package org.mitre.openid.connect.service.impl;

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.impl.JpaApprovedSiteRepository;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ApprovedSiteService
 * 
 * @author Michael Joseph Walsh
 *
 */
@Service
@Transactional
public class ApprovedSiteServiceImpl implements ApprovedSiteService {

	@Autowired
	private JpaApprovedSiteRepository approvedSiteRepository;

	/**
	 * Default constructor
	 */	
	public ApprovedSiteServiceImpl() {

	}
	
    /**
     * Constructor for use in test harnesses. 
     * 
     * @param repository
     */	
	public ApprovedSiteServiceImpl(JpaApprovedSiteRepository approvedSiteRepository) {
		this.approvedSiteRepository = approvedSiteRepository;
	}	
	
	@Override
	public Collection<ApprovedSite> getAll() {
		return approvedSiteRepository.getAll();
	}

	@Override
	public Collection<ApprovedSite> getByClientDetails(
			ClientDetailsEntity clientDetails) {
		return approvedSiteRepository.getByClientDetails(clientDetails);
	}

	@Override
	public Collection<ApprovedSite> getByUserInfo(UserInfo userInfo) {
		return approvedSiteRepository.getByUserInfo(userInfo);
	}

	@Override
	public void save(ApprovedSite approvedSite) {
		approvedSiteRepository.save(approvedSite);
	}

	@Override
	public ApprovedSite getById(Long id) {
		return approvedSiteRepository.getById(id);
	}

	@Override
	public void remove(ApprovedSite approvedSite) {
		approvedSiteRepository.remove(approvedSite);
	}

	@Override
	public void removeById(Long id) {
		approvedSiteRepository.removeById(id);
	}

}
