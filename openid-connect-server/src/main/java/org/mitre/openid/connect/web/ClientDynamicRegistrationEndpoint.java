/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
package org.mitre.openid.connect.web;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.service.OIDCTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;

@Controller
@RequestMapping(value = "register")
public class ClientDynamicRegistrationEndpoint {

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private OAuth2TokenEntityService tokenService;
	
	@Autowired
	private JwtSigningAndValidationService jwtService;

	@Autowired
	private SystemScopeService scopeService;

	@Autowired
	private ConfigurationPropertiesBean config;
	
	@Autowired
	private OIDCTokenService connectTokenService;

	private static Logger logger = LoggerFactory.getLogger(ClientDynamicRegistrationEndpoint.class);

	/**
	 * Create a new Client, issue a client ID, and create a registration access token.
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public String registerNewClient(@RequestBody String jsonString, Model m) {

		ClientDetailsEntity newClient = ClientDetailsEntityJsonProcessor.parse(jsonString);

		if (newClient != null) {
			// it parsed!

			//
			// Now do some post-processing consistency checks on it
			//

			// clear out any spurious id/secret (clients don't get to pick)
			newClient.setClientId(null);
			newClient.setClientSecret(null);

			// set of scopes that are OK for clients to dynamically register for
			Set<SystemScope> dynScopes = scopeService.getDynReg();

			// scopes that the client is asking for
			Set<SystemScope> requestedScopes = scopeService.fromStrings(newClient.getScope());

			// the scopes that the client can have must be a subset of the dynamically allowed scopes
			Set<SystemScope> allowedScopes = Sets.intersection(dynScopes, requestedScopes);

			// if the client didn't ask for any, give them the defaults
			if (allowedScopes == null || allowedScopes.isEmpty()) {
				allowedScopes = scopeService.getDefaults();
			}
			
			newClient.setScope(scopeService.toStrings(allowedScopes));


			// set default grant types if needed
			if (newClient.getGrantTypes() == null || newClient.getGrantTypes().isEmpty()) {
				if (newClient.getScope().contains("offline_access")) { // client asked for offline access
					newClient.setGrantTypes(Sets.newHashSet("authorization_code", "refresh_token")); // allow authorization code and refresh token grant types by default
				} else {
					newClient.setGrantTypes(Sets.newHashSet("authorization_code")); // allow authorization code grant type by default
				}
			}

			// set default response types if needed
			// TODO: these aren't checked by SECOAUTH
			// TODO: the consistency between the response_type and grant_type needs to be checked by the client service, most likely
			if (newClient.getResponseTypes() == null || newClient.getResponseTypes().isEmpty()) {
				newClient.setResponseTypes(Sets.newHashSet("code")); // default to allowing only the auth code flow
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
			newClient.setAccessTokenValiditySeconds((int)TimeUnit.HOURS.toSeconds(1)); // access tokens good for 1hr
			newClient.setIdTokenValiditySeconds((int)TimeUnit.MINUTES.toSeconds(10)); // id tokens good for 10min
			newClient.setRefreshTokenValiditySeconds(null); // refresh tokens good until revoked

			// this client has been dynamically registered (obviously)
			newClient.setDynamicallyRegistered(true);

			// TODO: check and enforce the sector URI if it's not null (#504)
			
			// now save it
			try {
				ClientDetailsEntity savedClient = clientService.saveNewClient(newClient);
	
				// generate the registration access token
				OAuth2AccessTokenEntity token = connectTokenService.createRegistrationAccessToken(savedClient);
				tokenService.saveAccessToken(token);
	
				// send it all out to the view
	
				// TODO: urlencode the client id for safety?
				RegisteredClient registered = new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "register/" + savedClient.getClientId());
	
				m.addAttribute("client", registered);
				m.addAttribute("code", HttpStatus.CREATED); // http 201
	
				return "clientInformationResponseView";
			} catch (IllegalArgumentException e) {
				logger.error("Couldn't save client", e);
				m.addAttribute("code", HttpStatus.BAD_REQUEST);
 
				return "httpCodeView";
			}
		} else {
			// didn't parse, this is a bad request
			logger.error("registerNewClient failed; submitted JSON is malformed");
			m.addAttribute("code", HttpStatus.BAD_REQUEST); // http 400

			return "httpCodeView";
		}

	}

	/**
	 * Get the meta information for a client.
	 * @param clientId
	 * @param m
	 * @param auth
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + OAuth2AccessTokenEntity.REGISTRATION_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String readClientConfiguration(@PathVariable("id") String clientId, Model m, OAuth2Authentication auth) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {


			// we return the token that we got in
			OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
			OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());

			// TODO: urlencode the client id for safety?
			RegisteredClient registered = new RegisteredClient(client, token.getValue(), config.getIssuer() + "register/" + client.getClientId());

			// send it all out to the view
			m.addAttribute("client", registered);
			m.addAttribute("code", HttpStatus.OK); // http 200

			return "clientInformationResponseView";
		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: "
					+ clientId + " and " + auth.getOAuth2Request().getClientId() + " do not match.");
			m.addAttribute("code", HttpStatus.FORBIDDEN); // http 403

			return "httpCodeView";
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
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + OAuth2AccessTokenEntity.REGISTRATION_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	public String updateClient(@PathVariable("id") String clientId, @RequestBody String jsonString, Model m, OAuth2Authentication auth) {


		ClientDetailsEntity newClient = ClientDetailsEntityJsonProcessor.parse(jsonString);
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
			newClient.setAllowIntrospection(oldClient.isAllowIntrospection());
			newClient.setAuthorities(oldClient.getAuthorities());
			newClient.setClientDescription(oldClient.getClientDescription());
			newClient.setCreatedAt(oldClient.getCreatedAt());
			newClient.setReuseRefreshToken(oldClient.isReuseRefreshToken());

			// set of scopes that are OK for clients to dynamically register for
			Set<SystemScope> dynScopes = scopeService.getDynReg();

			// scopes that the client is asking for
			Set<SystemScope> requestedScopes = scopeService.fromStrings(newClient.getScope());

			// the scopes that the client can have must be a subset of the dynamically allowed scopes
			Set<SystemScope> allowedScopes = Sets.intersection(dynScopes, requestedScopes);

			// make sure that the client doesn't ask for scopes it can't have
			newClient.setScope(scopeService.toStrings(allowedScopes));

			try {
				// save the client
				ClientDetailsEntity savedClient = clientService.updateClient(oldClient, newClient);
	
				// we return the token that we got in
				// TODO: rotate this after some set amount of time
				OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
				OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());
	
				// TODO: urlencode the client id for safety?
				RegisteredClient registered = new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "register/" + savedClient.getClientId());
	
				// send it all out to the view
				m.addAttribute("client", registered);
				m.addAttribute("code", HttpStatus.OK); // http 200
	
				return "clientInformationResponseView";
			} catch (IllegalArgumentException e) {
				logger.error("Couldn't save client", e);
				m.addAttribute("code", HttpStatus.BAD_REQUEST);
 
				return "httpCodeView";
			}
		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: "
					+ clientId + " and " + auth.getOAuth2Request().getClientId() + " do not match.");
			m.addAttribute("code", HttpStatus.FORBIDDEN); // http 403

			return "httpCodeView";
		}
	}

	/**
	 * Delete the indicated client from the system.
	 * @param clientId
	 * @param m
	 * @param auth
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('" + OAuth2AccessTokenEntity.REGISTRATION_TOKEN_SCOPE + "')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public String deleteClient(@PathVariable("id") String clientId, Model m, OAuth2Authentication auth) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {

			clientService.deleteClient(client);

			m.addAttribute("code", HttpStatus.NO_CONTENT); // http 204

			return "httpCodeView";
		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: "
					+ clientId + " and " + auth.getOAuth2Request().getClientId() + " do not match.");
			m.addAttribute("code", HttpStatus.FORBIDDEN); // http 403

			return "httpCodeView";
		}
	}

}
