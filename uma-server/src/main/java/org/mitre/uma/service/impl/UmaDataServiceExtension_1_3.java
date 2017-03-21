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

import static org.mitre.util.JsonUtils.readSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.mitre.openid.connect.service.MITREidDataService;
import org.mitre.openid.connect.service.MITREidDataServiceExtension;
import org.mitre.openid.connect.service.MITREidDataServiceMaps;
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
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author jricher
 *
 */
@Service("umaDataExtension_1_3")
public class UmaDataServiceExtension_1_3 extends MITREidDataServiceSupport implements MITREidDataServiceExtension {

	private static final String THIS_VERSION = MITREidDataService.MITREID_CONNECT_1_3;

	private static final String REGISTERED_CLIENT = "registeredClient";
	private static final String URI = "uri";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String VALUE = "value";
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
	private static final String TOKEN_PERMISSIONS = "tokenPermissions";
	private static final String TOKEN_ID = "tokenId";

	private static final Logger logger = LoggerFactory.getLogger(UmaDataServiceExtension_1_3.class);



	@Autowired
	private SavedRegisteredClientService registeredClientService;
	@Autowired
	private ResourceSetRepository resourceSetRepository;
	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private OAuth2TokenRepository tokenRepository;

	private Map<Long, Set<Long>> tokenToPermissionRefs = new HashMap<>();

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

		writer.name(TOKEN_PERMISSIONS);
		writer.beginArray();
		writeTokenPermissions(writer);
		writer.endArray();
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	private void writeTokenPermissions(JsonWriter writer) throws IOException {
		for (OAuth2AccessTokenEntity token : tokenRepository.getAllAccessTokens()) {
			if (!token.getPermissions().isEmpty()) { // skip tokens that don't have the permissions structure attached
				writer.beginObject();
				writer.name(TOKEN_ID).value(token.getId());
				writer.name(PERMISSIONS);
				writer.beginArray();
				for (Permission p : token.getPermissions()) {
					writer.beginObject();
					writer.name(RESOURCE_SET).value(p.getResourceSet().getId());
					writer.name(SCOPES);
					writer.beginArray();
					for (String s : p.getScopes()) {
						writer.value(s);
					}
					writer.endArray();
					writer.endObject();
				}
				writer.endArray();

				writer.endObject();
			}
		}
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
		} else if (name.equals(TOKEN_PERMISSIONS)) {
			readTokenPermissions(reader);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param reader
	 */
	private void readTokenPermissions(JsonReader reader) throws IOException {
		reader.beginArray();
		while(reader.hasNext()) {
			reader.beginObject();
			Long tokenId = null;
			Set<Long> permissions = new HashSet<>();
			while (reader.hasNext()) {
				switch(reader.peek()) {
					case END_OBJECT:
						continue;
					case NAME:
						String name = reader.nextName();
						if (name.equals(TOKEN_ID)) {
							tokenId = reader.nextLong();
						} else if (name.equals(PERMISSIONS)) {
							reader.beginArray();
							while (reader.hasNext()) {
								Permission p = new Permission();
								Long rsid = null;
								Set<String> scope = new HashSet<>();
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
												scope = readSet(reader);
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
								p.setScopes(scope);
								Permission saved = permissionRepository.saveRawPermission(p);
								permissionToResourceRefs.put(saved.getId(), rsid);
								permissions.add(saved.getId());
							}
							reader.endArray();
						}
						break;
					default:
						logger.debug("Found unexpected entry");
						reader.skipValue();
						continue;
				}
			}
			reader.endObject();
			tokenToPermissionRefs.put(tokenId, permissions);
		}
		reader.endArray();

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
	public void fixExtensionObjectReferences(MITREidDataServiceMaps maps) {
		for (Long permissionId : permissionToResourceRefs.keySet()) {
			Long oldResourceId = permissionToResourceRefs.get(permissionId);
			Long newResourceId = resourceSetOldToNewIdMap.get(oldResourceId);
			Permission p = permissionRepository.getById(permissionId);
			ResourceSet rs = resourceSetRepository.getById(newResourceId);
			p.setResourceSet(rs);
			permissionRepository.saveRawPermission(p);
			logger.debug("Mapping rsid " + oldResourceId + " to " + newResourceId + " for permission " + permissionId);
		}
		for (Long tokenId : tokenToPermissionRefs.keySet()) {
			Long newTokenId = maps.getAccessTokenOldToNewIdMap().get(tokenId);
			OAuth2AccessTokenEntity token = tokenRepository.getAccessTokenById(newTokenId);

			Set<Permission> permissions = new HashSet<>();
			for (Long permissionId : tokenToPermissionRefs.get(tokenId)) {
				Permission p = permissionRepository.getById(permissionId);
				permissions.add(p);
			}

			token.setPermissions(permissions);
			tokenRepository.saveAccessToken(token);
		}
		permissionToResourceRefs.clear();
		resourceSetOldToNewIdMap.clear();
		tokenToPermissionRefs.clear();
	}

}
