package org.mitre.openid.connect;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.mitre.openid.connect.model.ApprovedSite;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizationRequest;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ConnectAuthorizationRequest implements AuthorizationRequest {

	//SECOAUTH interface parameters
	private Map<String, String> authorizationParameters;
	private Map<String, String> approvalParameters;
	private String clientId;
	private Set<String> scope;
	private Set<String> resourceIds;
	private Collection<? extends GrantedAuthority> authorities;
	private boolean approved = false;
	private String state;
	private String redirectUri;
	private Set<String> responseTypes;
	
	//Extra parameters
	private ApprovedSite approvedSite; //See issue 230
	
	/**
	 * Default constructor. Initialize maps & sets.
	 */
	public ConnectAuthorizationRequest() {
		authorizationParameters = Maps.newHashMap();
		approvalParameters = Maps.newHashMap();
		scope = Sets.newHashSet();
		resourceIds = Sets.newHashSet();
		authorities = Sets.newHashSet();
		responseTypes = Sets.newHashSet();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param authorizationParameters
	 * @param approvalParameters
	 * @param clientId
	 * @param scope
	 * @param resourceIds
	 * @param authorities
	 * @param approved
	 * @param state
	 * @param redirectUri
	 * @param responseTypes
	 */
	public ConnectAuthorizationRequest(Map<String, String> authorizationParameters, Map<String, String> approvalParameters, String clientId, Set<String> scope, Set<String> resourceIds,
										Collection<? extends GrantedAuthority> authorities, boolean approved, String state, String redirectUri, Set<String> responseTypes) {
		this.authorizationParameters = authorizationParameters;
		this.approvalParameters = approvalParameters;
		this.clientId = clientId;
		this.scope = scope;
		this.resourceIds = resourceIds;
		this.authorities = authorities;
		this.approved = approved;
		this.state = state;
		this.redirectUri = redirectUri;
		this.responseTypes = responseTypes;
	}
	
	@Override
 	public Map<String, String> getAuthorizationParameters() {
		return authorizationParameters;
	}

	@Override
	public void setAuthorizationParameters(Map<String, String> authorizationParameters) {
		this.authorizationParameters = authorizationParameters;
	}

	@Override
	public Map<String, String> getApprovalParameters() {
		return approvalParameters;
	}

	@Override
	public void setApprovalParameters(Map<String, String> approvalParameters) {
		this.approvalParameters = approvalParameters;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public Set<String> getScope() {
		return scope;
	}
	
	@Override
	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	@Override
	public Set<String> getResourceIds() {
		return resourceIds;
	}

	@Override
	public void setResourceIds(Set<String> resourceIds) {
		this.resourceIds = resourceIds;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Override
	public boolean isApproved() {
		return approved;
	}

	@Override
	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@Override
	public boolean isDenied() {
		return !approved;
	}

	@Override
	public void setDenied(boolean denied) {
		this.approved = !denied;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String getRedirectUri() {
		return redirectUri;
	}

	@Override
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	@Override
	public Set<String> getResponseTypes() {
		return responseTypes;
	}

	@Override
	public void setResponseTypes(Set<String> responseTypes) {
		this.responseTypes = responseTypes;
	}

	/**
	 * @return the approvedSite
	 */
	public ApprovedSite getApprovedSite() {
		return approvedSite;
	}

	/**
	 * @param approvedSite the approvedSite to set
	 */
	public void setApprovedSite(ApprovedSite approvedSite) {
		this.approvedSite = approvedSite;
	}

}
