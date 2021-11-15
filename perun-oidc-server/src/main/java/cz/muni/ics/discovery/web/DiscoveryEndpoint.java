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
package cz.muni.ics.discovery.web;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import cz.muni.ics.discovery.util.WebfingerURLNormalizer;
import cz.muni.ics.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.PKCEAlgorithm;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.oauth2.web.DeviceEndpoint;
import cz.muni.ics.oauth2.web.IntrospectionEndpoint;
import cz.muni.ics.oauth2.web.RevocationEndpoint;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.UserInfoService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import cz.muni.ics.openid.connect.web.DynamicClientRegistrationEndpoint;
import cz.muni.ics.openid.connect.web.EndSessionEndpoint;
import cz.muni.ics.openid.connect.web.JWKSetPublishingEndpoint;
import cz.muni.ics.openid.connect.web.UserInfoEndpoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Handle OpenID Connect Discovery.
 *
 * @author jricher
 *
 */
@Controller
@Slf4j
public class DiscoveryEndpoint {

	public static final String WELL_KNOWN_URL = ".well-known";
	public static final String OPENID_CONFIGURATION_URL = WELL_KNOWN_URL + "/openid-configuration";
	public static final String WEBFINGER_URL = WELL_KNOWN_URL + "/webfinger";
	private static final String ISSUER_STRING = "http://openid.net/specs/connect/1.0/issuer";

	private final ConfigurationPropertiesBean config;
	private final SystemScopeService scopeService;
	private final JWTSigningAndValidationService signService;
	private final JWTEncryptionAndDecryptionService encService;
	private final UserInfoService userService;

	// used to map JWA algorithms objects to strings
	private final Function<Algorithm, String> toAlgorithmName = alg -> alg == null ? null : alg.getName();

	@Autowired
	public DiscoveryEndpoint(UserInfoService userService, ConfigurationPropertiesBean config,
							 SystemScopeService scopeService, JWTSigningAndValidationService signService,
							 JWTEncryptionAndDecryptionService encService) {
		this.userService = userService;
		this.config = config;
		this.scopeService = scopeService;
		this.signService = signService;
		this.encService = encService;
	}

	@RequestMapping(value = '/' + WEBFINGER_URL, produces = MediaType.APPLICATION_JSON_VALUE)
	public String webfinger(@RequestParam("resource") String resource,
							@RequestParam(value = "rel", required = false) String rel,
							Model model) {
		if (!Strings.isNullOrEmpty(rel) && !rel.equals(ISSUER_STRING)) {
			log.warn("Responding to webfinger request for non-OIDC relation: {}", rel);
		}

		if (!resource.equals(config.getIssuer())) {
			// it's not the issuer directly, need to check other methods
			UriComponents resourceUri = WebfingerURLNormalizer.normalizeResource(resource);
			if (resourceUri != null
					&& resourceUri.getScheme() != null
					&& resourceUri.getScheme().equals("acct")) {
				UserInfo user = extractUser(resourceUri);
				if (user == null) {
					log.info("User not found: {}", resource);
					model.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
					return HttpCodeView.VIEWNAME;
				}

				UriComponents issuerComponents = UriComponentsBuilder.fromHttpUrl(config.getIssuer()).build();
				if (!Strings.nullToEmpty(issuerComponents.getHost())
					.equals(Strings.nullToEmpty(resourceUri.getHost()))) {
					log.info("Host mismatch, expected " + issuerComponents.getHost() + " got " + resourceUri.getHost());
					model.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
					return HttpCodeView.VIEWNAME;
				}
			} else {
				log.info("Unknown URI format: " + resource);
				model.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
				return HttpCodeView.VIEWNAME;
			}
		}

		model.addAttribute("resource", resource);
		model.addAttribute("issuer", config.getIssuer());

		return "webfingerView";
	}

	private UserInfo extractUser(UriComponents resourceUri) {
		UserInfo user = userService.getByEmailAddress(resourceUri.getUserInfo() + "@" + resourceUri.getHost());
		if (user == null) {
			user = userService.getByUsername(resourceUri.getUserInfo()); // first part is the username
		}
		return user;
	}

	@RequestMapping("/" + OPENID_CONFIGURATION_URL)
	public String providerConfiguration(Model model) {

		/*
		    issuer
		        REQUIRED. URL using the https scheme with no query or fragment component that the OP asserts as its Issuer Identifier.
		    authorization_endpoint
		        OPTIONAL. URL of the OP's Authentication and Authorization Endpoint [OpenID.Messages].
		    token_endpoint
		        OPTIONAL. URL of the OP's OAuth 2.0 Token Endpoint [OpenID.Messages].
		    userinfo_endpoint
		        RECOMMENDED. URL of the OP's UserInfo Endpoint [OpenID.Messages]. This URL MUST use the https scheme
		        and MAY contain port, path, and query parameter components.
		    check_session_iframe
		        OPTIONAL. URL of an OP endpoint that provides a page to support cross-origin communications for session state information with
		        the RP Client, using the HTML5 postMessage API. The page is loaded from an invisible iframe embedded in an RP page so that
		        it can run in the OP's security context. See [OpenID.Session].
		    end_session_endpoint
		        OPTIONAL. URL of the OP's endpoint that initiates logging out the End-User. See [OpenID.Session].
		    jwks_uri
		        REQUIRED. URL of the OP's JSON Web Key Set [JWK] document. This contains the signing key(s) the Client uses to
		        validate signatures from the OP. The JWK Set MAY also contain the Server's encryption key(s),
		        which are used by Clients to encrypt requests to the Server. When both signing and encryption keys are made available,
		        a use (Key Use) parameter value is REQUIRED for all keys in the document to indicate each key's intended usage.
		    registration_endpoint
		        RECOMMENDED. URL of the OP's Dynamic Client Registration Endpoint [OpenID.Registration].
		    scopes_supported
		        RECOMMENDED. JSON array containing a list of the OAuth 2.0 [RFC6749] scope values that this server supports.
		        The server MUST support the openid scope value.
		    response_types_supported
		        REQUIRED. JSON array containing a list of the OAuth 2.0 response_type values that this server supports.
		        The server MUST support the code, id_token, and the token id_token response type values.
		    grant_types_supported
		        OPTIONAL. JSON array containing a list of the OAuth 2.0 grant type values that this server supports.
		        The server MUST support the authorization_code and implicit grant type values
		        and MAY support the urn:ietf:params:oauth:grant-type:jwt-bearer grant type defined in OAuth JWT Bearer Token Profiles [OAuth.JWT].
		        If omitted, the default value is ["authorization_code", "implicit"].
		    acr_values_supported
		        OPTIONAL. JSON array containing a list of the Authentication Context Class References that this server supports.
		    subject_types_supported
		        REQUIRED. JSON array containing a list of the subject identifier types that this server supports. Valid types include pairwise and public.
		    userinfo_signing_alg_values_supported
		        OPTIONAL. JSON array containing a list of the JWS [JWS] signing algorithms (alg values) [JWA] supported by the UserInfo Endpoint to
		        encode the Claims in a JWT [JWT].
		    userinfo_encryption_alg_values_supported
		        OPTIONAL. JSON array containing a list of the JWE [JWE] encryption algorithms (alg values) [JWA] supported by the UserInfo Endpoint to
		        encode the Claims in a JWT [JWT].
		    userinfo_encryption_enc_values_supported
		        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) [JWA] supported by the UserInfo Endpoint to
		        encode the Claims in a JWT [JWT].
		    id_token_signing_alg_values_supported
		        REQUIRED. JSON array containing a list of the JWS signing algorithms (alg values) supported by the Authorization Server for the
		        ID Token to encode the Claims in a JWT [JWT].
		    id_token_encryption_alg_values_supported
		        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (alg values) supported by the Authorization Server for the
		        ID Token to encode the Claims in a JWT [JWT].
		    id_token_encryption_enc_values_supported
		        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) supported by the Authorization Server for the
		        ID Token to encode the Claims in a JWT [JWT].
		    request_object_signing_alg_values_supported
		        OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the Authorization Server for
		        the Request Object described in Section 2.9 of OpenID Connect Messages 1.0 [OpenID.Messages]. These algorithms are used both when
		        the Request Object is passed by value (using the request parameter) and when it is passed by reference (using the request_uri parameter).
		        Servers SHOULD support none and RS256.
		    request_object_encryption_alg_values_supported
		        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (alg values) supported by the Authorization Server for
		        the Request Object described in Section 2.9 of OpenID Connect Messages 1.0 [OpenID.Messages]. These algorithms are used both when
		        the Request Object is passed by value and when it is passed by reference.
		    request_object_encryption_enc_values_supported
		        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) supported by the Authorization Server for
		        the Request Object described in Section 2.9 of OpenID Connect Messages 1.0 [OpenID.Messages]. These algorithms are used both when
		        the Request Object is passed by value and when it is passed by reference.
		    token_endpoint_auth_methods_supported
		        OPTIONAL. JSON array containing a list of authentication methods supported by this Token Endpoint.
		        The options are client_secret_post, client_secret_basic, client_secret_jwt, and private_key_jwt,
		        as described in Section 2.2.1 of OpenID Connect Messages 1.0 [OpenID.Messages].
		        Other authentication methods MAY be defined by extensions.
		        If omitted, the default is client_secret_basic -- the HTTP Basic Authentication Scheme as specified in
		        Section 2.3.1 of OAuth 2.0 [RFC6749].
		    token_endpoint_auth_signing_alg_values_supported
		        OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the Token Endpoint for
		        the private_key_jwt and client_secret_jwt methods to encode the JWT [JWT]. Servers SHOULD support RS256.
		    display_values_supported
		        OPTIONAL. JSON array containing a list of the display parameter values that the OpenID Provider supports.
		        These values are described in Section 2.1.1 of OpenID Connect Messages 1.0 [OpenID.Messages].
		    claim_types_supported
		        OPTIONAL. JSON array containing a list of the Claim Types that the OpenID Provider supports.
		        These Claim Types are described in Section 2.6 of OpenID Connect Messages 1.0 [OpenID.Messages].
		        Values defined by this specification are normal, aggregated, and distributed.
		        If not specified, the implementation supports only normal Claims.
		    claims_supported
		        RECOMMENDED. JSON array containing a list of the Claim Names of the Claims that the OpenID Provider MAY be able to supply values for.
		        Note that for privacy or other reasons, this might not be an exhaustive list.
		    service_documentation
		        OPTIONAL. URL of a page containing human-readable information that developers might want or need to know when using the OpenID Provider.
		        In particular, if the OpenID Provider does not support Dynamic Client Registration, then information on how to register Clients needs
		        to be provided in this documentation.
		    claims_locales_supported
		        OPTIONAL. Languages and scripts supported for values in Claims being returned, represented as a JSON array of
		        BCP47 [RFC5646] language tag values. Not all languages and scripts are necessarily supported for all Claim values.
		    ui_locales_supported
		        OPTIONAL. Languages and scripts supported for the user interface, represented as a JSON array of BCP47 [RFC5646] language tag values.
		    claims_parameter_supported
		        OPTIONAL. Boolean value specifying whether the OP supports use of the claims parameter, with true indicating support.
		        If omitted, the default value is false.
		    request_parameter_supported
		        OPTIONAL. Boolean value specifying whether the OP supports use of the request parameter, with true indicating support.
		        If omitted, the default value is false.
		    request_uri_parameter_supported
		        OPTIONAL. Boolean value specifying whether the OP supports use of the request_uri parameter, with true indicating support.
		        If omitted, the default value is true.
		    require_request_uri_registration
		        OPTIONAL. Boolean value specifying whether the OP requires any request_uri values used to be pre-registered using
		        the request_uris registration parameter. Pre-registration is REQUIRED when the value is true. If omitted, the default value is false.
		    op_policy_uri
		        OPTIONAL. URL that the OpenID Provider provides to the person registering the Client to read about the OP's requirements on
		        how the Relying Party can use the data provided by the OP. The registration process SHOULD display this URL to the person registering
		        the Client if it is given.
		    op_tos_uri
		        OPTIONAL. URL that the OpenID Provider provides to the person registering the Client to read about OpenID Provider's terms of service.
		        The registration process SHOULD display this URL to the person registering the Client if it is given.
		 */
		String baseUrl = config.getIssuer();

		if (!baseUrl.endsWith("/")) {
			log.debug("Configured issuer doesn't end in /, adding for discovery: {}", baseUrl);
			baseUrl = baseUrl.concat("/");
		}

		signService.getAllSigningAlgsSupported();
		Lists.newArrayList(JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512);
		Collection<JWSAlgorithm> clientSymmetricAndAsymmetricSigningAlgs = Lists.newArrayList(JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512,
				JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512,
				JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512,
				JWSAlgorithm.PS256, JWSAlgorithm.PS384, JWSAlgorithm.PS512);
		Collection<Algorithm> clientSymmetricAndAsymmetricSigningAlgsWithNone = Lists.newArrayList(JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512,
				JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512,
				JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512,
				JWSAlgorithm.PS256, JWSAlgorithm.PS384, JWSAlgorithm.PS512,
				Algorithm.NONE);
		ArrayList<String> grantTypes = Lists.newArrayList("authorization_code", "implicit", "urn:ietf:params:oauth:grant-type:jwt-bearer", "client_credentials", "urn:ietf:params:oauth:grant_type:redelegate", "urn:ietf:params:oauth:grant-type:device_code","refresh_token");

		Map<String, Object> m = new HashMap<>();
		m.put("issuer", config.getIssuer());
		m.put("authorization_endpoint", baseUrl + "authorize");
		m.put("token_endpoint", baseUrl + "token");
		m.put("userinfo_endpoint", baseUrl + UserInfoEndpoint.URL);
		//check_session_iframe
		m.put("end_session_endpoint", baseUrl + EndSessionEndpoint.URL);
		m.put("jwks_uri", baseUrl + JWKSetPublishingEndpoint.URL);
		m.put("registration_endpoint", baseUrl + DynamicClientRegistrationEndpoint.URL);
		m.put("scopes_supported", scopeService.toStrings(scopeService.getUnrestricted())); // these are the scopes that you can dynamically register for, which is what matters for discovery
		m.put("response_types_supported", Lists.newArrayList("code", "token")); // we don't support these yet: , "id_token", "id_token token"));
		m.put("grant_types_supported", grantTypes);
		//acr_values_supported
		m.put("subject_types_supported", Lists.newArrayList("public", "pairwise"));
		m.put("userinfo_signing_alg_values_supported", Collections2.transform(clientSymmetricAndAsymmetricSigningAlgs, toAlgorithmName));
		m.put("userinfo_encryption_alg_values_supported", Collections2.transform(encService.getAllEncryptionAlgsSupported(), toAlgorithmName));
		m.put("userinfo_encryption_enc_values_supported", Collections2.transform(encService.getAllEncryptionEncsSupported(), toAlgorithmName));
		m.put("id_token_signing_alg_values_supported", Collections2.transform(clientSymmetricAndAsymmetricSigningAlgsWithNone, toAlgorithmName));
		m.put("id_token_encryption_alg_values_supported", Collections2.transform(encService.getAllEncryptionAlgsSupported(), toAlgorithmName));
		m.put("id_token_encryption_enc_values_supported", Collections2.transform(encService.getAllEncryptionEncsSupported(), toAlgorithmName));
		m.put("request_object_signing_alg_values_supported", Collections2.transform(clientSymmetricAndAsymmetricSigningAlgs, toAlgorithmName));
		m.put("request_object_encryption_alg_values_supported", Collections2.transform(encService.getAllEncryptionAlgsSupported(), toAlgorithmName));
		m.put("request_object_encryption_enc_values_supported", Collections2.transform(encService.getAllEncryptionEncsSupported(), toAlgorithmName));
		m.put("token_endpoint_auth_methods_supported", Lists.newArrayList("client_secret_post", "client_secret_basic", "client_secret_jwt", "private_key_jwt", "none"));
		m.put("token_endpoint_auth_signing_alg_values_supported", Collections2.transform(clientSymmetricAndAsymmetricSigningAlgs, toAlgorithmName));
		//display_types_supported
		m.put("claim_types_supported", Lists.newArrayList("normal" /*, "aggregated", "distributed"*/));
		m.put("claims_supported", Lists.newArrayList(
				"sub",
				"name",
				"preferred_username",
				"given_name",
				"family_name",
				"middle_name",
				"nickname",
				"profile",
				"picture",
				"website",
				"gender",
				"zoneinfo",
				"locale",
				"updated_at",
				"birthdate",
				"email",
				"email_verified",
				"phone_number",
				"phone_number_verified",
				"address"
				));
		m.put("service_documentation", baseUrl + "about");
		//claims_locales_supported
		//ui_locales_supported
		m.put("claims_parameter_supported", false);
		m.put("request_parameter_supported", true);
		m.put("request_uri_parameter_supported", false);
		m.put("require_request_uri_registration", false);
		m.put("op_policy_uri", baseUrl + "about");
		m.put("op_tos_uri", baseUrl + "about");

		m.put("introspection_endpoint", baseUrl + IntrospectionEndpoint.URL); // token introspection endpoint for verifying tokens
		m.put("revocation_endpoint", baseUrl + RevocationEndpoint.URL); // token revocation endpoint

		m.put("code_challenge_methods_supported", Lists.newArrayList(PKCEAlgorithm.plain.getName(), PKCEAlgorithm.S256.getName()));

		m.put("device_authorization_endpoint", baseUrl + DeviceEndpoint.URL);

		model.addAttribute(JsonEntityView.ENTITY, m);

		return JsonEntityView.VIEWNAME;
	}

}
