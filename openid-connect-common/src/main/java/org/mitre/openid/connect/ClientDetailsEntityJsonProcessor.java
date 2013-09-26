/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
 ******************************************************************************/
/**
 * 
 */
package org.mitre.openid.connect;


import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.RegisteredClient;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.mitre.discovery.util.JsonUtils.*;

/**
 * @author jricher
 *
 */
public class ClientDetailsEntityJsonProcessor {

	private static JsonParser parser = new JsonParser();

	/**
	 * 
	 * Create an unbound ClientDetailsEntity from the given JSON string.
	 * 
	 * @param jsonString
	 * @return the entity if successful, null otherwise
	 */
	public static ClientDetailsEntity parse(String jsonString) {
		JsonElement jsonEl = parser.parse(jsonString);
		if (jsonEl.isJsonObject()) {

			JsonObject o = jsonEl.getAsJsonObject();
			ClientDetailsEntity c = new ClientDetailsEntity();

			// TODO: make these field names into constants

			// these two fields should only be sent in the update request, and MUST match existing values
			c.setClientId(getAsString(o, "client_id"));
			c.setClientSecret(getAsString(o, "client_secret"));

			// OAuth DynReg
			c.setRedirectUris(getAsStringSet(o, "redirect_uris"));
			c.setClientName(getAsString(o, "client_name"));
			c.setClientUri(getAsString(o, "client_uri"));
			c.setLogoUri(getAsString(o, "logo_uri"));
			c.setContacts(getAsStringSet(o, "contacts"));
			c.setTosUri(getAsString(o, "tos_uri"));

			String authMethod = getAsString(o, "token_endpoint_auth_method");
			if (authMethod != null) {
				c.setTokenEndpointAuthMethod(AuthMethod.getByValue(authMethod));
			}

			// scope is a space-separated string
			String scope = getAsString(o, "scope");
			if (scope != null) {
				c.setScope(Sets.newHashSet(Splitter.on(" ").split(scope)));
			}

			c.setGrantTypes(getAsStringSet(o, "grant_types"));
			c.setResponseTypes(getAsStringSet(o, "response_types"));
			c.setPolicyUri(getAsString(o, "policy_uri"));
			c.setJwksUri(getAsString(o, "jwks_uri"));


			// OIDC Additions
			String appType = getAsString(o, "application_type");
			if (appType != null) {
				c.setApplicationType(AppType.getByValue(appType));
			}

			c.setSectorIdentifierUri(getAsString(o, "sector_identifier_uri"));

			String subjectType = getAsString(o, "subject_type");
			if (subjectType != null) {
				c.setSubjectType(SubjectType.getByValue(subjectType));
			}

			c.setRequestObjectSigningAlg(getAsJwsAlgorithm(o, "request_object_signing_alg"));

			c.setUserInfoSignedResponseAlg(getAsJwsAlgorithm(o, "userinfo_signed_response_alg"));
			c.setUserInfoEncryptedResponseAlg(getAsJweAlgorithm(o, "userinfo_encrypted_response_alg"));
			c.setUserInfoEncryptedResponseEnc(getAsJweEncryptionMethod(o, "userinfo_encrypted_response_enc"));

			c.setIdTokenSignedResponseAlg(getAsJwsAlgorithm(o, "id_token_signed_response_alg"));
			c.setIdTokenEncryptedResponseAlg(getAsJweAlgorithm(o, "id_token_encrypted_response_alg"));
			c.setIdTokenEncryptedResponseEnc(getAsJweEncryptionMethod(o, "id_token_encrypted_response_enc"));
			
			c.setTokenEndpointAuthSigningAlg(getAsJwsAlgorithm(o, "token_endpoint_auth_signing_alg"));

			if (o.has("default_max_age")) {
				if (o.get("default_max_age").isJsonPrimitive()) {
					c.setDefaultMaxAge(o.get("default_max_age").getAsInt());
				}
			}

			if (o.has("require_auth_time")) {
				if (o.get("require_auth_time").isJsonPrimitive()) {
					c.setRequireAuthTime(o.get("require_auth_time").getAsBoolean());
				}
			}

			c.setDefaultACRvalues(getAsStringSet(o, "default_acr_values"));
			c.setInitiateLoginUri(getAsString(o, "initiate_login_uri"));
			c.setPostLogoutRedirectUri(getAsString(o, "post_logout_redirect_uri"));
			c.setRequestUris(getAsStringSet(o, "request_uris"));

			return c;
		} else {
			return null;
		}
	}

	/**
	 * Parse the JSON as a RegisteredClient (useful in the dynamic client filter)
	 */
	public static RegisteredClient parseRegistered(String jsonString) {


		JsonElement jsonEl = parser.parse(jsonString);
		if (jsonEl.isJsonObject()) {

			JsonObject o = jsonEl.getAsJsonObject();
			ClientDetailsEntity c = parse(jsonString);

			RegisteredClient rc = new RegisteredClient(c);
			// get any fields from the registration
			rc.setRegistrationAccessToken(getAsString(o, "registration_access_token"));
			rc.setRegistrationClientUri(getAsString(o, "registration_client_uri"));
			rc.setClientIdIssuedAt(getAsDate(o, "client_id_issued_at"));
			rc.setClientSecretExpiresAt(getAsDate(o, "client_secret_expires_at"));

			return rc;
		} else {
			return null;
		}
	}

	/**
	 * @param c
	 * @param token
	 * @param registrationUri
	 * @return
	 */
	public static JsonObject serialize(RegisteredClient c) {
		JsonObject o = new JsonObject();

		o.addProperty("client_id", c.getClientId());
		if (c.getClientSecret() != null) {
			o.addProperty("client_secret", c.getClientSecret());

			if (c.getClientSecretExpiresAt() == null) {
				o.addProperty("client_secret_expires_at", 0); // TODO: do we want to let secrets expire?
			} else {
				o.addProperty("client_secret_expires_at", c.getClientSecretExpiresAt().getTime() / 1000L);
			}
		}

		if (c.getClientIdIssuedAt() != null) {
			o.addProperty("client_id_issued_at", c.getClientIdIssuedAt().getTime() / 1000L);
		} else if (c.getCreatedAt() != null) {
			o.addProperty("client_id_issued_at", c.getCreatedAt().getTime() / 1000L);
		}
		if (c.getRegistrationAccessToken() != null) {
			o.addProperty("registration_access_token", c.getRegistrationAccessToken());
		}

		if (c.getRegistrationClientUri() != null) {
			o.addProperty("registration_client_uri", c.getRegistrationClientUri());
		}


		// add in all other client properties

		// OAuth DynReg
		o.add("redirect_uris", getAsArray(c.getRedirectUris()));
		o.addProperty("client_name", c.getClientName());
		o.addProperty("client_uri", c.getClientUri());
		o.addProperty("logo_uri", c.getLogoUri());
		o.add("contacts", getAsArray(c.getContacts()));
		o.addProperty("tos_uri", c.getTosUri());
		o.addProperty("token_endpoint_auth_method", c.getTokenEndpointAuthMethod() != null ? c.getTokenEndpointAuthMethod().getValue() : null);
		o.addProperty("scope", c.getScope() != null ? Joiner.on(" ").join(c.getScope()) : null);
		o.add("grant_types", getAsArray(c.getGrantTypes()));
		o.add("response_types", getAsArray(c.getResponseTypes()));
		o.addProperty("policy_uri", c.getPolicyUri());
		o.addProperty("jwks_uri", c.getJwksUri());

		// OIDC Registration
		o.addProperty("application_type", c.getApplicationType() != null ? c.getApplicationType().getValue() : null);
		o.addProperty("sector_identifier_uri", c.getSectorIdentifierUri());
		o.addProperty("subject_type", c.getSubjectType() != null ? c.getSubjectType().getValue() : null);
		o.addProperty("request_object_signing_alg", c.getRequestObjectSigningAlg() != null ? c.getRequestObjectSigningAlg().getName() : null);
		o.addProperty("userinfo_signed_response_alg", c.getUserInfoSignedResponseAlg() != null ? c.getUserInfoSignedResponseAlg().getName() : null);
		o.addProperty("userinfo_encrypted_response_alg", c.getUserInfoEncryptedResponseAlg() != null ? c.getUserInfoEncryptedResponseAlg().getName() : null);
		o.addProperty("userinfo_encrypted_response_enc", c.getUserInfoEncryptedResponseEnc() != null ? c.getUserInfoEncryptedResponseEnc().getName() : null);
		o.addProperty("id_token_signed_response_alg", c.getIdTokenSignedResponseAlg() != null ? c.getIdTokenSignedResponseAlg().getName() : null);
		o.addProperty("id_token_encrypted_response_alg", c.getIdTokenEncryptedResponseAlg() != null ? c.getIdTokenEncryptedResponseAlg().getName() : null);
		o.addProperty("id_token_encrypted_response_enc", c.getIdTokenEncryptedResponseEnc() != null ? c.getIdTokenEncryptedResponseEnc().getName() : null);
		o.addProperty("token_endpoint_auth_signing_alg", c.getTokenEndpointAuthSigningAlg() != null ? c.getTokenEndpointAuthSigningAlg().getName() : null);
		o.addProperty("default_max_age", c.getDefaultMaxAge());
		o.addProperty("require_auth_time", c.getRequireAuthTime());
		o.add("default_acr_values", getAsArray(c.getDefaultACRvalues()));
		o.addProperty("initiate_login_uri", c.getInitiateLoginUri());
		o.addProperty("post_logout_redirect_uri", c.getPostLogoutRedirectUri());
		o.add("request_uris", getAsArray(c.getRequestUris()));
		return o;
	}


}
