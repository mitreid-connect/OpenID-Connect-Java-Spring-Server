package org.mitre.openid.connect.model;

import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.mitre.oauth2.model.ClientDetailsEntity;

/**
 * Indicator that login to a site should be automatically granted 
 * without user interaction.
 * @author jricher
 *
 */
@Entity
@Table(name="whitelistedsite")
@NamedQueries({
	@NamedQuery(name = "WhitelistedSite.getAll", query = "select w from WhitelistedSite w")
})
public class WhitelistedSite {

    // unique id
    private Long id;
    
    // who added this site to the whitelist (should be an admin)
	private UserInfo userInfo;
	
	// which OAuth2 client is this tied to
	private ClientDetailsEntity clientDetails;
	
	// what scopes be allowed by default
	// this should include all information for what data to access
	private Set<String> allowedScopes;

	/**
	 * Empty constructor
	 */
	public WhitelistedSite() {
		
	}
	
	/**
	 * @return the id
	 */
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)	
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
	@ManyToOne
	@JoinColumn(name="userinfo_id")
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
	@ManyToOne
	@JoinColumn(name="clientdetails_id")
	public ClientDetailsEntity getClientDetails() {
		return clientDetails;
	}

	/**
	 * @param clientDetails the clientDetails to set
	 */
	public void setClientDetails(ClientDetailsEntity clientDetails) {
		this.clientDetails = clientDetails;
	}

	/**
	 * @return the allowedScopes
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	public Set<String> getAllowedScopes() {
		return allowedScopes;
	}

	/**
	 * @param allowedScopes the allowedScopes to set
	 */
	public void setAllowedScopes(Set<String> allowedScopes) {
		this.allowedScopes = allowedScopes;
	}
}
