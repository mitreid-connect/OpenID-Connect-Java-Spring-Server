// FIXME: update to latest DynReg spec

package org.mitre.openid.connect.web;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;
import org.mitre.jose.JWSAlgorithmEmbed;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.AuthorizationRequestManager;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@Controller
@RequestMapping(value = "register")
public class ClientDynamicRegistrationEndpoint {

	@Autowired
	private ClientDetailsEntityService clientService;
	
	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Autowired
	private SystemScopeService scopeService;
	
	@Autowired
	private AuthorizationRequestManager authorizationRequestManager;
	
	private static Logger logger = LoggerFactory.getLogger(ClientDynamicRegistrationEndpoint.class);
	private JsonParser parser = new JsonParser();
	private Gson gson = new Gson();
	
	/**
	 * Create a new Client, issue a client ID, and create a registration access token.
	 * @param jsonString
	 * @param m
	 * @param p
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public String registerNewClient(@RequestBody String jsonString, Model m) {
		
		ClientDetailsEntity newClient = parse(jsonString);
		
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

			// if the client didn't ask for any, give them the defaults
			if (requestedScopes == null || requestedScopes.isEmpty()) {
				requestedScopes = scopeService.getDefaults();
			}

			// the scopes that the client can have must be a subset of the dynamically allowed scopes
			Set<SystemScope> allowedScopes = Sets.intersection(dynScopes, requestedScopes);

			newClient.setScope(scopeService.toStrings(allowedScopes));
			

			// set default grant types if needed
			if (newClient.getGrantTypes() == null || newClient.getGrantTypes().isEmpty()) { 
				newClient.setGrantTypes(Sets.newHashSet("authorization_code", "refresh_token")); // allow authorization code and refresh token grant types by default
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
			
			// now save it
			ClientDetailsEntity savedClient = clientService.saveNewClient(newClient);
			
			// generate the registration access token
			OAuth2AccessTokenEntity token = createRegistrationAccessToken(savedClient);
			
			// send it all out to the view
			m.addAttribute("client", savedClient);
			m.addAttribute("code", HttpStatus.CREATED); // http 201
			m.addAttribute("token", token);
			
			return "clientInformationResponseView";
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
		
		if (client != null && client.getClientId().equals(auth.getAuthorizationRequest().getClientId())) {

			
			// we return the token that we got in
			OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
			OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());
			
			// send it all out to the view
			m.addAttribute("client", client);
			m.addAttribute("code", HttpStatus.OK); // http 200
			m.addAttribute("token", token);
			
			return "clientInformationResponseView";
		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: " 
					+ clientId + " and " + auth.getAuthorizationRequest().getClientId() + " do not match.");
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

		
		ClientDetailsEntity newClient = parse(jsonString);
		ClientDetailsEntity oldClient = clientService.loadClientByClientId(clientId);
		
		if (newClient != null && oldClient != null  // we have an existing client and the new one parsed
				&& oldClient.getClientId().equals(auth.getAuthorizationRequest().getClientId()) // the client passed in the URI matches the one in the auth
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
			
			// save the client
			ClientDetailsEntity savedClient = clientService.updateClient(oldClient, newClient);
			
			// we return the token that we got in
			// TODO: rotate this after some set amount of time
			OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
			OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());
			
			// send it all out to the view
			m.addAttribute("client", savedClient);
			m.addAttribute("code", HttpStatus.OK); // http 200
			m.addAttribute("token", token);
			
			return "clientInformationResponseView";
		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: " 
					+ clientId + " and " + auth.getAuthorizationRequest().getClientId() + " do not match.");
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
		
		if (client != null && client.getClientId().equals(auth.getAuthorizationRequest().getClientId())) {

			clientService.deleteClient(client);
			
			// we return the token that we got in
			OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
			OAuth2AccessTokenEntity token = tokenService.readAccessToken(details.getTokenValue());
			
			// send it all out to the view
			m.addAttribute("client", client);
			m.addAttribute("code", HttpStatus.OK); // http 200
			m.addAttribute("token", token);
			
			return "clientInformationResponseView";
		} else {
			// client mismatch
			logger.error("readClientConfiguration failed, client ID mismatch: " 
					+ clientId + " and " + auth.getAuthorizationRequest().getClientId() + " do not match.");
			m.addAttribute("code", HttpStatus.FORBIDDEN); // http 403
			
			return "httpCodeView";
		}
	}
	
	
	
	
	/**
	 * 
	 * Create an unbound ClientDetailsEntity from the given JSON string.
	 * 
	 * @param jsonString
	 * @return the entity if successful, null otherwise
	 */
    private ClientDetailsEntity parse(String jsonString) {
		JsonElement jsonEl = parser.parse(jsonString);
		if (jsonEl.isJsonObject()) {

			JsonObject o = jsonEl.getAsJsonObject();
			ClientDetailsEntity c = new ClientDetailsEntity();
			
			// TODO: make these field names into constants

			// these two fields should only be sent in the update request, and MUST match existing values
			c.setClientId(getAsString(o, "client_id"));
			c.setClientSecret(getAsString(o, "client_secret"));
			
			// OAuth DynReg
			c.setRedirectUris(getAsStringSet(o, "redirect_uris"));
			c.setClientName(getAsString(o, "client_name"));
			c.setClientUri(getAsString(o, "client_uri"));
			c.setLogoUri(getAsString(o, "logo_uri"));
			c.setContacts(getAsStringSet(o, "contacts"));
			c.setTosUri(getAsString(o, "tos_uri"));
			
			String authMethod = getAsString(o, "token_endpoint_auth_method");
			if (authMethod != null) {
				c.setTokenEndpointAuthMethod(AuthMethod.getByValue(authMethod));
			}
			
			// scope is a space-separated string
			String scope = getAsString(o, "scope");
			if (scope != null) {
				c.setScope(Sets.newHashSet(Splitter.on(" ").split(scope)));
			}
			
			c.setGrantTypes(getAsStringSet(o, "grant_types"));
			c.setPolicyUri(getAsString(o, "policy_uri"));
			c.setJwksUri(getAsString(o, "jwks_uri"));
			
			
			// OIDC Additions
			String appType = getAsString(o, "application_type");
			if (appType != null) {
				c.setApplicationType(AppType.getByValue(appType));
			}
			
			c.setSectorIdentifierUri(getAsString(o, "sector_identifier_uri"));
			
			String subjectType = getAsString(o, "subject_type");
			if (subjectType != null) {
				c.setSubjectType(SubjectType.getByValue(subjectType));
			}
			
			c.setRequestObjectSigningAlg(getAsJwsAlgorithm(o, "request_object_signing_alg"));
			
			c.setUserInfoSignedResponseAlg(getAsJwsAlgorithm(o, "userinfo_signed_response_alg"));
			c.setUserInfoEncryptedResponseAlg(getAsJweAlgorithm(o, "userinfo_encrypted_response_alg"));
			c.setUserInfoEncryptedResponseEnc(getAsJweEncryptionMethod(o, "userinfo_encrypted_response_enc"));
			
			c.setIdTokenSignedResponseAlg(getAsJwsAlgorithm(o, "id_token_signed_response_alg"));
			c.setIdTokenEncryptedResponseAlg(getAsJweAlgorithm(o, "id_token_encrypted_response_alg"));
			c.setIdTokenEncryptedResponseEnc(getAsJweEncryptionMethod(o, "id_token_encrypted_response_enc"));
			
			if (o.has("default_max_age")) {
				if (o.get("default_max_age").isJsonPrimitive()) {
					c.setDefaultMaxAge(o.get("default_max_age").getAsInt());
				}
			}
			
			if (o.has("require_auth_time")) {
				if (o.get("require_auth_time").isJsonPrimitive()) {
					c.setRequireAuthTime(o.get("require_auth_time").getAsBoolean());
				}
			}
			
			c.setDefaultACRvalues(getAsStringSet(o, "default_acr_values"));
			c.setInitiateLoginUri(getAsString(o, "initiate_login_uri"));
			c.setPostLogoutRedirectUri(getAsString(o, "post_logout_redirect_uri"));
			c.setRequestUris(getAsStringSet(o, "request_uris"));
			
			return c;
		} else {
	    	return null;
		}
    }

	/**
	 * Gets the value of the given given member as a set of strings, null if it doesn't exist
	 */
    private Set<String> getAsStringSet(JsonObject o, String member) throws JsonSyntaxException {
    	if (o.has(member)) {
    		return gson.fromJson(o.get(member), new TypeToken<Set<String>>(){}.getType());
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets the value of the given member as a string, null if it doesn't exist
     */
    private String getAsString(JsonObject o, String member) {
    	if (o.has(member)) {
    		JsonElement e = o.get(member);
    		if (e != null && e.isJsonPrimitive()) {
    			return e.getAsString();
    		} else {
    			return null;
    		}
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets the value of the given member as a JWS Algorithm, null if it doesn't exist
     */
    private JWSAlgorithmEmbed getAsJwsAlgorithm(JsonObject o, String member) {
    	String s = getAsString(o, member);
    	if (s != null) {
    		return JWSAlgorithmEmbed.getForAlgorithmName(s);
    	} else {
    		return null;
    	}
    }

    /**
     * Gets the value of the given member as a JWE Algorithm, null if it doesn't exist
     */
    private JWEAlgorithmEmbed getAsJweAlgorithm(JsonObject o, String member) {
    	String s = getAsString(o, member);
    	if (s != null) {
    		return JWEAlgorithmEmbed.getForAlgorithmName(s);
    	} else {
    		return null;
    	}
    }
    

    /**
     * Gets the value of the given member as a JWE Encryption Method, null if it doesn't exist
     */
    private JWEEncryptionMethodEmbed getAsJweEncryptionMethod(JsonObject o, String member) {
    	String s = getAsString(o, member);
    	if (s != null) {
    		return JWEEncryptionMethodEmbed.getForAlgorithmName(s);
    	} else {
    		return null;
    	}
    }
	/**
     * @param client
     * @return
     * @throws AuthenticationException
     */
    private OAuth2AccessTokenEntity createRegistrationAccessToken(ClientDetailsEntity client) throws AuthenticationException {
    	
    	Map<String, String> authorizationParameters = Maps.newHashMap();
    	authorizationParameters.put("client_id", client.getClientId());
    	authorizationParameters.put("scope", OAuth2AccessTokenEntity.REGISTRATION_TOKEN_SCOPE);
    	AuthorizationRequest authorizationRequest = authorizationRequestManager.createAuthorizationRequest(authorizationParameters);
    	authorizationRequest.setApproved(true);
    	authorizationRequest.setAuthorities(Sets.newHashSet(new SimpleGrantedAuthority("ROLE_CLIENT")));
		OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest, null);
		OAuth2AccessTokenEntity registrationAccessToken = (OAuth2AccessTokenEntity) tokenService.createAccessToken(authentication);
	    return registrationAccessToken;
    }
	
}