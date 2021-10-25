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

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.mitre.jwt.assertion.AssertionValidator;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.exception.ValidationException;
import org.mitre.openid.connect.service.DynamicClientValidationService;
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
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriUtils;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonSyntaxException;

@Controller
@RequestMapping(value = DynamicClientRegistrationEndpoint.URL)
public class DynamicClientRegistrationEndpoint {

  public static final String URL = "register";

  @Autowired
  private ClientDetailsEntityService clientService;

  @Autowired
  private OAuth2TokenEntityService tokenService;

  @Autowired
  private ConfigurationPropertiesBean config;

  @Autowired
  private OIDCTokenService connectTokenService;

  @Autowired
  private DynamicClientValidationService clientValidationService;

  @Autowired
  @Qualifier("clientAssertionValidator")
  private AssertionValidator assertionValidator;

  /**
   * Logger for this class
   */
  private static final Logger logger =
      LoggerFactory.getLogger(DynamicClientRegistrationEndpoint.class);

  public static final ImmutableSet<String> ALLOWED_GRANT_TYPES =
      ImmutableSet.of("authorization_code", "implicit", "client_credentials", "refresh_token",
          "urn:ietf:params:oauth:grant_type:redelegate",
          "urn:ietf:params:oauth:grant-type:device_code");

  /**
   * Create a new Client, issue a client ID, and create a registration access token.
   * 
   * @param jsonString
   * @param m
   * @param p
   * @return
   */
  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
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

      Set<String> requestedGrantTypes = newClient.getGrantTypes();
      requestedGrantTypes.retainAll(ALLOWED_GRANT_TYPES);
      newClient.setGrantTypes(requestedGrantTypes);

      // do validation on the fields
      try {
        newClient = clientValidationService.validateClient(newClient);
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

      if (newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_BASIC
          || newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_JWT
          || newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_POST) {

        // we need to generate a secret
        newClient = clientService.generateClientSecret(newClient);
      }

      // set some defaults for token timeouts
      if (config.isHeartMode()) {
        // heart mode has different defaults depending on primary grant type
        if (newClient.getGrantTypes().contains("authorization_code")) {
          newClient.setAccessTokenValiditySeconds((int) TimeUnit.HOURS.toSeconds(1)); // access
                                                                                      // tokens good
                                                                                      // for 1hr
          newClient.setIdTokenValiditySeconds((int) TimeUnit.MINUTES.toSeconds(5)); // id tokens
                                                                                    // good for 5min
          newClient.setRefreshTokenValiditySeconds((int) TimeUnit.HOURS.toSeconds(24)); // refresh
                                                                                        // tokens
                                                                                        // good for
                                                                                        // 24hr
        } else if (newClient.getGrantTypes().contains("implicit")) {
          newClient.setAccessTokenValiditySeconds((int) TimeUnit.MINUTES.toSeconds(15)); // access
                                                                                         // tokens
                                                                                         // good for
                                                                                         // 15min
          newClient.setIdTokenValiditySeconds((int) TimeUnit.MINUTES.toSeconds(5)); // id tokens
                                                                                    // good for 5min
          newClient.setRefreshTokenValiditySeconds(0); // no refresh tokens
        } else if (newClient.getGrantTypes().contains("client_credentials")) {
          newClient.setAccessTokenValiditySeconds((int) TimeUnit.HOURS.toSeconds(6)); // access
                                                                                      // tokens good
                                                                                      // for 6hr
          newClient.setIdTokenValiditySeconds(0); // no id tokens
          newClient.setRefreshTokenValiditySeconds(0); // no refresh tokens
        }
      } else {
        newClient.setAccessTokenValiditySeconds((int) TimeUnit.HOURS.toSeconds(1)); // access tokens
                                                                                    // good for 1hr
        newClient.setIdTokenValiditySeconds((int) TimeUnit.MINUTES.toSeconds(10)); // id tokens good
                                                                                   // for 10min
        newClient.setRefreshTokenValiditySeconds(null); // refresh tokens good until revoked
        newClient.setDeviceCodeValiditySeconds((int) TimeUnit.MINUTES.toSeconds(10));
      }


      // this client has been dynamically registered (obviously)
      newClient.setDynamicallyRegistered(true);

      // this client can't do token introspection
      newClient.setAllowIntrospection(false);

      // now save it
      try {
        ClientDetailsEntity savedClient = clientService.saveNewClient(newClient);

        // generate the registration access token
        OAuth2AccessTokenEntity token =
            connectTokenService.createRegistrationAccessToken(savedClient);
        token = tokenService.saveAccessToken(token);

        // send it all out to the view

        RegisteredClient registered =
            new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "register/"
                + UriUtils.encodePathSegment(savedClient.getClientId(), "UTF-8"));
        m.addAttribute("client", registered);
        m.addAttribute(HttpCodeView.CODE, HttpStatus.CREATED); // http 201

        return ClientInformationResponseView.VIEWNAME;
      } catch (IllegalArgumentException e) {
        logger.error("Couldn't save client", e);

        m.addAttribute(JsonErrorView.ERROR, "invalid_client_metadata");
        m.addAttribute(JsonErrorView.ERROR_MESSAGE,
            "Unable to save client due to invalid or inconsistent metadata.");
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
   * 
   * @param clientId
   * @param m
   * @param auth
   * @return
   */
  @PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('"
      + SystemScopeService.REGISTRATION_TOKEN_SCOPE + "')")
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String readClientConfiguration(@PathVariable("id") String clientId, Model m,
      OAuth2Authentication auth) {

    ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

    if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {
      OAuth2AccessTokenEntity token = rotateRegistrationTokenIfNecessary(auth, client);
      RegisteredClient registered =
          new RegisteredClient(client, token.getValue(), config.getIssuer() + "register/"
              + UriUtils.encodePathSegment(client.getClientId(), "UTF-8"));

      // send it all out to the view
      m.addAttribute("client", registered);
      m.addAttribute(HttpCodeView.CODE, HttpStatus.OK); // http 200

      return ClientInformationResponseView.VIEWNAME;

    } else {
      // client mismatch
      logger.error("readClientConfiguration failed, client ID mismatch: " + clientId + " and "
          + auth.getOAuth2Request().getClientId() + " do not match.");
      m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN); // http 403

      return HttpCodeView.VIEWNAME;
    }
  }

  /**
   * Update the metainformation for a given client.
   * 
   * @param clientId
   * @param jsonString
   * @param m
   * @param auth
   * @return
   */
  @PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('"
      + SystemScopeService.REGISTRATION_TOKEN_SCOPE + "')")
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public String updateClient(@PathVariable("id") String clientId, @RequestBody String jsonString,
      Model m, OAuth2Authentication auth) {


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

    if (newClient != null && oldClient != null // we have an existing client and the new one parsed
        && oldClient.getClientId().equals(auth.getOAuth2Request().getClientId()) // the client
                                                                                 // passed in the
                                                                                 // URI matches the
                                                                                 // one in the auth
        && oldClient.getClientId().equals(newClient.getClientId()) // the client passed in the body
                                                                   // matches the one in the URI
    ) {

      // a client can't ask to update its own client secret to any particular value
      newClient.setClientSecret(oldClient.getClientSecret());

      // we need to copy over all of the local and SECOAUTH fields
      newClient.setAccessTokenValiditySeconds(oldClient.getAccessTokenValiditySeconds());
      newClient.setIdTokenValiditySeconds(oldClient.getIdTokenValiditySeconds());
      newClient.setRefreshTokenValiditySeconds(oldClient.getRefreshTokenValiditySeconds());
      newClient.setDeviceCodeValiditySeconds(oldClient.getDeviceCodeValiditySeconds());
      newClient.setDynamicallyRegistered(true); // it's still dynamically registered
      newClient.setAllowIntrospection(false); // dynamically registered clients can't do
                                              // introspection -- use the resource registration
                                              // instead
      newClient.setAuthorities(oldClient.getAuthorities());
      newClient.setClientDescription(oldClient.getClientDescription());
      newClient.setCreatedAt(oldClient.getCreatedAt());
      newClient.setReuseRefreshToken(oldClient.isReuseRefreshToken());

      Set<String> requestedGrantTypes = newClient.getGrantTypes();
      requestedGrantTypes.retainAll(ALLOWED_GRANT_TYPES);
      newClient.setGrantTypes(requestedGrantTypes);

      Set<String> oldClientGrantedGrantTypes = oldClient.getGrantTypes();
      oldClientGrantedGrantTypes.removeAll(ALLOWED_GRANT_TYPES);

      // do validation on the fields
      try {
        newClient = clientValidationService.validateClient(newClient);
      } catch (ValidationException ve) {
        // validation failed, return an error
        m.addAttribute(JsonErrorView.ERROR, ve.getError());
        m.addAttribute(JsonErrorView.ERROR_MESSAGE, ve.getErrorDescription());
        m.addAttribute(HttpCodeView.CODE, ve.getStatus());
        return JsonErrorView.VIEWNAME;
      }

      try {

        if (!oldClientGrantedGrantTypes.isEmpty()) {
          newClient.getGrantTypes().addAll(oldClientGrantedGrantTypes);
        }
        // save the client
        ClientDetailsEntity savedClient = clientService.updateClient(oldClient, newClient);

        OAuth2AccessTokenEntity token = rotateRegistrationTokenIfNecessary(auth, savedClient);

        RegisteredClient registered =
            new RegisteredClient(savedClient, token.getValue(), config.getIssuer() + "register/"
                + UriUtils.encodePathSegment(savedClient.getClientId(), "UTF-8"));

        // send it all out to the view
        m.addAttribute("client", registered);
        m.addAttribute(HttpCodeView.CODE, HttpStatus.OK); // http 200

        return ClientInformationResponseView.VIEWNAME;
      } catch (IllegalArgumentException e) {
        logger.error("Couldn't save client", e);

        m.addAttribute(JsonErrorView.ERROR, "invalid_client_metadata");
        m.addAttribute(JsonErrorView.ERROR_MESSAGE,
            "Unable to save client due to invalid or inconsistent metadata.");
        m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST); // http 400

        return JsonErrorView.VIEWNAME;
      }
    } else {
      // client mismatch
      logger.error("updateClient failed, client ID mismatch: " + clientId + " and "
          + auth.getOAuth2Request().getClientId() + " do not match.");
      m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN); // http 403

      return HttpCodeView.VIEWNAME;
    }
  }

  /**
   * Delete the indicated client from the system.
   * 
   * @param clientId
   * @param m
   * @param auth
   * @return
   */
  @PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('"
      + SystemScopeService.REGISTRATION_TOKEN_SCOPE + "')")
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String deleteClient(@PathVariable("id") String clientId, Model m,
      OAuth2Authentication auth) {

    ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

    if (client != null && client.getClientId().equals(auth.getOAuth2Request().getClientId())) {

      clientService.deleteClient(client);

      m.addAttribute(HttpCodeView.CODE, HttpStatus.NO_CONTENT); // http 204

      return HttpCodeView.VIEWNAME;
    } else {
      // client mismatch
      logger.error("readClientConfiguration failed, client ID mismatch: " + clientId + " and "
          + auth.getOAuth2Request().getClientId() + " do not match.");
      m.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN); // http 403

      return HttpCodeView.VIEWNAME;
    }
  }

  /*
   * Rotates the registration token if it's expired, otherwise returns it
   */
  private OAuth2AccessTokenEntity rotateRegistrationTokenIfNecessary(OAuth2Authentication auth,
      ClientDetailsEntity client) {

    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
    OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());

    if (config.getRegTokenLifeTime() != null) {

      try {
        // Re-issue the token if it has been issued before [currentTime - validity]
        Date validToDate =
            new Date(System.currentTimeMillis() - config.getRegTokenLifeTime() * 1000);
        if (token.getJwt().getJWTClaimsSet().getIssueTime().before(validToDate)) {
          logger.info("Rotating the registration access token for " + client.getClientId());
          tokenService.revokeAccessToken(token);
          OAuth2AccessTokenEntity newToken =
              connectTokenService.createRegistrationAccessToken(client);
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
