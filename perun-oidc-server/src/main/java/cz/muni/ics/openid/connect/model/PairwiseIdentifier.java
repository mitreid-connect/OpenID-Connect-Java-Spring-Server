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
/**
 *
 */
package cz.muni.ics.openid.connect.model;

import static cz.muni.ics.openid.connect.model.PairwiseIdentifier.PARAM_SECTOR_IDENTIFIER;
import static cz.muni.ics.openid.connect.model.PairwiseIdentifier.PARAM_SUB;
import static cz.muni.ics.openid.connect.model.PairwiseIdentifier.QUERY_ALL;
import static cz.muni.ics.openid.connect.model.PairwiseIdentifier.QUERY_BY_SECTOR_IDENTIFIER;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * Holds the generated pairwise identifiers for a user. Can be tied to either a client ID or a sector identifier URL.
 *
 * @author jricher
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
@Table(name = "pairwise_identifier")
@NamedQueries({
	@NamedQuery(name = QUERY_ALL,
				query = "SELECT p FROM PairwiseIdentifier p"),
	@NamedQuery(name = QUERY_BY_SECTOR_IDENTIFIER,
				query = "SELECT p FROM PairwiseIdentifier p " +
						"WHERE p.userSub = :" + PARAM_SUB + ' ' +
						"AND p.sectorIdentifier = :" + PARAM_SECTOR_IDENTIFIER)
})
public class PairwiseIdentifier {

	public static final String QUERY_BY_SECTOR_IDENTIFIER = "PairwiseIdentifier.getBySectorIdentifier";
	public static final String QUERY_ALL = "PairwiseIdentifier.getAll";

	public static final String PARAM_SECTOR_IDENTIFIER = "sectorIdentifier";
	public static final String PARAM_SUB = "sub";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "identifier")
	private String identifier;

	@Column(name = PARAM_SUB)
	private String userSub;

	@Column(name = "sector_identifier")
	private String sectorIdentifier;

}
