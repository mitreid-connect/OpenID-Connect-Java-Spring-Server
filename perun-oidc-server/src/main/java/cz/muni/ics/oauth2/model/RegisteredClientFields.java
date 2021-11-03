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
package cz.muni.ics.oauth2.model;

public interface RegisteredClientFields {

	String SOFTWARE_ID = "software_id";
	String SOFTWARE_VERSION = "software_version";
	String SOFTWARE_STATEMENT = "software_statement";
	String CLAIMS_REDIRECT_URIS = "claims_redirect_uris";
	String CLIENT_SECRET_EXPIRES_AT = "client_secret_expires_at";
	String CLIENT_ID_ISSUED_AT = "client_id_issued_at";
	String REGISTRATION_CLIENT_URI = "registration_client_uri";
	String REGISTRATION_ACCESS_TOKEN = "registration_access_token";
	String REQUEST_URIS = "request_uris";
	String POST_LOGOUT_REDIRECT_URIS = "post_logout_redirect_uris";
	String INITIATE_LOGIN_URI = "initiate_login_uri";
	String DEFAULT_ACR_VALUES = "default_acr_values";
	String REQUIRE_AUTH_TIME = "require_auth_time";
	String DEFAULT_MAX_AGE = "default_max_age";
	String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";
	String ID_TOKEN_ENCRYPTED_RESPONSE_ENC = "id_token_encrypted_response_enc";
	String ID_TOKEN_ENCRYPTED_RESPONSE_ALG = "id_token_encrypted_response_alg";
	String ID_TOKEN_SIGNED_RESPONSE_ALG = "id_token_signed_response_alg";
	String USERINFO_ENCRYPTED_RESPONSE_ENC = "userinfo_encrypted_response_enc";
	String USERINFO_ENCRYPTED_RESPONSE_ALG = "userinfo_encrypted_response_alg";
	String USERINFO_SIGNED_RESPONSE_ALG = "userinfo_signed_response_alg";
	String REQUEST_OBJECT_SIGNING_ALG = "request_object_signing_alg";
	String SUBJECT_TYPE = "subject_type";
	String SECTOR_IDENTIFIER_URI = "sector_identifier_uri";
	String APPLICATION_TYPE = "application_type";
	String JWKS_URI = "jwks_uri";
	String JWKS = "jwks";
	String SCOPE_SEPARATOR = " ";
	String POLICY_URI = "policy_uri";
	String RESPONSE_TYPES = "response_types";
	String GRANT_TYPES = "grant_types";
	String SCOPE = "scope";
	String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";
	String TOS_URI = "tos_uri";
	String CONTACTS = "contacts";
	String LOGO_URI = "logo_uri";
	String CLIENT_URI = "client_uri";
	String CLIENT_NAME = "client_name";
	String REDIRECT_URIS = "redirect_uris";
	String CLIENT_SECRET = "client_secret";
	String CLIENT_ID = "client_id";
	String CODE_CHALLENGE_METHOD = "code_challenge_method";

}
