package org.mitre.host.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity(name = "HostInfo")
@Table(name = "host_info")
@NamedQueries({ @NamedQuery(name = DefaultHostInfo.QUERY_BY_HOST, query = "select h from HostInfo h where h.host = :"
		+ DefaultHostInfo.PARAM_HOST),
	@NamedQuery(name = DefaultHostInfo.QUERY_BY_UUID, query = "select h from HostInfo h where h.uuid = :"
			+ DefaultHostInfo.PARAM_UUID)})
public class DefaultHostInfo implements HostInfo {

	public static final String QUERY_BY_HOST = "HostInfo.queryByHost";
	public static final String QUERY_BY_UUID = "HostInfo.queryByUuid";

	public static final String PARAM_UUID = "uuid";
	public static final String PARAM_HOST = "host";

	private String uuid;
	private String host;
	private String config;

	public DefaultHostInfo() {
		this.uuid = UUID.randomUUID().toString();
	}
	
	@Id
	@Column(name = "uuid")
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
