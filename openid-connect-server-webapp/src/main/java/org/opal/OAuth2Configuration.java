package org.opal;

public class OAuth2Configuration {

	private String clientId;
	private String clientSecret;
	private String accessTokenUri;
	private String userAuthorizationUri;
	private String userInfoUri;
	private String tokenName;
    private String authenticationScheme;
    private String clientAuthenticationScheme;
    
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public String getAccessTokenUri() {
		return accessTokenUri;
	}
	public void setAccessTokenUri(String accessTokenUri) {
		this.accessTokenUri = accessTokenUri;
	}
	public String getUserAuthorizationUri() {
		return userAuthorizationUri;
	}
	public void setUserAuthorizationUri(String userAuthorizationUri) {
		this.userAuthorizationUri = userAuthorizationUri;
	}
	public String getUserInfoUri() {
		return userInfoUri;
	}
	public void setUserInfoUri(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}
	public String getTokenName() {
		return tokenName;
	}
	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}
	public String getAuthenticationScheme() {
		return authenticationScheme;
	}
	public void setAuthenticationScheme(String authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}
	public String getClientAuthenticationScheme() {
		return clientAuthenticationScheme;
	}
	public void setClientAuthenticationScheme(String clientAuthenticationScheme) {
		this.clientAuthenticationScheme = clientAuthenticationScheme;
	}

}
