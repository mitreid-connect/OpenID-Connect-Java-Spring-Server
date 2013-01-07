package org.mitre.openid.connect;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.mitre.oauth2.exception.NonceReuseException;
import org.mitre.openid.connect.model.Nonce;
import org.mitre.openid.connect.service.NonceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.AuthorizationRequestManager;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class ConnectAuthorizationRequestManager implements AuthorizationRequestManager {

	@Autowired
	private NonceService nonceService;
	
	@Autowired
	private ClientDetailsService clientDetailsService;
	
	//TODO how to specify this? Should use int "nonceValiditySeconds" instead?
	private Period nonceStorageDuration = new Period(1, 0, 0, 0, 0, 0, 0, 0);

	/**
	 * Constructor with arguments
	 * 
	 * @param clientDetailsService
	 * @param nonceService
	 */
	public ConnectAuthorizationRequestManager(ClientDetailsService clientDetailsService, NonceService nonceService) {
		this.clientDetailsService = clientDetailsService;
		this.nonceService = nonceService;
	}
	
	/**
	 * Default empty constructor
	 */
	public ConnectAuthorizationRequestManager() {
		
	}

	@Override
	public AuthorizationRequest createAuthorizationRequest(Map<String, String> parameters) {

		String clientId = parameters.get("client_id");
		if (clientId == null) {
			throw new InvalidClientException("A client id must be provided");
		}
		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
		
		String requestNonce = parameters.get("nonce");
		
		//Check request nonce for reuse
		Collection<Nonce> clientNonces = nonceService.getByClientId(client.getClientId());
		for (Nonce nonce : clientNonces) {
			if (nonce.getValue().equals(requestNonce)) {
				throw new NonceReuseException(client.getClientId(), nonce);
			}
		}
		
		//Store nonce
		Nonce nonce = new Nonce();
		nonce.setClientId(client.getClientId());
		nonce.setValue(requestNonce);
		DateTime now = new DateTime(new Date());
		nonce.setUseDate(now.toDate());
		DateTime expDate = now.plus(nonceStorageDuration);
		Date expirationJdkDate = expDate.toDate();
		nonce.setExpireDate(expirationJdkDate);
		
		nonceService.save(nonce);
		
		
		Set<String> scopes = OAuth2Utils.parseParameterList(parameters.get("scope"));
		if ((scopes == null || scopes.isEmpty())) {
			//TODO: do we want to allow default scoping at all?
			// If no scopes are specified in the incoming data, it is possible to default to the client's 
			//registered scopes, but minus the "openid" scope. OpenID Connect requests MUST have the "openid" scope.
			Set<String> clientScopes = client.getScope();
			if (clientScopes.contains("openid")) {
				clientScopes.remove("openid");
			}
			scopes = clientScopes;
		}
		DefaultAuthorizationRequest request = new DefaultAuthorizationRequest(parameters, Collections.<String, String> emptyMap(), clientId, scopes);
		request.addClientDetails(client);
		return request;

	}

	@Override
	public void validateParameters(Map<String, String> parameters, ClientDetails clientDetails) {
		if (parameters.containsKey("scope")) {
			if (clientDetails.isScoped()) {
				Set<String> validScope = clientDetails.getScope();
				for (String scope : OAuth2Utils.parseParameterList(parameters.get("scope"))) {
					if (!validScope.contains(scope)) {
						throw new InvalidScopeException("Invalid scope: " + scope, validScope);
					}
				}
			}
		}
	}
	
	/**
	 * @return the nonceStorageDuration
	 */
	public Period getNonceStorageDuration() {
		return nonceStorageDuration;
	}

	/**
	 * @param nonceStorageDuration the nonceStorageDuration to set
	 */
	public void setNonceStorageDuration(Period nonceStorageDuration) {
		this.nonceStorageDuration = nonceStorageDuration;
	}
	

}
