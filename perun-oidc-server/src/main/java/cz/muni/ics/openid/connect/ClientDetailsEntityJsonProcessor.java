/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
/**
 *
 */
package cz.muni.ics.openid.connect;


import static cz.muni.ics.oauth2.model.RegisteredClientFields.APPLICATION_TYPE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLAIMS_REDIRECT_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_ID;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_ID_ISSUED_AT;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_NAME;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_SECRET;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_SECRET_EXPIRES_AT;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CLIENT_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CODE_CHALLENGE_METHOD;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.CONTACTS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.DEFAULT_ACR_VALUES;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.DEFAULT_MAX_AGE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.GRANT_TYPES;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.ID_TOKEN_ENCRYPTED_RESPONSE_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.ID_TOKEN_ENCRYPTED_RESPONSE_ENC;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.ID_TOKEN_SIGNED_RESPONSE_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.INITIATE_LOGIN_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.JWKS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.JWKS_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.POLICY_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.POST_LOGOUT_REDIRECT_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REDIRECT_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REGISTRATION_ACCESS_TOKEN;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REGISTRATION_CLIENT_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REQUEST_OBJECT_SIGNING_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REQUEST_URIS;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.REQUIRE_AUTH_TIME;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.RESPONSE_TYPES;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SCOPE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SCOPE_SEPARATOR;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SECTOR_IDENTIFIER_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SOFTWARE_ID;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SOFTWARE_STATEMENT;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SOFTWARE_VERSION;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.SUBJECT_TYPE;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.TOKEN_ENDPOINT_AUTH_METHOD;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.TOKEN_ENDPOINT_AUTH_SIGNING_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.TOS_URI;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.USERINFO_ENCRYPTED_RESPONSE_ALG;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.USERINFO_ENCRYPTED_RESPONSE_ENC;
import static cz.muni.ics.oauth2.model.RegisteredClientFields.USERINFO_SIGNED_RESPONSE_ALG;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AppType;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AuthMethod;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.SubjectType;
import cz.muni.ics.oauth2.model.RegisteredClient;
import cz.muni.ics.util.JsonUtils;
import java.text.ParseException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to handle the parsing and serialization of ClientDetails objects.
 *
 * @author jricher
 */
@Slf4j
public class ClientDetailsEntityJsonProcessor {

	private static final JsonParser parser = new JsonParser();

	public static ClientDetailsEntity parse(String jsonString) {
		JsonElement jsonEl = parser.parse(jsonString);
		return parse(jsonEl);
	}

	public static ClientDetailsEntity parse(JsonElement jsonEl) {
		if (jsonEl.isJsonObject()) {

			JsonObject o = jsonEl.getAsJsonObject();
			ClientDetailsEntity c = new ClientDetailsEntity();

			// these two fields should only be sent in the update request, and MUST match existing values
			c.setClientId(JsonUtils.getAsString(o, CLIENT_ID));
			c.setClientSecret(JsonUtils.getAsString(o, CLIENT_SECRET));

			// OAuth DynReg
			c.setRedirectUris(JsonUtils.getAsStringSet(o, REDIRECT_URIS));
			c.setClientName(JsonUtils.getAsString(o, CLIENT_NAME));
			c.setClientUri(JsonUtils.getAsString(o, CLIENT_URI));
			c.setContacts(JsonUtils.getAsStringSet(o, CONTACTS));
			c.setTosUri(JsonUtils.getAsString(o, TOS_URI));

			String authMethod = JsonUtils.getAsString(o, TOKEN_ENDPOINT_AUTH_METHOD);
			if (authMethod != null) {
				c.setTokenEndpointAuthMethod(AuthMethod.getByValue(authMethod));
			}

			// scope is a space-separated string
			String scope = JsonUtils.getAsString(o, SCOPE);
			if (scope != null) {
				c.setScope(Sets.newHashSet(Splitter.on(SCOPE_SEPARATOR).split(scope)));
			}

			c.setGrantTypes(JsonUtils.getAsStringSet(o, GRANT_TYPES));
			c.setResponseTypes(JsonUtils.getAsStringSet(o, RESPONSE_TYPES));
			c.setPolicyUri(JsonUtils.getAsString(o, POLICY_URI));
			c.setJwksUri(JsonUtils.getAsString(o, JWKS_URI));

			JsonElement jwksEl = o.get(JWKS);
			if (jwksEl != null && jwksEl.isJsonObject()) {
				try {
					JWKSet jwks = JWKSet.parse(jwksEl.toString()); // we have to pass this through Nimbus's parser as a string
					c.setJwks(jwks);
				} catch (ParseException e) {
					log.error("Unable to parse JWK Set for client", e);
					return null;
				}
			}

			// OIDC Additions
			String appType = JsonUtils.getAsString(o, APPLICATION_TYPE);
			if (appType != null) {
				c.setApplicationType(AppType.getByValue(appType));
			}

			c.setSectorIdentifierUri(JsonUtils.getAsString(o, SECTOR_IDENTIFIER_URI));

			String subjectType = JsonUtils.getAsString(o, SUBJECT_TYPE);
			if (subjectType != null) {
				c.setSubjectType(SubjectType.getByValue(subjectType));
			}

			c.setRequestObjectSigningAlg(JsonUtils.getAsJwsAlgorithm(o, REQUEST_OBJECT_SIGNING_ALG));

			c.setUserInfoSignedResponseAlg(JsonUtils.getAsJwsAlgorithm(o, USERINFO_SIGNED_RESPONSE_ALG));
			c.setUserInfoEncryptedResponseAlg(JsonUtils.getAsJweAlgorithm(o, USERINFO_ENCRYPTED_RESPONSE_ALG));
			c.setUserInfoEncryptedResponseEnc(JsonUtils.getAsJweEncryptionMethod(o, USERINFO_ENCRYPTED_RESPONSE_ENC));

			c.setIdTokenSignedResponseAlg(JsonUtils.getAsJwsAlgorithm(o, ID_TOKEN_SIGNED_RESPONSE_ALG));
			c.setIdTokenEncryptedResponseAlg(JsonUtils.getAsJweAlgorithm(o, ID_TOKEN_ENCRYPTED_RESPONSE_ALG));
			c.setIdTokenEncryptedResponseEnc(JsonUtils.getAsJweEncryptionMethod(o, ID_TOKEN_ENCRYPTED_RESPONSE_ENC));

			c.setTokenEndpointAuthSigningAlg(JsonUtils.getAsJwsAlgorithm(o, TOKEN_ENDPOINT_AUTH_SIGNING_ALG));

			if (o.has(DEFAULT_MAX_AGE)) {
				if (o.get(DEFAULT_MAX_AGE).isJsonPrimitive()) {
					c.setDefaultMaxAge(o.get(DEFAULT_MAX_AGE).getAsInt());
				}
			}

			if (o.has(REQUIRE_AUTH_TIME)) {
				if (o.get(REQUIRE_AUTH_TIME).isJsonPrimitive()) {
					c.setRequireAuthTime(o.get(REQUIRE_AUTH_TIME).getAsBoolean());
				}
			}

			c.setDefaultACRvalues(JsonUtils.getAsStringSet(o, DEFAULT_ACR_VALUES));
			c.setInitiateLoginUri(JsonUtils.getAsString(o, INITIATE_LOGIN_URI));
			c.setPostLogoutRedirectUris(JsonUtils.getAsStringSet(o, POST_LOGOUT_REDIRECT_URIS));
			c.setRequestUris(JsonUtils.getAsStringSet(o, REQUEST_URIS));

			c.setClaimsRedirectUris(JsonUtils.getAsStringSet(o, CLAIMS_REDIRECT_URIS));

			c.setCodeChallengeMethod(JsonUtils.getAsPkceAlgorithm(o, CODE_CHALLENGE_METHOD));

			c.setSoftwareId(JsonUtils.getAsString(o, SOFTWARE_ID));
			c.setSoftwareVersion(JsonUtils.getAsString(o, SOFTWARE_VERSION));

			// note that this does not process or validate the software statement, that's handled in other components
			String softwareStatement = JsonUtils.getAsString(o,  SOFTWARE_STATEMENT);
			if (!Strings.isNullOrEmpty(softwareStatement)) {
				try {
					JWT softwareStatementJwt = JWTParser.parse(softwareStatement);
					c.setSoftwareStatement(softwareStatementJwt);
				} catch (ParseException e) {
					log.warn("Error parsing software statement", e);
					return null;
				}
			}



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
		return parseRegistered(jsonEl);
	}

	public static RegisteredClient parseRegistered(JsonElement jsonEl) {
		if (jsonEl.isJsonObject()) {

			JsonObject o = jsonEl.getAsJsonObject();
			ClientDetailsEntity c = parse(jsonEl);

			RegisteredClient rc = new RegisteredClient(c);
			// get any fields from the registration
			rc.setRegistrationAccessToken(JsonUtils.getAsString(o, REGISTRATION_ACCESS_TOKEN));
			rc.setRegistrationClientUri(JsonUtils.getAsString(o, REGISTRATION_CLIENT_URI));
			rc.setClientIdIssuedAt(JsonUtils.getAsDate(o, CLIENT_ID_ISSUED_AT));
			rc.setClientSecretExpiresAt(JsonUtils.getAsDate(o, CLIENT_SECRET_EXPIRES_AT));

			rc.setSource(o);

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

		if (c.getSource() != null) {
			// if we have the original object, just use that
			return c.getSource();
		} else {

			JsonObject o = new JsonObject();

			o.addProperty(CLIENT_ID, c.getClientId());
			if (c.getClientSecret() != null) {
				o.addProperty(CLIENT_SECRET, c.getClientSecret());

				if (c.getClientSecretExpiresAt() == null) {
					o.addProperty(CLIENT_SECRET_EXPIRES_AT, 0); // TODO: do we want to let secrets expire?
				} else {
					o.addProperty(CLIENT_SECRET_EXPIRES_AT, c.getClientSecretExpiresAt().getTime() / 1000L);
				}
			}

			if (c.getClientIdIssuedAt() != null) {
				o.addProperty(CLIENT_ID_ISSUED_AT, c.getClientIdIssuedAt().getTime() / 1000L);
			} else if (c.getCreatedAt() != null) {
				o.addProperty(CLIENT_ID_ISSUED_AT, c.getCreatedAt().getTime() / 1000L);
			}
			if (c.getRegistrationAccessToken() != null) {
				o.addProperty(REGISTRATION_ACCESS_TOKEN, c.getRegistrationAccessToken());
			}

			if (c.getRegistrationClientUri() != null) {
				o.addProperty(REGISTRATION_CLIENT_URI, c.getRegistrationClientUri());
			}


			// add in all other client properties

			// OAuth DynReg
			o.add(REDIRECT_URIS, JsonUtils.getAsArray(c.getRedirectUris()));
			o.addProperty(CLIENT_NAME, c.getClientName());
			o.addProperty(CLIENT_URI, c.getClientUri());
			o.add(CONTACTS, JsonUtils.getAsArray(c.getContacts()));
			o.addProperty(TOS_URI, c.getTosUri());
			o.addProperty(TOKEN_ENDPOINT_AUTH_METHOD, c.getTokenEndpointAuthMethod() != null ? c.getTokenEndpointAuthMethod().getValue() : null);
			o.addProperty(SCOPE, c.getScope() != null ? Joiner.on(SCOPE_SEPARATOR).join(c.getScope()) : null);
			o.add(GRANT_TYPES, JsonUtils.getAsArray(c.getGrantTypes()));
			o.add(RESPONSE_TYPES, JsonUtils.getAsArray(c.getResponseTypes()));
			o.addProperty(POLICY_URI, c.getPolicyUri());
			o.addProperty(JWKS_URI, c.getJwksUri());

			// get the JWKS sub-object
			if (c.getJwks() != null) {
				// We have to re-parse it into GSON because Nimbus uses a different parser
				JsonElement jwks = parser.parse(c.getJwks().toString());
				o.add(JWKS, jwks);
			} else {
				o.add(JWKS, null);
			}

			// OIDC Registration
			o.addProperty(APPLICATION_TYPE, c.getApplicationType() != null ? c.getApplicationType().getValue() : null);
			o.addProperty(SECTOR_IDENTIFIER_URI, c.getSectorIdentifierUri());
			o.addProperty(SUBJECT_TYPE, c.getSubjectType() != null ? c.getSubjectType().getValue() : null);
			o.addProperty(REQUEST_OBJECT_SIGNING_ALG, c.getRequestObjectSigningAlg() != null ? c.getRequestObjectSigningAlg().getName() : null);
			o.addProperty(USERINFO_SIGNED_RESPONSE_ALG, c.getUserInfoSignedResponseAlg() != null ? c.getUserInfoSignedResponseAlg().getName() : null);
			o.addProperty(USERINFO_ENCRYPTED_RESPONSE_ALG, c.getUserInfoEncryptedResponseAlg() != null ? c.getUserInfoEncryptedResponseAlg().getName() : null);
			o.addProperty(USERINFO_ENCRYPTED_RESPONSE_ENC, c.getUserInfoEncryptedResponseEnc() != null ? c.getUserInfoEncryptedResponseEnc().getName() : null);
			o.addProperty(ID_TOKEN_SIGNED_RESPONSE_ALG, c.getIdTokenSignedResponseAlg() != null ? c.getIdTokenSignedResponseAlg().getName() : null);
			o.addProperty(ID_TOKEN_ENCRYPTED_RESPONSE_ALG, c.getIdTokenEncryptedResponseAlg() != null ? c.getIdTokenEncryptedResponseAlg().getName() : null);
			o.addProperty(ID_TOKEN_ENCRYPTED_RESPONSE_ENC, c.getIdTokenEncryptedResponseEnc() != null ? c.getIdTokenEncryptedResponseEnc().getName() : null);
			o.addProperty(TOKEN_ENDPOINT_AUTH_SIGNING_ALG, c.getTokenEndpointAuthSigningAlg() != null ? c.getTokenEndpointAuthSigningAlg().getName() : null);
			o.addProperty(DEFAULT_MAX_AGE, c.getDefaultMaxAge());
			o.addProperty(REQUIRE_AUTH_TIME, c.getRequireAuthTime());
			o.add(DEFAULT_ACR_VALUES, JsonUtils.getAsArray(c.getDefaultACRvalues()));
			o.addProperty(INITIATE_LOGIN_URI, c.getInitiateLoginUri());
			o.add(POST_LOGOUT_REDIRECT_URIS, JsonUtils.getAsArray(c.getPostLogoutRedirectUris()));
			o.add(REQUEST_URIS, JsonUtils.getAsArray(c.getRequestUris()));

			o.add(CLAIMS_REDIRECT_URIS, JsonUtils.getAsArray(c.getClaimsRedirectUris()));

			o.addProperty(CODE_CHALLENGE_METHOD, c.getCodeChallengeMethod() != null ? c.getCodeChallengeMethod().getName() : null);

			o.addProperty(SOFTWARE_ID, c.getSoftwareId());
			o.addProperty(SOFTWARE_VERSION, c.getSoftwareVersion());

			if (c.getSoftwareStatement() != null) {
				o.addProperty(SOFTWARE_STATEMENT, c.getSoftwareStatement().serialize());
			}

			return o;
		}

	}
}
