/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.request;

public interface ConnectRequestParameters {

	String CLIENT_ID = "client_id";
	String RESPONSE_TYPE = "response_type";
	String REDIRECT_URI = "redirect_uri";
	String STATE = "state";
	String DISPLAY = "display";
	String REQUEST = "request";
	String LOGIN_HINT = "login_hint";
	String MAX_AGE = "max_age";
	String CLAIMS = "claims";
	String SCOPE = "scope";
	String NONCE = "nonce";
	String PROMPT = "prompt";

	// prompt values
    String PROMPT_LOGIN = "login";
	String PROMPT_NONE = "none";
	String PROMPT_CONSENT = "consent";
	String PROMPT_SEPARATOR = " ";

	// extensions
    String APPROVED_SITE = "approved_site";

	// responses
    String ERROR = "error";
	String LOGIN_REQUIRED = "login_required";

	// audience
    String AUD = "aud";

	// PKCE
    String CODE_CHALLENGE = "code_challenge";
	String CODE_CHALLENGE_METHOD = "code_challenge_method";
	String CODE_VERIFIER = "code_verifier";

}
