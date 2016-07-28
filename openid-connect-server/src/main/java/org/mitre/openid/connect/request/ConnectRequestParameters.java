/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
	public String SCOPE = "scope";
	public String NONCE = "nonce";
	public String PROMPT = "prompt";

	// prompt values
	public String PROMPT_LOGIN = "login";
	public String PROMPT_NONE = "none";
	public String PROMPT_CONSENT = "consent";
	public String PROMPT_SEPARATOR = " ";

	// extensions
	public String APPROVED_SITE = "approved_site";

	// responses
	public String ERROR = "error";
	public String LOGIN_REQUIRED = "login_required";
	
	// audience
	public String AUD = "aud";
	
	// PKCE
	public String CODE_CHALLENGE = "code_challenge";
	public String CODE_CHALLENGE_METHOD = "code_challenge_method";
	public String CODE_VERIFIER = "code_verifier";
	
}
