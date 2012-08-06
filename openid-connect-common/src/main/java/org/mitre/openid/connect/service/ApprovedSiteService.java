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
package org.mitre.openid.connect.service;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;

/**
 * Interface for ApprovedSite service
 * 
 * @author Michael Joseph Walsh, aanganes
 * 
 */
public interface ApprovedSiteService {
	
	
	public ApprovedSite createApprovedSite(String clientId, String userId, Date timeoutDate, Set<String> allowedScopes, WhitelistedSite whitelistedSite);
	
	/**
	 * Return a collection of all ApprovedSites
	 * 
	 * @return the ApprovedSite collection, or null
	 */
	public Collection<ApprovedSite> getAll();
	
	/**
	 * Return a collection of ApprovedSite managed by this repository matching the
	 * provided client ID and user ID
	 * 
	 * @param clientId
	 * @param userId
	 * @return
	 */	
	public ApprovedSite getByClientIdAndUserId(String clientId, String userId);
	
	/**
	 * Save an ApprovedSite
	 * 
	 * @param approvedSite
	 *            the ApprovedSite to be saved
	 */
	public ApprovedSite save(ApprovedSite approvedSite);

	/**
	 * Get ApprovedSite for id
	 * 
	 * @param id
	 *            id for ApprovedSite
	 * @return ApprovedSite for id, or null
	 */
	public ApprovedSite getById(Long id);

	/**
	 * Remove the ApprovedSite
	 * 
	 * @param approvedSite
	 *            the ApprovedSite to remove
	 */
	public void remove(ApprovedSite approvedSite);

	/**
	 * Remove the ApprovedSite
	 * 
	 * @param id
	 *            id for ApprovedSite to remove
	 */
	public void removeById(Long id);

}
