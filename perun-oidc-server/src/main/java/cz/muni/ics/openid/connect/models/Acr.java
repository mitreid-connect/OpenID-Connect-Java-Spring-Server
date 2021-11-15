package cz.muni.ics.openid.connect.models;

import static cz.muni.ics.openid.connect.models.Acr.PARAM_EXPIRES_AT;
import static cz.muni.ics.openid.connect.models.Acr.PARAM_SUB;

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
 * Model of ACR.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Entity
@Table(name = "acrs")
@NamedQueries({
	@NamedQuery(name = Acr.GET_ACTIVE, query = "SELECT acr FROM Acr acr WHERE " +
		"acr.sub = :" + PARAM_SUB +
		" AND acr.clientId = :" + Acr.PARAM_CLIENT_ID +
		" AND acr.state = :" + Acr.PARAM_STATE +
		" AND acr.expiresAt > :" + PARAM_EXPIRES_AT),
	@NamedQuery(name = Acr.GET_BY_ID,
		query = "SELECT acr FROM Acr acr " +
			"WHERE acr.id = :" + Acr.PARAM_ID +
			" AND acr.expiresAt > :" + PARAM_EXPIRES_AT),
	@NamedQuery(name = Acr.DELETE_EXPIRED,
		query = "DELETE FROM Acr acr WHERE acr.expiresAt <= :" + Acr.PARAM_EXPIRES_AT)
})
public class Acr {

	public static final String GET_ACTIVE = "Acr.getActive";
	public static final String GET_BY_ID = "Acr.getById";
	public static final String DELETE_EXPIRED = "Acr.deleteExpired";

	public static final String PARAM_ID = "id";
	public static final String PARAM_SUB = "sub";
	public static final String PARAM_CLIENT_ID = "client_id";
	public static final String PARAM_STATE = "state";
	public static final String PARAM_EXPIRES_AT = "expiration";

	private Long id;
	private String sub;
	private String clientId;
	private String state;
	private String shibAuthnContextClass;
	private long expiresAt;

	public Acr() { }

	public Acr(String sub, String clientId, String state, String shibAuthnContextClass, long expiresAt) {
		this.sub = sub;
		this.clientId = clientId;
		this.state = state;
		this.shibAuthnContextClass = shibAuthnContextClass;
		this.expiresAt = expiresAt;
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
	@Column(name = "sub")
	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	@Basic
	@Column(name = "client_id")
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Basic
	@Column(name = "state")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Basic
	@Column(name = "shib_authn_context_class")
	public String getShibAuthnContextClass() {
		return shibAuthnContextClass;
	}

	public void setShibAuthnContextClass(String shibAuthnContextClass) {
		this.shibAuthnContextClass = shibAuthnContextClass;
	}

	@Basic
	@Column(name = "expiration")
	public long getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(long expiresAt) {
		this.expiresAt = expiresAt;
	}

	@Override
	public String toString() {
		return "Acr{" +
			"id=" + id +
			", sub='" + sub + '\'' +
			", clientId='" + clientId + '\'' +
			", state='" + state + '\'' +
			", shibAuthnContextClass='" + shibAuthnContextClass + '\'' +
			", expiration=" + expiresAt +
			'}';
	}
}
