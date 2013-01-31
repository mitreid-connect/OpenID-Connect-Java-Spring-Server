package org.mitre.openid.connect.web;

import java.beans.PropertyEditorSupport;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.primitives.Booleans;

@Controller
@RequestMapping(value = "register"/*, method = RequestMethod.POST*/)
public class ClientDynamicRegistrationEndpoint {

	@Autowired
	private ClientDetailsEntityService clientService;
	
	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Autowired
	private SystemScopeService scopeService;
	
	/**
	 * Bind utility data types to their classes
	 * @param binder
	 */
	@InitBinder
	public void utilityDataInitBinder(WebDataBinder binder) {
		
		/*
		 * Application type
		 */
		binder.registerCustomEditor(AppType.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(AppType.getByValue(text));
				}
			}
			
			@Override
			public String getAsText() {
				AppType at = (AppType) getValue();
				return at == null ? null : at.getValue();
			}
		});

		/*
		 * Authentication type
		 */
		binder.registerCustomEditor(AuthMethod.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(AuthMethod.getByValue(text));
				}
			}
			
			@Override
			public String getAsText() {
				AuthMethod at = (AuthMethod) getValue();
				return at == null ? null : at.getValue();
			}
		});

		/*
		 * UserID type
		 */
		binder.registerCustomEditor(SubjectType.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(SubjectType.getByValue(text));
				}
			}
			
			@Override
			public String getAsText() {
				SubjectType ut = (SubjectType) getValue();
				return ut == null ? null : ut.getValue();
			}
		});
		
		/*
		 * JWS Algorithm
		 */
		binder.registerCustomEditor(JwsAlgorithm.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(JwsAlgorithm.getByJwaName(text));
				}
			}
			
			@Override
			public String getAsText() {
				JwsAlgorithm alg = (JwsAlgorithm) getValue();
				return alg == null ? null : alg.getJwaName();
			}
		});

		
		// FIXME: JWE needs to be handled much better than it is right now
		/*
		binder.registerCustomEditor(JweAlgorithms.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(JweAlgorithms.getByJwaName(text));
				}
			}
			
			@Override
			public String getAsText() {
				JweAlgorithms alg = (JweAlgorithms) getValue();
				return alg == null ? null : alg.getJwaName();
			}
		});
		*/
		
	}
	
	/**
	 * Bind a space-separated string to a Set<String>
	 * @param binder
	 */
	@InitBinder({"contacts", "redirect_uris", "scope", "grant_type"})
	public void stringSetInitbinder(WebDataBinder binder) {
		/*
		 * Space-separated set of strings
		 */
		binder.registerCustomEditor(Set.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(Sets.newHashSet(Splitter.on(" ").split(text)));
				}
			}
			
			@Override
			public String getAsText() {
				Set<String> set = (Set<String>) getValue();
				return set == null ? null : Joiner.on(" ").join(set);
			}
		});
	}
	
	@RequestMapping(params = "operation=client_register", produces = "application/json")
	public String clientRegister(
			@RequestParam(value = "redirect_uris", required = true) Set<String> redirectUris,
			@RequestParam(value = "client_name", required = false) String clientName,
			@RequestParam(value = "client_url", required = false) String clientUrl,
			@RequestParam(value = "logo_url", required = false) String logoUrl,
			@RequestParam(value = "contacts", required = false) Set<String> contacts,
			@RequestParam(value = "tos_url", required = false) String tosUrl,
			@RequestParam(value = "token_endpoint_auth_method", required = false) AuthMethod tokenEndpointAuthMethod,
			@RequestParam(value = "policy_url", required = false) String policyUrl,
			
			@RequestParam(value = "scope", required = false) Set<String> scope,
			@RequestParam(value = "grant_type", required = false) Set<String> grantType,
			
			@RequestParam(value = "jwk_url", required = false) String jwkUrl,
			@RequestParam(value = "jwk_encryption_url", required = false) String jwkEncryptionUrl,
			@RequestParam(value = "x509_url", required = false) String x509Url,
			@RequestParam(value = "x509_encryption_url", required = false) String x509EncryptionUrl,
			@RequestParam(value = "default_max_age", required = false) Integer defaultMaxAge,
			@RequestParam(value = "default_acr", required = false) String defaultAcr,
			
			// OPENID CONNECT EXTENSIONS BELOW
			@RequestParam(value = "application_type", required = false) AppType applicationType,
			@RequestParam(value = "sector_identifier_url", required = false) String sectorIdentifierUrl,
			@RequestParam(value = "subject_type", required = false) SubjectType subjectType,
			@RequestParam(value = "require_signed_request_object", required = false) JwsAlgorithm requireSignedRequestObject,
			// TODO: JWE needs to be handled properly, see @InitBinder above -- we'll ignore these right now
			/*
			@RequestParam(value = "userinfo_signed_response_alg", required = false) String userinfoSignedResponseAlg,
			@RequestParam(value = "userinfo_encrypted_response_alg", required = false) String userinfoEncryptedResponseAlg,
			@RequestParam(value = "userinfo_encrypted_response_enc", required = false) String userinfoEncryptedResponseEnc,
			@RequestParam(value = "userinfo_encrypted_response_int", required = false) String userinfoEncryptedResponseInt,
			@RequestParam(value = "idtoken_signed_response_alg", required = false) String idtokenSignedResponseAlg,
			@RequestParam(value = "idtoken_encrypted_response_alg", required = false) String idtokenEncryptedResponseAlg,
			@RequestParam(value = "idtoken_encrypted_response_enc", required = false) String idtokenEncryptedResponseEnc,
			@RequestParam(value = "idtoken_encrypted_response_int", required = false) String idtokenEncryptedResponseInt,
			*/
			
			@RequestParam(value = "require_auth_time", required = false, defaultValue = "true") Boolean requireAuthTime,
			ModelMap model
			) {
		
		
		// Create a new Client
		
		ClientDetailsEntity client = new ClientDetailsEntity();

		// if it's not using a private key or no auth, then generate a secret
		if (tokenEndpointAuthMethod != AuthMethod.PRIVATE_KEY && tokenEndpointAuthMethod != AuthMethod.NONE) {
			client = clientService.generateClientSecret(client);
		}
		
		client.setContacts(contacts);
		client.setApplicationType(applicationType);
		client.setClientName(clientName);
		client.setClientUrl(clientUrl);
		client.setTosUrl(tosUrl);
		client.setLogoUrl(logoUrl);
		client.setRegisteredRedirectUri(redirectUris);
		client.setTokenEndpointAuthMethod(tokenEndpointAuthMethod);
		client.setPolicyUrl(policyUrl);
		client.setJwkUrl(jwkUrl);
		client.setJwkEncryptionUrl(jwkEncryptionUrl);
		client.setX509Url(x509Url);
		client.setX509EncryptionUrl(x509EncryptionUrl);
		client.setSectorIdentifierUrl(sectorIdentifierUrl);
		client.setSubjectType(subjectType);
		client.setRequireSignedRequestObject(requireSignedRequestObject);
		client.setDefaultMaxAge(defaultMaxAge);
		client.setRequireAuthTime(requireAuthTime == null ? false : requireAuthTime.booleanValue());
		client.setDefaultACR(defaultAcr);

		// set of scopes that are OK for clients to dynamically register for
		Set<SystemScope> dynScopes = scopeService.getDynReg();
		
		// scopes that the client is asking for
		Set<SystemScope> requestedScopes = scopeService.fromStrings(scope);
		if (requestedScopes == null) {
			requestedScopes = scopeService.getDefaults();
		}
		
		// the scopes that the client can have must be a subset of the dynamically allowed scopes
		Set<SystemScope> allowedScopes = Sets.intersection(dynScopes, requestedScopes);

		client.setScope(scopeService.toStrings(allowedScopes));
		
		
		
		if (grantType != null) {
			// TODO: check against some kind of grant type service for validity
			client.setAuthorizedGrantTypes(grantType);
		} else {
			client.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code", "refresh_token")); // allow authorization code and refresh token grant types
		}
		
		// defaults for SECOAUTH functionality
		// TODO: extensions to request, or configuration?
		client.setAccessTokenValiditySeconds((int)TimeUnit.HOURS.toSeconds(1)); // access tokens good for 1hr
		client.setIdTokenValiditySeconds((int)TimeUnit.MINUTES.toSeconds(10)); // id tokens good for 10min
		client.setRefreshTokenValiditySeconds(null); // refresh tokens good until revoked
		
		client.setDynamicallyRegistered(true);
		
		ClientDetailsEntity saved = clientService.saveNewClient(client);
		
		OAuth2AccessTokenEntity registrationAccessToken = createRegistrationAccessToken(client);
		
		model.put("fullClient", Boolean.TRUE);
		model.put("client", saved);
		model.put("token", registrationAccessToken);
		
		return "clientRegistration";
	}

	/**
     * @param client
     * @return
     * @throws AuthenticationException
     */
    private OAuth2AccessTokenEntity createRegistrationAccessToken(ClientDetailsEntity client) throws AuthenticationException {
	    // create a registration access token, treat it like a client credentials flow
		// I can't use the auth request interface here because it has no setters and bad constructors -- THIS IS BAD API DESIGN
		DefaultAuthorizationRequest authorizationRequest = new DefaultAuthorizationRequest(client.getClientId(), Sets.newHashSet(OAuth2AccessTokenEntity.REGISTRATION_TOKEN_SCOPE));
		authorizationRequest.setApproved(true);
		authorizationRequest.setAuthorities(Sets.newHashSet(new SimpleGrantedAuthority("ROLE_CLIENT")));
		OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest, null);
		OAuth2AccessTokenEntity registrationAccessToken = (OAuth2AccessTokenEntity) tokenService.createAccessToken(authentication);
	    return registrationAccessToken;
    }
	
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('registration-token')")
	@RequestMapping(params = "operation=rotate_secret", produces = "application/json")
	public String rotateSecret(OAuth2Authentication auth, ModelMap model) {
		
		
		String clientId = auth.getAuthorizationRequest().getClientId();
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

		if (client == null) {
			throw new ClientNotFoundException("Could not find client: " + clientId);
		}
		
		// rotate the secret, if available
		if (client.isSecretRequired()) {
			client = clientService.generateClientSecret(client);
		}
		
		// mint a new access token
		OAuth2AccessTokenEntity registrationAccessToken = createRegistrationAccessToken(client);

		// revoke the old one
		OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
		if (details != null) {
			OAuth2AccessTokenEntity oldAccessToken = tokenService.readAccessToken(details.getTokenValue());
			if (oldAccessToken != null) {
				tokenService.revokeAccessToken(oldAccessToken);
			} else {
				// serious error here -- how'd we get this far without a valid token?!
				throw new OAuth2Exception("SEVERE: token not found, something is fishy");
			}
		}
		
		// save the client
		ClientDetailsEntity saved = clientService.updateClient(client, client);
		
		model.put("fullClient", Boolean.FALSE);
		model.put("client", saved);
		model.put("token", registrationAccessToken);
		
		return "clientRegistration";
	}
	
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('registration-token')")
	@RequestMapping(params = "operation=client_update", produces = "application/json")
	public String clientUpdate(
			@RequestParam(value = "redirect_uris", required = true) Set<String> redirectUris,
			@RequestParam(value = "client_name", required = false) String clientName,
			@RequestParam(value = "client_url", required = false) String clientUrl,
			@RequestParam(value = "logo_url", required = false) String logoUrl,
			@RequestParam(value = "contacts", required = false) Set<String> contacts,
			@RequestParam(value = "tos_url", required = false) String tosUrl,
			@RequestParam(value = "token_endpoint_auth_method", required = false) AuthMethod tokenEndpointAuthMethod,
			@RequestParam(value = "policy_url", required = false) String policyUrl,
			
			@RequestParam(value = "scope", required = false) Set<String> scope,
			@RequestParam(value = "grant_type", required = false) Set<String> grantType,
			
			@RequestParam(value = "jwk_url", required = false) String jwkUrl,
			@RequestParam(value = "jwk_encryption_url", required = false) String jwkEncryptionUrl,
			@RequestParam(value = "x509_url", required = false) String x509Url,
			@RequestParam(value = "x509_encryption_url", required = false) String x509EncryptionUrl,
			@RequestParam(value = "default_max_age", required = false) Integer defaultMaxAge,
			@RequestParam(value = "default_acr", required = false) String defaultAcr,
			
			// OPENID CONNECT EXTENSIONS BELOW
			@RequestParam(value = "application_type", required = false) AppType applicationType,
			@RequestParam(value = "sector_identifier_url", required = false) String sectorIdentifierUrl,
			@RequestParam(value = "subject_type", required = false) SubjectType subjectType,
			@RequestParam(value = "require_signed_request_object", required = false) JwsAlgorithm requireSignedRequestObject,
			@RequestParam(value = "require_auth_time", required = false, defaultValue = "true") Boolean requireAuthTime,
			// TODO: JWE needs to be handled properly, see @InitBinder above -- we'll ignore these right now
			/*
			@RequestParam(value = "userinfo_signed_response_alg", required = false) String userinfoSignedResponseAlg,
			@RequestParam(value = "userinfo_encrypted_response_alg", required = false) String userinfoEncryptedResponseAlg,
			@RequestParam(value = "userinfo_encrypted_response_enc", required = false) String userinfoEncryptedResponseEnc,
			@RequestParam(value = "userinfo_encrypted_response_int", required = false) String userinfoEncryptedResponseInt,
			@RequestParam(value = "idtoken_signed_response_alg", required = false) String idtokenSignedResponseAlg,
			@RequestParam(value = "idtoken_encrypted_response_alg", required = false) String idtokenEncryptedResponseAlg,
			@RequestParam(value = "idtoken_encrypted_response_enc", required = false) String idtokenEncryptedResponseEnc,
			@RequestParam(value = "idtoken_encrypted_response_int", required = false) String idtokenEncryptedResponseInt,
			*/
			
			@RequestParam Map<String, String> params,
			
			OAuth2Authentication auth,
			ModelMap model
			
			) {
		
		String clientId = auth.getAuthorizationRequest().getClientId();
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
		
		if (client == null) {
			throw new ClientNotFoundException("Could not find client: " + clientId);
		}
		
		/*
		 * now process each field:
		 *   1) If input is not provided (null, not in map), keep existing value
		 *   2) If input is provided (in map) but null or blank, remove existing value
		 *   3) If input is not null and not blank, replace existing value
		 */
		if (params.containsKey("contacts")) {
			client.setContacts(contacts);
		}
		if (params.containsKey("application_type")) {
			client.setApplicationType(applicationType);
		}
		if (params.containsKey("client_name")) {
			client.setClientName(Strings.emptyToNull(clientName));
		}
		if (params.containsKey("client_url")) {
			client.setClientUrl(Strings.emptyToNull(clientUrl));
		}
		if (params.containsKey("tos_url")) {
			client.setTosUrl(Strings.emptyToNull(tosUrl));
		}
		if (params.containsKey("logo_url")) {
			client.setLogoUrl(Strings.emptyToNull(logoUrl));
		}
		if (params.containsKey("redirect_uris")) {
			client.setRegisteredRedirectUri(redirectUris);
		}
		if (params.containsKey("token_endpoint_auth_method")) {
			client.setTokenEndpointAuthMethod(tokenEndpointAuthMethod);
		}
		if (params.containsKey("policy_url")) {
			client.setPolicyUrl(Strings.emptyToNull(policyUrl));
		}
		if (params.containsKey("jwk_url")) {
			client.setJwkUrl(Strings.emptyToNull(jwkUrl));
		}
		if (params.containsKey("jwk_encryption_url")) {
			client.setJwkEncryptionUrl(Strings.emptyToNull(jwkEncryptionUrl));
		}
		if (params.containsKey("x509_url")) {
			client.setX509Url(Strings.emptyToNull(x509Url));
		}
		if (params.containsKey("x509_encryption_url")) {
			client.setX509EncryptionUrl(Strings.emptyToNull(x509EncryptionUrl));
		}
		if (params.containsKey("default_max_age")) {
			client.setDefaultMaxAge(defaultMaxAge);
		}
		if (params.containsKey("default_acr")) {
			client.setDefaultACR(Strings.emptyToNull(defaultAcr));
		}
		if (params.containsKey("scope")) {
			// set of scopes that are OK for clients to dynamically register for
			Set<SystemScope> dynScopes = scopeService.getDynReg();
			
			// scopes that the client is asking for
			Set<SystemScope> requestedScopes = scopeService.fromStrings(scope);

			// the scopes that the client can have must be a subset of the dynamically allowed scopes
			Set<SystemScope> allowedScopes = Sets.intersection(dynScopes, requestedScopes);

			client.setScope(scopeService.toStrings(allowedScopes));
		}
		if (params.containsKey("grant_type")) {
			// TODO: check against some kind of grant type service for validity
			client.setAuthorizedGrantTypes(grantType);
		}
		
		
		// OIDC
		if (params.containsKey("sector_identifier_url")) {
			client.setSectorIdentifierUrl(Strings.emptyToNull(sectorIdentifierUrl));
		}
		if (params.containsKey("subject_type")) {
			client.setSubjectType(subjectType);
		}
		if (params.containsKey("require_signed_request_object")) { // TODO: rename field
			client.setRequireSignedRequestObject(requireSignedRequestObject);
		}
		if (params.containsKey("require_auth_time")) {
			client.setRequireAuthTime(requireAuthTime == null ? false : requireAuthTime.booleanValue()); // watch out for autoboxing
		}
		

		ClientDetailsEntity saved = clientService.updateClient(client, client);
		
		model.put("fullClient", Boolean.TRUE);
		model.put("client", saved);
		return "clientRegister";
	}
	
}
