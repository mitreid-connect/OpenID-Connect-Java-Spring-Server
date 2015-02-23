package org.mitre.openid.connect.model;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.mitre.oauth2.model.SystemScope;

@Entity
@Table(name = "resource_set")
public class ResourceSet {

	private Long id;
	private String name;
	private String uri;
	private String type;
	private Set<String> scopes;
	private String iconUri;
	
	private String policyUri;
	
	private String owner; // username of the person responsible for the reigistration (either directly or via OAuth token)
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	 * @return the name
	 */
	@Basic
	@Column(name = "name")
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the uri
	 */
	@Basic
	@Column(name = "uri")
	public String getUri() {
		return uri;
	}
	
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * @return the type
	 */
	@Basic
	@Column(name = "type")
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the scopes
	 */
	@OneToMany(fetch=FetchType.EAGER)
	@CollectionTable(
		name="resource_set_scope",
		joinColumns=@JoinColumn(name="owner_id")
	)
	public Set<String> getScopes() {
		return scopes;
	}
	
	/**
	 * @param scopes the scopes to set
	 */
	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}
	
	/**
	 * @return the iconUri
	 */
	@Basic
	@Column(name = "icon_uri")
	public String getIconUri() {
		return iconUri;
	}
	
	/**
	 * @param iconUri the iconUri to set
	 */
	public void setIconUri(String iconUri) {
		this.iconUri = iconUri;
	}
	
	@Basic
	@Column(name = "policy_uri")
	public String getPolicyUri() {
		return this.policyUri;
	}
	
	public void setPolicyUri(String policyUri) {
		this.policyUri = policyUri;
	}
	
	/**
	 * @return the owner
	 */
	@Basic
	@Column(name = "owner")
	public String getOwner() {
		return owner;
	}
	
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	
	
	
	
}
