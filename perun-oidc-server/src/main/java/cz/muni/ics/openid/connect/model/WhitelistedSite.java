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

import static cz.muni.ics.openid.connect.model.WhitelistedSite.PARAM_CLIENT_ID;
import static cz.muni.ics.openid.connect.model.WhitelistedSite.PARAM_USER_ID;
import static cz.muni.ics.openid.connect.model.WhitelistedSite.QUERY_ALL;
import static cz.muni.ics.openid.connect.model.WhitelistedSite.QUERY_BY_CLIENT_ID;
import static cz.muni.ics.openid.connect.model.WhitelistedSite.QUERY_BY_CREATOR;

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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * Indicator that login to a site should be automatically granted
 * without user interaction.
 * @author jricher, aanganes
 *
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
// DB ANNOTATIONS
@Entity
@Table(name="whitelisted_site")
@NamedQueries({
	@NamedQuery(name = QUERY_ALL,
				query = "SELECT w FROM WhitelistedSite w"),
	@NamedQuery(name = QUERY_BY_CLIENT_ID,
				query = "SELECT w FROM WhitelistedSite w " +
						"WHERE w.clientId = :" + PARAM_CLIENT_ID),
	@NamedQuery(name = QUERY_BY_CREATOR,
				query = "SELECT w FROM WhitelistedSite w " +
						"WHERE w.creatorUserId = :" + PARAM_USER_ID)
})
public class WhitelistedSite {

	public static final String QUERY_BY_CREATOR = "WhitelistedSite.getByCreatoruserId";
	public static final String QUERY_BY_CLIENT_ID = "WhitelistedSite.getByClientId";
	public static final String QUERY_ALL = "WhitelistedSite.getAll";

	public static final String PARAM_USER_ID = "userId";
	public static final String PARAM_CLIENT_ID = "clientId";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "creator_user_id")
	private String creatorUserId;

	@Column(name = "client_id")
	private String clientId;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "whitelisted_site_scope", joinColumns = @JoinColumn(name = "owner_id"))
	@Column(name = "scope")
	@CascadeOnDelete
	private Set<String> allowedScopes;

}
