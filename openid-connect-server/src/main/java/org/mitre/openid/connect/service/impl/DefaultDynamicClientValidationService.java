package org.mitre.openid.connect.service.impl;

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
import static org.mitre.oauth2.model.RegisteredClientFields.LOGO_URI;
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

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.mitre.jwt.assertion.AssertionValidator;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.exception.ValidationException;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.mitre.openid.connect.service.DynamicClientValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;

@Service
public class DefaultDynamicClientValidationService implements DynamicClientValidationService {
  public static final Logger LOG =
      LoggerFactory.getLogger(DefaultDynamicClientValidationService.class);

  @Autowired
  private SystemScopeService scopeService;

  @Autowired
  @Qualifier("clientAssertionValidator")
  private AssertionValidator assertionValidator;

  @Autowired
  private BlacklistedSiteService blacklistService;

  @Autowired
  private ConfigurationPropertiesBean config;

  @Autowired
  private ClientDetailsEntityService clientService;

  public static final ImmutableSet<String> ALLOWED_GRANT_TYPES =
      ImmutableSet.of("authorization_code", "implicit", "client_credentials", "refresh_token",
          "urn:ietf:params:oauth:grant_type:redelegate",
          "urn:ietf:params:oauth:grant-type:device_code");

  @Override
  public ClientDetailsEntity validateClient(ClientDetailsEntity client) throws ValidationException {

    client = validateSoftwareStatement(client);
    client = validateScopes(client);
    client = validateResponseTypes(client);
    client = validateGrantTypes(client);
    client = validateRedirectUris(client);
    client = validateAuth(client);

    return client;
  }

  protected ClientDetailsEntity validateScopes(ClientDetailsEntity newClient)
      throws ValidationException {
    // scopes that the client is asking for
    Set<SystemScope> requestedScopes = scopeService.fromStrings(newClient.getScope());

    // the scopes that the client can have must be a subset of the dynamically allowed scopes
    Set<SystemScope> allowedScopes =
        scopeService.removeRestrictedAndReservedScopes(requestedScopes);

    // if the client didn't ask for any, give them the defaults
    if (allowedScopes == null || allowedScopes.isEmpty()) {
      allowedScopes = scopeService.getDefaults();
    }

    newClient.setScope(scopeService.toStrings(allowedScopes));

    return newClient;
  }

  protected ClientDetailsEntity validateResponseTypes(ClientDetailsEntity newClient)
      throws ValidationException {
    if (newClient.getResponseTypes() == null) {
      newClient.setResponseTypes(new HashSet<String>());
    }
    return newClient;
  }

  protected ClientDetailsEntity validateGrantTypes(ClientDetailsEntity newClient)
      throws ValidationException {
    // set default grant types if needed
    if (newClient.getGrantTypes() == null || newClient.getGrantTypes().isEmpty()) {
      if (newClient.getScope().contains("offline_access")) { // client asked for offline access
        newClient.setGrantTypes(Sets.newHashSet("authorization_code", "refresh_token")); // allow
                                                                                         // authorization
                                                                                         // code and
                                                                                         // refresh
                                                                                         // token
                                                                                         // grant
                                                                                         // types by
                                                                                         // default
      } else {
        newClient.setGrantTypes(Sets.newHashSet("authorization_code")); // allow authorization code
                                                                        // grant type by default
      }
      if (config.isDualClient()) {
        Set<String> extendedGrandTypes = newClient.getGrantTypes();
        extendedGrandTypes.add("client_credentials");
        newClient.setGrantTypes(extendedGrandTypes);
      }
    }

    // don't allow "password" grant type for dynamic registration
    if (newClient.getGrantTypes().contains("password")) {
      // return an error, you can't dynamically register for the password grant
      throw new ValidationException("invalid_client_metadata",
          "The password grant type is not allowed in dynamic registration on this server.",
          HttpStatus.BAD_REQUEST);
    }

    // don't allow clients to have multiple incompatible grant types and scopes
    if (newClient.getGrantTypes().contains("authorization_code")) {

      // check for incompatible grants
      if (newClient.getGrantTypes().contains("implicit")
          || (!config.isDualClient() && newClient.getGrantTypes().contains("client_credentials"))) {
        // return an error, you can't have these grant types together
        throw new ValidationException("invalid_client_metadata",
            "Incompatible grant types requested: " + newClient.getGrantTypes(),
            HttpStatus.BAD_REQUEST);
      }

      if (newClient.getResponseTypes().contains("token")) {
        // return an error, you can't have this grant type and response type together
        throw new ValidationException(
            "invalid_client_metadata", "Incompatible response types requested: "
                + newClient.getGrantTypes() + " / " + newClient.getResponseTypes(),
            HttpStatus.BAD_REQUEST);
      }

      newClient.getResponseTypes().add("code");
    }

    if (newClient.getGrantTypes().contains("implicit")) {

      // check for incompatible grants
      if (newClient.getGrantTypes().contains("authorization_code")
          || (!config.isDualClient() && newClient.getGrantTypes().contains("client_credentials"))) {
        // return an error, you can't have these grant types together
        throw new ValidationException("invalid_client_metadata",
            "Incompatible grant types requested: " + newClient.getGrantTypes(),
            HttpStatus.BAD_REQUEST);
      }

      if (newClient.getResponseTypes().contains("code")) {
        // return an error, you can't have this grant type and response type together
        throw new ValidationException(
            "invalid_client_metadata", "Incompatible response types requested: "
                + newClient.getGrantTypes() + " / " + newClient.getResponseTypes(),
            HttpStatus.BAD_REQUEST);
      }

      newClient.getResponseTypes().add("token");

      // don't allow refresh tokens in implicit clients
      newClient.getGrantTypes().remove("refresh_token");
      newClient.getScope().remove(SystemScopeService.OFFLINE_ACCESS);
    }

    if (newClient.getGrantTypes().contains("client_credentials")) {

      // check for incompatible grants
      if (!config.isDualClient() && (newClient.getGrantTypes().contains("authorization_code")
          || newClient.getGrantTypes().contains("implicit"))) {
        // return an error, you can't have these grant types together
        throw new ValidationException("invalid_client_metadata",
            "Incompatible grant types requested: " + newClient.getGrantTypes(),
            HttpStatus.BAD_REQUEST);
      }

      if (!newClient.getResponseTypes().isEmpty()) {
        // return an error, you can't have this grant type and response type together
        throw new ValidationException(
            "invalid_client_metadata", "Incompatible response types requested: "
                + newClient.getGrantTypes() + " / " + newClient.getResponseTypes(),
            HttpStatus.BAD_REQUEST);
      }

      // don't allow refresh tokens or id tokens in client_credentials clients
      newClient.getGrantTypes().remove("refresh_token");
      newClient.getScope().remove(SystemScopeService.OFFLINE_ACCESS);
      newClient.getScope().remove(SystemScopeService.OPENID_SCOPE);
    }

    if (newClient.getGrantTypes().isEmpty()) {
      // return an error, you need at least one grant type selected
      throw new ValidationException("invalid_client_metadata",
          "Clients must register at least one grant type.", HttpStatus.BAD_REQUEST);
    }
    return newClient;
  }

  protected ClientDetailsEntity validateRedirectUris(ClientDetailsEntity newClient)
      throws ValidationException {
    // check to make sure this client registered a redirect URI if using a redirect flow
    if (newClient.getGrantTypes().contains("authorization_code")
        || newClient.getGrantTypes().contains("implicit")) {
      if (newClient.getRedirectUris() == null || newClient.getRedirectUris().isEmpty()) {
        // return an error
        throw new ValidationException("invalid_redirect_uri",
            "Clients using a redirect-based grant type must register at least one redirect URI.",
            HttpStatus.BAD_REQUEST);
      }

      for (String uri : newClient.getRedirectUris()) {
        if (blacklistService.isBlacklisted(uri)) {
          // return an error
          throw new ValidationException("invalid_redirect_uri",
              "Redirect URI is not allowed: " + uri, HttpStatus.BAD_REQUEST);
        }

        if (uri.contains("#")) {
          // if it contains the hash symbol then it has a fragment, which isn't allowed
          throw new ValidationException("invalid_redirect_uri",
              "Redirect URI can not have a fragment", HttpStatus.BAD_REQUEST);
        }
      }
    }

    return newClient;
  }

  protected ClientDetailsEntity validateAuth(ClientDetailsEntity newClient)
      throws ValidationException {
    if (newClient.getTokenEndpointAuthMethod() == null) {
      newClient.setTokenEndpointAuthMethod(AuthMethod.SECRET_BASIC);
    }

    if (newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_BASIC
        || newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_JWT
        || newClient.getTokenEndpointAuthMethod() == AuthMethod.SECRET_POST) {

      if (Strings.isNullOrEmpty(newClient.getClientSecret())) {
        // no secret yet, we need to generate a secret
        newClient = clientService.generateClientSecret(newClient);
      }
    } else if (newClient.getTokenEndpointAuthMethod() == AuthMethod.PRIVATE_KEY) {
      if (Strings.isNullOrEmpty(newClient.getJwksUri()) && newClient.getJwks() == null) {
        throw new ValidationException("invalid_client_metadata",
            "JWK Set URI required when using private key authentication", HttpStatus.BAD_REQUEST);
      }

      newClient.setClientSecret(null);
    } else if (newClient.getTokenEndpointAuthMethod() == AuthMethod.NONE) {
      newClient.setClientSecret(null);
    } else {
      throw new ValidationException("invalid_client_metadata", "Unknown authentication method",
          HttpStatus.BAD_REQUEST);
    }
    return newClient;
  }


  /**
   * @param newClient
   * @return
   * @throws ValidationException
   */
  protected ClientDetailsEntity validateSoftwareStatement(ClientDetailsEntity newClient)
      throws ValidationException {
    if (newClient.getSoftwareStatement() != null) {
      if (assertionValidator.isValid(newClient.getSoftwareStatement())) {
        // we have a software statement and its envelope passed all the checks from our validator

        // swap out all of the client's fields for the associated parts of the software statement
        try {
          JWTClaimsSet claimSet = newClient.getSoftwareStatement().getJWTClaimsSet();
          for (String claim : claimSet.getClaims().keySet()) {
            switch (claim) {
              case SOFTWARE_STATEMENT:
                throw new ValidationException("invalid_client_metadata",
                    "Software statement can't include another software statement",
                    HttpStatus.BAD_REQUEST);
              case CLAIMS_REDIRECT_URIS:
                newClient
                  .setClaimsRedirectUris(Sets.newHashSet(claimSet.getStringListClaim(claim)));
                break;
              case CLIENT_SECRET_EXPIRES_AT:
                throw new ValidationException("invalid_client_metadata",
                    "Software statement can't include a client secret expiration time",
                    HttpStatus.BAD_REQUEST);
              case CLIENT_ID_ISSUED_AT:
                throw new ValidationException("invalid_client_metadata",
                    "Software statement can't include a client ID issuance time",
                    HttpStatus.BAD_REQUEST);
              case REGISTRATION_CLIENT_URI:
                throw new ValidationException("invalid_client_metadata",
                    "Software statement can't include a client configuration endpoint",
                    HttpStatus.BAD_REQUEST);
              case REGISTRATION_ACCESS_TOKEN:
                throw new ValidationException("invalid_client_metadata",
                    "Software statement can't include a client registration access token",
                    HttpStatus.BAD_REQUEST);
              case REQUEST_URIS:
                newClient.setRequestUris(Sets.newHashSet(claimSet.getStringListClaim(claim)));
                break;
              case POST_LOGOUT_REDIRECT_URIS:
                newClient
                  .setPostLogoutRedirectUris(Sets.newHashSet(claimSet.getStringListClaim(claim)));
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
                newClient.setTokenEndpointAuthSigningAlg(
                    JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
                break;
              case ID_TOKEN_ENCRYPTED_RESPONSE_ENC:
                newClient.setIdTokenEncryptedResponseEnc(
                    EncryptionMethod.parse(claimSet.getStringClaim(claim)));
                break;
              case ID_TOKEN_ENCRYPTED_RESPONSE_ALG:
                newClient.setIdTokenEncryptedResponseAlg(
                    JWEAlgorithm.parse(claimSet.getStringClaim(claim)));
                break;
              case ID_TOKEN_SIGNED_RESPONSE_ALG:
                newClient
                  .setIdTokenSignedResponseAlg(JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
                break;
              case USERINFO_ENCRYPTED_RESPONSE_ENC:
                newClient.setUserInfoEncryptedResponseEnc(
                    EncryptionMethod.parse(claimSet.getStringClaim(claim)));
                break;
              case USERINFO_ENCRYPTED_RESPONSE_ALG:
                newClient.setUserInfoEncryptedResponseAlg(
                    JWEAlgorithm.parse(claimSet.getStringClaim(claim)));
                break;
              case USERINFO_SIGNED_RESPONSE_ALG:
                newClient
                  .setUserInfoSignedResponseAlg(JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
                break;
              case REQUEST_OBJECT_SIGNING_ALG:
                newClient
                  .setRequestObjectSigningAlg(JWSAlgorithm.parse(claimSet.getStringClaim(claim)));
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
                newClient.setTokenEndpointAuthMethod(
                    AuthMethod.getByValue(claimSet.getStringClaim(claim)));
                break;
              case TOS_URI:
                newClient.setTosUri(claimSet.getStringClaim(claim));
                break;
              case CONTACTS:
                newClient.setContacts(Sets.newHashSet(claimSet.getStringListClaim(claim)));
                break;
              case LOGO_URI:
                newClient.setLogoUri(claimSet.getStringClaim(claim));
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
                throw new ValidationException("invalid_client_metadata",
                    "Software statement can't contain client secret", HttpStatus.BAD_REQUEST);
              case CLIENT_ID:
                throw new ValidationException("invalid_client_metadata",
                    "Software statement can't contain client ID", HttpStatus.BAD_REQUEST);

              default:
                LOG.warn("Software statement contained unknown field: " + claim + " with value "
                    + claimSet.getClaim(claim));
                break;
            }
          }

          return newClient;
        } catch (ParseException e) {
          throw new ValidationException("invalid_client_metadata",
              "Software statement claims didn't parse", HttpStatus.BAD_REQUEST);
        }
      } else {
        throw new ValidationException("invalid_client_metadata",
            "Software statement rejected by validator", HttpStatus.BAD_REQUEST);
      }
    } else {
      // nothing to see here, carry on
      return newClient;
    }
  }
}
