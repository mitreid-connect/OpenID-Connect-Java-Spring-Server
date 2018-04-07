package org.opal.config;

public class ExternalAuthorities {

	private String client_id;
	private String client_secret;
	private String access_token_uri;
	private String user_authorization_uri;
	private String token_name;
	private String authentication_scheme;
	private String client_authentication_scheme;
	private String user_info_uri;
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getClient_secret() {
		return client_secret;
	}
	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}
	public String getAccess_token_uri() {
		return access_token_uri;
	}
	public void setAccess_token_uri(String access_token_uri) {
		this.access_token_uri = access_token_uri;
	}
	public String getUser_authorization_uri() {
		return user_authorization_uri;
	}
	public void setUser_authorization_uri(String user_authorization_uri) {
		this.user_authorization_uri = user_authorization_uri;
	}
	public String getToken_name() {
		return token_name;
	}
	public void setToken_name(String token_name) {
		this.token_name = token_name;
	}
	public String getAuthentication_scheme() {
		return authentication_scheme;
	}
	public void setAuthentication_scheme(String authentication_scheme) {
		this.authentication_scheme = authentication_scheme;
	}
	public String getClient_authentication_scheme() {
		return client_authentication_scheme;
	}
	public void setClient_authentication_scheme(String client_authentication_scheme) {
		this.client_authentication_scheme = client_authentication_scheme;
	}
	public String getUser_info_uri() {
		return user_info_uri;
	}
	public void setUser_info_uri(String user_info_uri) {
		this.user_info_uri = user_info_uri;
	}
	
	
}
