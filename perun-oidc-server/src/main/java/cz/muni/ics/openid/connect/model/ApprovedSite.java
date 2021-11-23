/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
package cz.muni.ics.openid.connect.model;

import static cz.muni.ics.openid.connect.model.ApprovedSite.PARAM_CLIENT_ID;
import static cz.muni.ics.openid.connect.model.ApprovedSite.PARAM_USER_ID;
import static cz.muni.ics.openid.connect.model.ApprovedSite.QUERY_ALL;
import static cz.muni.ics.openid.connect.model.ApprovedSite.QUERY_BY_CLIENT_ID;
import static cz.muni.ics.openid.connect.model.ApprovedSite.QUERY_BY_CLIENT_ID_AND_USER_ID;
import static cz.muni.ics.openid.connect.model.ApprovedSite.QUERY_BY_USER_ID;

import java.util.Date;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name="approved_site")
@NamedQueries({
	@NamedQuery(name = QUERY_ALL,
			query = "SELECT a FROM ApprovedSite a"),
	@NamedQuery(name = QUERY_BY_USER_ID,
			query = "SELECT a FROM ApprovedSite a " +
					"WHERE a.userId = :" + PARAM_USER_ID),
	@NamedQuery(name = QUERY_BY_CLIENT_ID,
			query = "SELECT a FROM ApprovedSite a " +
					"WHERE a.clientId = :" + PARAM_CLIENT_ID),
	@NamedQuery(name = QUERY_BY_CLIENT_ID_AND_USER_ID,
			query = "SELECT a FROM ApprovedSite a " +
					"WHERE a.clientId = :" + PARAM_CLIENT_ID + ' ' +
					"AND a.userId = :" + PARAM_USER_ID)
})
public class ApprovedSite {

	public static final String QUERY_BY_CLIENT_ID_AND_USER_ID = "ApprovedSite.getByClientIdAndUserId";
	public static final String QUERY_BY_CLIENT_ID = "ApprovedSite.getByClientId";
	public static final String QUERY_BY_USER_ID = "ApprovedSite.getByUserId";
	public static final String QUERY_ALL = "ApprovedSite.getAll";

	public static final String PARAM_CLIENT_ID = "clientId";
	public static final String PARAM_USER_ID = "userId";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "client_id")
	private String clientId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "access_date")
	private Date accessDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timeout_date")
	private Date timeoutDate;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "approved_site_scope", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "scope")
	private Set<String> allowedScopes;

	@Transient
	public boolean isExpired() {
		if (getTimeoutDate() != null) {
			Date now = new Date();
			return now.after(getTimeoutDate());
		} else {
			return false;
		}
	}

}
