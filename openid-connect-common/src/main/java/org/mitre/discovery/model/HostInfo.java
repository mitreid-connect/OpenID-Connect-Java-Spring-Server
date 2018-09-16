package org.mitre.discovery.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "host_info")
@NamedQueries({ @NamedQuery(name = HostInfo.QUERY_BY_HOST, query = "select h from HostInfo h where h.host = :"
		+ HostInfo.PARAM_HOST) })
public class HostInfo {

	public static final String QUERY_BY_HOST = "HostInfo.queryByHost";

	public static final String PARAM_HOST = "host";

	private String uuid;
	private String host;
	private String config;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}	
	
}
