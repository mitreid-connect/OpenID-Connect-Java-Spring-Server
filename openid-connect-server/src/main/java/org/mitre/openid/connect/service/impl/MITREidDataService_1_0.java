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
package org.mitre.openid.connect.service.impl;

import static org.mitre.util.JsonUtils.readMap;
import static org.mitre.util.JsonUtils.readSet;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTParser;
/**
 *
 * Data service to import MITREid 1.0 configuration.
 *
 * @author jricher
 * @author arielak
 */
@Service
@SuppressWarnings(value = {"unchecked"})
public class MITREidDataService_1_0 extends MITREidDataServiceSupport implements MITREidDataService {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(MITREidDataService_1_0.class);
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

	private static final String THIS_VERSION = MITREID_CONNECT_1_0;

	@Override
	public boolean supportsVersion(String version) {
		return THIS_VERSION.equals(version);
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataService#export(com.google.gson.stream.JsonWriter)
	 */

	@Override
	public void exportData(JsonWriter writer) throws IOException {
		throw new UnsupportedOperationException("Can not export 1.0 format from this version.");
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataService#importData(com.google.gson.stream.JsonReader)
	 */
	@Override
	public void importData(JsonReader reader) throws IOException {

		logger.info("Reading configuration for 1.0");

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
								if (extension.supportsVersion(THIS_VERSION)) {
									extension.importExtensionData(name, reader);
									break;
								}
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
					continue;			}
		}
		fixObjectReferences(ho);
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
			String currentUuid = null;
			String clientId = null;
			String authHolderUuid = null;
			while (reader.hasNext()) {
				switch (reader.peek()) {
					case END_OBJECT:
						continue;
					case NAME:
						String name = reader.nextName();
						if (reader.peek() == JsonToken.NULL) {
							reader.skipValue();
						} else if (name.equals("uuid")) {
							currentUuid = reader.nextString();
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
						} else if (name.equals("authenticationHolderUuid")) {
							authHolderUuid = reader.nextString();
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
			String newUuid = tokenRepository.saveRefreshToken(token).getUuid();
			maps.getRefreshTokenToClientRefs().put(currentUuid, clientId);
			maps.getRefreshTokenToAuthHolderRefs().put(currentUuid, authHolderUuid);
			maps.getRefreshTokenOldToNewIdMap().put(currentUuid, newUuid);
			logger.debug("Read refresh token {}", currentUuid);
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
			String currentUuid = null;
			String clientId = null;
			String authHolderUuid = null;
			String refreshTokenUuid = null;
			while (reader.hasNext()) {
				switch (reader.peek()) {
					case END_OBJECT:
						continue;
					case NAME:
						String name = reader.nextName();
						if (reader.peek() == JsonToken.NULL) {
							reader.skipValue();
						} else if (name.equals("uiid")) {
							currentUuid = reader.nextString();
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
						} else if (name.equals("authenticationHolderUuid")) {
							authHolderUuid = reader.nextString();
						} else if (name.equals("refreshTokenUuid")) {
							refreshTokenUuid = reader.nextString();
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
			String newUuid = tokenRepository.saveAccessToken(token).getUuid();
			maps.getAccessTokenToClientRefs().put(currentUuid, clientId);
			maps.getAccessTokenToAuthHolderRefs().put(currentUuid, authHolderUuid);
			if (refreshTokenUuid != null) {
				maps.getAccessTokenToRefreshTokenRefs().put(currentUuid, refreshTokenUuid);
			}
			maps.getAccessTokenOldToNewIdMap().put(currentUuid, newUuid);
			logger.debug("Read access token {}", currentUuid);
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
			String currentUuid = null;
			while (reader.hasNext()) {
				switch (reader.peek()) {
					case END_OBJECT:
						continue;
					case NAME:
						String name = reader.nextName();
						if (reader.peek() == JsonToken.NULL) {
							reader.skipValue();
						} else if (name.equals("uuid")) {
							currentUuid = reader.nextString();
						} else if (name.equals("ownerId")) {
							//not needed
							reader.skipValue();
						} else if (name.equals("authentication")) {
							OAuth2Request clientAuthorization = null;
							Authentication userAuthentication = null;
							reader.beginObject();
							while (reader.hasNext()) {
								switch (reader.peek()) {
									case END_OBJECT:
										continue;
									case NAME:
										String subName = reader.nextName();
										if (reader.peek() == JsonToken.NULL) {
											reader.skipValue();
										} else if (subName.equals("clientAuthorization")) {
											clientAuthorization = readAuthorizationRequest(reader);
										} else if (subName.equals("userAuthentication")) {
											// skip binary encoded version
											reader.skipValue();

										} else if (subName.equals("savedUserAuthentication")) {
											userAuthentication = readSavedUserAuthentication(reader);

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
							OAuth2Authentication auth = new OAuth2Authentication(clientAuthorization, userAuthentication);
							ahe.setAuthentication(auth);
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
			String newUuid = authHolderRepository.save(ahe).getUuid();
			maps.getAuthHolderOldToNewIdMap().put(currentUuid, newUuid);
			logger.debug("Read authentication holder {}", currentUuid);
		}
		reader.endArray();
		logger.info("Done reading authentication holders");
	}

	//used by readAuthenticationHolders
	private OAuth2Request readAuthorizationRequest(JsonReader reader) throws IOException {
		Set<String> scope = new LinkedHashSet<>();
		Set<String> resourceIds = new HashSet<>();
		boolean approved = false;
		Collection<GrantedAuthority> authorities = new HashSet<>();
		Map<String, String> authorizationParameters = new HashMap<>();
		Set<String> responseTypes = new HashSet<>();
		String redirectUri = null;
		String clientId = null;
		reader.beginObject();
		while (reader.hasNext()) {
			switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals("authorizationParameters")) {
						authorizationParameters = readMap(reader);
					} else if (name.equals("approvalParameters")) {
						reader.skipValue();
					} else if (name.equals("clientId")) {
						clientId = reader.nextString();
					} else if (name.equals("scope")) {
						scope = readSet(reader);
					} else if (name.equals("resourceIds")) {
						resourceIds = readSet(reader);
					} else if (name.equals("authorities")) {
						Set<String> authorityStrs = readSet(reader);
						authorities = new HashSet<>();
						for (String s : authorityStrs) {
							GrantedAuthority ga = new SimpleGrantedAuthority(s);
							authorities.add(ga);
						}
					} else if (name.equals("approved")) {
						approved = reader.nextBoolean();
					} else if (name.equals("denied")) {
						if (approved == false) {
							approved = !reader.nextBoolean();
						}
					} else if (name.equals("redirectUri")) {
						redirectUri = reader.nextString();
					} else if (name.equals("responseTypes")) {
						responseTypes = readSet(reader);
					} else {
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
		return new OAuth2Request(authorizationParameters, clientId, authorities, approved, scope, resourceIds, redirectUri, responseTypes, null);
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

	/**
	 * @param reader
	 * @throws IOException
	 */
	private void readGrants(JsonReader reader) throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			ApprovedSite site = new ApprovedSite();
			String currentUuid = null;
			Set<String> tokenIds = null;
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
					case END_OBJECT:
						continue;
					case NAME:
						String name = reader.nextName();
						if (reader.peek() == JsonToken.NULL) {
							reader.skipValue();
						} else if (name.equals("uuid")) {
							currentUuid = reader.nextString();
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
			String newUuid = approvedSiteRepository.save(site).getUuid();
			maps.getGrantOldToNewIdMap().put(currentUuid, newUuid);

			if (tokenIds != null) {
				maps.getGrantToAccessTokensRefs().put(currentUuid, tokenIds);
			}
			logger.debug("Read grant {}", currentUuid);
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
			String currentUuid = null;
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
					case END_OBJECT:
						continue;
					case NAME:
						String name = reader.nextName();
						if (name.equals("id")) {
							currentUuid = reader.nextString();
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
			String newUuid = wlSiteRepository.save(wlSite).getUuid();
			maps.getWhitelistedSiteOldToNewIdMap().put(currentUuid, newUuid);
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
							Set<GrantedAuthority> authorities = new HashSet<>();
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
							HashSet<String> postLogoutUris = Sets.newHashSet(reader.nextString());
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
						} else if (name.equals("allowDynReg")) {
							// previously "allowDynReg" scopes are now tagged as "not restricted" and vice versa
							scope.setRestricted(!reader.nextBoolean());
						} else if (name.equals("defaultScope")) {
							scope.setDefaultScope(reader.nextBoolean());
						} else if (name.equals("icon")) {
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

	private void fixObjectReferences(String hostUuid) {
		for (String oldRefreshTokenId : maps.getRefreshTokenToClientRefs().keySet()) {
			String clientRef = maps.getRefreshTokenToClientRefs().get(oldRefreshTokenId);
			ClientDetailsEntity client = clientRepository.getClientByClientId(hostUuid, clientRef);
			String newRefreshTokenId = maps.getRefreshTokenOldToNewIdMap().get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			refreshToken.setClient(client);
			tokenRepository.saveRefreshToken(refreshToken);
		}
		for (String oldRefreshTokenId : maps.getRefreshTokenToAuthHolderRefs().keySet()) {
			String oldAuthHolderId = maps.getRefreshTokenToAuthHolderRefs().get(oldRefreshTokenId);
			String newAuthHolderId = maps.getAuthHolderOldToNewIdMap().get(oldAuthHolderId);
			AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
			String newRefreshTokenId = maps.getRefreshTokenOldToNewIdMap().get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			refreshToken.setAuthenticationHolder(authHolder);
			tokenRepository.saveRefreshToken(refreshToken);
		}
		for (String oldAccessTokenId : maps.getAccessTokenToClientRefs().keySet()) {
			String clientRef = maps.getAccessTokenToClientRefs().get(oldAccessTokenId);
			ClientDetailsEntity client = clientRepository.getClientByClientId(hostUuid, clientRef);
			String newAccessTokenId = maps.getAccessTokenOldToNewIdMap().get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setClient(client);
			tokenRepository.saveAccessToken(accessToken);
		}
		for (String oldAccessTokenId : maps.getAccessTokenToAuthHolderRefs().keySet()) {
			String oldAuthHolderId = maps.getAccessTokenToAuthHolderRefs().get(oldAccessTokenId);
			String newAuthHolderId = maps.getAuthHolderOldToNewIdMap().get(oldAuthHolderId);
			AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
			String newAccessTokenId = maps.getAccessTokenOldToNewIdMap().get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setAuthenticationHolder(authHolder);
			tokenRepository.saveAccessToken(accessToken);
		}
		maps.getAccessTokenToAuthHolderRefs().clear();
		for (String oldAccessTokenId : maps.getAccessTokenToRefreshTokenRefs().keySet()) {
			String oldRefreshTokenId = maps.getAccessTokenToRefreshTokenRefs().get(oldAccessTokenId);
			String newRefreshTokenId = maps.getRefreshTokenOldToNewIdMap().get(oldRefreshTokenId);
			OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
			String newAccessTokenId = maps.getAccessTokenOldToNewIdMap().get(oldAccessTokenId);
			OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
			accessToken.setRefreshToken(refreshToken);
			tokenRepository.saveAccessToken(accessToken);
		}
		for (String oldGrantId : maps.getGrantToAccessTokensRefs().keySet()) {
			Set<String> oldAccessTokenIds = maps.getGrantToAccessTokensRefs().get(oldGrantId);

			String newGrantId = maps.getGrantOldToNewIdMap().get(oldGrantId);
			ApprovedSite site = approvedSiteRepository.getById(newGrantId);

			for(String oldTokenId : oldAccessTokenIds) {
				String newTokenId = maps.getAccessTokenOldToNewIdMap().get(oldTokenId);
				OAuth2AccessTokenEntity token = tokenRepository.getAccessTokenById(newTokenId);
				token.setApprovedSite(site);
				tokenRepository.saveAccessToken(token);
			}

			approvedSiteRepository.save(site);
		}
	}

}
