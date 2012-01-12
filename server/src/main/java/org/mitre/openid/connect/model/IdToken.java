package org.mitre.openid.connect.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mitre.jwt.model.Jwt;

@Entity
@Table(name="idtoken")
@NamedQueries({
	@NamedQuery(name = "IdToken.getAll", query = "select i from IdToken i")
})
public class IdToken extends Jwt {

	private Long id;

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
	 * @return the tokenClaims
	 */
	@Transient
	public IdTokenClaims getTokenClaims() {
		return (IdTokenClaims) super.getClaims();
	}

	/**
	 * @param tokenClaims the tokenClaims to set
	 */
	public void setTokenClaims(IdTokenClaims tokenClaims) {
		super.setClaims(tokenClaims);
	}
	
	
	
}
