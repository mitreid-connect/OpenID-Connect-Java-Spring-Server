package org.mitre.openid.connect;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.oauth2.exception.NonceReuseException;
import org.mitre.openid.connect.model.Nonce;
import org.mitre.openid.connect.service.NonceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.AuthorizationRequestManager;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component("authorizationRequestManager")
public class ConnectAuthorizationRequestManager implements AuthorizationRequestManager, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(ConnectAuthorizationRequestManager.class);
	
	@Autowired
	private NonceService nonceService;
	
	@Autowired
	private ClientDetailsService clientDetailsService;
	
	private Period nonceStorageDuration;

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
	
	/**
	 * Make sure that the nonce storage duration was set
	 */
	public void afterPropertiesSet() throws Exception {
		if (nonceStorageDuration == null) {
			logger.error("Nonce storage duration must be set!");
		}
	}

	@Override
	public AuthorizationRequest createAuthorizationRequest(Map<String, String> inputParams) {

		Map<String, String> parameters = processRequestObject(inputParams);
		
		String clientId = parameters.get("client_id");
		if (clientId == null) {
			throw new InvalidClientException("A client id must be provided");
		}
		ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
		
		String requestNonce = parameters.get("nonce");
		
		//Only process if the user is authenticated. If the user is not authenticated yet, this 
		//code will be called a second time once the user is redirected from the login page back 
		//to the auth endpoint.
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if (requestNonce != null && principal != null && principal instanceof Authentication) {

			//Check request nonce for reuse
			Collection<Nonce> clientNonces = nonceService.getByClientId(client.getClientId());
			for (Nonce nonce : clientNonces) {
				String nonceVal = nonce.getValue();
				if (nonceVal.equals(requestNonce)) {
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
	
		}
		
		Set<String> scopes = OAuth2Utils.parseParameterList(parameters.get("scope"));
		if ((scopes == null || scopes.isEmpty())) {
			//TODO: do we want to allow default scoping at all?
			Set<String> clientScopes = client.getScope();
			scopes = clientScopes;
		}
		DefaultAuthorizationRequest request = new DefaultAuthorizationRequest(inputParams, Collections.<String, String> emptyMap(), clientId, scopes);
		request.addClientDetails(client);
		return request;

	}

	/**
	 * @param inputParams
	 * @return
	 */
    private Map<String, String> processRequestObject(Map<String, String> inputParams) {
    	
    	String jwtString = inputParams.get("request");
    	
    	// if there's no request object, bail early
    	if (Strings.isNullOrEmpty(jwtString)) {
    		return inputParams;
    	}

    	// start by copying over what's already in there
    	Map<String, String> parameters = new HashMap<String, String>(inputParams);

    	// parse the request object
		Jwt jwt = Jwt.parse(jwtString);
		JwtClaims claims = jwt.getClaims();
		
		// TODO: validate JWT signature
		
		String clientId = claims.getClaimAsString("client_id");

		// TODO: check parameter consistency, move keys to constants
		
		/*
		 * if (in Claims):
		 * 		if (in params):
		 * 			if (equal):
		 * 				all set
		 * 			else (not equal):
		 * 				error
		 * 		else (not in params):
		 * 			add to params
		 * else (not in claims):
		 * 		we don't care
		 */
		
		String responseTypes = claims.getClaimAsString("response_type");
		if (responseTypes != null) {
			parameters.put("response_type", responseTypes);
		}
		
		if (clientId != null) {
			parameters.put("client_id", clientId);
		}
		
		if (claims.getClaimAsString("redirect_uri") != null) {
			if (inputParams.containsKey("redirect_uri") == false) {
				parameters.put("redirect_uri", claims.getClaimAsString("redirect_uri"));
			}
		}
		
		String state = claims.getClaimAsString("state");
		if(state != null) {
			if (inputParams.containsKey("state") == false) {
				parameters.put("state", state);
			}
		}
		
		String nonce = claims.getClaimAsString("nonce");
		if(nonce != null) {
			if (inputParams.containsKey("nonce") == false) {
				parameters.put("nonce", nonce);
			}
		}
		
		String display = claims.getClaimAsString("display");
		if (display != null) {
			if (inputParams.containsKey("display") == false) {
				parameters.put("display", display);
			}
		}
		
		String prompt = claims.getClaimAsString("prompt");
		if (prompt != null) {
			if (inputParams.containsKey("prompt") == false) {
				parameters.put("prompt", prompt);
			}
		}
		
		String scope = claims.getClaimAsString("scope");
		if (scope != null) {
			if (inputParams.containsKey("scope") == false) {
				parameters.put("scope", scope);
			}
		}
		
		return parameters;
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
