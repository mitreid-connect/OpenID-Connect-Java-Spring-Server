/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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
 ******************************************************************************/

package org.mitre.oauth2.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;
import org.mitre.jose.JWSAlgorithmEmbed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

/**
 * @author jricher
 * 
 */
public interface ClientDetailsEntity extends ClientDetails {

	public enum AuthMethod {
		SECRET_POST("client_secret_post"),
		SECRET_BASIC("client_secret_basic"),
		SECRET_JWT("client_secret_jwt"),
		PRIVATE_KEY("private_key_jwt"),
		NONE("none");

		private final String value;

		// map to aid reverse lookup
		private static final Map<String, AuthMethod> lookup = new HashMap<String, AuthMethod>();
		static {
			for (AuthMethod a : AuthMethod.values()) {
				lookup.put(a.getValue(), a);
			}
		}

		AuthMethod(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static AuthMethod getByValue(String value) {
			return lookup.get(value);
		}
	}

	public enum AppType {
		WEB("web"), NATIVE("native");

		private final String value;

		// map to aid reverse lookup
		private static final Map<String, AppType> lookup = new HashMap<String, AppType>();
		static {
			for (AppType a : AppType.values()) {
				lookup.put(a.getValue(), a);
			}
		}

		AppType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static AppType getByValue(String value) {
			return lookup.get(value);
		}
	}

	public enum SubjectType {
		PAIRWISE("pairwise"), PUBLIC("public");

		private final String value;

		// map to aid reverse lookup
		private static final Map<String, SubjectType> lookup = new HashMap<String, SubjectType>();
		static {
			for (SubjectType u : SubjectType.values()) {
				lookup.put(u.getValue(), u);
			}
		}

		SubjectType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static SubjectType getByValue(String value) {
			return lookup.get(value);
		}
	}

	/**
	 * @return the id
	 */
	Long getId();
	
	/**
	 * 
	 * @param id the id to set
	 */
	void setId(Long id);

	/**
	 * @return the clientDescription
	 */
	String getClientDescription();

	/**
	 * @param clientDescription Human-readable long description of the client (optional)
	 */
	void setClientDescription(String clientDescription);

	/**
	 * @return the allowRefresh
	 */
	boolean isAllowRefresh();

	boolean isReuseRefreshToken();

	void setReuseRefreshToken(boolean reuseRefreshToken);

	/**
	 * Number of seconds ID token is valid for. MUST be a positive integer, can not be null.
	 * 
	 * @return the idTokenValiditySeconds
	 */
	Integer getIdTokenValiditySeconds();

	/**
	 * @param idTokenValiditySeconds the idTokenValiditySeconds to set
	 */
	void setIdTokenValiditySeconds(Integer idTokenValiditySeconds);

	/**
	 * @return the dynamicallyRegistered
	 */
	boolean isDynamicallyRegistered();

	/**
	 * @param dynamicallyRegistered the dynamicallyRegistered to set
	 */
	void setDynamicallyRegistered(boolean dynamicallyRegistered);

	/**
	 * @return the allowIntrospection
	 */
	boolean isAllowIntrospection();

	/**
	 * @param allowIntrospection the allowIntrospection to set
	 */
	void setAllowIntrospection(boolean allowIntrospection);
	
	/**
	 * @param clientId The OAuth2 client_id, must be unique to this client
	 */
	void setClientId(String clientId);

	/**
	 * @param clientSecret the OAuth2 client_secret (optional)
	 */
	void setClientSecret(String clientSecret);
	
	/**
	 * @param scope the set of scopes allowed to be issued to this client
	 */
	void setScope(Set<String> scope);

	/**
	 * @return the authorizedGrantTypes
	 */
	Set<String> getGrantTypes();

	/**
	 * @param authorizedGrantTypes the OAuth2 grant types that this client is allowed to use
	 */
	void setGrantTypes(Set<String> grantTypes);

	/**
	 * @return the authorizedGrantTypes
	 */
	@Override
	Set<GrantedAuthority> getAuthorities();

	/**
	 * @param authorities the Spring Security authorities this client is given
	 */
	void setAuthorities(Set<GrantedAuthority> authorities);

	/**
	 * @param accessTokenTimeout the accessTokenTimeout to set
	 */
	void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds);
	
	/**
	 * @param refreshTokenTimeout Lifetime of refresh tokens, in seconds (optional - leave null for no timeout)
	 */
	void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds);

	/**
	 * @return the registeredRedirectUri
	 */
	Set<String> getRedirectUris();

	/**
	 * @param registeredRedirectUri the registeredRedirectUri to set
	 */
	void setRedirectUris(Set<String> redirectUris);
	
	/**
	 * @param resourceIds the resourceIds to set
	 */
	void setResourceIds(Set<String> resourceIds);
	
	AppType getApplicationType();

	void setApplicationType(AppType applicationType);

	String getClientName();

	void setClientName(String clientName);

	AuthMethod getTokenEndpointAuthMethod();

	void setTokenEndpointAuthMethod(AuthMethod tokenEndpointAuthMethod);

	SubjectType getSubjectType();

	void setSubjectType(SubjectType subjectType);

	Set<String> getContacts();

	void setContacts(Set<String> contacts);

	String getLogoUri();

	void setLogoUri(String logoUri);

	String getPolicyUri();
	
	void setPolicyUri(String policyUri);
	
	/**
	 * @return the clientUrl
	 */
	String getClientUri();
	
	/**
	 * @param clientUrl the clientUrl to set
	 */
	void setClientUri(String clientUri);
	
	/**
	 * @return the tosUrl
	 */
	String getTosUri();
	
	/**
	 * @param tosUrl the tosUrl to set
	 */
	void setTosUri(String tosUri);
	
	String getJwksUri();
	
	void setJwksUri(String jwksUri);
	
	String getSectorIdentifierUri();
	
	void setSectorIdentifierUri(String sectorIdentifierUri);
	
	JWSAlgorithmEmbed getRequestObjectSigningAlgEmbed();
	
	void setRequestObjectSigningAlgEmbed(JWSAlgorithmEmbed requestObjectSigningAlg);
	
	JWSAlgorithmEmbed getUserInfoSignedResponseAlgEmbed();
	
	void setUserInfoSignedResponseAlgEmbed(JWSAlgorithmEmbed userInfoSignedResponseAlg);
	
	JWEAlgorithmEmbed getUserInfoEncryptedResponseAlgEmbed();

	void setUserInfoEncryptedResponseAlgEmbed(JWEAlgorithmEmbed userInfoEncryptedResponseAlg);
	
	JWEEncryptionMethodEmbed getUserInfoEncryptedResponseEncEmbed();
	
	void setUserInfoEncryptedResponseEncEmbed(JWEEncryptionMethodEmbed userInfoEncryptedResponseEnc);
	
	JWSAlgorithmEmbed getIdTokenSignedResponseAlgEmbed();
	
	void setIdTokenSignedResponseAlgEmbed(JWSAlgorithmEmbed idTokenSignedResponseAlg);
	
	JWEAlgorithmEmbed getIdTokenEncryptedResponseAlgEmbed();
	
	void setIdTokenEncryptedResponseAlgEmbed(JWEAlgorithmEmbed idTokenEncryptedResponseAlg);
	
	JWEEncryptionMethodEmbed getIdTokenEncryptedResponseEncEmbed();
	
	void setIdTokenEncryptedResponseEncEmbed(JWEEncryptionMethodEmbed idTokenEncryptedResponseEnc);
	
	JWSAlgorithmEmbed getTokenEndpointAuthSigningAlgEmbed();
	
	void setTokenEndpointAuthSigningAlgEmbed(JWSAlgorithmEmbed tokenEndpointAuthSigningAlgEmbed);
	
	JWSAlgorithm getRequestObjectSigningAlg();
	
	void setRequestObjectSigningAlg(JWSAlgorithm requestObjectSigningAlg);
	
	JWSAlgorithm getUserInfoSignedResponseAlg();
	
	void setUserInfoSignedResponseAlg(JWSAlgorithm userInfoSignedResponseAlg);
	
	JWEAlgorithm getUserInfoEncryptedResponseAlg();
	
	void setUserInfoEncryptedResponseAlg(JWEAlgorithm userInfoEncryptedResponseAlg);
	
	EncryptionMethod getUserInfoEncryptedResponseEnc();

	void setUserInfoEncryptedResponseEnc(EncryptionMethod userInfoEncryptedResponseEnc);
	
	JWSAlgorithm getIdTokenSignedResponseAlg();
	
	void setIdTokenSignedResponseAlg(JWSAlgorithm idTokenSignedResponseAlg);
	
	JWEAlgorithm getIdTokenEncryptedResponseAlg();
	
	void setIdTokenEncryptedResponseAlg(JWEAlgorithm idTokenEncryptedResponseAlg);
	
	EncryptionMethod getIdTokenEncryptedResponseEnc();
	
	void setIdTokenEncryptedResponseEnc(EncryptionMethod idTokenEncryptedResponseEnc);
	
	JWSAlgorithm getTokenEndpointAuthSigningAlg();
	
	void setTokenEndpointAuthSigningAlg(JWSAlgorithm tokenEndpointAuthSigningAlg);
	
	Integer getDefaultMaxAge();
	
	void setDefaultMaxAge(Integer defaultMaxAge);
	
	Boolean getRequireAuthTime();
	
	void setRequireAuthTime(Boolean requireAuthTime);
	
	/**
	 * @return the responseTypes
	 */
	Set<String> getResponseTypes();
	
	/**
	 * @param responseTypes the responseTypes to set
	 */
	void setResponseTypes(Set<String> responseTypes);
	
	/**
	 * @return the defaultACRvalues
	 */
	Set<String> getDefaultACRvalues();
	
	/**
	 * @param defaultACRvalues the defaultACRvalues to set
	 */
	void setDefaultACRvalues(Set<String> defaultACRvalues);
	
	/**
	 * @return the initiateLoginUri
	 */
	String getInitiateLoginUri();
	
	/**
	 * @param initiateLoginUri the initiateLoginUri to set
	 */
	void setInitiateLoginUri(String initiateLoginUri);
	
	/**
	 * @return the postLogoutRedirectUri
	 */
	String getPostLogoutRedirectUri();
	
	/**
	 * @param postLogoutRedirectUri the postLogoutRedirectUri to set
	 */
	void setPostLogoutRedirectUri(String postLogoutRedirectUri);
	
	/**
	 * @return the requestUris
	 */
	Set<String> getRequestUris();
	
	/**
	 * @param requestUris the requestUris to set
	 */
	void setRequestUris(Set<String> requestUris);
	
	/**
	 * @return the createdAt
	 */
	Date getCreatedAt();
	
	/**
	 * @param createdAt the createdAt to set
	 */
	void setCreatedAt(Date createdAt);
	
}
