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
package org.mitre.openid.connect.web;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import org.mitre.jwt.assertion.AssertionValidator;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.exception.ValidationException;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.mitre.openid.connect.service.OIDCTokenService;
import org.mitre.openid.connect.view.ClientInformationResponseView;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mitre.oauth2.model.RegisteredClientFields.APPLICATION_TYPE;
import static org.mitre.oauth2.model.RegisteredClientFields.CLAIMS_REDIRECT_URIS;
import static org.mitre.oauth2.model.RegisteredClientFields.CLIENT_ID;
import static org.mitre.oauth2.model.RegisteredClientFields.CLIENT_ID_ISSUED_AT;
import static org.mitre.oauth2.model.RegisteredClientFields.CLIENT_NAME;
import static org.mitre.oauth2.model.RegisteredClientFields.CLIENT_SECRET;
import static org.mitre.oauth2.model.RegisteredClientFields.CLIENT_SECRET_EXPIRES_AT;
import static org.mitre.oauth2.model.RegisteredClientFields.CLIENT_URI;
import static org.mitre.oauth2.model.RegisteredClientFields.CONTACTS;
import static org.mitre.oauth2.model.RegisteredClientFields.DEFAULT_ACR_VALUES;
import static org.mitre.oauth2.model.RegisteredClientFields.DEFAULT_MAX_AGE;
import static org.mitre.oauth2.model.RegisteredClientFields.GRANT_TYPES;
import static org.mitre.oauth2.model.RegisteredClientFields.ID_TOKEN_ENCRYPTED_RESPONSE_ALG;
import static org.mitre.oauth2.model.RegisteredClientFields.ID_TOKEN_ENCRYPTED_RESPONSE_ENC;
import static org.mitre.oauth2.model.RegisteredClientFields.ID_TOKEN_SIGNED_RESPONSE_ALG;
import static org.mitre.oauth2.model.RegisteredClientFields.INITIATE_LOGIN_URI;
import static org.mitre.oauth2.model.RegisteredClientFields.JWKS;
import static org.mitre.oauth2.model.RegisteredClientFields.JWKS_URI;
import static org.mitre.oauth2.model.RegisteredClientFields.POLICY_URI;
import static org.mitre.oauth2.model.RegisteredClientFields.POST_LOGOUT_REDIRECT_URIS;
import static org.mitre.oauth2.model.RegisteredClientFields.REDIRECT_URIS;
import static org.mitre.oauth2.model.RegisteredClientFields.REGISTRATION_ACCESS_TOKEN;
import static org.mitre.oauth2.model.RegisteredClientFields.REGISTRATION_CLIENT_URI;
import static org.mitre.oauth2.model.RegisteredClientFields.REQUEST_OBJECT_SIGNING_ALG;
import static org.mitre.oauth2.model.RegisteredClientFields.REQUEST_URIS;
import static org.mitre.oauth2.model.RegisteredClientFields.REQUIRE_AUTH_TIME;
import static org.mitre.oauth2.model.RegisteredClientFields.RESPONSE_TYPES;
import static org.mitre.oauth2.model.RegisteredClientFields.SCOPE;
import static org.mitre.oauth2.model.RegisteredClientFields.SECTOR_IDENTIFIER_URI;
import static org.mitre.oauth2.model.RegisteredClientFields.SOFTWARE_STATEMENT;
import static org.mitre.oauth2.model.RegisteredClientFields.SUBJECT_TYPE;
import static org.mitre.oauth2.model.RegisteredClientFields.TOKEN_ENDPOINT_AUTH_METHOD;
import static org.mitre.oauth2.model.RegisteredClientFields.TOKEN_ENDPOINT_AUTH_SIGNING_ALG;
import static org.mitre.oauth2.model.RegisteredClientFields.TOS_URI;
import static org.mitre.oauth2.model.RegisteredClientFields.USERINFO_ENCRYPTED_RESPONSE_ALG;
import static org.mitre.oauth2.model.RegisteredClientFields.USERINFO_ENCRYPTED_RESPONSE_ENC;
import static org.mitre.oauth2.model.RegisteredClientFields.USERINFO_SIGNED_RESPONSE_ALG;

@Controller
@RequestMapping(value = DynamicClientRegistrationEndpoint.URL)
public class DynamicClientRegistrationEndpoint {

	public static final String URL = "register";

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Autowired
	private SystemScopeService scopeService;

	@Autowired
	private BlacklistedSiteService blacklistService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@Autowired
	private OIDCTokenService connectTokenService;

	@Autowired
	@Qualifier("clientAssertionValidator")
	private AssertionValidator assertionValidator;

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(DynamicClientRegistrationEndpoint.class);

	/**
	 * Create a new Client, issue a client ID, and create a registration access token.
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String registerNewClient(@RequestBody String jsonString, Model m) {

		ClientDetailsEntity newClient = null;
		try {
			newClient = ClientDetailsEntityJsonProcessor.parse(jsonString);
		} catch (JsonSyntaxException e) {
			// bad parse
			// didn't parse, this is a bad request
			logger.error("registerNewClient failed; submitted JSON is malformed");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400
			return HttpCodeView.VIEWNAME;
		}

		if (newClient != null) {
			// it parsed!

			//
			// Now do some post-processing consistency checks on it
			//

			// clear out any spurious id/secret (clients don't get to pick)
			newClient.setClientId(null);
			newClient.setClientSecret(null);

			// do validation on the fields
			try {
				newClient = validateSoftwareStatement(newClient); // need to handle the software statement first because it might override requested values
				newClient = validateScopes(newClient);
				newClient = validateResponseTypes(newClient);
				newClient = validateGrantTypes(newClient);
				newClient = validateRedirectUris(newClient);
				newClient = validateAuth(newClient);
			} catch (ValidationException ve) {
				// validation failed, return an error
				m.addAttribute(JsonErrorView.ERROR, ve.getError());
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, ve.getErrorDescription());
				m.addAttribute(HttpCodeView.CODE, ve.getStatus());
				return JsonErrorView.VIEWNAME;
			}

			if (newClient.getTokenEndpointAuthMethod() == null) {
				newClient.setTokenEndpointAuthMethod(AuthMethod.SECRET_BASIC);
			}

			if (newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_BASIC ||
					newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_JWT ||
					newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_POST) {

				// we need to generate a secret
				newClient = clientService.generateClientSecret(newClient);
			}

			// set some defaults for token timeouts
			if (config.isHeartMode()) {
				// heart mode has different defaults depending on primary grant type
				if (newClient.getGrantTypes().contains("authorization_code")) {
					newClient.setAccessTokenValiditySeconds((int)TimeUnit.HOURS.toSeconds(1)); // access tokens good for 1hr
					newClient.setIdTokenValiditySeconds((int)TimeUnit.MINUTES.toSeconds(5)); // id tokens good for 5min
					newClient.setRefreshTokenValiditySeconds((int)TimeUnit.HOURS.toSeconds(24)); // refresh tokens good for 24hr
				} else if (newClient.getGrantTypes().contains("implicit")) {
					newClient.setAccessTokenValiditySeconds((int)TimeUnit.MINUTES.toSeconds(15)); // access tokens good for 15min
					newClient.setIdTokenValiditySeconds((int)TimeUnit.MINUTES.toSeconds(5)); // id tokens good for 5min
					newClient.setRefreshTokenValiditySeconds(0); // no refresh tokens
				} else if (newClient.getGrantTypes().contains("client_credentials")) {
					newClient.setAccessTokenValiditySeconds((int)TimeUnit.HOURS.toSeconds(6)); // access tokens good for 6hr
					newClient.setIdTokenValiditySeconds(0); // no id tokens
					newClient.setRefreshTokenValiditySeconds(0); // no refresh tokens
				}
			} else {
				newClient.setAccessTokenValiditySeconds((int)TimeUnit.HOURS.toSeconds(1)); // access tokens good for 1hr
				newClient.setIdTokenValiditySeconds((int)TimeUnit.MINUTES.toSeconds(10)); // id tokens good for 10min
				newClient.setRefreshTokenValiditySeconds(null); // refresh tokens good until revoked
			}

			// this client has been dynamically registered (obviously)
			newClient.setDynamicallyRegistered(true);

			// this client can't do token introspection
			newClient.setAllowIntrospection(false);

			// now save it
			try {
				ClientDetailsEntity savedClient = clientService.saveNewClient(newClient);

				// generate the registration access token
				OAuth2AccessTokenEntity token = connectTokenService.createRegistrationAccessToken(savedClient);
				token = tokenService.saveAccessToken(token);

				// send it all out to the view

				RegisteredClient registered = new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "register/" + UriUtils.encodePathSegment(savedClient.getClientId(), "UTF-8"));
				m.addAttribute("client", registered);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.CREATED); // http 201

				return ClientInformationResponseView.VIEWNAME;
			} catch (UnsupportedEncodingException e) {
				logger.error("Unsupported encoding", e);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				return HttpCodeView.VIEWNAME;
			} catch (IllegalArgumentException e) {
				logger.error("Couldn't save client", e);

				m.addAttribute(JsonErrorView.ERROR, "invalid_client_metadata");
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client due to invalid or inconsistent metadata.");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400

				return JsonErrorView.VIEWNAME;
			}
		} else {
			// didn't parse, this is a bad request
			logger.error("registerNewClient failed; submitted JSON is malformed");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400

			return HttpCodeView.VIEWNAME;
		}

	}

	/**
	 * Get the meta information for a client.
	 * @param clientId
	 * @param m
	 * @param auth
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + SystemScopeService.REGISTRATION_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String readClientConfiguration(@PathVariable("id") String clientId, Model m, OAuth2Authentication auth) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {

			try {
				OAuth2AccessTokenEntity token = rotateRegistrationTokenIfNecessary(auth, client);
				RegisteredClient registered = new RegisteredClient(client, token.getValue(), config.getIssuer() + "register/" +  UriUtils.encodePathSegment(client.getClientId(), "UTF-8"));

				// send it all out to the view
				m.addAttribute("client", registered);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.OK); // http 200

				return ClientInformationResponseView.VIEWNAME;
			} catch (UnsupportedEncodingException e) {
				logger.error("Unsupported encoding", e);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				return HttpCodeView.VIEWNAME;
			}

		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: "
					+ clientId + " and " + auth.getOAuth2Request().getClientId() + " do not match.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN); // http 403

			return HttpCodeView.VIEWNAME;
		}
	}

	/**
	 * Update the metainformation for a given client.
	 * @param clientId
	 * @param jsonString
	 * @param m
	 * @param auth
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + SystemScopeService.REGISTRATION_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String updateClient(@PathVariable("id") String clientId, @RequestBody String jsonString, Model m, OAuth2Authentication auth) {


		ClientDetailsEntity newClient = null;
		try {
			newClient = ClientDetailsEntityJsonProcessor.parse(jsonString);
		} catch (JsonSyntaxException e) {
			// bad parse
			// didn't parse, this is a bad request
			logger.error("updateClient failed; submitted JSON is malformed");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400
			return HttpCodeView.VIEWNAME;
		}
		ClientDetailsEntity oldClient = clientService.loadClientByClientId(clientId);

		if (newClient != null && oldClient != null  // we have an existing client and the new one parsed
				&& oldClient.getClientId().equals(auth.getOAuth2Request().getClientId()) // the client passed in the URI matches the one in the auth
				&& oldClient.getClientId().equals(newClient.getClientId()) // the client passed in the body matches the one in the URI
				) {

			// a client can't ask to update its own client secret to any particular value
			newClient.setClientSecret(oldClient.getClientSecret());

			// we need to copy over all of the local and SECOAUTH fields
			newClient.setAccessTokenValiditySeconds(oldClient.getAccessTokenValiditySeconds());
			newClient.setIdTokenValiditySeconds(oldClient.getIdTokenValiditySeconds());
			newClient.setRefreshTokenValiditySeconds(oldClient.getRefreshTokenValiditySeconds());
			newClient.setDynamicallyRegistered(true); // it's still dynamically registered
			newClient.setAllowIntrospection(false); // dynamically registered clients can't do introspection -- use the resource registration instead
			newClient.setAuthorities(oldClient.getAuthorities());
			newClient.setClientDescription(oldClient.getClientDescription());
			newClient.setCreatedAt(oldClient.getCreatedAt());
			newClient.setReuseRefreshToken(oldClient.isReuseRefreshToken());

			// do validation on the fields
			try {
				newClient = validateSoftwareStatement(newClient); // need to handle the software statement first because it might override requested values
				newClient = validateScopes(newClient);
				newClient = validateResponseTypes(newClient);
				newClient = validateGrantTypes(newClient);
				newClient = validateRedirectUris(newClient);
				newClient = validateAuth(newClient);
			} catch (ValidationException ve) {
				// validation failed, return an error
				m.addAttribute(JsonErrorView.ERROR, ve.getError());
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, ve.getErrorDescription());
				m.addAttribute(HttpCodeView.CODE, ve.getStatus());
				return JsonErrorView.VIEWNAME;
			}

			try {
				// save the client
				ClientDetailsEntity savedClient = clientService.updateClient(oldClient, newClient);

				OAuth2AccessTokenEntity token = rotateRegistrationTokenIfNecessary(auth, savedClient);

				RegisteredClient registered = new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "register/" + UriUtils.encodePathSegment(savedClient.getClientId(), "UTF-8"));

				// send it all out to the view
				m.addAttribute("client", registered);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.OK); // http 200

				return ClientInformationResponseView.VIEWNAME;
			} catch (UnsupportedEncodingException e) {
				logger.error("Unsupported encoding", e);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				return HttpCodeView.VIEWNAME;
			} catch (IllegalArgumentException e) {
				logger.error("Couldn't save client", e);

				m.addAttribute(JsonErrorView.ERROR, "invalid_client_metadata");
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client due to invalid or inconsistent metadata.");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400

				return JsonErrorView.VIEWNAME;
			}
		} else {
			// client mismatch
			logger.error("updateClient failed, client ID mismatch: "
					+ clientId + " and " + auth.getOAuth2Request().getClientId() + " do not match.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN); // http 403

			return HttpCodeView.VIEWNAME;
		}
	}

	/**
	 * Delete the indicated client from the system.
	 * @param clientId
	 * @param m
	 * @param auth
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + SystemScopeService.REGISTRATION_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteClient(@PathVariable("id") String clientId, Model m, OAuth2Authentication auth) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {

			clientService.deleteClient(client);

			m.addAttribute(HttpCodeView.CODE, HttpStatus.NO_CONTENT); // http 204

			return HttpCodeView.VIEWNAME;
		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: "
					+ clientId + " and " + auth.getOAuth2Request().getClientId() + " do not match.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN); // http 403

			return HttpCodeView.VIEWNAME;
		}
	}

	private ClientDetailsEntity validateScopes(ClientDetailsEntity newClient) throws ValidationException {
		// scopes that the client is asking for
		Set<SystemScope> requestedScopes = scopeService.fromStrings(newClient.getScope());

		// the scopes that the client can have must be a subset of the dynamically allowed scopes
		Set<SystemScope> allowedScopes = scopeService.removeRestrictedAndReservedScopes(requestedScopes);

		// if the client didn't ask for any, give them the defaults
		if (allowedScopes == null || allowedScopes.isEmpty()) {
			allowedScopes = scopeService.getDefaults();
		}

		newClient.setScope(scopeService.toStrings(allowedScopes));

		return newClient;
	}

	private ClientDetailsEntity validateResponseTypes(ClientDetailsEntity newClient) throws ValidationException {
		if (newClient.getResponseTypes() == null) {
			newClient.setResponseTypes(new HashSet<String>());
		}
		return newClient;
	}

	private ClientDetailsEntity validateGrantTypes(ClientDetailsEntity newClient) throws ValidationException {
		// set default grant types if needed
		if (newClient.getGrantTypes() == null || newClient.getGrantTypes().isEmpty()) {
			if (newClient.getScope().contains("offline_access")) { // client asked for offline access
				newClient.setGrantTypes(Sets.newHashSet("authorization_code", "refresh_token")); // allow authorization code and refresh token grant types by default
			} else {
				newClient.setGrantTypes(Sets.newHashSet("authorization_code")); // allow authorization code grant type by default
			}
			if (config.isDualClient()) {
				Set<String> extendedGrandTypes = newClient.getGrantTypes();
				extendedGrandTypes.add("client_credentials");
				newClient.setGrantTypes(extendedGrandTypes);
			}
		}

		// filter out unknown grant types
		// TODO: make this a pluggable service
		Set<String> requestedGrantTypes = new HashSet<>(newClient.getGrantTypes());
		requestedGrantTypes.retainAll(
				ImmutableSet.of("authorization_code", "implicit",
						"password", "client_credentials", "refresh_token",
						"urn:ietf:params:oauth:grant_type:redelegate"));

		// don't allow "password" grant type for dynamic registration
		if (newClient.getGrantTypes().contains("password")) {
			// return an error, you can't dynamically register for the password grant
			throw new ValidationException("invalid_client_metadata", "The password grant type is not allowed in dynamic registration on this server.", HttpStatus.BAD_REQUEST);
		}

		// don't allow clients to have multiple incompatible grant types and scopes
		if (newClient.getGrantTypes().contains("authorization_code")) {

			// check for incompatible grants
			if (newClient.getGrantTypes().contains("implicit") ||
					(!config.isDualClient() && newClient.getGrantTypes().contains("client_credentials"))) {
				// return an error, you can't have these grant types together
				throw new ValidationException("invalid_client_metadata", "Incompatible grant types requested: " + newClient.getGrantTypes(), HttpStatus.BAD_REQUEST);
			}

			if (newClient.getResponseTypes().contains("token")) {
				// return an error, you can't have this grant type and response type together
				throw new ValidationException("invalid_client_metadata", "Incompatible response types requested: " + newClient.getGrantTypes() + " / " + newClient.getResponseTypes(), HttpStatus.BAD_REQUEST);
			}

			newClient.getResponseTypes().add("code");
		}

		if (newClient.getGrantTypes().contains("implicit")) {

			// check for incompatible grants
			if (newClient.getGrantTypes().contains("authorization_code") ||
					(!config.isDualClient() && newClient.getGrantTypes().contains("client_credentials"))) {
				// return an error, you can't have these grant types together
				throw new ValidationException("invalid_client_metadata", "Incompatible grant types requested: " + newClient.getGrantTypes(), HttpStatus.BAD_REQUEST);
			}

			if (newClient.getResponseTypes().contains("code")) {
				// return an error, you can't have this grant type and response type together
				throw new ValidationException("invalid_client_metadata", "Incompatible response types requested: " + newClient.getGrantTypes() + " / " + newClient.getResponseTypes(), HttpStatus.BAD_REQUEST);
			}

			newClient.getResponseTypes().add("token");

			// don't allow refresh tokens in implicit clients
			newClient.getGrantTypes().remove("refresh_token");
			newClient.getScope().remove(SystemScopeService.OFFLINE_ACCESS);
		}

		if (newClient.getGrantTypes().contains("client_credentials")) {

			// check for incompatible grants
			if (!config.isDualClient() &&
					(newClient.getGrantTypes().contains("authorization_code") || newClient.getGrantTypes().contains("implicit"))) {
				// return an error, you can't have these grant types together
				throw new ValidationException("invalid_client_metadata", "Incompatible grant types requested: " + newClient.getGrantTypes(), HttpStatus.BAD_REQUEST);
			}

			if (!newClient.getResponseTypes().isEmpty()) {
				// return an error, you can't have this grant type and response type together
				throw new ValidationException("invalid_client_metadata", "Incompatible response types requested: " + newClient.getGrantTypes() + " / " + newClient.getResponseTypes(), HttpStatus.BAD_REQUEST);
			}

			// don't allow refresh tokens or id tokens in client_credentials clients
			newClient.getGrantTypes().remove("refresh_token");
			newClient.getScope().remove(SystemScopeService.OFFLINE_ACCESS);
			newClient.getScope().remove(SystemScopeService.OPENID_SCOPE);
		}

		if (newClient.getGrantTypes().isEmpty()) {
			// return an error, you need at least one grant type selected
			throw new ValidationException("invalid_client_metadata", "Clients must register at least one grant type.", HttpStatus.BAD_REQUEST);
		}
		return newClient;
	}

	private ClientDetailsEntity validateRedirectUris(ClientDetailsEntity newClient) throws ValidationException {
		// check to make sure this client registered a redirect URI if using a redirect flow
		if (newClient.getGrantTypes().contains("authorization_code") || newClient.getGrantTypes().contains("implicit")) {
			if (newClient.getRedirectUris() == null || newClient.getRedirectUris().isEmpty()) {
				// return an error
				throw new ValidationException("invalid_redirect_uri", "Clients using a redirect-based grant type must register at least one redirect URI.", HttpStatus.BAD_REQUEST);
			}

			for (String uri : newClient.getRedirectUris()) {
				if (blacklistService.isBlacklisted(uri)) {
					// return an error
					throw new ValidationException("invalid_redirect_uri", "Redirect URI is not allowed: " + uri, HttpStatus.BAD_REQUEST);
				}

				if (uri.contains("#")) {
					// if it contains the hash symbol then it has a fragment, which isn't allowed
					throw new ValidationException("invalid_redirect_uri", "Redirect URI can not have a fragment", HttpStatus.BAD_REQUEST);
				}
			}
		}

		return newClient;
	}

	private ClientDetailsEntity validateAuth(ClientDetailsEntity newClient) throws ValidationException {
		if (newClient.getTokenEndpointAuthMethod() == null) {
			newClient.setTokenEndpointAuthMethod(AuthMethod.SECRET_BASIC);
		}

		if (newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_BASIC ||
				newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_JWT ||
				newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_POST) {

			if (Strings.isNullOrEmpty(newClient.getClientSecret())) {
				// no secret yet, we need to generate a secret
				newClient = clientService.generateClientSecret(newClient);
			}
		} else if (newClient.getTokenEndpointAuthMethod() == AuthMethod.PRIVATE_KEY) {
			if (Strings.isNullOrEmpty(newClient.getJwksUri()) && newClient.getJwks() == null) {
				throw new ValidationException("invalid_client_metadata", "JWK Set URI required when using private key authentication", HttpStatus.BAD_REQUEST);
			}

			newClient.setClientSecret(null);
		} else if (newClient.getTokenEndpointAuthMethod() == AuthMethod.NONE) {
			newClient.setClientSecret(null);
		} else {
			throw new ValidationException("invalid_client_metadata", "Unknown authentication method", HttpStatus.BAD_REQUEST);
		}
		return newClient;
	}


	/**
	 * @param newClient
	 * @return
	 * @throws ValidationException
	 */
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
								logger.warn("Software statement contained unknown field: " + claim + " with value " + claimSet.getClaim(claim));
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


	/*
	 * Rotates the registration token if it's expired, otherwise returns it
	 */
	private OAuth2AccessTokenEntity rotateRegistrationTokenIfNecessary(OAuth2Authentication auth, ClientDetailsEntity client) {

		OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
		OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());

		if (config.getRegTokenLifeTime() != null) {

			try {
				// Re-issue the token if it has been issued before [currentTime - validity]
				Date validToDate = new Date(System.currentTimeMillis() - config.getRegTokenLifeTime() * 1000);
				if(token.getJwt().getJWTClaimsSet().getIssueTime().before(validToDate)) {
					logger.info("Rotating the registration access token for " + client.getClientId());
					tokenService.revokeAccessToken(token);
					OAuth2AccessTokenEntity newToken = connectTokenService.createRegistrationAccessToken(client);
					tokenService.saveAccessToken(newToken);
					return newToken;
				} else {
					// it's not expired, keep going
					return token;
				}
			} catch (ParseException e) {
				logger.error("Couldn't parse a known-valid token?", e);
				return token;
			}
		} else {
			// tokens don't expire, just return it
			return token;
		}
	}

}
