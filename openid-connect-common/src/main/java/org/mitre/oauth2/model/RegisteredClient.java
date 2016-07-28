/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package org.mitre.oauth2.model;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.springframework.security.core.GrantedAuthority;

import com.google.gson.JsonObject;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;

/**
 * @author jricher
 *
 */
public class RegisteredClient {

	// these fields are needed in addition to the ones in ClientDetailsEntity
	private String registrationAccessToken;
	private String registrationClientUri;
	private Date clientSecretExpiresAt;
	private Date clientIdIssuedAt;
	private ClientDetailsEntity client;
	private JsonObject src;

	/**
	 * 
	 */
	public RegisteredClient() {
		this.client = new ClientDetailsEntity();
	}

	/**
	 * @param client
	 */
	public RegisteredClient(ClientDetailsEntity client) {
		this.client = client;
	}

	/**
	 * @param client
	 * @param registrationAccessToken
	 * @param registrationClientUri
	 */
	public RegisteredClient(ClientDetailsEntity client, String registrationAccessToken, String registrationClientUri) {
		this.client = client;
		this.registrationAccessToken = registrationAccessToken;
		this.registrationClientUri = registrationClientUri;
	}

	/**
	 * @return the client
	 */
	public ClientDetailsEntity getClient() {
		return client;
	}
	/**
	 * @param client the client to set
	 */
	public void setClient(ClientDetailsEntity client) {
		this.client = client;
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getClientDescription()
	 */
	public String getClientDescription() {
		return client.getClientDescription();
	}
	/**
	 * @param clientDescription
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientDescription(java.lang.String)
	 */
	public void setClientDescription(String clientDescription) {
		client.setClientDescription(clientDescription);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#isAllowRefresh()
	 */
	public boolean isAllowRefresh() {
		return client.isAllowRefresh();
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#isReuseRefreshToken()
	 */
	public boolean isReuseRefreshToken() {
		return client.isReuseRefreshToken();
	}
	/**
	 * @param reuseRefreshToken
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setReuseRefreshToken(boolean)
	 */
	public void setReuseRefreshToken(boolean reuseRefreshToken) {
		client.setReuseRefreshToken(reuseRefreshToken);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getIdTokenValiditySeconds()
	 */
	public Integer getIdTokenValiditySeconds() {
		return client.getIdTokenValiditySeconds();
	}
	/**
	 * @param idTokenValiditySeconds
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setIdTokenValiditySeconds(java.lang.Integer)
	 */
	public void setIdTokenValiditySeconds(Integer idTokenValiditySeconds) {
		client.setIdTokenValiditySeconds(idTokenValiditySeconds);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#isDynamicallyRegistered()
	 */
	public boolean isDynamicallyRegistered() {
		return client.isDynamicallyRegistered();
	}
	/**
	 * @param dynamicallyRegistered
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setDynamicallyRegistered(boolean)
	 */
	public void setDynamicallyRegistered(boolean dynamicallyRegistered) {
		client.setDynamicallyRegistered(dynamicallyRegistered);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#isAllowIntrospection()
	 */
	public boolean isAllowIntrospection() {
		return client.isAllowIntrospection();
	}
	/**
	 * @param allowIntrospection
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setAllowIntrospection(boolean)
	 */
	public void setAllowIntrospection(boolean allowIntrospection) {
		client.setAllowIntrospection(allowIntrospection);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#isSecretRequired()
	 */
	public boolean isSecretRequired() {
		return client.isSecretRequired();
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#isScoped()
	 */
	public boolean isScoped() {
		return client.isScoped();
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getClientId()
	 */
	public String getClientId() {
		return client.getClientId();
	}
	/**
	 * @param clientId
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientId(java.lang.String)
	 */
	public void setClientId(String clientId) {
		client.setClientId(clientId);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getClientSecret()
	 */
	public String getClientSecret() {
		return client.getClientSecret();
	}
	/**
	 * @param clientSecret
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientSecret(java.lang.String)
	 */
	public void setClientSecret(String clientSecret) {
		client.setClientSecret(clientSecret);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getScope()
	 */
	public Set<String> getScope() {
		return client.getScope();
	}
	/**
	 * @param scope
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setScope(java.util.Set)
	 */
	public void setScope(Set<String> scope) {
		client.setScope(scope);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getGrantTypes()
	 */
	public Set<String> getGrantTypes() {
		return client.getGrantTypes();
	}
	/**
	 * @param grantTypes
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setGrantTypes(java.util.Set)
	 */
	public void setGrantTypes(Set<String> grantTypes) {
		client.setGrantTypes(grantTypes);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getAuthorizedGrantTypes()
	 */
	public Set<String> getAuthorizedGrantTypes() {
		return client.getAuthorizedGrantTypes();
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getAuthorities()
	 */
	public Set<GrantedAuthority> getAuthorities() {
		return client.getAuthorities();
	}
	/**
	 * @param authorities
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setAuthorities(java.util.Set)
	 */
	public void setAuthorities(Set<GrantedAuthority> authorities) {
		client.setAuthorities(authorities);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getAccessTokenValiditySeconds()
	 */
	public Integer getAccessTokenValiditySeconds() {
		return client.getAccessTokenValiditySeconds();
	}
	/**
	 * @param accessTokenValiditySeconds
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setAccessTokenValiditySeconds(java.lang.Integer)
	 */
	public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
		client.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getRefreshTokenValiditySeconds()
	 */
	public Integer getRefreshTokenValiditySeconds() {
		return client.getRefreshTokenValiditySeconds();
	}
	/**
	 * @param refreshTokenValiditySeconds
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setRefreshTokenValiditySeconds(java.lang.Integer)
	 */
	public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
		client.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getRedirectUris()
	 */
	public Set<String> getRedirectUris() {
		return client.getRedirectUris();
	}
	/**
	 * @param redirectUris
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setRedirectUris(java.util.Set)
	 */
	public void setRedirectUris(Set<String> redirectUris) {
		client.setRedirectUris(redirectUris);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getRegisteredRedirectUri()
	 */
	public Set<String> getRegisteredRedirectUri() {
		return client.getRegisteredRedirectUri();
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getResourceIds()
	 */
	public Set<String> getResourceIds() {
		return client.getResourceIds();
	}
	/**
	 * @param resourceIds
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setResourceIds(java.util.Set)
	 */
	public void setResourceIds(Set<String> resourceIds) {
		client.setResourceIds(resourceIds);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getAdditionalInformation()
	 */
	public Map<String, Object> getAdditionalInformation() {
		return client.getAdditionalInformation();
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getApplicationType()
	 */
	public AppType getApplicationType() {
		return client.getApplicationType();
	}
	/**
	 * @param applicationType
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setApplicationType(org.mitre.oauth2.model.ClientDetailsEntity.AppType)
	 */
	public void setApplicationType(AppType applicationType) {
		client.setApplicationType(applicationType);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getClientName()
	 */
	public String getClientName() {
		return client.getClientName();
	}
	/**
	 * @param clientName
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientName(java.lang.String)
	 */
	public void setClientName(String clientName) {
		client.setClientName(clientName);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getTokenEndpointAuthMethod()
	 */
	public AuthMethod getTokenEndpointAuthMethod() {
		return client.getTokenEndpointAuthMethod();
	}
	/**
	 * @param tokenEndpointAuthMethod
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setTokenEndpointAuthMethod(org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod)
	 */
	public void setTokenEndpointAuthMethod(AuthMethod tokenEndpointAuthMethod) {
		client.setTokenEndpointAuthMethod(tokenEndpointAuthMethod);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getSubjectType()
	 */
	public SubjectType getSubjectType() {
		return client.getSubjectType();
	}
	/**
	 * @param subjectType
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setSubjectType(org.mitre.oauth2.model.ClientDetailsEntity.SubjectType)
	 */
	public void setSubjectType(SubjectType subjectType) {
		client.setSubjectType(subjectType);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getContacts()
	 */
	public Set<String> getContacts() {
		return client.getContacts();
	}
	/**
	 * @param contacts
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setContacts(java.util.Set)
	 */
	public void setContacts(Set<String> contacts) {
		client.setContacts(contacts);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getLogoUri()
	 */
	public String getLogoUri() {
		return client.getLogoUri();
	}
	/**
	 * @param logoUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setLogoUri(java.lang.String)
	 */
	public void setLogoUri(String logoUri) {
		client.setLogoUri(logoUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getPolicyUri()
	 */
	public String getPolicyUri() {
		return client.getPolicyUri();
	}
	/**
	 * @param policyUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setPolicyUri(java.lang.String)
	 */
	public void setPolicyUri(String policyUri) {
		client.setPolicyUri(policyUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getClientUri()
	 */
	public String getClientUri() {
		return client.getClientUri();
	}
	/**
	 * @param clientUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setClientUri(java.lang.String)
	 */
	public void setClientUri(String clientUri) {
		client.setClientUri(clientUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getTosUri()
	 */
	public String getTosUri() {
		return client.getTosUri();
	}
	/**
	 * @param tosUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setTosUri(java.lang.String)
	 */
	public void setTosUri(String tosUri) {
		client.setTosUri(tosUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getJwksUri()
	 */
	public String getJwksUri() {
		return client.getJwksUri();
	}
	/**
	 * @param jwksUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setJwksUri(java.lang.String)
	 */
	public void setJwksUri(String jwksUri) {
		client.setJwksUri(jwksUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getJwks()
	 */
	public JWKSet getJwks() {
		return client.getJwks();
	}

	/**
	 * @param jwks
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setJwks(com.nimbusds.jose.jwk.JWKSet)
	 */
	public void setJwks(JWKSet jwks) {
		client.setJwks(jwks);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getSectorIdentifierUri()
	 */
	public String getSectorIdentifierUri() {
		return client.getSectorIdentifierUri();
	}
	/**
	 * @param sectorIdentifierUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setSectorIdentifierUri(java.lang.String)
	 */
	public void setSectorIdentifierUri(String sectorIdentifierUri) {
		client.setSectorIdentifierUri(sectorIdentifierUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getDefaultMaxAge()
	 */
	public Integer getDefaultMaxAge() {
		return client.getDefaultMaxAge();
	}
	/**
	 * @param defaultMaxAge
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setDefaultMaxAge(java.lang.Integer)
	 */
	public void setDefaultMaxAge(Integer defaultMaxAge) {
		client.setDefaultMaxAge(defaultMaxAge);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getRequireAuthTime()
	 */
	public Boolean getRequireAuthTime() {
		return client.getRequireAuthTime();
	}
	/**
	 * @param requireAuthTime
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setRequireAuthTime(java.lang.Boolean)
	 */
	public void setRequireAuthTime(Boolean requireAuthTime) {
		client.setRequireAuthTime(requireAuthTime);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getResponseTypes()
	 */
	public Set<String> getResponseTypes() {
		return client.getResponseTypes();
	}
	/**
	 * @param responseTypes
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setResponseTypes(java.util.Set)
	 */
	public void setResponseTypes(Set<String> responseTypes) {
		client.setResponseTypes(responseTypes);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getDefaultACRvalues()
	 */
	public Set<String> getDefaultACRvalues() {
		return client.getDefaultACRvalues();
	}
	/**
	 * @param defaultACRvalues
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setDefaultACRvalues(java.util.Set)
	 */
	public void setDefaultACRvalues(Set<String> defaultACRvalues) {
		client.setDefaultACRvalues(defaultACRvalues);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getInitiateLoginUri()
	 */
	public String getInitiateLoginUri() {
		return client.getInitiateLoginUri();
	}
	/**
	 * @param initiateLoginUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setInitiateLoginUri(java.lang.String)
	 */
	public void setInitiateLoginUri(String initiateLoginUri) {
		client.setInitiateLoginUri(initiateLoginUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getPostLogoutRedirectUris()
	 */
	public Set<String> getPostLogoutRedirectUris() {
		return client.getPostLogoutRedirectUris();
	}
	/**
	 * @param postLogoutRedirectUri
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setPostLogoutRedirectUris(java.lang.String)
	 */
	public void setPostLogoutRedirectUris(Set<String> postLogoutRedirectUri) {
		client.setPostLogoutRedirectUris(postLogoutRedirectUri);
	}
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getRequestUris()
	 */
	public Set<String> getRequestUris() {
		return client.getRequestUris();
	}
	/**
	 * @param requestUris
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setRequestUris(java.util.Set)
	 */
	public void setRequestUris(Set<String> requestUris) {
		client.setRequestUris(requestUris);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getRequestObjectSigningAlg()
	 */
	public JWSAlgorithm getRequestObjectSigningAlg() {
		return client.getRequestObjectSigningAlg();
	}

	/**
	 * @param requestObjectSigningAlg
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setRequestObjectSigningAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	public void setRequestObjectSigningAlg(JWSAlgorithm requestObjectSigningAlg) {
		client.setRequestObjectSigningAlg(requestObjectSigningAlg);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getUserInfoSignedResponseAlg()
	 */
	public JWSAlgorithm getUserInfoSignedResponseAlg() {
		return client.getUserInfoSignedResponseAlg();
	}

	/**
	 * @param userInfoSignedResponseAlg
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setUserInfoSignedResponseAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	public void setUserInfoSignedResponseAlg(JWSAlgorithm userInfoSignedResponseAlg) {
		client.setUserInfoSignedResponseAlg(userInfoSignedResponseAlg);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getUserInfoEncryptedResponseAlg()
	 */
	public JWEAlgorithm getUserInfoEncryptedResponseAlg() {
		return client.getUserInfoEncryptedResponseAlg();
	}

	/**
	 * @param userInfoEncryptedResponseAlg
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setUserInfoEncryptedResponseAlg(com.nimbusds.jose.JWEAlgorithm)
	 */
	public void setUserInfoEncryptedResponseAlg(JWEAlgorithm userInfoEncryptedResponseAlg) {
		client.setUserInfoEncryptedResponseAlg(userInfoEncryptedResponseAlg);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getUserInfoEncryptedResponseEnc()
	 */
	public EncryptionMethod getUserInfoEncryptedResponseEnc() {
		return client.getUserInfoEncryptedResponseEnc();
	}

	/**
	 * @param userInfoEncryptedResponseEnc
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setUserInfoEncryptedResponseEnc(com.nimbusds.jose.EncryptionMethod)
	 */
	public void setUserInfoEncryptedResponseEnc(EncryptionMethod userInfoEncryptedResponseEnc) {
		client.setUserInfoEncryptedResponseEnc(userInfoEncryptedResponseEnc);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getIdTokenSignedResponseAlg()
	 */
	public JWSAlgorithm getIdTokenSignedResponseAlg() {
		return client.getIdTokenSignedResponseAlg();
	}

	/**
	 * @param idTokenSignedResponseAlg
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setIdTokenSignedResponseAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	public void setIdTokenSignedResponseAlg(JWSAlgorithm idTokenSignedResponseAlg) {
		client.setIdTokenSignedResponseAlg(idTokenSignedResponseAlg);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getIdTokenEncryptedResponseAlg()
	 */
	public JWEAlgorithm getIdTokenEncryptedResponseAlg() {
		return client.getIdTokenEncryptedResponseAlg();
	}

	/**
	 * @param idTokenEncryptedResponseAlg
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setIdTokenEncryptedResponseAlg(com.nimbusds.jose.JWEAlgorithm)
	 */
	public void setIdTokenEncryptedResponseAlg(JWEAlgorithm idTokenEncryptedResponseAlg) {
		client.setIdTokenEncryptedResponseAlg(idTokenEncryptedResponseAlg);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getIdTokenEncryptedResponseEnc()
	 */
	public EncryptionMethod getIdTokenEncryptedResponseEnc() {
		return client.getIdTokenEncryptedResponseEnc();
	}

	/**
	 * @param idTokenEncryptedResponseEnc
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setIdTokenEncryptedResponseEnc(com.nimbusds.jose.EncryptionMethod)
	 */
	public void setIdTokenEncryptedResponseEnc(EncryptionMethod idTokenEncryptedResponseEnc) {
		client.setIdTokenEncryptedResponseEnc(idTokenEncryptedResponseEnc);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getTokenEndpointAuthSigningAlg()
	 */
	public JWSAlgorithm getTokenEndpointAuthSigningAlg() {
		return client.getTokenEndpointAuthSigningAlg();
	}

	/**
	 * @param tokenEndpointAuthSigningAlg
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setTokenEndpointAuthSigningAlg(com.nimbusds.jose.JWSAlgorithm)
	 */
	public void setTokenEndpointAuthSigningAlg(JWSAlgorithm tokenEndpointAuthSigningAlg) {
		client.setTokenEndpointAuthSigningAlg(tokenEndpointAuthSigningAlg);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getCreatedAt()
	 */
	public Date getCreatedAt() {
		return client.getCreatedAt();
	}
	/**
	 * @param createdAt
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setCreatedAt(java.util.Date)
	 */
	public void setCreatedAt(Date createdAt) {
		client.setCreatedAt(createdAt);
	}
	/**
	 * @return the registrationAccessToken
	 */
	public String getRegistrationAccessToken() {
		return registrationAccessToken;
	}
	/**
	 * @param registrationAccessToken the registrationAccessToken to set
	 */
	public void setRegistrationAccessToken(String registrationAccessToken) {
		this.registrationAccessToken = registrationAccessToken;
	}
	/**
	 * @return the registrationClientUri
	 */
	public String getRegistrationClientUri() {
		return registrationClientUri;
	}
	/**
	 * @param registrationClientUri the registrationClientUri to set
	 */
	public void setRegistrationClientUri(String registrationClientUri) {
		this.registrationClientUri = registrationClientUri;
	}
	/**
	 * @return the clientSecretExpiresAt
	 */
	public Date getClientSecretExpiresAt() {
		return clientSecretExpiresAt;
	}
	/**
	 * @param clientSecretExpiresAt the clientSecretExpiresAt to set
	 */
	public void setClientSecretExpiresAt(Date expiresAt) {
		this.clientSecretExpiresAt = expiresAt;
	}
	/**
	 * @return the clientIdIssuedAt
	 */
	public Date getClientIdIssuedAt() {
		return clientIdIssuedAt;
	}
	/**
	 * @param clientIdIssuedAt the clientIdIssuedAt to set
	 */
	public void setClientIdIssuedAt(Date issuedAt) {
		this.clientIdIssuedAt = issuedAt;
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getClaimsRedirectUris()
	 */
	public Set<String> getClaimsRedirectUris() {
		return client.getClaimsRedirectUris();
	}

	/**
	 * @param claimsRedirectUris
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setClaimsRedirectUris(java.util.Set)
	 */
	public void setClaimsRedirectUris(Set<String> claimsRedirectUris) {
		client.setClaimsRedirectUris(claimsRedirectUris);
	}

	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getSoftwareStatement()
	 */
	public JWT getSoftwareStatement() {
		return client.getSoftwareStatement();
	}

	/**
	 * @param softwareStatement
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setSoftwareStatement(com.nimbusds.jwt.JWT)
	 */
	public void setSoftwareStatement(JWT softwareStatement) {
		client.setSoftwareStatement(softwareStatement);
	}
	
	/**
	 * @return
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#getCodeChallengeMethod()
	 */
	public PKCEAlgorithm getCodeChallengeMethod() {
		return client.getCodeChallengeMethod();
	}

	/**
	 * @param codeChallengeMethod
	 * @see org.mitre.oauth2.model.ClientDetailsEntity#setCodeChallengeMethod(org.mitre.oauth2.model.PKCEAlgorithm)
	 */
	public void setCodeChallengeMethod(PKCEAlgorithm codeChallengeMethod) {
		client.setCodeChallengeMethod(codeChallengeMethod);
	}

	/**
	 * @return the src
	 */
	public JsonObject getSource() {
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public void setSource(JsonObject src) {
		this.src = src;
	}



}
