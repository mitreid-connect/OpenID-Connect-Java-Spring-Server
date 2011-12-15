package org.mitre.openid.connect.repository;

import org.mitre.openid.connect.model.ApprovedSite;

public interface ApprovedSiteRepository {

	public ApprovedSite getById(Long id);
	
	public ApprovedSite getByUrl(String url);
	
}
