/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

import com.google.common.base.Strings;
import com.google.gson.JsonSyntaxException;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AuthMethod;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.model.RegisteredClient;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.OAuth2TokenEntityService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.ClientDetailsEntityJsonProcessor;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.exception.ValidationException;
import cz.muni.ics.openid.connect.service.OIDCTokenService;
import cz.muni.ics.openid.connect.view.ClientInformationResponseView;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonErrorView;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;

@Controller
@RequestMapping(value = ProtectedResourceRegistrationEndpoint.URL)
@Slf4j
public class ProtectedResourceRegistrationEndpoint {

	/**
	 *
	 */
	public static final String URL = "resource";

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Autowired
	private SystemScopeService scopeService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@Autowired
	private OIDCTokenService connectTokenService;

	/**
	 * Create a new Client, issue a client ID, and create a registration access token.
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String registerNewProtectedResource(@RequestBody String jsonString, Model m) {

		ClientDetailsEntity newClient = null;
		try {
			newClient = ClientDetailsEntityJsonProcessor.parse(jsonString);
		} catch (JsonSyntaxException e) {
			// bad parse
			// didn't parse, this is a bad request
			log.error("registerNewProtectedResource failed; submitted JSON is malformed");
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
				newClient = validateScopes(newClient);
				newClient = validateAuth(newClient);
			} catch (ValidationException ve) {
				// validation failed, return an error
				m.addAttribute(JsonErrorView.ERROR, ve.getError());
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, ve.getErrorDescription());
				m.addAttribute(HttpCodeView.CODE, ve.getStatus());
				return JsonErrorView.VIEWNAME;
			}


			// no grant types are allowed
			newClient.setGrantTypes(new HashSet<String>());
			newClient.setResponseTypes(new HashSet<String>());
			newClient.setRedirectUris(new HashSet<String>());

			// don't issue tokens to this client
			newClient.setAccessTokenValiditySeconds(0);
			newClient.setIdTokenValiditySeconds(0);
			newClient.setRefreshTokenValiditySeconds(0);

			// clear out unused fields
			newClient.setDefaultACRvalues(new HashSet<String>());
			newClient.setDefaultMaxAge(null);
			newClient.setIdTokenEncryptedResponseAlg(null);
			newClient.setIdTokenEncryptedResponseEnc(null);
			newClient.setIdTokenSignedResponseAlg(null);
			newClient.setInitiateLoginUri(null);
			newClient.setPostLogoutRedirectUris(null);
			newClient.setRequestObjectSigningAlg(null);
			newClient.setRequireAuthTime(null);
			newClient.setReuseRefreshToken(false);
			newClient.setSectorIdentifierUri(null);
			newClient.setSubjectType(null);
			newClient.setUserInfoEncryptedResponseAlg(null);
			newClient.setUserInfoEncryptedResponseEnc(null);
			newClient.setUserInfoSignedResponseAlg(null);

			// this client has been dynamically registered (obviously)
			newClient.setDynamicallyRegistered(true);

			// this client has access to the introspection endpoint
			newClient.setAllowIntrospection(true);

			// now save it
			try {
				ClientDetailsEntity savedClient = clientService.saveNewClient(newClient);

				// generate the registration access token
				OAuth2AccessTokenEntity token = connectTokenService.createResourceAccessToken(savedClient);
				tokenService.saveAccessToken(token);

				// send it all out to the view

				RegisteredClient registered = new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "resource/" + UriUtils.encodePathSegment(savedClient.getClientId(), "UTF-8"));
				m.addAttribute("client", registered);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.CREATED); // http 201

				return ClientInformationResponseView.VIEWNAME;
			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported encoding", e);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				return HttpCodeView.VIEWNAME;
			} catch (IllegalArgumentException e) {
				log.error("Couldn't save client", e);

				m.addAttribute(JsonErrorView.ERROR, "invalid_client_metadata");
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client due to invalid or inconsistent metadata.");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400

				return JsonErrorView.VIEWNAME;
			}
		} else {
			// didn't parse, this is a bad request
			log.error("registerNewClient failed; submitted JSON is malformed");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400

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

	/**
	 * Get the meta information for a client.
	 * @param clientId
	 * @param m
	 * @param auth
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + SystemScopeService.RESOURCE_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String readResourceConfiguration(@PathVariable("id") String clientId, Model m, OAuth2Authentication auth) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {



			try {
				// possibly update the token
				OAuth2AccessTokenEntity token = fetchValidRegistrationToken(auth, client);

				RegisteredClient registered = new RegisteredClient(client, token.getValue(), config.getIssuer() + "resource/" +  UriUtils.encodePathSegment(client.getClientId(), "UTF-8"));

				// send it all out to the view
				m.addAttribute("client", registered);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.OK); // http 200

				return ClientInformationResponseView.VIEWNAME;
			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported encoding", e);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				return HttpCodeView.VIEWNAME;
			}
		} else {
			// client mismatch
			log.error("readResourceConfiguration failed, client ID mismatch: "
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
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + SystemScopeService.RESOURCE_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String updateProtectedResource(@PathVariable("id") String clientId, @RequestBody String jsonString, Model m, OAuth2Authentication auth) {


		ClientDetailsEntity newClient = null;
		try {
			newClient = ClientDetailsEntityJsonProcessor.parse(jsonString);
		} catch (JsonSyntaxException e) {
			// bad parse
			// didn't parse, this is a bad request
			log.error("updateProtectedResource failed; submitted JSON is malformed");
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

			newClient.setCreatedAt(oldClient.getCreatedAt());

			// no grant types are allowed
			newClient.setGrantTypes(new HashSet<String>());
			newClient.setResponseTypes(new HashSet<String>());
			newClient.setRedirectUris(new HashSet<String>());

			// don't issue tokens to this client
			newClient.setAccessTokenValiditySeconds(0);
			newClient.setIdTokenValiditySeconds(0);
			newClient.setRefreshTokenValiditySeconds(0);

			// clear out unused fields
			newClient.setDefaultACRvalues(new HashSet<String>());
			newClient.setDefaultMaxAge(null);
			newClient.setIdTokenEncryptedResponseAlg(null);
			newClient.setIdTokenEncryptedResponseEnc(null);
			newClient.setIdTokenSignedResponseAlg(null);
			newClient.setInitiateLoginUri(null);
			newClient.setPostLogoutRedirectUris(null);
			newClient.setRequestObjectSigningAlg(null);
			newClient.setRequireAuthTime(null);
			newClient.setReuseRefreshToken(false);
			newClient.setSectorIdentifierUri(null);
			newClient.setSubjectType(null);
			newClient.setUserInfoEncryptedResponseAlg(null);
			newClient.setUserInfoEncryptedResponseEnc(null);
			newClient.setUserInfoSignedResponseAlg(null);

			// this client has been dynamically registered (obviously)
			newClient.setDynamicallyRegistered(true);

			// this client has access to the introspection endpoint
			newClient.setAllowIntrospection(true);

			// do validation on the fields
			try {
				newClient = validateScopes(newClient);
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

				// possibly update the token
				OAuth2AccessTokenEntity token = fetchValidRegistrationToken(auth, savedClient);

				RegisteredClient registered = new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "resource/" + UriUtils.encodePathSegment(savedClient.getClientId(), "UTF-8"));

				// send it all out to the view
				m.addAttribute("client", registered);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.OK); // http 200

				return ClientInformationResponseView.VIEWNAME;
			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported encoding", e);
				m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
				return HttpCodeView.VIEWNAME;
			} catch (IllegalArgumentException e) {
				log.error("Couldn't save client", e);

				m.addAttribute(JsonErrorView.ERROR, "invalid_client_metadata");
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Unable to save client due to invalid or inconsistent metadata.");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400

				return JsonErrorView.VIEWNAME;
			}
		} else {
			// client mismatch
			log.error("updateProtectedResource" +
					" failed, client ID mismatch: "
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
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + SystemScopeService.RESOURCE_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String deleteResource(@PathVariable("id") String clientId, Model m, OAuth2Authentication auth) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {

			clientService.deleteClient(client);

			m.addAttribute(HttpCodeView.CODE, HttpStatus.NO_CONTENT); // http 204

			return HttpCodeView.VIEWNAME;
		} else {
			// client mismatch
			log.error("readClientConfiguration failed, client ID mismatch: "
					+ clientId + " and " + auth.getOAuth2Request().getClientId() + " do not match.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN); // http 403

			return HttpCodeView.VIEWNAME;
		}
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

	private OAuth2AccessTokenEntity fetchValidRegistrationToken(OAuth2Authentication auth, ClientDetailsEntity client) {

		OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
		OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());

		if (config.getRegTokenLifeTime() != null) {

			try {
				// Re-issue the token if it has been issued before [currentTime - validity]
				Date validToDate = new Date(System.currentTimeMillis() - config.getRegTokenLifeTime() * 1000);
				if(token.getJwt().getJWTClaimsSet().getIssueTime().before(validToDate)) {
					log.info("Rotating the registration access token for " + client.getClientId());
					tokenService.revokeAccessToken(token);
					OAuth2AccessTokenEntity newToken = connectTokenService.createResourceAccessToken(client);
					tokenService.saveAccessToken(newToken);
					return newToken;
				} else {
					// it's not expired, keep going
					return token;
				}
			} catch (ParseException e) {
				log.error("Couldn't parse a known-valid token?", e);
				return token;
			}
		} else {
			// tokens don't expire, just return it
			return token;
		}
	}

}
