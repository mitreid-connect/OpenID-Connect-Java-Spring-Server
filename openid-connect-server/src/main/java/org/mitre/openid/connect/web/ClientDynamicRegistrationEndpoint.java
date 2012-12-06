package org.mitre.openid.connect.web;

import java.beans.PropertyEditorSupport;
import java.util.Set;

import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthType;
import org.mitre.oauth2.model.ClientDetailsEntity.UserIdType;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
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
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

@Controller
@RequestMapping(value = "register"/*, method = RequestMethod.POST*/)
public class ClientDynamicRegistrationEndpoint {

	@Autowired
	private ClientDetailsEntityService clientService;
	
	@Autowired
	private OAuth2TokenEntityService tokenService;
	
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
		binder.registerCustomEditor(AuthType.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(AuthType.getByValue(text));
				}
			}
			
			@Override
			public String getAsText() {
				AuthType at = (AuthType) getValue();
				return at == null ? null : at.getValue();
			}
		});

		/*
		 * UserID type
		 */
		binder.registerCustomEditor(UserIdType.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (Strings.isNullOrEmpty(text)) {
					setValue(null);
				} else {
					setValue(UserIdType.getByValue(text));
				}
			}
			
			@Override
			public String getAsText() {
				UserIdType ut = (UserIdType) getValue();
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
	@InitBinder({"contacts", "redirect_uris"})
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
	
	@RequestMapping(params = "type=client_associate", produces = "application/json")
	public String clientAssociate(
			@RequestParam(value = "contacts", required = false) Set<String> contacts,
			@RequestParam(value = "application_type", required = false) AppType applicationType,
			@RequestParam(value = "application_name", required = false) String applicationName,
			@RequestParam(value = "logo_url", required = false) String logoUrl,
			@RequestParam(value = "redirect_uris", required = false) Set<String> redirectUris,
			@RequestParam(value = "token_endpoint_auth_type", required = false) AuthType tokenEndpointAuthType,
			@RequestParam(value = "policy_url", required = false) String policyUrl,
			@RequestParam(value = "jwk_url", required = false) String jwkUrl,
			@RequestParam(value = "jwk_encryption_url", required = false) String jwkEncryptionUrl,
			@RequestParam(value = "x509_url", required = false) String x509Url,
			@RequestParam(value = "x509_encryption_url", required = false) String x509EncryptionUrl,
			@RequestParam(value = "sector_identifier_url", required = false) String sectorIdentifierUrl,
			@RequestParam(value = "user_id_type", required = false) UserIdType userIdType,
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
			
			@RequestParam(value = "default_max_age", required = false) Integer defaultMaxAge,
			@RequestParam(value = "require_auth_time", required = false, defaultValue = "true") Boolean requireAuthTime,
			@RequestParam(value = "default_acr", required = false) String defaultAcr,
			ModelMap model
			) {
		
		
		// Create a new Client
		
		ClientDetailsEntity client = new ClientDetailsEntity();

		// always generate a secret for this client
		client = clientService.generateClientSecret(client);
		
		client.setContacts(contacts);
		client.setApplicationType(applicationType);
		client.setApplicationName(applicationName);
		client.setLogoUrl(logoUrl);
		client.setRegisteredRedirectUri(redirectUris);
		client.setTokenEndpointAuthType(tokenEndpointAuthType);
		client.setPolicyUrl(policyUrl);
		client.setJwkUrl(jwkUrl);
		client.setJwkEncryptionUrl(jwkEncryptionUrl);
		client.setX509Url(x509Url);
		client.setX509EncryptionUrl(x509EncryptionUrl);
		client.setSectorIdentifierUrl(sectorIdentifierUrl);
		client.setUserIdType(userIdType);
		client.setRequireSignedRequestObject(requireSignedRequestObject);
		client.setDefaultMaxAge(defaultMaxAge);
		client.setRequireAuthTime(requireAuthTime);
		client.setDefaultACR(defaultAcr);

		// defaults for SECOAUTH functionality
		// TODO: extensions to request, or configuration?
		client.setScope(Sets.newHashSet("openid", "phone", "address", "profile", "email")); // provision all scopes
		client.setAllowRefresh(true); // by default allow refresh tokens on dynamic clients
		client.setAccessTokenValiditySeconds(3600); // access tokens good for 1hr
		client.setIdTokenValiditySeconds(600); // id tokens good for 10min
		client.setRefreshTokenValiditySeconds(null); // refresh tokens good until revoked
		client.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code"));
		
		client.setDynamicallyRegistered(true);
		
		ClientDetailsEntity saved = clientService.saveNewClient(client);
		
		OAuth2AccessTokenEntity registrationAccessToken = createRegistrationAccessToken(client);
		
		model.put("client", saved);
		model.put("token", registrationAccessToken);
		
		return "clientAssociate";
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
	@RequestMapping(params = "type=rotate_secret", produces = "application/json")
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
		
		model.put("client", saved);
		model.put("token", registrationAccessToken);
		
		return "clientAssociate";
	}
	
	@PreAuthorize("hasRole('ROLE_CLIENT') and #oauth2.hasScope('registration-token')")
	@RequestMapping(params = "type=client_update", produces = "application/json")
	public String clientUpdate(
			@RequestParam(value = "contacts", required = false) Set<String> contacts,
			@RequestParam(value = "application_type", required = false) AppType applicationType,
			@RequestParam(value = "application_name", required = false) String applicationName,
			@RequestParam(value = "logo_url", required = false) String logoUrl,
			@RequestParam(value = "redirect_uris", required = false) Set<String> redirectUris,
			@RequestParam(value = "token_endpoint_auth_type", required = false) AuthType tokenEndpointAuthType,
			@RequestParam(value = "policy_url", required = false) String policyUrl,
			@RequestParam(value = "jwk_url", required = false) String jwkUrl,
			@RequestParam(value = "jwk_encryption_url", required = false) String jwkEncryptionUrl,
			@RequestParam(value = "x509_url", required = false) String x509Url,
			@RequestParam(value = "x509_encryption_url", required = false) String x509EncryptionUrl,
			@RequestParam(value = "sector_identifier_url", required = false) String sectorIdentifierUrl,
			@RequestParam(value = "user_id_type", required = false) UserIdType userIdType,
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
			
			@RequestParam(value = "default_max_age", required = false) Integer defaultMaxAge,
			@RequestParam(value = "require_auth_time", required = false) Boolean requireAuthTime,
			@RequestParam(value = "default_acr", required = false) String defaultAcr,
			OAuth2Authentication auth,
			ModelMap model
			
			) {
		
		String clientId = auth.getAuthorizationRequest().getClientId();
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
		
		if (client == null) {
			throw new ClientNotFoundException("Could not find client: " + clientId);
		}
		
		client.setContacts(contacts);
		client.setApplicationType(applicationType);
		client.setApplicationName(applicationName);
		client.setLogoUrl(logoUrl);
		client.setRegisteredRedirectUri(redirectUris);
		client.setTokenEndpointAuthType(tokenEndpointAuthType);
		client.setPolicyUrl(policyUrl);
		client.setJwkUrl(jwkUrl);
		client.setJwkEncryptionUrl(jwkEncryptionUrl);
		client.setX509Url(x509Url);
		client.setX509EncryptionUrl(x509EncryptionUrl);
		client.setSectorIdentifierUrl(sectorIdentifierUrl);
		client.setUserIdType(userIdType);
		client.setRequireSignedRequestObject(requireSignedRequestObject);
		client.setDefaultMaxAge(defaultMaxAge);
		client.setRequireAuthTime(requireAuthTime);
		client.setDefaultACR(defaultAcr);

		ClientDetailsEntity saved = clientService.updateClient(client, client);
		
		model.put("client", saved);
		return "clientUpdate";
	}
	
}
