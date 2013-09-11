/**
 * 
 */
package org.mitre.openid.connect.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * 
 * Holds the generated pairwise identifiers for a user. Can be tied to either a client ID or a sector identifier URL.
 * 
 * @author jricher
 *
 */
@Entity
@Table(name = "pairwise_identifier")
@NamedQueries({
	@NamedQuery(name="PairwiseIdentifier.getAll", query = "select p from PairwiseIdentifier p"),
	@NamedQuery(name="PairwiseIdentifier.getBySectorIdentifier", query = "select p from PairwiseIdentifier p WHERE p.userSub = :sub AND p.sectorIdentifier = :sectorIdentifier")
})
public class PairwiseIdentifier {

	private Long id;
	private String identifier;
	private String userSub;
	private String sectorIdentifier;

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the identifier
	 */
	@Basic
	@Column(name = "identifier")
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the userSub
	 */
	@Basic
	@Column(name = "sub")
	public String getUserSub() {
		return userSub;
	}
	
	/**
	 * @param userSub the userSub to set
	 */
	public void setUserSub(String userSub) {
		this.userSub = userSub;
	}
	
	/**
	 * @return the sectorIdentifier
	 */
	@Basic
	@Column(name = "sector_identifier")
	public String getSectorIdentifier() {
		return sectorIdentifier;
	}
	
	/**
	 * @param sectorIdentifier the sectorIdentifier to set
	 */
	public void setSectorIdentifier(String sectorIdentifier) {
		this.sectorIdentifier = sectorIdentifier;
	}
}
