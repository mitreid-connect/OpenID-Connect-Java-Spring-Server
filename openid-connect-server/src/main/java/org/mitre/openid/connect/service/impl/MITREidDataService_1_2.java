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

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
 * Data service to import and export MITREid 1.2 configuration.
 *
 * @author jricher
 * @author arielak
 */
@Service
@SuppressWarnings(value = {"unchecked"})
public class MITREidDataService_1_2 extends MITREidDataServiceSupport implements MITREidDataService {

	private static final String DEFAULT_SCOPE = "defaultScope";
	private static final String STRUCTURED_PARAMETER = "structuredParameter";
	private static final String STRUCTURED = "structured";
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
	@Autowired(required = false)
	private List<MITREidDataServiceExtension> extensions = Collections.emptyList();

	private MITREidDataServiceMaps maps = new MITREidDataServiceMaps();

	private static final String THIS_VERSION = MITREID_CONNECT_1_2;

	@Override
	public boolean supportsVersion(String version) {
		return THIS_VERSION.equals(version);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataService#export(com.google.gson.stream.JsonWriter)
	 */
	@Override
	public void exportData(JsonWriter writer) throws IOException {

		throw new UnsupportedOperationException("Can not export 1.2 format from this version.");
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
						for (MITREidDataServiceExtension extension : extensions) {
							if (extension.supportsVersion(THIS_VERSION)) {
								extension.importExtensionData(name, reader);
								break;
							}
						}
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
						} else if (name.equals(STRUCTURED)) {
							logger.warn("Found a structured scope, ignoring structure");
						} else if (name.equals(STRUCTURED_PARAMETER)) {
							logger.warn("Found a structured scope, ignoring structure");
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
		logger.info("Done fixing object references.");
	}

}
