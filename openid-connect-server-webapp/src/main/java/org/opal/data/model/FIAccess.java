package org.opal.data.model;

import javax.persistence.*;

@Entity
@Table(name="fi_access")
@NamedQueries({
	@NamedQuery(name=FIAccess.QUERY_BY_USERNAME, query = "select u from FIAccess u WHERE u.creatorUserId = :" + FIAccess.PARAM_USERNAME),
	@NamedQuery(name=FIAccess.QUERY_BY_USERNAME_AND_CLIENTID, query = "select u from FIAccess u WHERE u.creatorUserId = :" + 
			FIAccess.PARAM_USERNAME + " and u.clientId = :" + FIAccess.PARAM_CLIENT_ID)
})
public class FIAccess {
	
	public static final String QUERY_BY_USERNAME = "FIAccess.getByUsername";
	public static final String QUERY_BY_USERNAME_AND_CLIENTID = "FIAccess.getByByUsernameAndClientId";

	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_CLIENT_ID = "clientId";
	
	private Long id;
	private String creatorUserId;
	private String clientId;
	private String sessionInfo;
	
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
	@Column(name="session_info")
	public String getSessionInfo() {
		return sessionInfo;
	}
	public void setSessionInfo(String sessionInfo) {
		this.sessionInfo = sessionInfo;
	}
	
	

}
