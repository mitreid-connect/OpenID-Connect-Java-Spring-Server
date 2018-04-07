package org.opal.data.model;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="fi_access")
@NamedQueries({
	@NamedQuery(name=FIAccess.QUERY_BY_USERNAME, query = "select u from FIAccess u WHERE u.creatorUserId = :" + FIAccess.PARAM_USERNAME),
	@NamedQuery(name=FIAccess.QUERY_BY_UCI, query = "select u from FIAccess u WHERE u.creatorUserId = :" + 
			FIAccess.PARAM_USERNAME + " and u.clientId = :" + FIAccess.PARAM_CLIENT_ID +" and u.issuer = :"+FIAccess.PARAM_ISSUER)
})
public class FIAccess {
	
	public static final String QUERY_BY_USERNAME = "FIAccess.getByUsername";
	public static final String QUERY_BY_UCI = "FIAccess.getByByUCI";

	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_ISSUER = "issuer";
	public static final String PARAM_CLIENT_ID = "clientId";
	
	private Long id;
	private String creatorUserId;
	private String clientId;
	private String issuer;
	private String sessionInfo;
	private String accessToken;
	private String refreshToken;
	private Date expiration;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Basic
	@Column(name="creator_user_id")
	public String getCreatorUserId() {
		return creatorUserId;
	}
	public void setCreatorUserId(String creatorUserId) {
		this.creatorUserId = creatorUserId;
	}
	
	@Basic
	@Column(name="client_id")
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	@Basic
	@Column(name="issuer")
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	@Basic
	@Column(name="session_info", nullable=true)
	public String getSessionInfo() {
		return sessionInfo;
	}
	public void setSessionInfo(String sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	@Basic
	@Column(name="access_token", nullable=true)
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	@Basic
	@Column(name="refresh_token", nullable=true)
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	@Basic
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	@Column(name = "expiration", nullable=true)
	public Date getExpiration() {
		return expiration;
	}
	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	
}
