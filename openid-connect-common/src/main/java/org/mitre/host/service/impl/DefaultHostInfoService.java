package org.mitre.host.service.impl;

import java.net.URL;

import org.mitre.host.model.HostInfo;
import org.mitre.host.repository.HostInfoRepository;
import org.mitre.host.service.HostInfoService;
import org.mitre.host.util.HostUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultHostInfoService implements HostInfoService {

	@Autowired
	HostInfoRepository hostInfoRepository;

	@Override
	public HostInfo getCurrentHostInfo() {
		URL hostUrl = HostUtils.getCurrentHost();
		return hostInfoRepository.getByHost(hostUrl.getHost());
	}

	@Override
	public String getCurrentHostUuid() {
		return getCurrentHostInfo().getUuid();
	}

	@Override
	public void validateHost(String hostUuid) {
		if (!getCurrentHostUuid().equals(hostUuid)) {
			throw new IllegalArgumentException("Host is violated");
		}
	}

}
