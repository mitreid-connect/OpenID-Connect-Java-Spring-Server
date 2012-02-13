/**
 * 
 */
package org.mitre.openid.connect.token;

import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.impl.DefaultOAuth2ProviderTokenService;
import org.mitre.openid.connect.model.IdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientCredentialsChecker;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.stereotype.Component;

/**
 * AccessToken granter for Authorization Code flow. 
 * 
 * Note: does this need to be able to grant straight OAuth2.0 Access Tokens as 
 * well as Connect Access Tokens?
 * 
 * 
 * @author AANGANES
 * 
 */
@Component
public class ConnectAuthCodeTokenGranter implements TokenGranter {

	private static final String GRANT_TYPE = "authorization_code";
	
	@Autowired
	private JdbcAuthorizationCodeServices authorizationCodeServices;

	@Autowired
	private ClientCredentialsChecker clientCredentialsChecker;

	@Autowired
	private DefaultOAuth2ProviderTokenService tokenServices;
	
	
	/**
	 * Default empty constructor
	 */
	public ConnectAuthCodeTokenGranter() {
	}
	
	/**
	 * Constructor for unit tests
	 * 
	 * @param tokenServices
	 * @param authorizationCodeServices
	 * @param clientDetailsService
	 */
	public ConnectAuthCodeTokenGranter(
			DefaultOAuth2ProviderTokenService tokenServices,
			JdbcAuthorizationCodeServices authorizationCodeServices,
			ClientDetailsService clientDetailsService) {
		
		setTokenServices(tokenServices);
		setAuthorizationCodeServices(authorizationCodeServices);
		setClientCredentialsChecker(new ClientCredentialsChecker(clientDetailsService));

	}
	

	/**
	 * Grant an OpenID Connect Access Token 
	 * 
	 * @param grantType
	 * @param parameters
	 * @param clientId
	 * @param scope
	 */
	@Override
	public OAuth2AccessToken grant(String grantType,
			Map<String, String> parameters, String clientId, Set<String> scope) {
		
		if (!GRANT_TYPE.equals(grantType)) {
			return null;
		}

		String authorizationCode = parameters.get("code");
		String redirectUri = parameters.get("redirect_uri");

		if (authorizationCode == null) {
			throw new OAuth2Exception("An authorization code must be supplied.");
		}

		AuthorizationRequestHolder storedAuth = authorizationCodeServices.consumeAuthorizationCode(authorizationCode);
		if (storedAuth == null) {
			throw new InvalidGrantException("Invalid authorization code: " + authorizationCode);
		}

		AuthorizationRequest unconfirmedAuthorizationRequest = storedAuth.getAuthenticationRequest();
		if (unconfirmedAuthorizationRequest.getRequestedRedirect() != null
				&& !unconfirmedAuthorizationRequest.getRequestedRedirect().equals(redirectUri)) {
			throw new RedirectMismatchException("Redirect URI mismatch.");
		}

		if (clientId != null && !clientId.equals(unconfirmedAuthorizationRequest.getClientId())) {
			// just a sanity check.
			throw new InvalidClientException("Client ID mismatch");
		}

		// Secret is not required in the authorization request, so it won't be available
		// in the unconfirmedAuthorizationCodeAuth. We do want to check that a secret is provided
		// in the new request, but that happens elsewhere.

		// Similarly scopes are not required in the authorization request, so we don't make a comparison here, just
		// enforce validity through the ClientCredentialsChecker
		AuthorizationRequest authorizationRequest = clientCredentialsChecker.validateCredentials(grantType, clientId,
				unconfirmedAuthorizationRequest.getScope());
		if (authorizationRequest == null) {
			return null;
		}

		Authentication userAuth = storedAuth.getUserAuthentication();
		
		OAuth2AccessTokenEntity token = tokenServices.createAccessToken(new OAuth2Authentication(authorizationRequest, userAuth));
		
		IdToken idToken = new IdToken();
		
		//TODO: build IdToken
		
		//TODO: insert IdToken into OAuth2AccessTokenEntity
		
		return token;
	}

	/**
	 * @return the authorizationCodeServices
	 */
	public JdbcAuthorizationCodeServices getAuthorizationCodeServices() {
		return authorizationCodeServices;
	}

	/**
	 * @param authorizationCodeServices the authorizationCodeServices to set
	 */
	public void setAuthorizationCodeServices(JdbcAuthorizationCodeServices authorizationCodeServices) {
		this.authorizationCodeServices = authorizationCodeServices;
	}

	/**
	 * @return the clientCredentialsChecker
	 */
	public ClientCredentialsChecker getClientCredentialsChecker() {
		return clientCredentialsChecker;
	}

	/**
	 * @param clientCredentialsChecker the clientCredentialsChecker to set
	 */
	public void setClientCredentialsChecker(ClientCredentialsChecker clientCredentialsChecker) {
		this.clientCredentialsChecker = clientCredentialsChecker;
	}

	/**
	 * @return the tokenServices
	 */
	public DefaultOAuth2ProviderTokenService getTokenServices() {
		return tokenServices;
	}

	/**
	 * @param tokenServices the tokenServices to set
	 */
	public void setTokenServices(DefaultOAuth2ProviderTokenService tokenServices) {
		this.tokenServices = tokenServices;
	}

	
	
}
