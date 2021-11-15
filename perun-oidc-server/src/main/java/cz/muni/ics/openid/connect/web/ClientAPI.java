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
package cz.muni.ics.openid.connect.web;

import static cz.muni.ics.oauth2.model.RegisteredClientFields.APPLICATION_TYPE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLAIMS_REDIRECT_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_ID;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_ID_ISSUED_AT;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_NAME;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_SECRET;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_SECRET_EXPIRES_AT;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CONTACTS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.DEFAULT_ACR_VALUES;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.DEFAULT_MAX_AGE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.GRANT_TYPES;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.ID_TOKEN_ENCRYPTED_RESPONSE_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.ID_TOKEN_ENCRYPTED_RESPONSE_ENC;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.ID_TOKEN_SIGNED_RESPONSE_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.INITIATE_LOGIN_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.JWKS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.JWKS_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.POLICY_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.POST_LOGOUT_REDIRECT_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REDIRECT_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REGISTRATION_ACCESS_TOKEN;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REGISTRATION_CLIENT_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REQUEST_OBJECT_SIGNING_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REQUEST_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REQUIRE_AUTH_TIME;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.RESPONSE_TYPES;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SCOPE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SECTOR_IDENTIFIER_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SOFTWARE_STATEMENT;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SUBJECT_TYPE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.TOKEN_ENDPOINT_AUTH_METHOD;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.TOKEN_ENDPOINT_AUTH_SIGNING_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.TOS_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.USERINFO_ENCRYPTED_RESPONSE_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.USERINFO_ENCRYPTED_RESPONSE_ENC;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.USERINFO_SIGNED_RESPONSE_ALG;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import cz.muni.ics.jwt.assertion.AssertionValidator;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AppType;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AuthMethod;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.SubjectType;
import cz.muni.ics.oauth2.model.PKCEAlgorithm;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.web.AuthenticationUtilities;
import cz.muni.ics.openid.connect.exception.ValidationException;
import cz.muni.ics.openid.connect.view.ClientEntityViewForAdmins;
import cz.muni.ics.openid.connect.view.ClientEntityViewForUsers;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import cz.muni.ics.openid.connect.view.JsonErrorView;
import java.lang.reflect.Type;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.util.Collection;
import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/" + ClientAPI.URL)
@PreAuthorize("hasRole('ROLE_USER')")
@Slf4j
public class ClientAPI {

	public static final String URL = RootController.API_URL + "/clients";

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	@Qualifier("clientAssertionValidator")
	private AssertionValidator assertionValidator;

	private JsonParser parser = new JsonParser();

	private Gson gson = new GsonBuilder()
			.serializeNulls()
			.registerTypeAdapter(JWSAlgorithm.class, new JsonDeserializer<Algorithm>() {
				@Override
				public JWSAlgorithm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					if (json.isJsonPrimitive()) {
						return JWSAlgorithm.parse(json.getAsString());
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(JWEAlgorithm.class, new JsonDeserializer<Algorithm>() {
				@Override
				public JWEAlgorithm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					if (json.isJsonPrimitive()) {
						return JWEAlgorithm.parse(json.getAsString());
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(EncryptionMethod.class, new JsonDeserializer<Algorithm>() {
				@Override
				public EncryptionMethod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					if (json.isJsonPrimitive()) {
						return EncryptionMethod.parse(json.getAsString());
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(JWKSet.class, new JsonDeserializer<JWKSet>() {
				@Override
				public JWKSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					if (json.isJsonObject()) {
						try {
							return JWKSet.parse(json.toString());
						} catch (ParseException e) {
							return null;
						}
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(JWT.class, new JsonDeserializer<JWT>() {
				@Override
				public JWT deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					if (json.isJsonPrimitive()) {
						try {
							return JWTParser.parse(json.getAsString());
						} catch (ParseException e) {
							return null;
						}
					} else {
						return null;
					}
				}
			})
			.registerTypeAdapter(PKCEAlgorithm.class, new JsonDeserializer<Algorithm>() {
				@Override
				public PKCEAlgorithm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					if (json.isJsonPrimitive()) {
						return PKCEAlgorithm.parse(json.getAsString());
					} else {
						return null;
					}
				}
			})
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			.create();

	/**
	 * Get a list of all clients
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiGetAllClients(Model model, Authentication auth) {

		Collection<ClientDetailsEntity> clients = clientService.getAllClients();
		model.addAttribute(JsonEntityView.ENTITY, clients);

		if (AuthenticationUtilities.isAdmin(auth)) {
			return ClientEntityViewForAdmins.VIEWNAME;
		} else {
			return ClientEntityViewForUsers.VIEWNAME;
		}
	}

	/**
	 * Create a new client
	 * @param json
	 * @param m
	 * @param principal
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiAddClient(@RequestBody String jsonString, Model m, Authentication auth) {

		JsonObject json = null;
		ClientDetailsEntity client = null;

		try {
			json = parser.parse(jsonString).getAsJsonObject();
			client = gson.fromJson(json, ClientDetailsEntity.class);
			client = validateSoftwareStatement(client);
		} catch (JsonSyntaxException e) {
			log.error("apiAddClient failed due to JsonSyntaxException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not save new client. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			log.error("apiAddClient failed due to IllegalStateException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not save new client. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (ValidationException e) {
			log.error("apiUpdateClient failed due to ValidationException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The server encountered a ValidationException.");
			return JsonErrorView.VIEWNAME;
		}

		// if they leave the client identifier empty, force it to be generated
		if (Strings.isNullOrEmpty(client.getClientId())) {
			client = clientService.generateClientId(client);
		}

		if (client.getTokenEndpointAuthMethod() == null ||
				client.getTokenEndpointAuthMethod().equals(AuthMethod.NONE)) {
			// we shouldn't have a secret for this client

			client.setClientSecret(null);

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_BASIC)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_POST)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT)) {

			// if they've asked for us to generate a client secret (or they left it blank but require one), do so here
			if (json.has("generateClientSecret") && json.get("generateClientSecret").getAsBoolean()
					|| Strings.isNullOrEmpty(client.getClientSecret())) {
				client = clientService.generateClientSecret(client);
			}

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY)) {

			if (Strings.isNullOrEmpty(client.getJwksUri()) && client.getJwks() == null) {
				log.error("tried to create client with private key auth but no private key");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Can not create a client with private key authentication without registering a key via the JWK Set URI or JWK Set Value.");
				return JsonErrorView.VIEWNAME;
			}

			// otherwise we shouldn't have a secret for this client
			client.setClientSecret(null);

		} else {

			log.error("unknown auth method");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unknown auth method requested");
			return JsonErrorView.VIEWNAME;


		}

		client.setDynamicallyRegistered(false);

		try {
			ClientDetailsEntity newClient = clientService.saveNewClient(client);
			m.addAttribute(JsonEntityView.ENTITY, newClient);

			if (AuthenticationUtilities.isAdmin(auth)) {
				return ClientEntityViewForAdmins.VIEWNAME;
			} else {
				return ClientEntityViewForUsers.VIEWNAME;
			}
		} catch (IllegalArgumentException e) {
			log.error("Unable to save client: {}", e.getMessage());
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client: " + e.getMessage());
			return JsonErrorView.VIEWNAME;
		} catch (PersistenceException e) {
			Throwable cause = e.getCause();
			if (cause instanceof DatabaseException) {
				Throwable databaseExceptionCause = cause.getCause();
				if(databaseExceptionCause instanceof SQLIntegrityConstraintViolationException) {
					log.error("apiAddClient failed; duplicate client id entry found: {}", client.getClientId());
					m.addAttribute(HttpCodeView.CODE, HttpStatus.CONFLICT);
					m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client. Duplicate client id entry found: " + client.getClientId());
					return JsonErrorView.VIEWNAME;
				}
			}
			throw e;
		}
	}

	/**
	 * Update an existing client
	 * @param id
	 * @param jsonString
	 * @param m
	 * @param principal
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiUpdateClient(@PathVariable("id") Long id, @RequestBody String jsonString, Model m, Authentication auth) {

		JsonObject json = null;
		ClientDetailsEntity client = null;

		try {
			// parse the client passed in (from JSON) and fetch the old client from the store
			json = parser.parse(jsonString).getAsJsonObject();
			client = gson.fromJson(json, ClientDetailsEntity.class);
			client = validateSoftwareStatement(client);
		} catch (JsonSyntaxException e) {
			log.error("apiUpdateClient failed due to JsonSyntaxException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The server encountered a JSON syntax exception. Contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (IllegalStateException e) {
			log.error("apiUpdateClient failed due to IllegalStateException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The server encountered an IllegalStateException. Refresh and try again - if the problem persists, contact a system administrator for assistance.");
			return JsonErrorView.VIEWNAME;
		} catch (ValidationException e) {
			log.error("apiUpdateClient failed due to ValidationException", e);
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The server encountered a ValidationException.");
			return JsonErrorView.VIEWNAME;
		}

		ClientDetailsEntity oldClient = clientService.getClientById(id);

		if (oldClient == null) {
			log.error("apiUpdateClient failed; client with id " + id + " could not be found.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Could not update client. The requested client with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		}

		// if they leave the client identifier empty, force it to be generated
		if (Strings.isNullOrEmpty(client.getClientId())) {
			client = clientService.generateClientId(client);
		}

		if (client.getTokenEndpointAuthMethod() == null ||
				client.getTokenEndpointAuthMethod().equals(AuthMethod.NONE)) {
			// we shouldn't have a secret for this client

			client.setClientSecret(null);

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_BASIC)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_POST)
				|| client.getTokenEndpointAuthMethod().equals(AuthMethod.SECRET_JWT)) {

			// if they've asked for us to generate a client secret (or they left it blank but require one), do so here
			if (json.has("generateClientSecret") && json.get("generateClientSecret").getAsBoolean()
					|| Strings.isNullOrEmpty(client.getClientSecret())) {
				client = clientService.generateClientSecret(client);
			}

		} else if (client.getTokenEndpointAuthMethod().equals(AuthMethod.PRIVATE_KEY)) {

			if (Strings.isNullOrEmpty(client.getJwksUri()) && client.getJwks() == null) {
				log.error("tried to create client with private key auth but no private key");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Can not create a client with private key authentication without registering a key via the JWK Set URI or JWK Set Value.");
				return JsonErrorView.VIEWNAME;
			}

			// otherwise we shouldn't have a secret for this client
			client.setClientSecret(null);

		} else {

			log.error("unknown auth method");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unknown auth method requested");
			return JsonErrorView.VIEWNAME;


		}

		try {
			ClientDetailsEntity newClient = clientService.updateClient(oldClient, client);
			m.addAttribute(JsonEntityView.ENTITY, newClient);

			if (AuthenticationUtilities.isAdmin(auth)) {
				return ClientEntityViewForAdmins.VIEWNAME;
			} else {
				return ClientEntityViewForUsers.VIEWNAME;
			}
		} catch (IllegalArgumentException e) {
			log.error("Unable to save client: {}", e.getMessage());
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client: " + e.getMessage());
			return JsonErrorView.VIEWNAME;
		}
	}

	/**
	 * Delete a client
	 * @param id
	 * @param modelAndView
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public String apiDeleteClient(@PathVariable("id") Long id, ModelAndView modelAndView) {

		ClientDetailsEntity client = clientService.getClientById(id);

		if (client == null) {
			log.error("apiDeleteClient failed; client with id " + id + " could not be found.");
			modelAndView.getModelMap().put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			modelAndView.getModelMap().put(JsonErrorView.ERROR_MESSAGE, "Could not delete client. The requested client with id " + id + "could not be found.");
			return JsonErrorView.VIEWNAME;
		} else {
			modelAndView.getModelMap().put(HttpCodeView.CODE, HttpStatus.OK);
			clientService.deleteClient(client);
		}

		return HttpCodeView.VIEWNAME;
	}


	/**
	 * Get an individual client
	 * @param id
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiShowClient(@PathVariable("id") Long id, Model model, Authentication auth) {

		ClientDetailsEntity client = clientService.getClientById(id);

		if (client == null) {
			log.error("apiShowClient failed; client with id " + id + " could not be found.");
			model.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			model.addAttribute(JsonErrorView.ERROR_MESSAGE, "The requested client with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}

		model.addAttribute(JsonEntityView.ENTITY, client);

		if (AuthenticationUtilities.isAdmin(auth)) {
			return ClientEntityViewForAdmins.VIEWNAME;
		} else {
			return ClientEntityViewForUsers.VIEWNAME;
		}
	}

	private ClientDetailsEntity validateSoftwareStatement(ClientDetailsEntity newClient) throws ValidationException {
		if (newClient.getSoftwareStatement() != null) {
			if (assertionValidator.isValid(newClient.getSoftwareStatement())) {
				// we have a software statement and its envelope passed all the checks from our validator

				// swap out all of the client's fields for the associated parts of the software statement
				try {
					JWTClaimsSet claimSet = newClient.getSoftwareStatement().getJWTClaimsSet();
					for (String claim : claimSet.getClaims().keySet()) {
						switch (claim) {
							case SOFTWARE_STATEMENT:
								throw new ValidationException("invalid_client_metadata", "Software statement can't include another software statement", HttpStatus.BAD_REQUEST);
							case CLAIMS_REDIRECT_URIS:
								newClient.setClaimsRedirectUris(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case CLIENT_SECRET_EXPIRES_AT:
								throw new ValidationException("invalid_client_metadata", "Software statement can't include a client secret expiration time", HttpStatus.BAD_REQUEST);
							case CLIENT_ID_ISSUED_AT:
								throw new ValidationException("invalid_client_metadata", "Software statement can't include a client ID issuance time", HttpStatus.BAD_REQUEST);
							case REGISTRATION_CLIENT_URI:
								throw new ValidationException("invalid_client_metadata", "Software statement can't include a client configuration endpoint", HttpStatus.BAD_REQUEST);
							case REGISTRATION_ACCESS_TOKEN:
								throw new ValidationException("invalid_client_metadata", "Software statement can't include a client registration access token", HttpStatus.BAD_REQUEST);
							case REQUEST_URIS:
								newClient.setRequestUris(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case POST_LOGOUT_REDIRECT_URIS:
								newClient.setPostLogoutRedirectUris(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case INITIATE_LOGIN_URI:
								newClient.setInitiateLoginUri(claimSet.getStringClaim(claim));
								break;
							case DEFAULT_ACR_VALUES:
								newClient.setDefaultACRvalues(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case REQUIRE_AUTH_TIME:
								newClient.setRequireAuthTime(claimSet.getBooleanClaim(claim));
								break;
							case DEFAULT_MAX_AGE:
								newClient.setDefaultMaxAge(claimSet.getIntegerClaim(claim));
								break;
							case TOKEN_ENDPOINT_AUTH_SIGNING_ALG:
								newClient.setTokenEndpointAuthSigningAlg(JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
								break;
							case ID_TOKEN_ENCRYPTED_RESPONSE_ENC:
								newClient.setIdTokenEncryptedResponseEnc(EncryptionMethod.parse(claimSet.getStringClaim(claim)));
								break;
							case ID_TOKEN_ENCRYPTED_RESPONSE_ALG:
								newClient.setIdTokenEncryptedResponseAlg(JWEAlgorithm.parse(claimSet.getStringClaim(claim)));
								break;
							case ID_TOKEN_SIGNED_RESPONSE_ALG:
								newClient.setIdTokenSignedResponseAlg(JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
								break;
							case USERINFO_ENCRYPTED_RESPONSE_ENC:
								newClient.setUserInfoEncryptedResponseEnc(EncryptionMethod.parse(claimSet.getStringClaim(claim)));
								break;
							case USERINFO_ENCRYPTED_RESPONSE_ALG:
								newClient.setUserInfoEncryptedResponseAlg(JWEAlgorithm.parse(claimSet.getStringClaim(claim)));
								break;
							case USERINFO_SIGNED_RESPONSE_ALG:
								newClient.setUserInfoSignedResponseAlg(JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
								break;
							case REQUEST_OBJECT_SIGNING_ALG:
								newClient.setRequestObjectSigningAlg(JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
								break;
							case SUBJECT_TYPE:
								newClient.setSubjectType(SubjectType.getByValue(claimSet.getStringClaim(claim)));
								break;
							case SECTOR_IDENTIFIER_URI:
								newClient.setSectorIdentifierUri(claimSet.getStringClaim(claim));
								break;
							case APPLICATION_TYPE:
								newClient.setApplicationType(AppType.getByValue(claimSet.getStringClaim(claim)));
								break;
							case JWKS_URI:
								newClient.setJwksUri(claimSet.getStringClaim(claim));
								break;
							case JWKS:
								newClient.setJwks(JWKSet.parse(claimSet.getJSONObjectClaim(claim).toJSONString()));
								break;
							case POLICY_URI:
								newClient.setPolicyUri(claimSet.getStringClaim(claim));
								break;
							case RESPONSE_TYPES:
								newClient.setResponseTypes(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case GRANT_TYPES:
								newClient.setGrantTypes(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case SCOPE:
								newClient.setScope(OAuth2Utils.parseParameterList(claimSet.getStringClaim(claim)));
								break;
							case TOKEN_ENDPOINT_AUTH_METHOD:
								newClient.setTokenEndpointAuthMethod(AuthMethod.getByValue(claimSet.getStringClaim(claim)));
								break;
							case TOS_URI:
								newClient.setTosUri(claimSet.getStringClaim(claim));
								break;
							case CONTACTS:
								newClient.setContacts(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case CLIENT_URI:
								newClient.setClientUri(claimSet.getStringClaim(claim));
								break;
							case CLIENT_NAME:
								newClient.setClientName(claimSet.getStringClaim(claim));
								break;
							case REDIRECT_URIS:
								newClient.setRedirectUris(Sets.newHashSet(claimSet.getStringListClaim(claim)));
								break;
							case CLIENT_SECRET:
								throw new ValidationException("invalid_client_metadata", "Software statement can't contain client secret", HttpStatus.BAD_REQUEST);
							case CLIENT_ID:
								throw new ValidationException("invalid_client_metadata", "Software statement can't contain client ID", HttpStatus.BAD_REQUEST);

							default:
								log.warn("Software statement contained unknown field: " + claim + " with value " + claimSet.getClaim(claim));
								break;
						}
					}

					return newClient;
				} catch (ParseException e) {
					throw new ValidationException("invalid_client_metadata", "Software statement claims didn't parse", HttpStatus.BAD_REQUEST);
				}
			} else {
				throw new ValidationException("invalid_client_metadata", "Software statement rejected by validator", HttpStatus.BAD_REQUEST);
			}
		} else {
			// nothing to see here, carry on
			return newClient;
		}

	}

}
