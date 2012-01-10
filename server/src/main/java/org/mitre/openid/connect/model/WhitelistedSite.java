package org.mitre.openid.connect.model;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * Indicator that login to a site should be automatically granted 
 * without user interaction.
 * @author jricher
 *
 */
@Entity
@Table(name="whitelistedsite")
public class WhitelistedSite {

    // unique id
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // who added this site to the whitelist (should be an admin)
	@ManyToOne
	@JoinColumn(name="userinfo_id")
	private UserInfo userInfo;
	
	// which OAuth2 client is this tied to
	@ManyToOne
	@JoinColumn(name="clientdetails_id")
	private ClientDetails clientDetails;
	
	// what scopes be allowed by default
	// this should include all information for what data to access
	@OneToMany(mappedBy="whitelistedsite")
	private Collection<String> allowedScopes;

	/**
	 * Empty constructor
	 */
	public WhitelistedSite() {
		
	}
	
	/**
	 * @return the id
	 */
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
	 * @return the userInfo
	 */
	public UserInfo getUserInfo() {
		return userInfo;
	}

	/**
	 * @param userInfo the userInfo to set
	 */
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	/**
	 * @return the clientDetails
	 */
	public ClientDetails getClientDetails() {
		return clientDetails;
	}

	/**
	 * @param clientDetails the clientDetails to set
	 */
	public void setClientDetails(ClientDetails clientDetails) {
		this.clientDetails = clientDetails;
	}

	/**
	 * @return the allowedScopes
	 */
	public Collection<String> getAllowedScopes() {
		return allowedScopes;
	}

	/**
	 * @param allowedScopes the allowedScopes to set
	 */
	public void setAllowedScopes(Collection<String> allowedScopes) {
		this.allowedScopes = allowedScopes;
	}
}
