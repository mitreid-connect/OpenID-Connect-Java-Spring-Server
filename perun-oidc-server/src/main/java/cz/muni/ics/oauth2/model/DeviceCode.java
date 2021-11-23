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

import static cz.muni.ics.oauth2.model.DeviceCode.PARAM_DATE;
import static cz.muni.ics.oauth2.model.DeviceCode.PARAM_DEVICE_CODE;
import static cz.muni.ics.oauth2.model.DeviceCode.PARAM_USER_CODE;
import static cz.muni.ics.oauth2.model.DeviceCode.QUERY_BY_DEVICE_CODE;
import static cz.muni.ics.oauth2.model.DeviceCode.QUERY_BY_USER_CODE;
import static cz.muni.ics.oauth2.model.DeviceCode.QUERY_EXPIRED_BY_DATE;

import java.util.Date;
import java.util.Map;
import java.util.Set;
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
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * @author jricher
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name = "device_code")
@NamedQueries({
		@NamedQuery(name = QUERY_BY_USER_CODE,
				query = "SELECT d FROM DeviceCode d " +
						"WHERE d.userCode = :" + PARAM_USER_CODE),
		@NamedQuery(name = QUERY_BY_DEVICE_CODE,
				query = "SELECT d FROM DeviceCode d " +
						"WHERE d.deviceCode = :" + PARAM_DEVICE_CODE),
		@NamedQuery(name = QUERY_EXPIRED_BY_DATE,
				query = "SELECT d FROM DeviceCode d " +
						"WHERE d.expiration <= :" + PARAM_DATE)
})
public class DeviceCode {

	public static final String QUERY_BY_USER_CODE = "DeviceCode.queryByUserCode";
	public static final String QUERY_BY_DEVICE_CODE = "DeviceCode.queryByDeviceCode";
	public static final String QUERY_EXPIRED_BY_DATE = "DeviceCode.queryExpiredByDate";

	public static final String PARAM_USER_CODE = "userCode";
	public static final String PARAM_DEVICE_CODE = "deviceCode";
	public static final String PARAM_DATE = "date";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "device_code")
	private String deviceCode;

	@Column(name = "user_code")
	private String userCode;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "device_code_scope", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "scope")
	@CascadeOnDelete
	private Set<String> scope;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expiration")
	private Date expiration;

	@Column(name = "client_id")
	private String clientId;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "device_code_request_parameter", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "val")
	@MapKeyColumn(name = "param")
	@CascadeOnDelete
	private Map<String, String> requestParameters;

	@Column(name = "approved")
	private boolean approved;

	@ManyToOne
	@JoinColumn(name = "auth_holder_id")
	private AuthenticationHolderEntity authenticationHolder;

	public DeviceCode(String deviceCode,
					  String userCode,
					  Set<String> scope,
					  String clientId,
					  Map<String, String> params)
	{
		this.deviceCode = deviceCode;
		this.userCode = userCode;
		this.scope = scope;
		this.clientId = clientId;
		this.requestParameters = params;
	}

}
