package org.mitre.openid.connect.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;


@Entity
@Table(name="client_nonce")
@NamedQueries({
	@NamedQuery(name = "Nonce.getAll", query = "select n from Nonce n"),
	@NamedQuery(name = "Nonce.getByClientId", query = "select n from Nonce n where n.clientId = :clientId"),
	@NamedQuery(name = "Nonce.getExpired", query = "select n from Nonce n where n.expireDate < now()")
})
public class Nonce {

	
	private Long id; //the ID of this Nonce
	
	private String clientId;//The id of the client who used this Nonce
	
	private Date useDate; //the date this Nonce was used
	
	private Date expireDate; //the date after which this Nonce should be removed from the database 

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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
	 * @return the clientId
	 */
	@Basic
	@Column(name="client_id")
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the useDate
	 */
	@Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name="use_date")
	public Date getUseDate() {
		return useDate;
	}

	/**
	 * @param useDate the useDate to set
	 */
	public void setUseDate(Date useDate) {
		this.useDate = useDate;
	}

	/**
	 * @return the expireDate
	 */
	@Basic
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name="expire_date")
	public Date getExpireDate() {
		return expireDate;
	}

	/**
	 * @param expireDate the expireDate to set
	 */
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	
	
	
	
}
