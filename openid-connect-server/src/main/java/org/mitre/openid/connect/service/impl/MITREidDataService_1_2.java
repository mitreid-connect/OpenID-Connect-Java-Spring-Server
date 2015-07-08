/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/
package org.mitre.openid.connect.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.SavedUserAuthentication;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.BlacklistedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.repository.BlacklistedSiteRepository;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.mitre.openid.connect.service.MITREidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTParser;

import static org.mitre.util.JsonUtils.readMap;
import static org.mitre.util.JsonUtils.readSet;
import static org.mitre.util.JsonUtils.writeNullSafeArray;

/**
 *
 * Data service to import and export MITREid 1.2 configuration.
 *
 * @author jricher
 * @author arielak
 */
@Service
@SuppressWarnings(value = {"unchecked"})
public class MITREidDataService_1_2 extends MITREidDataServiceSupport implements MITREidDataService {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(MITREidDataService_1_2.class);
	@Autowired
	private OAuth2ClientRepository clientRepository;
	@Autowired
	private ApprovedSiteRepository approvedSiteRepository;
	@Autowired
	private WhitelistedSiteRepository wlSiteRepository;
	@Autowired
	private BlacklistedSiteRepository blSiteRepository;
	@Autowired
	private AuthenticationHolderRepository authHolderRepository;
	@Autowired
	private OAuth2TokenRepository tokenRepository;
	@Autowired
	private SystemScopeRepository sysScopeRepository;
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataService#export(com.google.gson.stream.JsonWriter)
	 */
	@Override
	public void exportData(JsonWriter writer) throws IOException {

		// version tag at the root
		writer.name(MITREID_CONNECT_1_2);

		writer.beginObject();

		// clients list
		writer.name(CLIENTS);
		writer.beginArray();
		writeClients(writer);
		writer.endArray();

		writer.name(GRANTS);
		writer.beginArray();
		writeGrants(writer);
		writer.endArray();

		writer.name(WHITELISTEDSITES);
		writer.beginArray();
		writeWhitelistedSites(writer);
		writer.endArray();

		writer.name(BLACKLISTEDSITES);
		writer.beginArray();
		writeBlacklistedSites(writer);
		writer.endArray();

		writer.name(AUTHENTICATIONHOLDERS);
		writer.beginArray();
		writeAuthenticationHolders(writer);
		writer.endArray();

		writer.name(ACCESSTOKENS);
		writer.beginArray();
		writeAccessTokens(writer);
		writer.endArray();

		writer.name(REFRESHTOKENS);
		writer.beginArray();
		writeRefreshTokens(writer);
		writer.endArray();

		writer.name(SYSTEMSCOPES);
		writer.beginArray();
		writeSystemScopes(writer);
		writer.endArray();

		writer.endObject(); // end mitreid-connect-1.1
	}

	/**
	 * @param writer
	 */
	private void writeRefreshTokens(JsonWriter writer) throws IOException {
		for (OAuth2RefreshTokenEntity token : tokenRepository.getAllRefreshTokens()) {
			writer.beginObject();
			writer.name("id").value(token.getId());
			writer.name("expiration").value(toUTCString(token.getExpiration()));
			writer.name("clientId")
			.value((token.getClient() != null) ? token.getClient().getClientId() : null);
			writer.name("authenticationHolderId")
			.value((token.getAuthenticationHolder() != null) ? token.getAuthenticationHolder().getId() : null);
			writer.name("value").value(token.getValue());
			writer.endObject();
			logger.debug("Wrote refresh token {}", token.getId());
		}
		logger.info("Done writing refresh tokens");
	}

	/**
	 * @param writer
	 */
	private void writeAccessTokens(JsonWriter writer) throws IOException {
		for (OAuth2AccessTokenEntity token : tokenRepository.getAllAccessTokens()) {
			writer.beginObject();
			writer.name("id").value(token.getId());
			writer.name("expiration").value(toUTCString(token.getExpiration()));
			writer.name("clientId")
			.value((token.getClient() != null) ? token.getClient().getClientId() : null);
			writer.name("authenticationHolderId")
			.value((token.getAuthenticationHolder() != null) ? token.getAuthenticationHolder().getId() : null);
			writer.name("refreshTokenId")
			.value((token.getRefreshToken() != null) ? token.getRefreshToken().getId() : null);
			writer.name("idTokenId")
			.value((token.getIdToken() != null) ? token.getIdToken().getId() : null);
			writer.name("scope");
			writer.beginArray();
			for (String s : token.getScope()) {
				writer.value(s);
			}
			writer.endArray();
			writer.name("type").value(token.getTokenType());
			writer.name("value").value(token.getValue());
			writer.endObject();
			logger.debug("Wrote access token {}", token.getId());
		}
		logger.info("Done writing access tokens");
	}

	/**
	 * @param writer
	 */
	private void writeAuthenticationHolders(JsonWriter writer) throws IOException {
		for (AuthenticationHolderEntity holder : authHolderRepository.getAll()) {
			writer.beginObject();
			writer.name("id").value(holder.getId());
			
			writer.name("requestParameters");
			writer.beginObject();
			for (Entry<String, String> entry : holder.getRequestParameters().entrySet()) {
				writer.name(entry.getKey()).value(entry.getValue());
			}
			writer.endObject();
			writer.name("clientId").value(holder.getClientId());
			Set<String> scope = holder.getScope();
			writer.name("scope");
			writer.beginArray();
			for (String s : scope) {
				writer.value(s);
			}
			writer.endArray();
			writer.name("resourceIds");
			writer.beginArray();
			if (holder.getResourceIds() != null) {
				for (String s : holder.getResourceIds()) {
					writer.value(s);
				}
			}
			writer.endArray();
			writer.name("authorities");
			writer.beginArray();
			for (GrantedAuthority authority : holder.getAuthorities()) {
				writer.value(authority.getAuthority());
			}
			writer.endArray();
			writer.name("approved").value(holder.isApproved());
			writer.name("redirectUri").value(holder.getRedirectUri());
			writer.name("responseTypes");
			writer.beginArray();
			for (String s : holder.getResponseTypes()) {
				writer.value(s);
			}
			writer.endArray();
			writer.name("extensions");
			writer.beginObject();
			for (Entry<String, Serializable> entry : holder.getExtensions().entrySet()) {
				// while the extension map itself is Serializable, we enforce storage of Strings
				if (entry.getValue() instanceof String) {
					writer.name(entry.getKey()).value((String) entry.getValue());
				} else {
					logger.warn("Skipping non-string extension: " + entry);
				}
			}
			writer.endObject();

			writer.name("savedUserAuthentication");
			if (holder.getUserAuth() != null) {
				writer.beginObject();
				writer.name("name").value(holder.getUserAuth().getName());
				writer.name("sourceClass").value(holder.getUserAuth().getSourceClass());
				writer.name("authenticated").value(holder.getUserAuth().isAuthenticated());
				writer.name("authorities");
				writer.beginArray();
				for (GrantedAuthority authority : holder.getUserAuth().getAuthorities()) {
					writer.value(authority.getAuthority());
				}
				writer.endArray();
				
				writer.endObject();
			} else {
				writer.nullValue();
			}
			
			
			writer.endObject();
			logger.debug("Wrote authentication holder {}", holder.getId());
		}
		logger.info("Done writing authentication holders");
	}

	/**
	 * @param writer
	 */
	private void writeGrants(JsonWriter writer) throws IOException {
		for (ApprovedSite site : approvedSiteRepository.getAll()) {
			writer.beginObject();
			writer.name("id").value(site.getId());
			writer.name("accessDate").value(toUTCString(site.getAccessDate()));
			writer.name("clientId").value(site.getClientId());
			writer.name("creationDate").value(toUTCString(site.getCreationDate()));
			writer.name("timeoutDate").value(toUTCString(site.getTimeoutDate()));
			writer.name("userId").value(site.getUserId());
			writer.name("allowedScopes");
			writeNullSafeArray(writer, site.getAllowedScopes());
			Set<OAuth2AccessTokenEntity> tokens = site.getApprovedAccessTokens();
			writer.name("approvedAccessTokens");
			writer.beginArray();
			for (OAuth2AccessTokenEntity token : tokens) {
				writer.value(token.getId());
			}
			writer.endArray();
			writer.endObject();
			logger.debug("Wrote grant {}", site.getId());
		}
		logger.info("Done writing grants");
	}

	/**
	 * @param writer
	 */
	private void writeWhitelistedSites(JsonWriter writer) throws IOException {
		for (WhitelistedSite wlSite : wlSiteRepository.getAll()) {
			writer.beginObject();
			writer.name("id").value(wlSite.getId());
			writer.name("clientId").value(wlSite.getClientId());
			writer.name("creatorUserId").value(wlSite.getCreatorUserId());
			writer.name("allowedScopes");
			writeNullSafeArray(writer, wlSite.getAllowedScopes());
			writer.endObject();
			logger.debug("Wrote whitelisted site {}", wlSite.getId());
		}
		logger.info("Done writing whitelisted sites");
	}

	/**
	 * @param writer
	 */
	private void writeBlacklistedSites(JsonWriter writer) throws IOException {
		for (BlacklistedSite blSite : blSiteRepository.getAll()) {
			writer.beginObject();
			writer.name("id").value(blSite.getId());
			writer.name("uri").value(blSite.getUri());
			writer.endObject();
			logger.debug("Wrote blacklisted site {}", blSite.getId());
		}
		logger.info("Done writing blacklisted sites");
	}

	/**
	 * @param writer
	 */
	private void writeClients(JsonWriter writer) {
		for (ClientDetailsEntity client : clientRepository.getAllClients()) {
			try {
				writer.beginObject();
				writer.name("clientId").value(client.getClientId());
				writer.name("resourceIds");
				writeNullSafeArray(writer, client.getResourceIds());

				writer.name("secret").value(client.getClientSecret());

				writer.name("scope");
				writeNullSafeArray(writer, client.getScope());

				writer.name("authorities");
				writer.beginArray();
				for (GrantedAuthority authority : client.getAuthorities()) {
					writer.value(authority.getAuthority());
				}
				writer.endArray();
				writer.name("accessTokenValiditySeconds").value(client.getAccessTokenValiditySeconds());
				writer.name("refreshTokenValiditySeconds").value(client.getRefreshTokenValiditySeconds());
				writer.name("redirectUris");
				writeNullSafeArray(writer, client.getRedirectUris());
				writer.name("name").value(client.getClientName());
				writer.name("uri").value(client.getClientUri());
				writer.name("logoUri").value(client.getLogoUri());
				writer.name("contacts");
				writeNullSafeArray(writer, client.getContacts());
				writer.name("tosUri").value(client.getTosUri());
				writer.name("tokenEndpointAuthMethod")
				.value((client.getTokenEndpointAuthMethod() != null) ? client.getTokenEndpointAuthMethod().getValue() : null);
				writer.name("grantTypes");
				writer.beginArray();
				for (String s : client.getGrantTypes()) {
					writer.value(s);
				}
				writer.endArray();
				writer.name("responseTypes");
				writer.beginArray();
				for (String s : client.getResponseTypes()) {
					writer.value(s);
				}
				writer.endArray();
				writer.name("policyUri").value(client.getPolicyUri());
				writer.name("jwksUri").value(client.getJwksUri());
				writer.name("jwks").value((client.getJwks() != null) ? client.getJwks().toString() : null);
				writer.name("applicationType")
				.value((client.getApplicationType() != null) ? client.getApplicationType().getValue() : null);
				writer.name("sectorIdentifierUri").value(client.getSectorIdentifierUri());
				writer.name("subjectType")
				.value((client.getSubjectType() != null) ? client.getSubjectType().getValue() : null);
				writer.name("requestObjectSigningAlg")
				.value((client.getRequestObjectSigningAlg() != null) ? client.getRequestObjectSigningAlg().getName() : null);
				writer.name("idTokenSignedResponseAlg")
				.value((client.getIdTokenSignedResponseAlg() != null) ? client.getIdTokenSignedResponseAlg().getName() : null);
				writer.name("idTokenEncryptedResponseAlg")
				.value((client.getIdTokenEncryptedResponseAlg() != null) ? client.getIdTokenEncryptedResponseAlg().getName() : null);
				writer.name("idTokenEncryptedResponseEnc")
				.value((client.getIdTokenEncryptedResponseEnc() != null) ? client.getIdTokenEncryptedResponseEnc().getName() : null);
				writer.name("userInfoSignedResponseAlg")
				.value((client.getUserInfoSignedResponseAlg() != null) ? client.getUserInfoSignedResponseAlg().getName() : null);
				writer.name("userInfoEncryptedResponseAlg")
				.value((client.getUserInfoEncryptedResponseAlg() != null) ? client.getUserInfoEncryptedResponseAlg().getName() : null);
				writer.name("userInfoEncryptedResponseEnc")
				.value((client.getUserInfoEncryptedResponseEnc() != null) ? client.getUserInfoEncryptedResponseEnc().getName() : null);
				writer.name("tokenEndpointAuthSigningAlg")
				.value((client.getTokenEndpointAuthSigningAlg() != null) ? client.getTokenEndpointAuthSigningAlg().getName() : null);
				writer.name("defaultMaxAge").value(client.getDefaultMaxAge());
				Boolean requireAuthTime = null;
				try {
					requireAuthTime = client.getRequireAuthTime();
				} catch (NullPointerException e) {
				}
				if (requireAuthTime != null) {
					writer.name("requireAuthTime").value(requireAuthTime);
				}
				writer.name("defaultACRValues");
				writeNullSafeArray(writer, client.getDefaultACRvalues());
				writer.name("intitateLoginUri").value(client.getInitiateLoginUri());
				writer.name("postLogoutRedirectUri");
				writeNullSafeArray(writer, client.getPostLogoutRedirectUris());
				writer.name("requestUris");
				writeNullSafeArray(writer, client.getRequestUris());
				writer.name("description").value(client.getClientDescription());
				writer.name("allowIntrospection").value(client.isAllowIntrospection());
				writer.name("reuseRefreshToken").value(client.isReuseRefreshToken());
				writer.name("clearAccessTokensOnRefresh").value(client.isClearAccessTokensOnRefresh());
				writer.name("dynamicallyRegistered").value(client.isDynamicallyRegistered());
				writer.endObject();
				logger.debug("Wrote client {}", client.getId());
			} catch (IOException ex) {
				logger.error("Unable to write client {}", client.getId(), ex);
			}
		}
		logger.info("Done writing clients");
	}

	/**
	 * @param writer
	 */
	private void writeSystemScopes(JsonWriter writer) {
		for (SystemScope sysScope : sysScopeRepository.getAll()) {
			try {
				writer.beginObject();
				writer.name("id").value(sysScope.getId());
				writer.name("description").value(sysScope.getDescription());
				writer.name("icon").value(sysScope.getIcon());
				writer.name("value").value(sysScope.getValue());
				writer.name("restricted").value(sysScope.isRestricted());
				writer.name("structured").value(sysScope.isStructured());
				writer.name("structuredParameter").value(sysScope.getStructuredParamDescription());
				writer.name("defaultScope").value(sysScope.isDefaultScope());
				writer.endObject();
				logger.debug("Wrote system scope {}", sysScope.getId());
			} catch (IOException ex) {
				logger.error("Unable to write system scope {}", sysScope.getId(), ex);
			}
		}
		logger.info("Done writing system scopes");
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataService#importData(com.google.gson.stream.JsonReader)
	 */
	@Override
	public void importData(JsonReader reader) throws IOException {

		logger.info("Reading configuration for 1.2");

		// this *HAS* to start as an object
		reader.beginObject();

		while (reader.hasNext()) {
			JsonToken tok = reader.peek();
			switch (tok) {
			case NAME:
				String name = reader.nextName();
				// find out which member it is
				if (name.equals(CLIENTS)) {
					readClients(reader);
				} else if (name.equals(GRANTS)) {
					readGrants(reader);
				} else if (name.equals(WHITELISTEDSITES)) {
					readWhitelistedSites(reader);
				} else if (name.equals(BLACKLISTEDSITES)) {
					readBlacklistedSites(reader);
				} else if (name.equals(AUTHENTICATIONHOLDERS)) {
					readAuthenticationHolders(reader);
				} else if (name.equals(ACCESSTOKENS)) {
					readAccessTokens(reader);
				} else if (name.equals(REFRESHTOKENS)) {
					readRefreshTokens(reader);
				} else if (name.equals(SYSTEMSCOPES)) {
					readSystemScopes(reader);
				} else {
					// unknown token, skip it
					reader.skipValue();
				}
				break;
			case END_OBJECT:
				// the object ended, we're done here
				reader.endObject();
				continue;
			default:
				logger.debug("Found unexpected entry");
				reader.skipValue();
				continue;
			}
		}
		fixObjectReferences();
	}
	private Map<Long, String> refreshTokenToClientRefs = new HashMap<Long, String>();
	private Map<Long, Long> refreshTokenToAuthHolderRefs = new HashMap<Long, Long>();
	private Map<Long, Long> refreshTokenOldToNewIdMap = new HashMap<Long, Long>();

	/**
	 * @param reader
	 * @throws IOException
	 */
	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readRefreshTokens(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			OAuth2RefreshTokenEntity token = new OAuth2RefreshTokenEntity();
			reader.beginObject();
			Long currentId = null;
			String clientId = null;
			Long authHolderId = null;
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals("id")) {
						currentId = reader.nextLong();
					} else if (name.equals("expiration")) {
						Date date = utcToDate(reader.nextString());
						token.setExpiration(date);
					} else if (name.equals("value")) {
						String value = reader.nextString();
						try {
							token.setJwt(JWTParser.parse(value));
						} catch (ParseException ex) {
							logger.error("Unable to set refresh token value to {}", value, ex);
						}
					} else if (name.equals("clientId")) {
						clientId = reader.nextString();
					} else if (name.equals("authenticationHolderId")) {
						authHolderId = reader.nextLong();
					} else {
						logger.debug("Found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			Long newId = tokenRepository.saveRefreshToken(token).getId();
			refreshTokenToClientRefs.put(currentId, clientId);
			refreshTokenToAuthHolderRefs.put(currentId, authHolderId);
			refreshTokenOldToNewIdMap.put(currentId, newId);
			logger.debug("Read refresh token {}", currentId);
		}
		reader.endArray();
		logger.info("Done reading refresh tokens");
	}
	private Map<Long, String> accessTokenToClientRefs = new HashMap<Long, String>();
	private Map<Long, Long> accessTokenToAuthHolderRefs = new HashMap<Long, Long>();
	private Map<Long, Long> accessTokenToRefreshTokenRefs = new HashMap<Long, Long>();
	private Map<Long, Long> accessTokenToIdTokenRefs = new HashMap<Long, Long>();
	private Map<Long, Long> accessTokenOldToNewIdMap = new HashMap<Long, Long>();

	/**
	 * @param reader
	 * @throws IOException
	 */
	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readAccessTokens(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
			reader.beginObject();
			Long currentId = null;
			String clientId = null;
			Long authHolderId = null;
			Long refreshTokenId = null;
			Long idTokenId = null;
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals("id")) {
						currentId = reader.nextLong();
					} else if (name.equals("expiration")) {
						Date date = utcToDate(reader.nextString());
						token.setExpiration(date);
					} else if (name.equals("value")) {
						String value = reader.nextString();
						try {
							// all tokens are JWTs
							token.setJwt(JWTParser.parse(value));
						} catch (ParseException ex) {
							logger.error("Unable to set refresh token value to {}", value, ex);
						}
					} else if (name.equals("clientId")) {
						clientId = reader.nextString();
					} else if (name.equals("authenticationHolderId")) {
						authHolderId = reader.nextLong();
					} else if (name.equals("refreshTokenId")) {
						refreshTokenId = reader.nextLong();
					} else if (name.equals("idTokenId")) {
						idTokenId = reader.nextLong();
					} else if (name.equals("scope")) {
						Set<String> scope = readSet(reader);
						token.setScope(scope);
					} else if (name.equals("type")) {
						token.setTokenType(reader.nextString());
					} else {
						logger.debug("Found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			Long newId = tokenRepository.saveAccessToken(token).getId();
			accessTokenToClientRefs.put(currentId, clientId);
			accessTokenToAuthHolderRefs.put(currentId, authHolderId);
			if (refreshTokenId != null) {
				accessTokenToRefreshTokenRefs.put(currentId, refreshTokenId);
			}
			if (idTokenId != null) {
				accessTokenToIdTokenRefs.put(currentId, idTokenId);
			}
			accessTokenOldToNewIdMap.put(currentId, newId);
			logger.debug("Read access token {}", currentId);
		}
		reader.endArray();
		logger.info("Done reading access tokens");
	}
	private Map<Long, Long> authHolderOldToNewIdMap = new HashMap<Long, Long>();

	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readAuthenticationHolders(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			AuthenticationHolderEntity ahe = new AuthenticationHolderEntity();
			reader.beginObject();
			Long currentId = null;
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals("id")) {
						currentId = reader.nextLong();
					} else if (name.equals("requestParameters")) {
						ahe.setRequestParameters(readMap(reader));
					} else if (name.equals("clientId")) {
						ahe.setClientId(reader.nextString());
					} else if (name.equals("scope")) {
						ahe.setScope(readSet(reader));
					} else if (name.equals("resourceIds")) {
						ahe.setResourceIds(readSet(reader));
					} else if (name.equals("authorities")) {
						Set<String> authorityStrs = readSet(reader);
						Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
						for (String s : authorityStrs) {
							GrantedAuthority ga = new SimpleGrantedAuthority(s);
							authorities.add(ga);
						}
						ahe.setAuthorities(authorities);
					} else if (name.equals("approved")) {
						ahe.setApproved(reader.nextBoolean());
					} else if (name.equals("redirectUri")) {
						ahe.setRedirectUri(reader.nextString());
					} else if (name.equals("responseTypes")) {
						ahe.setResponseTypes(readSet(reader));
					} else if (name.equals("extensions")) {
						ahe.setExtensions(readMap(reader));
					} else if (name.equals("savedUserAuthentication")) {
						ahe.setUserAuth(readSavedUserAuthentication(reader));
					} else {
						logger.debug("Found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			Long newId = authHolderRepository.save(ahe).getId();
			authHolderOldToNewIdMap.put(currentId, newId);
			logger.debug("Read authentication holder {}", currentId);
		}
		reader.endArray();
		logger.info("Done reading authentication holders");
	}

	/**
	 * @param reader
	 * @return
	 * @throws IOException 
	 */
	private SavedUserAuthentication readSavedUserAuthentication(JsonReader reader) throws IOException {
		SavedUserAuthentication savedUserAuth = new SavedUserAuthentication();
		reader.beginObject();
		
		while (reader.hasNext()) {
			switch(reader.peek()) {
			case END_OBJECT:
				continue;
			case NAME:
				String name = reader.nextName();
				if (reader.peek() == JsonToken.NULL) {
					reader.skipValue();
				} else if (name.equals("name")) {
					savedUserAuth.setName(reader.nextString());
				} else if (name.equals("sourceClass")) {
					savedUserAuth.setSourceClass(reader.nextString());
				} else if (name.equals("authenticated")) {
					savedUserAuth.setAuthenticated(reader.nextBoolean());
				} else if (name.equals("authorities")) {
					Set<String> authorityStrs = readSet(reader);
					Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
					for (String s : authorityStrs) {
						GrantedAuthority ga = new SimpleGrantedAuthority(s);
						authorities.add(ga);
					}
					savedUserAuth.setAuthorities(authorities);
				} else {
					logger.debug("Found unexpected entry");
					reader.skipValue();
				}
				break;
			default:
				logger.debug("Found unexpected entry");
				reader.skipValue();
				continue;
			}
		}
		
		reader.endObject();
		return savedUserAuth;
	}

	Map<Long, Long> grantOldToNewIdMap = new HashMap<>();
	Map<Long, Set<Long>> grantToAccessTokensRefs = new HashMap<>();

	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readGrants(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			ApprovedSite site = new ApprovedSite();
			Long currentId = null;
			Set<Long> tokenIds = null;
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals("id")) {
						currentId = reader.nextLong();
					} else if (name.equals("accessDate")) {
						Date date = utcToDate(reader.nextString());
						site.setAccessDate(date);
					} else if (name.equals("clientId")) {
						site.setClientId(reader.nextString());
					} else if (name.equals("creationDate")) {
						Date date = utcToDate(reader.nextString());
						site.setCreationDate(date);
					} else if (name.equals("timeoutDate")) {
						Date date = utcToDate(reader.nextString());
						site.setTimeoutDate(date);
					} else if (name.equals("userId")) {
						site.setUserId(reader.nextString());
					} else if (name.equals("allowedScopes")) {
						Set<String> allowedScopes = readSet(reader);
						site.setAllowedScopes(allowedScopes);
					} else if (name.equals("approvedAccessTokens")) {
						tokenIds = readSet(reader);
					} else {
						logger.debug("Found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			Long newId = approvedSiteRepository.save(site).getId();
			grantOldToNewIdMap.put(currentId, newId);
			if (tokenIds != null) {
				grantToAccessTokensRefs.put(currentId, tokenIds);
			}
			logger.debug("Read grant {}", currentId);
		}
		reader.endArray();
		logger.info("Done reading grants");
	}
	Map<Long, Long> whitelistedSiteOldToNewIdMap = new HashMap<Long, Long>();

	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readWhitelistedSites(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			WhitelistedSite wlSite = new WhitelistedSite();
			Long currentId = null;
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (name.equals("id")) {
						currentId = reader.nextLong();
					} else if (name.equals("clientId")) {
						wlSite.setClientId(reader.nextString());
					} else if (name.equals("creatorUserId")) {
						wlSite.setCreatorUserId(reader.nextString());
					} else if (name.equals("allowedScopes")) {
						Set<String> allowedScopes = readSet(reader);
						wlSite.setAllowedScopes(allowedScopes);
					} else {
						logger.debug("Found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			Long newId = wlSiteRepository.save(wlSite).getId();
			whitelistedSiteOldToNewIdMap.put(currentId, newId);
		}
		reader.endArray();
		logger.info("Done reading whitelisted sites");
	}

	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readBlacklistedSites(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			BlacklistedSite blSite = new BlacklistedSite();
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (name.equals("id")) {
						reader.skipValue();
					} else if (name.equals("uri")) {
						blSite.setUri(reader.nextString());
					} else {
						logger.debug("Found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			blSiteRepository.save(blSite);
		}
		reader.endArray();
		logger.info("Done reading blacklisted sites");
	}

	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readClients(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			ClientDetailsEntity client = new ClientDetailsEntity();
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals("clientId")) {
						client.setClientId(reader.nextString());
					} else if (name.equals("resourceIds")) {
						Set<String> resourceIds = readSet(reader);
						client.setResourceIds(resourceIds);
					} else if (name.equals("secret")) {
						client.setClientSecret(reader.nextString());
					} else if (name.equals("scope")) {
						Set<String> scope = readSet(reader);
						client.setScope(scope);
					} else if (name.equals("authorities")) {
						Set<String> authorityStrs = readSet(reader);
						Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
						for (String s : authorityStrs) {
							GrantedAuthority ga = new SimpleGrantedAuthority(s);
							authorities.add(ga);
						}
						client.setAuthorities(authorities);
					} else if (name.equals("accessTokenValiditySeconds")) {
						client.setAccessTokenValiditySeconds(reader.nextInt());
					} else if (name.equals("refreshTokenValiditySeconds")) {
						client.setRefreshTokenValiditySeconds(reader.nextInt());
					} else if (name.equals("redirectUris")) {
						Set<String> redirectUris = readSet(reader);
						client.setRedirectUris(redirectUris);
					} else if (name.equals("name")) {
						client.setClientName(reader.nextString());
					} else if (name.equals("uri")) {
						client.setClientUri(reader.nextString());
					} else if (name.equals("logoUri")) {
						client.setLogoUri(reader.nextString());
					} else if (name.equals("contacts")) {
						Set<String> contacts = readSet(reader);
						client.setContacts(contacts);
					} else if (name.equals("tosUri")) {
						client.setTosUri(reader.nextString());
					} else if (name.equals("tokenEndpointAuthMethod")) {
						AuthMethod am = AuthMethod.getByValue(reader.nextString());
						client.setTokenEndpointAuthMethod(am);
					} else if (name.equals("grantTypes")) {
						Set<String> grantTypes = readSet(reader);
						client.setGrantTypes(grantTypes);
					} else if (name.equals("responseTypes")) {
						Set<String> responseTypes = readSet(reader);
						client.setResponseTypes(responseTypes);
					} else if (name.equals("policyUri")) {
						client.setPolicyUri(reader.nextString());
					} else if (name.equals("applicationType")) {
						AppType appType = AppType.getByValue(reader.nextString());
						client.setApplicationType(appType);
					} else if (name.equals("sectorIdentifierUri")) {
						client.setSectorIdentifierUri(reader.nextString());
					} else if (name.equals("subjectType")) {
						SubjectType st = SubjectType.getByValue(reader.nextString());
						client.setSubjectType(st);
					} else if (name.equals("jwks_uri")) {
						client.setJwksUri(reader.nextString());
					} else if (name.equals("jwks")) {
						try {
							client.setJwks(JWKSet.parse(reader.nextString()));
						} catch (ParseException e) {
							logger.error("Couldn't parse JWK Set", e);
						}
					} else if (name.equals("requestObjectSigningAlg")) {
						JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
						client.setRequestObjectSigningAlg(alg);
					} else if (name.equals("userInfoEncryptedResponseAlg")) {
						JWEAlgorithm alg = JWEAlgorithm.parse(reader.nextString());
						client.setUserInfoEncryptedResponseAlg(alg);
					} else if (name.equals("userInfoEncryptedResponseEnc")) {
						EncryptionMethod alg = EncryptionMethod.parse(reader.nextString());
						client.setUserInfoEncryptedResponseEnc(alg);
					} else if (name.equals("userInfoSignedResponseAlg")) {
						JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
						client.setUserInfoSignedResponseAlg(alg);
					} else if (name.equals("idTokenSignedResonseAlg")) {
						JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
						client.setIdTokenSignedResponseAlg(alg);
					} else if (name.equals("idTokenEncryptedResponseAlg")) {
						JWEAlgorithm alg = JWEAlgorithm.parse(reader.nextString());
						client.setIdTokenEncryptedResponseAlg(alg);
					} else if (name.equals("idTokenEncryptedResponseEnc")) {
						EncryptionMethod alg = EncryptionMethod.parse(reader.nextString());
						client.setIdTokenEncryptedResponseEnc(alg);
					} else if (name.equals("tokenEndpointAuthSigningAlg")) {
						JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
						client.setTokenEndpointAuthSigningAlg(alg);
					} else if (name.equals("defaultMaxAge")) {
						client.setDefaultMaxAge(reader.nextInt());
					} else if (name.equals("requireAuthTime")) {
						client.setRequireAuthTime(reader.nextBoolean());
					} else if (name.equals("defaultACRValues")) {
						Set<String> defaultACRvalues = readSet(reader);
						client.setDefaultACRvalues(defaultACRvalues);
					} else if (name.equals("initiateLoginUri")) {
						client.setInitiateLoginUri(reader.nextString());
					} else if (name.equals("postLogoutRedirectUri")) {
						Set<String> postLogoutUris = readSet(reader);
						client.setPostLogoutRedirectUris(postLogoutUris);
					} else if (name.equals("requestUris")) {
						Set<String> requestUris = readSet(reader);
						client.setRequestUris(requestUris);
					} else if (name.equals("description")) {
						client.setClientDescription(reader.nextString());
					} else if (name.equals("allowIntrospection")) {
						client.setAllowIntrospection(reader.nextBoolean());
					} else if (name.equals("reuseRefreshToken")) {
						client.setReuseRefreshToken(reader.nextBoolean());
					} else if (name.equals("clearAccessTokensOnRefresh")) {
						client.setClearAccessTokensOnRefresh(reader.nextBoolean());
					} else if (name.equals("dynamicallyRegistered")) {
						client.setDynamicallyRegistered(reader.nextBoolean());
					} else {
						logger.debug("Found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			clientRepository.saveClient(client);
		}
		reader.endArray();
		logger.info("Done reading clients");
	}

	/**
	 * Read the list of system scopes from the reader and insert them into the
	 * scope repository.
	 *
	 * @param reader
	 * @throws IOException
	 */
	private void readSystemScopes(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			SystemScope scope = new SystemScope();
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals("value")) {
						scope.setValue(reader.nextString());
					} else if (name.equals("description")) {
						scope.setDescription(reader.nextString());
					} else if (name.equals("restricted")) {
						scope.setRestricted(reader.nextBoolean());
					} else if (name.equals("defaultScope")) {
						scope.setDefaultScope(reader.nextBoolean());
					} else if (name.equals("icon")) {
						scope.setIcon(reader.nextString());
					} else if (name.equals("structured")) {
						scope.setStructured(reader.nextBoolean());
					} else if (name.equals("structuredParameter")) {
						scope.setStructuredParamDescription(reader.nextString());
					} else {
						logger.debug("found unexpected entry");
						reader.skipValue();
					}
					break;
				default:
					logger.debug("Found unexpected entry");
					reader.skipValue();
					continue;
				}
			}
			reader.endObject();
			sysScopeRepository.save(scope);
		}
		reader.endArray();
		logger.info("Done reading system scopes");
	}

	private void fixObjectReferences() {
		logger.info("Fixing object references...");
		for (Long oldRefreshTokenId : refreshTokenToClientRefs.keySet()) {
			String clientRef = refreshTokenToClientRefs.get(oldRefreshTokenId);
			ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
			Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			refreshToken.setClient(client);
			tokenRepository.saveRefreshToken(refreshToken);
		}
		refreshTokenToClientRefs.clear();
		for (Long oldRefreshTokenId : refreshTokenToAuthHolderRefs.keySet()) {
			Long oldAuthHolderId = refreshTokenToAuthHolderRefs.get(oldRefreshTokenId);
			Long newAuthHolderId = authHolderOldToNewIdMap.get(oldAuthHolderId);
			AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
			Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			refreshToken.setAuthenticationHolder(authHolder);
			tokenRepository.saveRefreshToken(refreshToken);
		}
		refreshTokenToAuthHolderRefs.clear();
		for (Long oldAccessTokenId : accessTokenToClientRefs.keySet()) {
			String clientRef = accessTokenToClientRefs.get(oldAccessTokenId);
			ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
			Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setClient(client);
			tokenRepository.saveAccessToken(accessToken);
		}
		accessTokenToClientRefs.clear();
		for (Long oldAccessTokenId : accessTokenToAuthHolderRefs.keySet()) {
			Long oldAuthHolderId = accessTokenToAuthHolderRefs.get(oldAccessTokenId);
			Long newAuthHolderId = authHolderOldToNewIdMap.get(oldAuthHolderId);
			AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
			Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setAuthenticationHolder(authHolder);
			tokenRepository.saveAccessToken(accessToken);
		}
		accessTokenToAuthHolderRefs.clear();
		for (Long oldAccessTokenId : accessTokenToRefreshTokenRefs.keySet()) {
			Long oldRefreshTokenId = accessTokenToRefreshTokenRefs.get(oldAccessTokenId);
			Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setRefreshToken(refreshToken);
			tokenRepository.saveAccessToken(accessToken);
		}
		accessTokenToRefreshTokenRefs.clear();
		refreshTokenOldToNewIdMap.clear();
		for (Long oldAccessTokenId : accessTokenToIdTokenRefs.keySet()) {
			Long oldIdTokenId = accessTokenToIdTokenRefs.get(oldAccessTokenId);
			Long newIdTokenId = accessTokenOldToNewIdMap.get(oldIdTokenId);
			OAuth2AccessTokenEntity idToken = tokenRepository.getAccessTokenById(newIdTokenId);
			Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setIdToken(idToken);
			tokenRepository.saveAccessToken(accessToken);
		}
		accessTokenToIdTokenRefs.clear();
		for (Long oldGrantId : grantToAccessTokensRefs.keySet()) {
			Set<Long> oldAccessTokenIds = grantToAccessTokensRefs.get(oldGrantId);
			Set<OAuth2AccessTokenEntity> tokens = new HashSet<OAuth2AccessTokenEntity>();
			for(Long oldTokenId : oldAccessTokenIds) {
				Long newTokenId = accessTokenOldToNewIdMap.get(oldTokenId);
				tokens.add(tokenRepository.getAccessTokenById(newTokenId));
			}
			Long newGrantId = grantOldToNewIdMap.get(oldGrantId);
			ApprovedSite site = approvedSiteRepository.getById(newGrantId);
			site.setApprovedAccessTokens(tokens);
			approvedSiteRepository.save(site);
		}
		accessTokenOldToNewIdMap.clear();
		grantOldToNewIdMap.clear();
		logger.info("Done fixing object references.");
	}
	
}
