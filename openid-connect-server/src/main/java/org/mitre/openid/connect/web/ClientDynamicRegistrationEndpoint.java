package org.mitre.openid.connect.web;

import java.util.Map;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthType;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

@Controller
@RequestMapping(value = "register", method = RequestMethod.POST)
public class ClientDynamicRegistrationEndpoint {

	@Autowired
	private ClientDetailsEntityService clientService;
	
	
	@RequestMapping(params = "type=client_associate")
	public String clientAssociate(
			// TODO: use @InitBinding or @ModelAttribute to clean up this data processing
			@RequestParam(value = "contacts", required = false) String contacts,
			@RequestParam(value = "application_type", required = false) String applicationType,
			@RequestParam(value = "application_name", required = false) String applicationName,
			@RequestParam(value = "logo_url", required = false) String logoUrl,
			@RequestParam(value = "redirect_uris", required = false) String redirectUris,
			@RequestParam(value = "token_endpoint_auth_type", required = false) String tokenEndpointAuthType,
			@RequestParam(value = "policy_url", required = false) String policyUrl,
			@RequestParam(value = "jwk_url", required = false) String jwkUrl,
			@RequestParam(value = "jwk_encryption_url", required = false) String jwkEncryptionUrl,
			@RequestParam(value = "x509_url", required = false) String x509Url,
			@RequestParam(value = "x509_encryption_url", required = false) String x509EncryptionUrl,
			@RequestParam(value = "sector_identifier_url", required = false) String sectorIdentifierUrl,
			@RequestParam(value = "user_id_type", required = false) String userIdType,
			@RequestParam(value = "require_signed_request_object", required = false) String requireSignedRequestObject,
			@RequestParam(value = "userinfo_signed_response_alg", required = false) String userinfoSignedResponseAlg,
			@RequestParam(value = "userinfo_encrypted_response_alg", required = false) String userinfoEncryptedResponseAlg,
			@RequestParam(value = "userinfo_encrypted_response_enc", required = false) String userinfoEncryptedResponseEnc,
			@RequestParam(value = "userinfo_encrypted_response_int", required = false) String userinfoEncryptedResponseInt,
			@RequestParam(value = "idtoken_signed_response_alg", required = false) String idtokenSignedResponseAlg,
			@RequestParam(value = "idtoken_encrypted_response_alg", required = false) String idtokenEncryptedResponseAlg,
			@RequestParam(value = "idtoken_encrypted_response_enc", required = false) String idtokenEncryptedResponseEnc,
			@RequestParam(value = "idtoken_encrypted_response_int", required = false) String idtokenEncryptedResponseInt,
			@RequestParam(value = "default_max_age", required = false) Integer defaultMaxAge,
			@RequestParam(value = "require_auth_time", required = false) Boolean requireAuthTime,
			@RequestParam(value = "default_acr", required = false) String defaultAcr,
			@RequestParam Map<String, String> parameters
			) {
		
		
		// Create a new Client
		
		ClientDetailsEntity client = new ClientDetailsEntity();

		// always generate a new client ID and secret for this client
		client = clientService.generateClientId(client);
		client = clientService.generateClientSecret(client);
		
		parameters.containsKey(key)
		
		if (!Strings.isNullOrEmpty(contacts)) {
			// space-separated list
			client.setContacts(Sets.newHashSet(Splitter.on(" ").split(contacts)));
		}
		
		if (!Strings.isNullOrEmpty(applicationType)) {
			client.setApplicationType(AppType.getByValue(applicationType));
		}
		
		if (!Strings.isNullOrEmpty(applicationName)) {
			client.setApplicationName(applicationName);
		}
		
		if (!Strings.isNullOrEmpty(logoUrl)) {
			client.setLogoUrl(logoUrl);
		}
		
		if (!Strings.isNullOrEmpty(redirectUris)) {
			// space-separated list
			client.setRegisteredRedirectUri(Sets.newHashSet(Splitter.on(" ").split(redirectUris)));
		}
		
		if (!Strings.isNullOrEmpty(tokenEndpointAuthType)) {
			client.setTokenEndpointAuthType(AuthType.getByValue(tokenEndpointAuthType));
		}
		
		if (!Strings.isNullOrEmpty(policyUrl)) {
			client.setPolicyUrl(policyUrl);
		}
		
		if (!Strings.is)
		
		return "clientAssociate";
	}
	
	@RequestMapping(params = "type=rotate_secret")
	public String rotateSecret(@RequestParam("client_id") String clientId, @RequestParam("client_secret") String clientSecret) {
		
		
		
		return "clientAssociate";
	}
	
	@RequestMapping(params = "type=client_update")
	public String clientUpdate() {
		
		return "clientUpdate";
	}
	
}
