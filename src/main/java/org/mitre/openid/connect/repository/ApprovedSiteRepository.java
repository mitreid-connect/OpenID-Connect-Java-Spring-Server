package org.mitre.openid.connect.repository;

import java.util.Collection;
import java.util.Collections;

import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.UserInfo;

public interface ApprovedSiteRepository {

	public ApprovedSite getById(Long id);
	
	public Collection<ApprovedSite> getAllForUser(UserInfo user);
	
	public Collection<ApprovedSite> getAllExpired();

	public ApprovedSite save(ApprovedSite site);
	
}
