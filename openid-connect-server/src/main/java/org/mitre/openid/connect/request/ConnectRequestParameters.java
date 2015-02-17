package org.mitre.openid.connect.request;

public interface ConnectRequestParameters {

	public String CLIENT_ID = "client_id";
	public String RESPONSE_TYPE = "response_type";
	public String REDIRECT_URI = "redirect_uri";
	public String STATE = "state";
	public String DISPLAY = "display";
	public String REQUEST = "request";
	public String LOGIN_HINT = "login_hint";
	public String MAX_AGE = "max_age";
	public String CLAIMS = "claims";
	public String NONCE = "nonce";
	public String PROMPT = "prompt";

	// prompt values
	public String PROMPT_LOGIN = "login";
	public String PROMPT_NONE = "none";
	public String PROMPT_CONSENT = "consent";
	public String PROMPT_SEPARATOR = " ";

	// extensions
	public String CSRF = "csrf";
	public String APPROVED_SITE = "approved_site";



}
