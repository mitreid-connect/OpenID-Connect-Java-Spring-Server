/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
 *******************************************************************************/

package cz.muni.ics.oauth2.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * @author jricher
 */
@Entity
@Table(name = "device_code")
@NamedQueries({
	@NamedQuery(name = DeviceCode.QUERY_BY_USER_CODE, query = "select d from DeviceCode d where d.userCode = :" + DeviceCode.PARAM_USER_CODE),
	@NamedQuery(name = DeviceCode.QUERY_BY_DEVICE_CODE, query = "select d from DeviceCode d where d.deviceCode = :" + DeviceCode.PARAM_DEVICE_CODE),
	@NamedQuery(name = DeviceCode.QUERY_EXPIRED_BY_DATE, query = "select d from DeviceCode d where d.expiration <= :" + DeviceCode.PARAM_DATE)
})
public class DeviceCode {

	public static final String QUERY_BY_USER_CODE = "DeviceCode.queryByUserCode";
	public static final String QUERY_BY_DEVICE_CODE = "DeviceCode.queryByDeviceCode";
	public static final String QUERY_EXPIRED_BY_DATE = "DeviceCode.queryExpiredByDate";

	public static final String PARAM_USER_CODE = "userCode";
	public static final String PARAM_DEVICE_CODE = "deviceCode";
	public static final String PARAM_DATE = "date";

	private Long id;
	private String deviceCode;
	private String userCode;
	private Set<String> scope;
	private Date expiration;
	private String clientId;
	private Map<String, String> requestParameters;
	private boolean approved;
	private AuthenticationHolderEntity authenticationHolder;

	public DeviceCode() { }

	public DeviceCode(String deviceCode, String userCode, Set<String> scope, String clientId, Map<String, String> params) {
		this.deviceCode = deviceCode;
		this.userCode = userCode;
		this.scope = scope;
		this.clientId = clientId;
		this.requestParameters = params;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "device_code")
	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	@Basic
	@Column(name = "user_code")
	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="device_code_scope", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="scope")
	public Set<String> getScope() {
		return scope;
	}

	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	@Basic
	@Column(name = "client_id")
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="device_code_request_parameter", joinColumns=@JoinColumn(name="owner_id"))
	@Column(name="val")
	@MapKeyColumn(name="param")
	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String> params) {
		this.requestParameters = params;
	}

	@Basic
	@Column(name = "approved")
	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
	public AuthenticationHolderEntity getAuthenticationHolder() {
		return authenticationHolder;
	}

	public void setAuthenticationHolder(AuthenticationHolderEntity authenticationHolder) {
		this.authenticationHolder = authenticationHolder;
	}

}
