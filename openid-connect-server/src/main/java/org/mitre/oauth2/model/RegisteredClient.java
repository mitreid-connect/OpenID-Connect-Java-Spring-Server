/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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

import com.google.gson.JsonObject;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.springframework.security.core.GrantedAuthority;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author jricher
 */
public class RegisteredClient {

	private String registrationAccessToken;
	private String registrationClientUri;
	private Date clientSecretExpiresAt;
	private Date clientIdIssuedAt;
	private ClientDetailsEntity client;
	private JsonObject src;

	public RegisteredClient() {
		this.client = new ClientDetailsEntity();
	}

	public RegisteredClient(ClientDetailsEntity client) {
		this.client = client;
	}

	public RegisteredClient(ClientDetailsEntity client, String registrationAccessToken, String registrationClientUri) {
		this.client = client;
		this.registrationAccessToken = registrationAccessToken;
		this.registrationClientUri = registrationClientUri;
	}

	public ClientDetailsEntity getClient() {
		return client;
	}

	public void setClient(ClientDetailsEntity client) {
		this.client = client;
	}

	public String getClientDescription() {
		return client.getClientDescription();
	}

	public void setClientDescription(String clientDescription) {
		client.setClientDescription(clientDescription);
	}

	public boolean isAllowRefresh() {
		return client.isAllowRefresh();
	}

	public boolean isReuseRefreshToken() {
		return client.isReuseRefreshToken();
	}

	public void setReuseRefreshToken(boolean reuseRefreshToken) {
		client.setReuseRefreshToken(reuseRefreshToken);
	}

	public Integer getIdTokenValiditySeconds() {
		return client.getIdTokenValiditySeconds();
	}

	public void setIdTokenValiditySeconds(Integer idTokenValiditySeconds) {
		client.setIdTokenValiditySeconds(idTokenValiditySeconds);
	}

	public boolean isDynamicallyRegistered() {
		return client.isDynamicallyRegistered();
	}

	public void setDynamicallyRegistered(boolean dynamicallyRegistered) {
		client.setDynamicallyRegistered(dynamicallyRegistered);
	}

	public boolean isAllowIntrospection() {
		return client.isAllowIntrospection();
	}

	public void setAllowIntrospection(boolean allowIntrospection) {
		client.setAllowIntrospection(allowIntrospection);
	}

	public boolean isSecretRequired() {
		return client.isSecretRequired();
	}

	public boolean isScoped() {
		return client.isScoped();
	}

	public String getClientId() {
		return client.getClientId();
	}

	public void setClientId(String clientId) {
		client.setClientId(clientId);
	}

	public String getClientSecret() {
		return client.getClientSecret();
	}

	public void setClientSecret(String clientSecret) {
		client.setClientSecret(clientSecret);
	}

	public Set<String> getScope() {
		return client.getScope();
	}

	public void setScope(Set<String> scope) {
		client.setScope(scope);
	}

	public Set<String> getGrantTypes() {
		return client.getGrantTypes();
	}

	public void setGrantTypes(Set<String> grantTypes) {
		client.setGrantTypes(grantTypes);
	}

	public Set<String> getAuthorizedGrantTypes() {
		return client.getAuthorizedGrantTypes();
	}

	public Set<GrantedAuthority> getAuthorities() {
		return client.getAuthorities();
	}

	public void setAuthorities(Set<GrantedAuthority> authorities) {
		client.setAuthorities(authorities);
	}

	public Integer getAccessTokenValiditySeconds() {
		return client.getAccessTokenValiditySeconds();
	}

	public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
		client.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
	}

	public Integer getRefreshTokenValiditySeconds() {
		return client.getRefreshTokenValiditySeconds();
	}

	public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
		client.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
	}

	public Set<String> getRedirectUris() {
		return client.getRedirectUris();
	}

	public void setRedirectUris(Set<String> redirectUris) {
		client.setRedirectUris(redirectUris);
	}

	public Set<String> getRegisteredRedirectUri() {
		return client.getRegisteredRedirectUri();
	}

	public Set<String> getResourceIds() {
		return client.getResourceIds();
	}

	public void setResourceIds(Set<String> resourceIds) {
		client.setResourceIds(resourceIds);
	}

	public Map<String, Object> getAdditionalInformation() {
		return client.getAdditionalInformation();
	}

	public AppType getApplicationType() {
		return client.getApplicationType();
	}

	public void setApplicationType(AppType applicationType) {
		client.setApplicationType(applicationType);
	}

	public String getClientName() {
		return client.getClientName();
	}

	public void setClientName(String clientName) {
		client.setClientName(clientName);
	}

	public AuthMethod getTokenEndpointAuthMethod() {
		return client.getTokenEndpointAuthMethod();
	}

	public void setTokenEndpointAuthMethod(AuthMethod tokenEndpointAuthMethod) {
		client.setTokenEndpointAuthMethod(tokenEndpointAuthMethod);
	}

	public SubjectType getSubjectType() {
		return client.getSubjectType();
	}

	public void setSubjectType(SubjectType subjectType) {
		client.setSubjectType(subjectType);
	}

	public Set<String> getContacts() {
		return client.getContacts();
	}

	public void setContacts(Set<String> contacts) {
		client.setContacts(contacts);
	}

	public String getPolicyUri() {
		return client.getPolicyUri();
	}

	public void setPolicyUri(String policyUri) {
		client.setPolicyUri(policyUri);
	}

	public String getClientUri() {
		return client.getClientUri();
	}

	public void setClientUri(String clientUri) {
		client.setClientUri(clientUri);
	}

	public String getTosUri() {
		return client.getTosUri();
	}

	public void setTosUri(String tosUri) {
		client.setTosUri(tosUri);
	}

	public String getJwksUri() {
		return client.getJwksUri();
	}

	public void setJwksUri(String jwksUri) {
		client.setJwksUri(jwksUri);
	}

	public JWKSet getJwks() {
		return client.getJwks();
	}

	public void setJwks(JWKSet jwks) {
		client.setJwks(jwks);
	}

	public String getSectorIdentifierUri() {
		return client.getSectorIdentifierUri();
	}

	public void setSectorIdentifierUri(String sectorIdentifierUri) {
		client.setSectorIdentifierUri(sectorIdentifierUri);
	}

	public Integer getDefaultMaxAge() {
		return client.getDefaultMaxAge();
	}

	public void setDefaultMaxAge(Integer defaultMaxAge) {
		client.setDefaultMaxAge(defaultMaxAge);
	}

	public Boolean getRequireAuthTime() {
		return client.getRequireAuthTime();
	}

	public void setRequireAuthTime(Boolean requireAuthTime) {
		client.setRequireAuthTime(requireAuthTime);
	}

	public Set<String> getResponseTypes() {
		return client.getResponseTypes();
	}

	public void setResponseTypes(Set<String> responseTypes) {
		client.setResponseTypes(responseTypes);
	}

	public Set<String> getDefaultACRvalues() {
		return client.getDefaultACRvalues();
	}

	public void setDefaultACRvalues(Set<String> defaultACRvalues) {
		client.setDefaultACRvalues(defaultACRvalues);
	}

	public String getInitiateLoginUri() {
		return client.getInitiateLoginUri();
	}

	public void setInitiateLoginUri(String initiateLoginUri) {
		client.setInitiateLoginUri(initiateLoginUri);
	}

	public Set<String> getPostLogoutRedirectUris() {
		return client.getPostLogoutRedirectUris();
	}

	public void setPostLogoutRedirectUris(Set<String> postLogoutRedirectUri) {
		client.setPostLogoutRedirectUris(postLogoutRedirectUri);
	}

	public Set<String> getRequestUris() {
		return client.getRequestUris();
	}

	public void setRequestUris(Set<String> requestUris) {
		client.setRequestUris(requestUris);
	}

	public JWSAlgorithm getRequestObjectSigningAlg() {
		return client.getRequestObjectSigningAlg();
	}

	public void setRequestObjectSigningAlg(JWSAlgorithm requestObjectSigningAlg) {
		client.setRequestObjectSigningAlg(requestObjectSigningAlg);
	}

	public JWSAlgorithm getUserInfoSignedResponseAlg() {
		return client.getUserInfoSignedResponseAlg();
	}

	public void setUserInfoSignedResponseAlg(JWSAlgorithm userInfoSignedResponseAlg) {
		client.setUserInfoSignedResponseAlg(userInfoSignedResponseAlg);
	}

	public JWEAlgorithm getUserInfoEncryptedResponseAlg() {
		return client.getUserInfoEncryptedResponseAlg();
	}

	public void setUserInfoEncryptedResponseAlg(JWEAlgorithm userInfoEncryptedResponseAlg) {
		client.setUserInfoEncryptedResponseAlg(userInfoEncryptedResponseAlg);
	}

	public EncryptionMethod getUserInfoEncryptedResponseEnc() {
		return client.getUserInfoEncryptedResponseEnc();
	}

	public void setUserInfoEncryptedResponseEnc(EncryptionMethod userInfoEncryptedResponseEnc) {
		client.setUserInfoEncryptedResponseEnc(userInfoEncryptedResponseEnc);
	}

	public JWSAlgorithm getIdTokenSignedResponseAlg() {
		return client.getIdTokenSignedResponseAlg();
	}

	public void setIdTokenSignedResponseAlg(JWSAlgorithm idTokenSignedResponseAlg) {
		client.setIdTokenSignedResponseAlg(idTokenSignedResponseAlg);
	}

	public JWEAlgorithm getIdTokenEncryptedResponseAlg() {
		return client.getIdTokenEncryptedResponseAlg();
	}

	public void setIdTokenEncryptedResponseAlg(JWEAlgorithm idTokenEncryptedResponseAlg) {
		client.setIdTokenEncryptedResponseAlg(idTokenEncryptedResponseAlg);
	}

	public EncryptionMethod getIdTokenEncryptedResponseEnc() {
		return client.getIdTokenEncryptedResponseEnc();
	}

	public void setIdTokenEncryptedResponseEnc(EncryptionMethod idTokenEncryptedResponseEnc) {
		client.setIdTokenEncryptedResponseEnc(idTokenEncryptedResponseEnc);
	}

	public JWSAlgorithm getTokenEndpointAuthSigningAlg() {
		return client.getTokenEndpointAuthSigningAlg();
	}

	public void setTokenEndpointAuthSigningAlg(JWSAlgorithm tokenEndpointAuthSigningAlg) {
		client.setTokenEndpointAuthSigningAlg(tokenEndpointAuthSigningAlg);
	}

	public Date getCreatedAt() {
		return client.getCreatedAt();
	}

	public void setCreatedAt(Date createdAt) {
		client.setCreatedAt(createdAt);
	}

	public String getRegistrationAccessToken() {
		return registrationAccessToken;
	}

	public void setRegistrationAccessToken(String registrationAccessToken) {
		this.registrationAccessToken = registrationAccessToken;
	}

	public String getRegistrationClientUri() {
		return registrationClientUri;
	}

	public void setRegistrationClientUri(String registrationClientUri) {
		this.registrationClientUri = registrationClientUri;
	}

	public Date getClientSecretExpiresAt() {
		return clientSecretExpiresAt;
	}

	public void setClientSecretExpiresAt(Date expiresAt) {
		this.clientSecretExpiresAt = expiresAt;
	}

	public Date getClientIdIssuedAt() {
		return clientIdIssuedAt;
	}

	public void setClientIdIssuedAt(Date issuedAt) {
		this.clientIdIssuedAt = issuedAt;
	}

	public Set<String> getClaimsRedirectUris() {
		return client.getClaimsRedirectUris();
	}

	public void setClaimsRedirectUris(Set<String> claimsRedirectUris) {
		client.setClaimsRedirectUris(claimsRedirectUris);
	}

	public JWT getSoftwareStatement() {
		return client.getSoftwareStatement();
	}

	public void setSoftwareStatement(JWT softwareStatement) {
		client.setSoftwareStatement(softwareStatement);
	}

	public PKCEAlgorithm getCodeChallengeMethod() {
		return client.getCodeChallengeMethod();
	}

	public void setCodeChallengeMethod(PKCEAlgorithm codeChallengeMethod) {
		client.setCodeChallengeMethod(codeChallengeMethod);
	}

	public JsonObject getSource() {
		return src;
	}

	public void setSource(JsonObject src) {
		this.src = src;
	}

	public Integer getDeviceCodeValiditySeconds() {
		return client.getDeviceCodeValiditySeconds();
	}

	public void setDeviceCodeValiditySeconds(Integer deviceCodeValiditySeconds) {
		client.setDeviceCodeValiditySeconds(deviceCodeValiditySeconds);
	}

	public String getSoftwareId() {
		return client.getSoftwareId();
	}

	public void setSoftwareId(String softwareId) {
		client.setSoftwareId(softwareId);
	}

	public String getSoftwareVersion() {
		return client.getSoftwareVersion();
	}

	public void setSoftwareVersion(String softwareVersion) {
		client.setSoftwareVersion(softwareVersion);
	}

}
