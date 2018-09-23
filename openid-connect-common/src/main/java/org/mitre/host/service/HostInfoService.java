package org.mitre.host.service;

import org.mitre.host.model.HostInfo;

public interface HostInfoService {

	
	HostInfo getCurrentHostInfo();
	
	String getCurrentHostUuid();
	
	void validateHost(String hostUuid);
}
