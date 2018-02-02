/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

import static org.mitre.util.JsonUtils.readMap;
import static org.mitre.util.JsonUtils.readSet;
import static org.mitre.util.JsonUtils.writeNullSafeArray;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.PKCEAlgorithm;
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
import org.mitre.openid.connect.service.MITREidDataServiceExtension;
import org.mitre.openid.connect.service.MITREidDataServiceMaps;
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

/**
 *
 * Data service to import and export MITREid 1.3 configuration.
 *
 * @author jricher
 * @author arielak
 */
@Service
@SuppressWarnings(value = {"unchecked"})
public class MITREidDataService_1_3 extends MITREidDataServiceSupport implements MITREidDataService {

	private static final String DEFAULT_SCOPE = "defaultScope";
	private static final String RESTRICTED = "restricted";
	private static final String ICON = "icon";
	private static final String DYNAMICALLY_REGISTERED = "dynamicallyRegistered";
	private static final String CLEAR_ACCESS_TOKENS_ON_REFRESH = "clearAccessTokensOnRefresh";
	private static final String REUSE_REFRESH_TOKEN = "reuseRefreshToken";
	private static final String ALLOW_INTROSPECTION = "allowIntrospection";
	private static final String DESCRIPTION = "description";
	private static final String REQUEST_URIS = "requestUris";
	private static final String POST_LOGOUT_REDIRECT_URI = "postLogoutRedirectUri";
	private static final String INTITATE_LOGIN_URI = "intitateLoginUri";
	private static final String DEFAULT_ACR_VALUES = "defaultACRValues";
	private static final String REQUIRE_AUTH_TIME = "requireAuthTime";
	private static final String DEFAULT_MAX_AGE = "defaultMaxAge";
	private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "tokenEndpointAuthSigningAlg";
	private static final String USER_INFO_ENCRYPTED_RESPONSE_ENC = "userInfoEncryptedResponseEnc";
	private static final String USER_INFO_ENCRYPTED_RESPONSE_ALG = "userInfoEncryptedResponseAlg";
	private static final String USER_INFO_SIGNED_RESPONSE_ALG = "userInfoSignedResponseAlg";
	private static final String ID_TOKEN_ENCRYPTED_RESPONSE_ENC = "idTokenEncryptedResponseEnc";
	private static final String ID_TOKEN_ENCRYPTED_RESPONSE_ALG = "idTokenEncryptedResponseAlg";
	private static final String ID_TOKEN_SIGNED_RESPONSE_ALG = "idTokenSignedResponseAlg";
	private static final String REQUEST_OBJECT_SIGNING_ALG = "requestObjectSigningAlg";
	private static final String SUBJECT_TYPE = "subjectType";
	private static final String SECTOR_IDENTIFIER_URI = "sectorIdentifierUri";
	private static final String APPLICATION_TYPE = "applicationType";
	private static final String JWKS = "jwks";
	private static final String JWKS_URI = "jwksUri";
	private static final String POLICY_URI = "policyUri";
	private static final String GRANT_TYPES = "grantTypes";
	private static final String TOKEN_ENDPOINT_AUTH_METHOD = "tokenEndpointAuthMethod";
	private static final String TOS_URI = "tosUri";
	private static final String CONTACTS = "contacts";
	private static final String LOGO_URI = "logoUri";
	private static final String REDIRECT_URIS = "redirectUris";
	private static final String REFRESH_TOKEN_VALIDITY_SECONDS = "refreshTokenValiditySeconds";
	private static final String ACCESS_TOKEN_VALIDITY_SECONDS = "accessTokenValiditySeconds";
	private static final String ID_TOKEN_VALIDITY_SECONDS = "idTokenValiditySeconds";
	private static final String DEVICE_CODE_VALIDITY_SECONDS = "deviceCodeValiditySeconds";
	private static final String SECRET = "secret";
	private static final String URI = "uri";
	private static final String CREATOR_USER_ID = "creatorUserId";
	private static final String APPROVED_ACCESS_TOKENS = "approvedAccessTokens";
	private static final String ALLOWED_SCOPES = "allowedScopes";
	private static final String USER_ID = "userId";
	private static final String TIMEOUT_DATE = "timeoutDate";
	private static final String CREATION_DATE = "creationDate";
	private static final String ACCESS_DATE = "accessDate";
	private static final String AUTHENTICATED = "authenticated";
	private static final String SOURCE_CLASS = "sourceClass";
	private static final String NAME = "name";
	private static final String SAVED_USER_AUTHENTICATION = "savedUserAuthentication";
	private static final String EXTENSIONS = "extensions";
	private static final String RESPONSE_TYPES = "responseTypes";
	private static final String REDIRECT_URI = "redirectUri";
	private static final String APPROVED = "approved";
	private static final String AUTHORITIES = "authorities";
	private static final String RESOURCE_IDS = "resourceIds";
	private static final String REQUEST_PARAMETERS = "requestParameters";
	private static final String TYPE = "type";
	private static final String SCOPE = "scope";
	private static final String REFRESH_TOKEN_ID = "refreshTokenId";
	private static final String VALUE = "value";
	private static final String AUTHENTICATION_HOLDER_ID = "authenticationHolderId";
	private static final String CLIENT_ID = "clientId";
	private static final String EXPIRATION = "expiration";
	private static final String CLAIMS_REDIRECT_URIS = "claimsRedirectUris";
	private static final String ID = "id";
	private static final String CODE_CHALLENGE_METHOD = "codeChallengeMethod";
	private static final String SOFTWARE_STATEMENT = "softwareStatement";
	private static final String SOFTWARE_VERSION = "softwareVersion";
	private static final String SOFTWARE_ID = "softwareId";

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(MITREidDataService_1_3.class);
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
	@Autowired(required = false)
	private List<MITREidDataServiceExtension> extensions = Collections.emptyList();

	private static final String THIS_VERSION = MITREID_CONNECT_1_3;

	private MITREidDataServiceMaps maps = new MITREidDataServiceMaps();

	@Override
	public boolean supportsVersion(String version) {
		return THIS_VERSION.equals(version);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataService#export(com.google.gson.stream.JsonWriter)
	 */
	@Override
	public void exportData(JsonWriter writer) throws IOException {

		// version tag at the root
		writer.name(THIS_VERSION);

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

		for (MITREidDataServiceExtension extension : extensions) {
			if (extension.supportsVersion(THIS_VERSION)) {
				extension.exportExtensionData(writer);
				break;
			}
		}

		writer.endObject(); // end mitreid-connect-1.3
	}

	/**
	 * @param writer
	 */
	private void writeRefreshTokens(JsonWriter writer) throws IOException {
		for (OAuth2RefreshTokenEntity token : tokenRepository.getAllRefreshTokens()) {
			writer.beginObject();
			writer.name(ID).value(token.getId());
			writer.name(EXPIRATION).value(toUTCString(token.getExpiration()));
			writer.name(CLIENT_ID)
			.value((token.getClient() != null) ? token.getClient().getClientId() : null);
			writer.name(AUTHENTICATION_HOLDER_ID)
			.value((token.getAuthenticationHolder() != null) ? token.getAuthenticationHolder().getId() : null);
			writer.name(VALUE).value(token.getValue());
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
			writer.name(ID).value(token.getId());
			writer.name(EXPIRATION).value(toUTCString(token.getExpiration()));
			writer.name(CLIENT_ID)
			.value((token.getClient() != null) ? token.getClient().getClientId() : null);
			writer.name(AUTHENTICATION_HOLDER_ID)
			.value((token.getAuthenticationHolder() != null) ? token.getAuthenticationHolder().getId() : null);
			writer.name(REFRESH_TOKEN_ID)
			.value((token.getRefreshToken() != null) ? token.getRefreshToken().getId() : null);
			writer.name(SCOPE);
			writer.beginArray();
			for (String s : token.getScope()) {
				writer.value(s);
			}
			writer.endArray();
			writer.name(TYPE).value(token.getTokenType());
			writer.name(VALUE).value(token.getValue());
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
			writer.name(ID).value(holder.getId());

			writer.name(REQUEST_PARAMETERS);
			writer.beginObject();
			for (Entry<String, String> entry : holder.getRequestParameters().entrySet()) {
				writer.name(entry.getKey()).value(entry.getValue());
			}
			writer.endObject();
			writer.name(CLIENT_ID).value(holder.getClientId());
			Set<String> scope = holder.getScope();
			writer.name(SCOPE);
			writer.beginArray();
			for (String s : scope) {
				writer.value(s);
			}
			writer.endArray();
			writer.name(RESOURCE_IDS);
			writer.beginArray();
			if (holder.getResourceIds() != null) {
				for (String s : holder.getResourceIds()) {
					writer.value(s);
				}
			}
			writer.endArray();
			writer.name(AUTHORITIES);
			writer.beginArray();
			for (GrantedAuthority authority : holder.getAuthorities()) {
				writer.value(authority.getAuthority());
			}
			writer.endArray();
			writer.name(APPROVED).value(holder.isApproved());
			writer.name(REDIRECT_URI).value(holder.getRedirectUri());
			writer.name(RESPONSE_TYPES);
			writer.beginArray();
			for (String s : holder.getResponseTypes()) {
				writer.value(s);
			}
			writer.endArray();
			writer.name(EXTENSIONS);
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

			writer.name(SAVED_USER_AUTHENTICATION);
			if (holder.getUserAuth() != null) {
				writer.beginObject();
				writer.name(NAME).value(holder.getUserAuth().getName());
				writer.name(SOURCE_CLASS).value(holder.getUserAuth().getSourceClass());
				writer.name(AUTHENTICATED).value(holder.getUserAuth().isAuthenticated());
				writer.name(AUTHORITIES);
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
			writer.name(ID).value(site.getId());
			writer.name(ACCESS_DATE).value(toUTCString(site.getAccessDate()));
			writer.name(CLIENT_ID).value(site.getClientId());
			writer.name(CREATION_DATE).value(toUTCString(site.getCreationDate()));
			writer.name(TIMEOUT_DATE).value(toUTCString(site.getTimeoutDate()));
			writer.name(USER_ID).value(site.getUserId());
			writer.name(ALLOWED_SCOPES);
			writeNullSafeArray(writer, site.getAllowedScopes());
			List<OAuth2AccessTokenEntity> tokens = tokenRepository.getAccessTokensForApprovedSite(site);
			writer.name(APPROVED_ACCESS_TOKENS);
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
			writer.name(ID).value(wlSite.getId());
			writer.name(CLIENT_ID).value(wlSite.getClientId());
			writer.name(CREATOR_USER_ID).value(wlSite.getCreatorUserId());
			writer.name(ALLOWED_SCOPES);
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
			writer.name(ID).value(blSite.getId());
			writer.name(URI).value(blSite.getUri());
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
				writer.name(CLIENT_ID).value(client.getClientId());
				writer.name(RESOURCE_IDS);
				writeNullSafeArray(writer, client.getResourceIds());

				writer.name(SECRET).value(client.getClientSecret());

				writer.name(SCOPE);
				writeNullSafeArray(writer, client.getScope());

				writer.name(AUTHORITIES);
				writer.beginArray();
				for (GrantedAuthority authority : client.getAuthorities()) {
					writer.value(authority.getAuthority());
				}
				writer.endArray();
				writer.name(ACCESS_TOKEN_VALIDITY_SECONDS).value(client.getAccessTokenValiditySeconds());
				writer.name(REFRESH_TOKEN_VALIDITY_SECONDS).value(client.getRefreshTokenValiditySeconds());
				writer.name(ID_TOKEN_VALIDITY_SECONDS).value(client.getIdTokenValiditySeconds());
				writer.name(DEVICE_CODE_VALIDITY_SECONDS).value(client.getDeviceCodeValiditySeconds());
				writer.name(REDIRECT_URIS);
				writeNullSafeArray(writer, client.getRedirectUris());
				writer.name(CLAIMS_REDIRECT_URIS);
				writeNullSafeArray(writer, client.getClaimsRedirectUris());
				writer.name(NAME).value(client.getClientName());
				writer.name(URI).value(client.getClientUri());
				writer.name(LOGO_URI).value(client.getLogoUri());
				writer.name(CONTACTS);
				writeNullSafeArray(writer, client.getContacts());
				writer.name(TOS_URI).value(client.getTosUri());
				writer.name(TOKEN_ENDPOINT_AUTH_METHOD)
				.value((client.getTokenEndpointAuthMethod() != null) ? client.getTokenEndpointAuthMethod().getValue() : null);
				writer.name(GRANT_TYPES);
				writer.beginArray();
				for (String s : client.getGrantTypes()) {
					writer.value(s);
				}
				writer.endArray();
				writer.name(RESPONSE_TYPES);
				writer.beginArray();
				for (String s : client.getResponseTypes()) {
					writer.value(s);
				}
				writer.endArray();
				writer.name(POLICY_URI).value(client.getPolicyUri());
				writer.name(JWKS_URI).value(client.getJwksUri());
				writer.name(JWKS).value((client.getJwks() != null) ? client.getJwks().toString() : null);
				writer.name(APPLICATION_TYPE)
				.value((client.getApplicationType() != null) ? client.getApplicationType().getValue() : null);
				writer.name(SECTOR_IDENTIFIER_URI).value(client.getSectorIdentifierUri());
				writer.name(SUBJECT_TYPE)
				.value((client.getSubjectType() != null) ? client.getSubjectType().getValue() : null);
				writer.name(REQUEST_OBJECT_SIGNING_ALG)
				.value((client.getRequestObjectSigningAlg() != null) ? client.getRequestObjectSigningAlg().getName() : null);
				writer.name(ID_TOKEN_SIGNED_RESPONSE_ALG)
				.value((client.getIdTokenSignedResponseAlg() != null) ? client.getIdTokenSignedResponseAlg().getName() : null);
				writer.name(ID_TOKEN_ENCRYPTED_RESPONSE_ALG)
				.value((client.getIdTokenEncryptedResponseAlg() != null) ? client.getIdTokenEncryptedResponseAlg().getName() : null);
				writer.name(ID_TOKEN_ENCRYPTED_RESPONSE_ENC)
				.value((client.getIdTokenEncryptedResponseEnc() != null) ? client.getIdTokenEncryptedResponseEnc().getName() : null);
				writer.name(USER_INFO_SIGNED_RESPONSE_ALG)
				.value((client.getUserInfoSignedResponseAlg() != null) ? client.getUserInfoSignedResponseAlg().getName() : null);
				writer.name(USER_INFO_ENCRYPTED_RESPONSE_ALG)
				.value((client.getUserInfoEncryptedResponseAlg() != null) ? client.getUserInfoEncryptedResponseAlg().getName() : null);
				writer.name(USER_INFO_ENCRYPTED_RESPONSE_ENC)
				.value((client.getUserInfoEncryptedResponseEnc() != null) ? client.getUserInfoEncryptedResponseEnc().getName() : null);
				writer.name(TOKEN_ENDPOINT_AUTH_SIGNING_ALG)
				.value((client.getTokenEndpointAuthSigningAlg() != null) ? client.getTokenEndpointAuthSigningAlg().getName() : null);
				writer.name(DEFAULT_MAX_AGE).value(client.getDefaultMaxAge());
				Boolean requireAuthTime = null;
				try {
					requireAuthTime = client.getRequireAuthTime();
				} catch (NullPointerException e) {
				}
				if (requireAuthTime != null) {
					writer.name(REQUIRE_AUTH_TIME).value(requireAuthTime);
				}
				writer.name(DEFAULT_ACR_VALUES);
				writeNullSafeArray(writer, client.getDefaultACRvalues());
				writer.name(INTITATE_LOGIN_URI).value(client.getInitiateLoginUri());
				writer.name(POST_LOGOUT_REDIRECT_URI);
				writeNullSafeArray(writer, client.getPostLogoutRedirectUris());
				writer.name(REQUEST_URIS);
				writeNullSafeArray(writer, client.getRequestUris());
				writer.name(DESCRIPTION).value(client.getClientDescription());
				writer.name(ALLOW_INTROSPECTION).value(client.isAllowIntrospection());
				writer.name(REUSE_REFRESH_TOKEN).value(client.isReuseRefreshToken());
				writer.name(CLEAR_ACCESS_TOKENS_ON_REFRESH).value(client.isClearAccessTokensOnRefresh());
				writer.name(DYNAMICALLY_REGISTERED).value(client.isDynamicallyRegistered());
				writer.name(CODE_CHALLENGE_METHOD).value(client.getCodeChallengeMethod() != null ? client.getCodeChallengeMethod().getName() : null);
				writer.name(SOFTWARE_ID).value(client.getSoftwareId());
				writer.name(SOFTWARE_VERSION).value(client.getSoftwareVersion());
				writer.name(SOFTWARE_STATEMENT).value(client.getSoftwareStatement() != null ? client.getSoftwareStatement().serialize() : null);
				writer.name(CREATION_DATE).value(toUTCString(client.getCreatedAt()));
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
				writer.name(ID).value(sysScope.getId());
				writer.name(DESCRIPTION).value(sysScope.getDescription());
				writer.name(ICON).value(sysScope.getIcon());
				writer.name(VALUE).value(sysScope.getValue());
				writer.name(RESTRICTED).value(sysScope.isRestricted());
				writer.name(DEFAULT_SCOPE).value(sysScope.isDefaultScope());
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

		logger.info("Reading configuration for 1.3");

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
						boolean processed = false;
						for (MITREidDataServiceExtension extension : extensions) {
							if (extension.supportsVersion(THIS_VERSION)) {
								processed = extension.importExtensionData(name, reader);
								if (processed) {
									// if the extension processed data, break out of this inner loop
									// (only the first extension to claim an extension point gets it)
									break;
								}
							}
						}
						if (!processed) {
							// unknown token, skip it
							reader.skipValue();
						}
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
		for (MITREidDataServiceExtension extension : extensions) {
			if (extension.supportsVersion(THIS_VERSION)) {
				extension.fixExtensionObjectReferences(maps);
				break;
			}
		}
		maps.clearAll();
	}

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
						} else if (name.equals(ID)) {
							currentId = reader.nextLong();
						} else if (name.equals(EXPIRATION)) {
							Date date = utcToDate(reader.nextString());
							token.setExpiration(date);
						} else if (name.equals(VALUE)) {
							String value = reader.nextString();
							try {
								token.setJwt(JWTParser.parse(value));
							} catch (ParseException ex) {
								logger.error("Unable to set refresh token value to {}", value, ex);
							}
						} else if (name.equals(CLIENT_ID)) {
							clientId = reader.nextString();
						} else if (name.equals(AUTHENTICATION_HOLDER_ID)) {
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
			maps.getRefreshTokenToClientRefs().put(currentId, clientId);
			maps.getRefreshTokenToAuthHolderRefs().put(currentId, authHolderId);
			maps.getRefreshTokenOldToNewIdMap().put(currentId, newId);
			logger.debug("Read refresh token {}", currentId);
		}
		reader.endArray();
		logger.info("Done reading refresh tokens");
	}
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
			while (reader.hasNext()) {
				switch (reader.peek()) {
					case END_OBJECT:
						continue;
					case NAME:
						String name = reader.nextName();
						if (reader.peek() == JsonToken.NULL) {
							reader.skipValue();
						} else if (name.equals(ID)) {
							currentId = reader.nextLong();
						} else if (name.equals(EXPIRATION)) {
							Date date = utcToDate(reader.nextString());
							token.setExpiration(date);
						} else if (name.equals(VALUE)) {
							String value = reader.nextString();
							try {
								// all tokens are JWTs
								token.setJwt(JWTParser.parse(value));
							} catch (ParseException ex) {
								logger.error("Unable to set refresh token value to {}", value, ex);
							}
						} else if (name.equals(CLIENT_ID)) {
							clientId = reader.nextString();
						} else if (name.equals(AUTHENTICATION_HOLDER_ID)) {
							authHolderId = reader.nextLong();
						} else if (name.equals(REFRESH_TOKEN_ID)) {
							refreshTokenId = reader.nextLong();
						} else if (name.equals(SCOPE)) {
							Set<String> scope = readSet(reader);
							token.setScope(scope);
						} else if (name.equals(TYPE)) {
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
			maps.getAccessTokenToClientRefs().put(currentId, clientId);
			maps.getAccessTokenToAuthHolderRefs().put(currentId, authHolderId);
			if (refreshTokenId != null) {
				maps.getAccessTokenToRefreshTokenRefs().put(currentId, refreshTokenId);
			}
			maps.getAccessTokenOldToNewIdMap().put(currentId, newId);
			logger.debug("Read access token {}", currentId);
		}
		reader.endArray();
		logger.info("Done reading access tokens");
	}
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
						} else if (name.equals(ID)) {
							currentId = reader.nextLong();
						} else if (name.equals(REQUEST_PARAMETERS)) {
							ahe.setRequestParameters(readMap(reader));
						} else if (name.equals(CLIENT_ID)) {
							ahe.setClientId(reader.nextString());
						} else if (name.equals(SCOPE)) {
							ahe.setScope(readSet(reader));
						} else if (name.equals(RESOURCE_IDS)) {
							ahe.setResourceIds(readSet(reader));
						} else if (name.equals(AUTHORITIES)) {
							Set<String> authorityStrs = readSet(reader);
							Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
							for (String s : authorityStrs) {
								GrantedAuthority ga = new SimpleGrantedAuthority(s);
								authorities.add(ga);
							}
							ahe.setAuthorities(authorities);
						} else if (name.equals(APPROVED)) {
							ahe.setApproved(reader.nextBoolean());
						} else if (name.equals(REDIRECT_URI)) {
							ahe.setRedirectUri(reader.nextString());
						} else if (name.equals(RESPONSE_TYPES)) {
							ahe.setResponseTypes(readSet(reader));
						} else if (name.equals(EXTENSIONS)) {
							ahe.setExtensions(readMap(reader));
						} else if (name.equals(SAVED_USER_AUTHENTICATION)) {
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
			maps.getAuthHolderOldToNewIdMap().put(currentId, newId);
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
					} else if (name.equals(NAME)) {
						savedUserAuth.setName(reader.nextString());
					} else if (name.equals(SOURCE_CLASS)) {
						savedUserAuth.setSourceClass(reader.nextString());
					} else if (name.equals(AUTHENTICATED)) {
						savedUserAuth.setAuthenticated(reader.nextBoolean());
					} else if (name.equals(AUTHORITIES)) {
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
						} else if (name.equals(ID)) {
							currentId = reader.nextLong();
						} else if (name.equals(ACCESS_DATE)) {
							Date date = utcToDate(reader.nextString());
							site.setAccessDate(date);
						} else if (name.equals(CLIENT_ID)) {
							site.setClientId(reader.nextString());
						} else if (name.equals(CREATION_DATE)) {
							Date date = utcToDate(reader.nextString());
							site.setCreationDate(date);
						} else if (name.equals(TIMEOUT_DATE)) {
							Date date = utcToDate(reader.nextString());
							site.setTimeoutDate(date);
						} else if (name.equals(USER_ID)) {
							site.setUserId(reader.nextString());
						} else if (name.equals(ALLOWED_SCOPES)) {
							Set<String> allowedScopes = readSet(reader);
							site.setAllowedScopes(allowedScopes);
						} else if (name.equals(APPROVED_ACCESS_TOKENS)) {
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
			maps.getGrantOldToNewIdMap().put(currentId, newId);
			if (tokenIds != null) {
				maps.getGrantToAccessTokensRefs().put(currentId, tokenIds);
			}
			logger.debug("Read grant {}", currentId);
		}
		reader.endArray();
		logger.info("Done reading grants");
	}

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
						if (name.equals(ID)) {
							currentId = reader.nextLong();
						} else if (name.equals(CLIENT_ID)) {
							wlSite.setClientId(reader.nextString());
						} else if (name.equals(CREATOR_USER_ID)) {
							wlSite.setCreatorUserId(reader.nextString());
						} else if (name.equals(ALLOWED_SCOPES)) {
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
			maps.getWhitelistedSiteOldToNewIdMap().put(currentId, newId);
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
						if (name.equals(ID)) {
							reader.skipValue();
						} else if (name.equals(URI)) {
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
						} else if (name.equals(CLIENT_ID)) {
							client.setClientId(reader.nextString());
						} else if (name.equals(RESOURCE_IDS)) {
							Set<String> resourceIds = readSet(reader);
							client.setResourceIds(resourceIds);
						} else if (name.equals(SECRET)) {
							client.setClientSecret(reader.nextString());
						} else if (name.equals(SCOPE)) {
							Set<String> scope = readSet(reader);
							client.setScope(scope);
						} else if (name.equals(AUTHORITIES)) {
							Set<String> authorityStrs = readSet(reader);
							Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
							for (String s : authorityStrs) {
								GrantedAuthority ga = new SimpleGrantedAuthority(s);
								authorities.add(ga);
							}
							client.setAuthorities(authorities);
						} else if (name.equals(ACCESS_TOKEN_VALIDITY_SECONDS)) {
							client.setAccessTokenValiditySeconds(reader.nextInt());
						} else if (name.equals(REFRESH_TOKEN_VALIDITY_SECONDS)) {
							client.setRefreshTokenValiditySeconds(reader.nextInt());
						} else if (name.equals(ID_TOKEN_VALIDITY_SECONDS)) {
							client.setIdTokenValiditySeconds(reader.nextInt());
						} else if (name.equals(DEVICE_CODE_VALIDITY_SECONDS)) {
							client.setDeviceCodeValiditySeconds(reader.nextInt());
						} else if (name.equals(REDIRECT_URIS)) {
							Set<String> redirectUris = readSet(reader);
							client.setRedirectUris(redirectUris);
						} else if (name.equals(CLAIMS_REDIRECT_URIS)) {
							Set<String> claimsRedirectUris = readSet(reader);
							client.setClaimsRedirectUris(claimsRedirectUris);
						} else if (name.equals(NAME)) {
							client.setClientName(reader.nextString());
						} else if (name.equals(URI)) {
							client.setClientUri(reader.nextString());
						} else if (name.equals(LOGO_URI)) {
							client.setLogoUri(reader.nextString());
						} else if (name.equals(CONTACTS)) {
							Set<String> contacts = readSet(reader);
							client.setContacts(contacts);
						} else if (name.equals(TOS_URI)) {
							client.setTosUri(reader.nextString());
						} else if (name.equals(TOKEN_ENDPOINT_AUTH_METHOD)) {
							AuthMethod am = AuthMethod.getByValue(reader.nextString());
							client.setTokenEndpointAuthMethod(am);
						} else if (name.equals(GRANT_TYPES)) {
							Set<String> grantTypes = readSet(reader);
							client.setGrantTypes(grantTypes);
						} else if (name.equals(RESPONSE_TYPES)) {
							Set<String> responseTypes = readSet(reader);
							client.setResponseTypes(responseTypes);
						} else if (name.equals(POLICY_URI)) {
							client.setPolicyUri(reader.nextString());
						} else if (name.equals(APPLICATION_TYPE)) {
							AppType appType = AppType.getByValue(reader.nextString());
							client.setApplicationType(appType);
						} else if (name.equals(SECTOR_IDENTIFIER_URI)) {
							client.setSectorIdentifierUri(reader.nextString());
						} else if (name.equals(SUBJECT_TYPE)) {
							SubjectType st = SubjectType.getByValue(reader.nextString());
							client.setSubjectType(st);
						} else if (name.equals(JWKS_URI)) {
							client.setJwksUri(reader.nextString());
						} else if (name.equals(JWKS)) {
							try {
								client.setJwks(JWKSet.parse(reader.nextString()));
							} catch (ParseException e) {
								logger.error("Couldn't parse JWK Set", e);
							}
						} else if (name.equals(REQUEST_OBJECT_SIGNING_ALG)) {
							JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
							client.setRequestObjectSigningAlg(alg);
						} else if (name.equals(USER_INFO_ENCRYPTED_RESPONSE_ALG)) {
							JWEAlgorithm alg = JWEAlgorithm.parse(reader.nextString());
							client.setUserInfoEncryptedResponseAlg(alg);
						} else if (name.equals(USER_INFO_ENCRYPTED_RESPONSE_ENC)) {
							EncryptionMethod alg = EncryptionMethod.parse(reader.nextString());
							client.setUserInfoEncryptedResponseEnc(alg);
						} else if (name.equals(USER_INFO_SIGNED_RESPONSE_ALG)) {
							JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
							client.setUserInfoSignedResponseAlg(alg);
						} else if (name.equals(ID_TOKEN_SIGNED_RESPONSE_ALG)) {
							JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
							client.setIdTokenSignedResponseAlg(alg);
						} else if (name.equals(ID_TOKEN_ENCRYPTED_RESPONSE_ALG)) {
							JWEAlgorithm alg = JWEAlgorithm.parse(reader.nextString());
							client.setIdTokenEncryptedResponseAlg(alg);
						} else if (name.equals(ID_TOKEN_ENCRYPTED_RESPONSE_ENC)) {
							EncryptionMethod alg = EncryptionMethod.parse(reader.nextString());
							client.setIdTokenEncryptedResponseEnc(alg);
						} else if (name.equals(TOKEN_ENDPOINT_AUTH_SIGNING_ALG)) {
							JWSAlgorithm alg = JWSAlgorithm.parse(reader.nextString());
							client.setTokenEndpointAuthSigningAlg(alg);
						} else if (name.equals(DEFAULT_MAX_AGE)) {
							client.setDefaultMaxAge(reader.nextInt());
						} else if (name.equals(REQUIRE_AUTH_TIME)) {
							client.setRequireAuthTime(reader.nextBoolean());
						} else if (name.equals(DEFAULT_ACR_VALUES)) {
							Set<String> defaultACRvalues = readSet(reader);
							client.setDefaultACRvalues(defaultACRvalues);
						} else if (name.equals("initiateLoginUri")) {
							client.setInitiateLoginUri(reader.nextString());
						} else if (name.equals(POST_LOGOUT_REDIRECT_URI)) {
							Set<String> postLogoutUris = readSet(reader);
							client.setPostLogoutRedirectUris(postLogoutUris);
						} else if (name.equals(REQUEST_URIS)) {
							Set<String> requestUris = readSet(reader);
							client.setRequestUris(requestUris);
						} else if (name.equals(DESCRIPTION)) {
							client.setClientDescription(reader.nextString());
						} else if (name.equals(ALLOW_INTROSPECTION)) {
							client.setAllowIntrospection(reader.nextBoolean());
						} else if (name.equals(REUSE_REFRESH_TOKEN)) {
							client.setReuseRefreshToken(reader.nextBoolean());
						} else if (name.equals(CLEAR_ACCESS_TOKENS_ON_REFRESH)) {
							client.setClearAccessTokensOnRefresh(reader.nextBoolean());
						} else if (name.equals(DYNAMICALLY_REGISTERED)) {
							client.setDynamicallyRegistered(reader.nextBoolean());
						} else if (name.equals(CODE_CHALLENGE_METHOD)) {
							client.setCodeChallengeMethod(PKCEAlgorithm.parse(reader.nextString()));
						} else if (name.equals(SOFTWARE_ID)) {
							client.setSoftwareId(reader.nextString());
						} else if (name.equals(SOFTWARE_VERSION)) {
							client.setSoftwareVersion(reader.nextString());
						} else if (name.equals(SOFTWARE_STATEMENT)) {
							try {
								client.setSoftwareStatement(JWTParser.parse(reader.nextString()));
							} catch (ParseException e) {
								logger.error("Couldn't parse software statement", e);
							}
						} else if (name.equals(CREATION_DATE)) {
							Date date = utcToDate(reader.nextString());
							client.setCreatedAt(date);
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
						} else if (name.equals(VALUE)) {
							scope.setValue(reader.nextString());
						} else if (name.equals(DESCRIPTION)) {
							scope.setDescription(reader.nextString());
						} else if (name.equals(RESTRICTED)) {
							scope.setRestricted(reader.nextBoolean());
						} else if (name.equals(DEFAULT_SCOPE)) {
							scope.setDefaultScope(reader.nextBoolean());
						} else if (name.equals(ICON)) {
							scope.setIcon(reader.nextString());
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
		for (Long oldRefreshTokenId : maps.getRefreshTokenToClientRefs().keySet()) {
			String clientRef = maps.getRefreshTokenToClientRefs().get(oldRefreshTokenId);
			ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
			Long newRefreshTokenId = maps.getRefreshTokenOldToNewIdMap().get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			refreshToken.setClient(client);
			tokenRepository.saveRefreshToken(refreshToken);
		}
		for (Long oldRefreshTokenId : maps.getRefreshTokenToAuthHolderRefs().keySet()) {
			Long oldAuthHolderId = maps.getRefreshTokenToAuthHolderRefs().get(oldRefreshTokenId);
			Long newAuthHolderId = maps.getAuthHolderOldToNewIdMap().get(oldAuthHolderId);
			AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
			Long newRefreshTokenId = maps.getRefreshTokenOldToNewIdMap().get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			refreshToken.setAuthenticationHolder(authHolder);
			tokenRepository.saveRefreshToken(refreshToken);
		}
		for (Long oldAccessTokenId : maps.getAccessTokenToClientRefs().keySet()) {
			String clientRef = maps.getAccessTokenToClientRefs().get(oldAccessTokenId);
			ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
			Long newAccessTokenId = maps.getAccessTokenOldToNewIdMap().get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setClient(client);
			tokenRepository.saveAccessToken(accessToken);
		}
		for (Long oldAccessTokenId : maps.getAccessTokenToAuthHolderRefs().keySet()) {
			Long oldAuthHolderId = maps.getAccessTokenToAuthHolderRefs().get(oldAccessTokenId);
			Long newAuthHolderId = maps.getAuthHolderOldToNewIdMap().get(oldAuthHolderId);
			AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
			Long newAccessTokenId = maps.getAccessTokenOldToNewIdMap().get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setAuthenticationHolder(authHolder);
			tokenRepository.saveAccessToken(accessToken);
		}
		for (Long oldAccessTokenId : maps.getAccessTokenToRefreshTokenRefs().keySet()) {
			Long oldRefreshTokenId = maps.getAccessTokenToRefreshTokenRefs().get(oldAccessTokenId);
			Long newRefreshTokenId = maps.getRefreshTokenOldToNewIdMap().get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			Long newAccessTokenId = maps.getAccessTokenOldToNewIdMap().get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setRefreshToken(refreshToken);
			tokenRepository.saveAccessToken(accessToken);
		}
		for (Long oldGrantId : maps.getGrantToAccessTokensRefs().keySet()) {
			Set<Long> oldAccessTokenIds = maps.getGrantToAccessTokensRefs().get(oldGrantId);

			Long newGrantId = maps.getGrantOldToNewIdMap().get(oldGrantId);
			ApprovedSite site = approvedSiteRepository.getById(newGrantId);

			for(Long oldTokenId : oldAccessTokenIds) {
				Long newTokenId = maps.getAccessTokenOldToNewIdMap().get(oldTokenId);
				OAuth2AccessTokenEntity token = tokenRepository.getAccessTokenById(newTokenId);
				token.setApprovedSite(site);
				tokenRepository.saveAccessToken(token);
			}

			approvedSiteRepository.save(site);
		}
		/*
		refreshTokenToClientRefs.clear();
		refreshTokenToAuthHolderRefs.clear();
		accessTokenToClientRefs.clear();
		accessTokenToAuthHolderRefs.clear();
		accessTokenToRefreshTokenRefs.clear();
		refreshTokenOldToNewIdMap.clear();
		accessTokenOldToNewIdMap.clear();
		grantOldToNewIdMap.clear();
		 */
		logger.info("Done fixing object references.");
	}

}
