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
package org.mitre.oauth2.model;

public interface RegisteredClientFields {
	public String SOFTWARE_STATEMENT = "software_statement";
	public String CLAIMS_REDIRECT_URIS = "claims_redirect_uris";
	public String CLIENT_SECRET_EXPIRES_AT = "client_secret_expires_at";
	public String CLIENT_ID_ISSUED_AT = "client_id_issued_at";
	public String REGISTRATION_CLIENT_URI = "registration_client_uri";
	public String REGISTRATION_ACCESS_TOKEN = "registration_access_token";
	public String REQUEST_URIS = "request_uris";
	public String POST_LOGOUT_REDIRECT_URIS = "post_logout_redirect_uris";
	public String INITIATE_LOGIN_URI = "initiate_login_uri";
	public String DEFAULT_ACR_VALUES = "default_acr_values";
	public String REQUIRE_AUTH_TIME = "require_auth_time";
	public String DEFAULT_MAX_AGE = "default_max_age";
	public String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";
	public String ID_TOKEN_ENCRYPTED_RESPONSE_ENC = "id_token_encrypted_response_enc";
	public String ID_TOKEN_ENCRYPTED_RESPONSE_ALG = "id_token_encrypted_response_alg";
	public String ID_TOKEN_SIGNED_RESPONSE_ALG = "id_token_signed_response_alg";
	public String USERINFO_ENCRYPTED_RESPONSE_ENC = "userinfo_encrypted_response_enc";
	public String USERINFO_ENCRYPTED_RESPONSE_ALG = "userinfo_encrypted_response_alg";
	public String USERINFO_SIGNED_RESPONSE_ALG = "userinfo_signed_response_alg";
	public String REQUEST_OBJECT_SIGNING_ALG = "request_object_signing_alg";
	public String SUBJECT_TYPE = "subject_type";
	public String SECTOR_IDENTIFIER_URI = "sector_identifier_uri";
	public String APPLICATION_TYPE = "application_type";
	public String JWKS_URI = "jwks_uri";
	public String JWKS = "jwks";
	public String SCOPE_SEPARATOR = " ";
	public String POLICY_URI = "policy_uri";
	public String RESPONSE_TYPES = "response_types";
	public String GRANT_TYPES = "grant_types";
	public String SCOPE = "scope";
	public String TOKEN_ENDPOINT_AUTH_METHOD = "token_endpoint_auth_method";
	public String TOS_URI = "tos_uri";
	public String CONTACTS = "contacts";
	public String LOGO_URI = "logo_uri";
	public String CLIENT_URI = "client_uri";
	public String CLIENT_NAME = "client_name";
	public String REDIRECT_URIS = "redirect_uris";
	public String CLIENT_SECRET = "client_secret";
	public String CLIENT_ID = "client_id";

}
