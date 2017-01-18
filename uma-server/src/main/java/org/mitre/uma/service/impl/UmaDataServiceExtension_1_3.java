/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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

package org.mitre.uma.service.impl;

import static org.mitre.util.JsonUtils.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.mitre.openid.connect.service.MITREidDataService;
import org.mitre.openid.connect.service.MITREidDataServiceExtension;
import org.mitre.openid.connect.service.impl.MITREidDataServiceSupport;
import org.mitre.uma.model.Claim;
import org.mitre.uma.model.Permission;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.Policy;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.model.SavedRegisteredClient;
import org.mitre.uma.repository.PermissionRepository;
import org.mitre.uma.repository.ResourceSetRepository;
import org.mitre.uma.service.SavedRegisteredClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author jricher
 *
 */
public class UmaDataServiceExtension_1_3 extends MITREidDataServiceSupport implements MITREidDataServiceExtension {
	
	private static final String THIS_VERSION = MITREidDataService.MITREID_CONNECT_1_3;

	private static final String REGISTERED_CLIENT = "registeredClient";
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
	private static final String ID_TOKEN_ID = "idTokenId";
	private static final String REFRESH_TOKEN_ID = "refreshTokenId";
	private static final String VALUE = "value";
	private static final String AUTHENTICATION_HOLDER_ID = "authenticationHolderId";
	private static final String CLIENT_ID = "clientId";
	private static final String EXPIRATION = "expiration";
	private static final String ID = "id";
	private static final String ICON_URI = "iconUri";
	private static final String OWNER = "owner";
	private static final String POLICIES = "policies";
	private static final String SCOPES = "scopes";
	private static final String CLAIMS_REQUIRED = "claimsRequired";
	private static final String ISSUER = "issuer";
	private static final String CLAIM_TOKEN_FORMAT = "claimTokenFormat";
	private static final String CLAIM_TYPE = "claimType";
	private static final String FRIENDLY_NAME = "friendlyName";
	private static final String PERMISSIONS = "permissions";
	private static final String RESOURCE_SET = "resourceSet";
	private static final String PERMISSION_TICKETS = "permissionTickets";
	private static final String PERMISSION = "permission";
	private static final String TICKET = "ticket";
	private static final String CLAIMS_SUPPLIED = "claimsSupplied";
	private static final String SAVED_REGISTERED_CLIENTS = "savedRegisteredClients";
	private static final String RESOURCE_SETS = "resourceSets";

	private static final Logger logger = LoggerFactory.getLogger(UmaDataServiceExtension_1_3.class);
	
	@Autowired
	private SavedRegisteredClientService registeredClientService;
	@Autowired
	private ResourceSetRepository resourceSetRepository;
	@Autowired
	private PermissionRepository permissionRepository;

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataServiceExtension#supportsVersion(java.lang.String)
	 */
	@Override
	public boolean supportsVersion(String version) {
		return THIS_VERSION.equals(version);

	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataServiceExtension#exportExtensionData(com.google.gson.stream.JsonWriter)
	 */
	@Override
	public void exportExtensionData(JsonWriter writer) throws IOException {
		writer.name(SAVED_REGISTERED_CLIENTS);
		writer.beginArray();
		writeSavedRegisteredClients(writer);
		writer.endArray();

		writer.name(RESOURCE_SETS);
		writer.beginArray();
		writeResourceSets(writer);
		writer.endArray();

		writer.name(PERMISSION_TICKETS);
		writer.beginArray();
		writePermissionTickets(writer);
		writer.endArray();
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	private void writePermissionTickets(JsonWriter writer) throws IOException {
		for (PermissionTicket ticket : permissionRepository.getAll()) {
			writer.beginObject();

			writer.name(CLAIMS_SUPPLIED);
			writer.beginArray();
			for (Claim claim : ticket.getClaimsSupplied()) {
				writer.beginObject();

				writer.name(ISSUER);
				writer.beginArray();
				for (String issuer : claim.getIssuer()) {
					writer.value(issuer);
				}
				writer.endArray();
				writer.name(CLAIM_TOKEN_FORMAT);
				writer.beginArray();
				for (String format : claim.getClaimTokenFormat()) {
					writer.value(format);
				}
				writer.endArray();
				writer.name(CLAIM_TYPE).value(claim.getClaimType());
				writer.name(FRIENDLY_NAME).value(claim.getFriendlyName());
				writer.name(NAME).value(claim.getName());
				writer.name(VALUE).value(claim.getValue().toString());
				writer.endObject();
			}
			writer.endArray();

			writer.name(EXPIRATION).value(toUTCString(ticket.getExpiration()));

			writer.name(PERMISSION);
			writer.beginObject();
			Permission p = ticket.getPermission();
			writer.name(RESOURCE_SET).value(p.getResourceSet().getId());
			writer.name(SCOPES);
			writer.beginArray();
			for (String s : p.getScopes()) {
				writer.value(s);
			}
			writer.endArray();
			writer.endObject();

			writer.name(TICKET).value(ticket.getTicket());

			writer.endObject();
		}


	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	private void writeResourceSets(JsonWriter writer) throws IOException {
		for (ResourceSet rs : resourceSetRepository.getAll()) {
			writer.beginObject();
			writer.name(ID).value(rs.getId());
			writer.name(CLIENT_ID).value(rs.getClientId());
			writer.name(ICON_URI).value(rs.getIconUri());
			writer.name(NAME).value(rs.getName());
			writer.name(TYPE).value(rs.getType());
			writer.name(URI).value(rs.getUri());
			writer.name(OWNER).value(rs.getOwner());
			writer.name(POLICIES);
			writer.beginArray();
			for (Policy policy : rs.getPolicies()) {
				writer.beginObject();
				writer.name(NAME).value(policy.getName());
				writer.name(SCOPES);
				writer.beginArray();
				for (String scope : policy.getScopes()) {
					writer.value(scope);
				}
				writer.endArray();
				writer.name(CLAIMS_REQUIRED);
				writer.beginArray();
				for (Claim claim : policy.getClaimsRequired()) {
					writer.beginObject();

					writer.name(ISSUER);
					writer.beginArray();
					for (String issuer : claim.getIssuer()) {
						writer.value(issuer);
					}
					writer.endArray();
					writer.name(CLAIM_TOKEN_FORMAT);
					writer.beginArray();
					for (String format : claim.getClaimTokenFormat()) {
						writer.value(format);
					}
					writer.endArray();
					writer.name(CLAIM_TYPE).value(claim.getClaimType());
					writer.name(FRIENDLY_NAME).value(claim.getFriendlyName());
					writer.name(NAME).value(claim.getName());
					writer.name(VALUE).value(claim.getValue().toString());
					writer.endObject();
				}
				writer.endArray();
				writer.endObject();
			}
			writer.endArray();
			writer.name(SCOPES);
			writer.beginArray();
			for (String scope : rs.getScopes()) {
				writer.value(scope);
			}
			writer.endArray();
			writer.endObject();
			logger.debug("Finished writing resource set {}", rs.getId());
		}

	}

	/**
	 * @param writer
	 */
	private void writeSavedRegisteredClients(JsonWriter writer) throws IOException {
		for (SavedRegisteredClient src : registeredClientService.getAll()) {
			writer.beginObject();
			writer.name(ISSUER).value(src.getIssuer());
			writer.name(REGISTERED_CLIENT).value(src.getRegisteredClient().getSource().toString());
			writer.endObject();
			logger.debug("Wrote saved registered client {}", src.getId());
		}
		logger.info("Done writing saved registered clients");
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataServiceExtension#importExtensionData(com.google.gson.stream.JsonReader)
	 */
	@Override
	public boolean importExtensionData(String name, JsonReader reader) throws IOException {
		if (name.equals(SAVED_REGISTERED_CLIENTS)) {
			readSavedRegisteredClients(reader);
			return true;
		} else if (name.equals(RESOURCE_SETS)) {
			readResourceSets(reader);
			return true;
		} else if (name.equals(PERMISSION_TICKETS)) {
			readPermissionTickets(reader);
			return true;
		} else {
			return false;
		}
	}
	
	private Map<Long, Long> permissionToResourceRefs = new HashMap<>();

	/**
	 * @param reader
	 */
	private void readPermissionTickets(JsonReader reader) throws IOException {
		JsonParser parser = new JsonParser();
		reader.beginArray();
		while (reader.hasNext()) {
			PermissionTicket ticket = new PermissionTicket();
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals(CLAIMS_SUPPLIED)) {
						Set<Claim> claimsSupplied = new HashSet<>();
						reader.beginArray();
						while (reader.hasNext()) {
							Claim c = new Claim();
							reader.beginObject();
							while (reader.hasNext()) {
								switch (reader.peek()) {
								case END_OBJECT:
									continue;
								case NAME:
									String cname = reader.nextName();
									if (reader.peek() == JsonToken.NULL) {
										reader.skipValue();
									} else if (cname.equals(ISSUER)) {
										c.setIssuer(readSet(reader));
									} else if (cname.equals(CLAIM_TOKEN_FORMAT)) {
										c.setClaimTokenFormat(readSet(reader));
									} else if (cname.equals(CLAIM_TYPE)) {
										c.setClaimType(reader.nextString());
									} else if (cname.equals(FRIENDLY_NAME)) {
										c.setFriendlyName(reader.nextString());
									} else if (cname.equals(NAME)) {
										c.setName(reader.nextString());
									} else if (cname.equals(VALUE)) {
										JsonElement e = parser.parse(reader.nextString());
										c.setValue(e);
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
							claimsSupplied.add(c);
						}
						reader.endArray();
						ticket.setClaimsSupplied(claimsSupplied);
					} else if (name.equals(EXPIRATION)) {
						ticket.setExpiration(utcToDate(reader.nextString()));
					} else if (name.equals(PERMISSION)) {
						Permission p = new Permission();
						Long rsid = null;
						reader.beginObject();
						while (reader.hasNext()) {
							switch (reader.peek()) {
							case END_OBJECT:
								continue;
							case NAME:
								String pname = reader.nextName();
								if (reader.peek() == JsonToken.NULL) {
									reader.skipValue();
								} else if (pname.equals(RESOURCE_SET)) {
									rsid = reader.nextLong();
								} else if (pname.equals(SCOPES)) {
									p.setScopes(readSet(reader));
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
						Permission saved = permissionRepository.saveRawPermission(p);
						permissionToResourceRefs.put(saved.getId(), rsid);
						ticket.setPermission(saved);
					} else if (name.equals(TICKET)) {
						ticket.setTicket(reader.nextString());
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
			permissionRepository.save(ticket);
		}
		reader.endArray();
	}


	private Map<Long, Long> resourceSetOldToNewIdMap = new HashMap<>();

	/**
	 * @param reader
	 */
	private void readResourceSets(JsonReader reader) throws IOException {
		JsonParser parser = new JsonParser();
		reader.beginArray();
		while (reader.hasNext()) {
			Long oldId = null;
			ResourceSet rs = new ResourceSet();
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
						oldId = reader.nextLong();
					} else if (name.equals(CLIENT_ID)) {
						rs.setClientId(reader.nextString());
					} else if (name.equals(ICON_URI)) {
						rs.setIconUri(reader.nextString());
					} else if (name.equals(NAME)) {
						rs.setName(reader.nextString());
					} else if (name.equals(TYPE)) {
						rs.setType(reader.nextString());
					} else if (name.equals(URI)) {
						rs.setUri(reader.nextString());
					} else if (name.equals(OWNER)) {
						rs.setOwner(reader.nextString());
					} else if (name.equals(POLICIES)) {
						Set<Policy> policies = new HashSet<>();
						reader.beginArray();
						while (reader.hasNext()) {
							Policy p = new Policy();
							reader.beginObject();
							while (reader.hasNext()) {
								switch (reader.peek()) {
								case END_OBJECT:
									continue;
								case NAME:
									String pname = reader.nextName();
									if (reader.peek() == JsonToken.NULL) {
										reader.skipValue();
									} else if (pname.equals(NAME)) {
										p.setName(reader.nextString());
									} else if (pname.equals(SCOPES)) {
										p.setScopes(readSet(reader));
									} else if (pname.equals(CLAIMS_REQUIRED)) {
										Set<Claim> claimsRequired = new HashSet<>();
										reader.beginArray();
										while (reader.hasNext()) {
											Claim c = new Claim();
											reader.beginObject();
											while (reader.hasNext()) {
												switch (reader.peek()) {
												case END_OBJECT:
													continue;
												case NAME:
													String cname = reader.nextName();
													if (reader.peek() == JsonToken.NULL) {
														reader.skipValue();
													} else if (cname.equals(ISSUER)) {
														c.setIssuer(readSet(reader));
													} else if (cname.equals(CLAIM_TOKEN_FORMAT)) {
														c.setClaimTokenFormat(readSet(reader));
													} else if (cname.equals(CLAIM_TYPE)) {
														c.setClaimType(reader.nextString());
													} else if (cname.equals(FRIENDLY_NAME)) {
														c.setFriendlyName(reader.nextString());
													} else if (cname.equals(NAME)) {
														c.setName(reader.nextString());
													} else if (cname.equals(VALUE)) {
														JsonElement e = parser.parse(reader.nextString());
														c.setValue(e);
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
											claimsRequired.add(c);
										}
										reader.endArray();
										p.setClaimsRequired(claimsRequired);
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
							policies.add(p);
						}
						reader.endArray();
						rs.setPolicies(policies);
					} else if (name.equals(SCOPES)) {
						rs.setScopes(readSet(reader));
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
			Long newId = resourceSetRepository.save(rs).getId();
			resourceSetOldToNewIdMap.put(oldId, newId);
		}
		reader.endArray();
		logger.info("Done reading resource sets");
	}

	/**
	 * @param reader
	 */
	private void readSavedRegisteredClients(JsonReader reader) throws IOException{
		reader.beginArray();
		while (reader.hasNext()) {
			String issuer = null;
			String clientString = null;
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.peek()) {
				case END_OBJECT:
					continue;
				case NAME:
					String name = reader.nextName();
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					} else if (name.equals(ISSUER)) {
						issuer = reader.nextString();
					} else if (name.equals(REGISTERED_CLIENT)) {
						clientString = reader.nextString();
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
			RegisteredClient client = ClientDetailsEntityJsonProcessor.parseRegistered(clientString);
			registeredClientService.save(issuer, client);
			logger.debug("Saved registered client");
		}
		reader.endArray();
		logger.info("Done reading saved registered clients");
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.MITREidDataServiceExtension#fixExtensionObjectReferences()
	 */
	@Override
	public void fixExtensionObjectReferences() {
		for (Long permissionId : permissionToResourceRefs.keySet()) {
			Long oldResourceId = permissionToResourceRefs.get(permissionId);
			Long newResourceId = resourceSetOldToNewIdMap.get(oldResourceId);
			Permission p = permissionRepository.getById(permissionId);
			ResourceSet rs = resourceSetRepository.getById(newResourceId);
			p.setResourceSet(rs);
			permissionRepository.saveRawPermission(p);
			logger.debug("Mapping rsid " + oldResourceId + " to " + newResourceId + " for permission " + permissionId);
		}
		permissionToResourceRefs.clear();
		resourceSetOldToNewIdMap.clear();
	}

}
