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

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
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
	private ApprovedSiteRepository approvedSiteRepository;

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
	public ApprovedSiteServiceImpl(ApprovedSiteRepository approvedSiteRepository) {
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
	public Collection<ApprovedSite> getByUserId(String userId) {
		return approvedSiteRepository.getByUserId(userId);
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
