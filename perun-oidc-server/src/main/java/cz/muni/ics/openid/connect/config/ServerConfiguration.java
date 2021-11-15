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
package cz.muni.ics.openid.connect.config;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import java.util.List;



/**
 *
 * Container class for a client's view of a server's configuration
 *
 * @author nemonik, jricher
 *
 */
public class ServerConfiguration {

	/*
	 *
    issuer
        REQUIRED. URL using the https scheme with no query or fragment component that the OP asserts as its Issuer Identifier.
    authorization_endpoint
        OPTIONAL. URL of the OP's Authentication and Authorization Endpoint [OpenID.Messages].
    token_endpoint
        OPTIONAL. URL of the OP's OAuth 2.0 Token Endpoint [OpenID.Messages].
    userinfo_endpoint
        RECOMMENDED. URL of the OP's UserInfo Endpoint [OpenID.Messages]. This URL MUST use the
        https scheme and MAY contain port, path, and query parameter components.
    check_session_iframe
        OPTIONAL. URL of an OP endpoint that provides a page to support cross-origin communications for
        session state information with the RP Client, using the HTML5 postMessage API. The page is loaded
        from an invisible iframe embedded in an RP page so that it can run in the OP's security context. See [OpenID.Session].
    end_session_endpoint
        OPTIONAL. URL of the OP's endpoint that initiates logging out the End-User. See [OpenID.Session].
    jwks_uri
        REQUIRED. URL of the OP's JSON Web Key Set [JWK] document. This contains the signing key(s) the
        Client uses to validate signatures from the OP. The JWK Set MAY also contain the Server's encryption key(s), which are used by Clients to encrypt requests to the Server. When both signing and encryption keys are made available, a use (Key Use) parameter value is REQUIRED for all keys in the document to indicate each key's intended usage.
    registration_endpoint
        RECOMMENDED. URL of the OP's Dynamic Client Registration Endpoint [OpenID.Registration].
    scopes_supported
        RECOMMENDED. JSON array containing a list of the OAuth 2.0 [RFC6749] scope values that this server
    response_types_supported
        REQUIRED. JSON array containing a list of the OAuth 2.0 response_type values that this server
        supports. The server MUST support the code, id_token, and the token id_token response type values.
    grant_types_supported
        OPTIONAL. JSON array containing a list of the OAuth 2.0 grant type values that this server supports.
        The server MUST support the authorization_code and implicit grant type values and MAY support the
        urn:ietf:params:oauth:grant-type:jwt-bearer grant type defined in OAuth JWT Bearer Token Profiles [OAuth.JWT].
        If omitted, the default value is ["authorization_code", "implicit"].
    acr_values_supported
        OPTIONAL. JSON array containing a list of the Authentication Context Class References that this server supports.
    subject_types_supported
        REQUIRED. JSON array containing a list of the subject identifier types that this server supports.
        Valid types include pairwise and public.
    userinfo_signing_alg_values_supported
        OPTIONAL. JSON array containing a list of the JWS [JWS] signing algorithms (alg values) [JWA] supported
        by the UserInfo Endpoint to encode the Claims in a JWT [JWT].
    userinfo_encryption_alg_values_supported
        OPTIONAL. JSON array containing a list of the JWE [JWE] encryption algorithms (alg values) [JWA] supported
        by the UserInfo Endpoint to encode the Claims in a JWT [JWT].
    userinfo_encryption_enc_values_supported
        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) [JWA] supported
        by the UserInfo Endpoint to encode the Claims in a JWT [JWT].
    id_token_signing_alg_values_supported
        REQUIRED. JSON array containing a list of the JWS signing algorithms (alg values) supported by the
        Authorization Server for the ID Token to encode the Claims in a JWT [JWT].
    id_token_encryption_alg_values_supported
        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (alg values) supported by the
        Authorization Server for the ID Token to encode the Claims in a JWT [JWT].
    id_token_encryption_enc_values_supported
        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) supported by the
        Authorization Server for the ID Token to encode the Claims in a JWT [JWT].
    request_object_signing_alg_values_supported
        OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the
        Authorization Server for the Request Object described in Section 2.9 of OpenID Connect Messages 1.0
        [OpenID.Messages]. These algorithms are used both when the Request Object is passed by value (using the
        request parameter) and when it is passed by reference (using the request_uri parameter). Servers SHOULD
        support none and RS256.
    request_object_encryption_alg_values_supported
        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (alg values) supported by the
        Authorization Server for the Request Object described in Section 2.9 of OpenID Connect Messages 1.0
        [OpenID.Messages]. These algorithms are used both when the Request Object is passed by value and when it
        is passed by reference.
    request_object_encryption_enc_values_supported
        OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) supported by the
        Authorization Server for the Request Object described in Section 2.9 of OpenID Connect Messages 1.0
        [OpenID.Messages]. These algorithms are used both when the Request Object is passed by value and when
        it is passed by reference.
    token_endpoint_auth_methods_supported
        OPTIONAL. JSON array containing a list of authentication methods supported by this Token Endpoint.
        The options are client_secret_post, client_secret_basic, client_secret_jwt, and private_key_jwt, as
        described in Section 2.2.1 of OpenID Connect Messages 1.0 [OpenID.Messages]. Other authentication
        methods MAY be defined by extensions. If omitted, the default is client_secret_basic -- the HTTP
        Basic Authentication Scheme as specified in Section 2.3.1 of OAuth 2.0 [RFC6749].
    token_endpoint_auth_signing_alg_values_supported
        OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the
        Token Endpoint for the private_key_jwt and client_secret_jwt methods to encode the JWT [JWT]. Servers
        SHOULD support RS256.
    display_values_supported
        OPTIONAL. JSON array containing a list of the display parameter values that the OpenID Provider
        supports. These values are described in Section 2.1.1 of OpenID Connect Messages 1.0 [OpenID.Messages].
    claim_types_supported
        OPTIONAL. JSON array containing a list of the Claim Types that the OpenID Provider supports. These Claim
        Types are described in Section 2.6 of OpenID Connect Messages 1.0 [OpenID.Messages]. Values defined by
        this specification are normal, aggregated, and distributed. If not specified, the implementation supports
        only normal Claims.
    claims_supported
        RECOMMENDED. JSON array containing a list of the Claim Names of the Claims that the OpenID Provider MAY
        be able to supply values for. Note that for privacy or other reasons, this might not be an exhaustive list.
    service_documentation
        OPTIONAL. URL of a page containing human-readable information that developers might want or need to
        know when using the OpenID Provider. In particular, if the OpenID Provider does not support Dynamic
        Client Registration, then information on how to register Clients needs to be provided in this documentation.
    claims_locales_supported
        OPTIONAL. Languages and scripts supported for values in Claims being returned, represented as a JSON array
        of BCP47 [RFC5646] language tag values. Not all languages and scripts are necessarily supported for all
        Claim values.
    ui_locales_supported
        OPTIONAL. Languages and scripts supported for the user interface, represented as a JSON array of BCP47
        [RFC5646] language tag values.
    claims_parameter_supported
        OPTIONAL. Boolean value specifying whether the OP supports use of the claims parameter, with true
        indicating support. If omitted, the default value is false.
    request_parameter_supported
        OPTIONAL. Boolean value specifying whether the OP supports use of the request parameter, with true
        indicating support. If omitted, the default value is false.
    request_uri_parameter_supported
        OPTIONAL. Boolean value specifying whether the OP supports use of the request_uri parameter, with
        true indicating support. If omitted, the default value is true.
    require_request_uri_registration
        OPTIONAL. Boolean value specifying whether the OP requires any request_uri values used to be
        pre-registered using the request_uris registration parameter. Pre-registration is REQUIRED when
        the value is true. If omitted, the default value is false.
    op_policy_uri
        OPTIONAL. URL that the OpenID Provider provides to the person registering the Client to read
        about the OP's requirements on how the Relying Party can use the data provided by the OP. The
        registration process SHOULD display this URL to the person registering the Client if it is given.
    op_tos_uri
        OPTIONAL. URL that the OpenID Provider provides to the person registering the Client to read about
        OpenID Provider's terms of service. The registration process SHOULD display this URL to the person
        registering the Client if it is given.
	 */

	private String authorizationEndpointUri;
	private String tokenEndpointUri;
	private String registrationEndpointUri;
	private String issuer;
	private String jwksUri;
	private String userInfoUri;
	private String introspectionEndpointUri;
	private String revocationEndpointUri;
	private String checkSessionIframe;
	private String endSessionEndpoint;
	private List<String> scopesSupported;
	private List<String> responseTypesSupported;
	private List<String> grantTypesSupported;
	private List<String> acrValuesSupported;
	private List<String> subjectTypesSupported;
	private List<JWSAlgorithm> userinfoSigningAlgValuesSupported;
	private List<JWEAlgorithm> userinfoEncryptionAlgValuesSupported;
	private List<EncryptionMethod> userinfoEncryptionEncValuesSupported;
	private List<JWSAlgorithm> idTokenSigningAlgValuesSupported;
	private List<JWEAlgorithm> idTokenEncryptionAlgValuesSupported;
	private List<EncryptionMethod> idTokenEncryptionEncValuesSupported;
	private List<JWSAlgorithm> requestObjectSigningAlgValuesSupported;
	private List<JWEAlgorithm> requestObjectEncryptionAlgValuesSupported;
	private List<EncryptionMethod> requestObjectEncryptionEncValuesSupported;
	private List<String> tokenEndpointAuthMethodsSupported;
	private List<JWSAlgorithm> tokenEndpointAuthSigningAlgValuesSupported;
	private List<String> displayValuesSupported;
	private List<String> claimTypesSupported;
	private List<String> claimsSupported;
	private String serviceDocumentation;
	private List<String> claimsLocalesSupported;
	private List<String> uiLocalesSupported;
	private Boolean claimsParameterSupported;
	private Boolean requestParameterSupported;
	private Boolean requestUriParameterSupported;
	private Boolean requireRequestUriRegistration;
	private String opPolicyUri;
	private String opTosUri;

	private UserInfoTokenMethod userInfoTokenMethod;

	public enum UserInfoTokenMethod {
		HEADER,
		FORM,
		QUERY
	}

	public String getAuthorizationEndpointUri() {
		return authorizationEndpointUri;
	}

	public void setAuthorizationEndpointUri(String authorizationEndpointUri) {
		this.authorizationEndpointUri = authorizationEndpointUri;
	}

	public String getTokenEndpointUri() {
		return tokenEndpointUri;
	}

	public void setTokenEndpointUri(String tokenEndpointUri) {
		this.tokenEndpointUri = tokenEndpointUri;
	}

	public String getRegistrationEndpointUri() {
		return registrationEndpointUri;
	}

	public void setRegistrationEndpointUri(String registrationEndpointUri) {
		this.registrationEndpointUri = registrationEndpointUri;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getJwksUri() {
		return jwksUri;
	}

	public void setJwksUri(String jwksUri) {
		this.jwksUri = jwksUri;
	}

	public String getUserInfoUri() {
		return userInfoUri;
	}

	public void setUserInfoUri(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}

	public String getIntrospectionEndpointUri() {
		return introspectionEndpointUri;
	}

	public void setIntrospectionEndpointUri(String introspectionEndpointUri) {
		this.introspectionEndpointUri = introspectionEndpointUri;
	}

	public String getCheckSessionIframe() {
		return checkSessionIframe;
	}

	public void setCheckSessionIframe(String checkSessionIframe) {
		this.checkSessionIframe = checkSessionIframe;
	}

	public String getEndSessionEndpoint() {
		return endSessionEndpoint;
	}

	public void setEndSessionEndpoint(String endSessionEndpoint) {
		this.endSessionEndpoint = endSessionEndpoint;
	}

	public List<String> getScopesSupported() {
		return scopesSupported;
	}

	public void setScopesSupported(List<String> scopesSupported) {
		this.scopesSupported = scopesSupported;
	}

	public List<String> getResponseTypesSupported() {
		return responseTypesSupported;
	}

	public void setResponseTypesSupported(List<String> responseTypesSupported) {
		this.responseTypesSupported = responseTypesSupported;
	}

	public List<String> getGrantTypesSupported() {
		return grantTypesSupported;
	}

	public void setGrantTypesSupported(List<String> grantTypesSupported) {
		this.grantTypesSupported = grantTypesSupported;
	}

	public List<String> getAcrValuesSupported() {
		return acrValuesSupported;
	}

	public void setAcrValuesSupported(List<String> acrValuesSupported) {
		this.acrValuesSupported = acrValuesSupported;
	}

	public List<String> getSubjectTypesSupported() {
		return subjectTypesSupported;
	}

	public void setSubjectTypesSupported(List<String> subjectTypesSupported) {
		this.subjectTypesSupported = subjectTypesSupported;
	}

	public List<JWSAlgorithm> getUserinfoSigningAlgValuesSupported() {
		return userinfoSigningAlgValuesSupported;
	}

	public void setUserinfoSigningAlgValuesSupported(List<JWSAlgorithm> userinfoSigningAlgValuesSupported) {
		this.userinfoSigningAlgValuesSupported = userinfoSigningAlgValuesSupported;
	}

	public List<JWEAlgorithm> getUserinfoEncryptionAlgValuesSupported() {
		return userinfoEncryptionAlgValuesSupported;
	}

	public void setUserinfoEncryptionAlgValuesSupported(List<JWEAlgorithm> userinfoEncryptionAlgValuesSupported) {
		this.userinfoEncryptionAlgValuesSupported = userinfoEncryptionAlgValuesSupported;
	}

	public List<EncryptionMethod> getUserinfoEncryptionEncValuesSupported() {
		return userinfoEncryptionEncValuesSupported;
	}

	public void setUserinfoEncryptionEncValuesSupported(List<EncryptionMethod> userinfoEncryptionEncValuesSupported) {
		this.userinfoEncryptionEncValuesSupported = userinfoEncryptionEncValuesSupported;
	}

	public List<JWSAlgorithm> getIdTokenSigningAlgValuesSupported() {
		return idTokenSigningAlgValuesSupported;
	}

	public void setIdTokenSigningAlgValuesSupported(List<JWSAlgorithm> idTokenSigningAlgValuesSupported) {
		this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
	}

	public List<JWEAlgorithm> getIdTokenEncryptionAlgValuesSupported() {
		return idTokenEncryptionAlgValuesSupported;
	}

	public void setIdTokenEncryptionAlgValuesSupported(List<JWEAlgorithm> idTokenEncryptionAlgValuesSupported) {
		this.idTokenEncryptionAlgValuesSupported = idTokenEncryptionAlgValuesSupported;
	}

	public List<EncryptionMethod> getIdTokenEncryptionEncValuesSupported() {
		return idTokenEncryptionEncValuesSupported;
	}

	public void setIdTokenEncryptionEncValuesSupported(List<EncryptionMethod> idTokenEncryptionEncValuesSupported) {
		this.idTokenEncryptionEncValuesSupported = idTokenEncryptionEncValuesSupported;
	}

	public List<JWSAlgorithm> getRequestObjectSigningAlgValuesSupported() {
		return requestObjectSigningAlgValuesSupported;
	}

	public void setRequestObjectSigningAlgValuesSupported(List<JWSAlgorithm> requestObjectSigningAlgValuesSupported) {
		this.requestObjectSigningAlgValuesSupported = requestObjectSigningAlgValuesSupported;
	}

	public List<JWEAlgorithm> getRequestObjectEncryptionAlgValuesSupported() {
		return requestObjectEncryptionAlgValuesSupported;
	}

	public void setRequestObjectEncryptionAlgValuesSupported(List<JWEAlgorithm> requestObjectEncryptionAlgValuesSupported) {
		this.requestObjectEncryptionAlgValuesSupported = requestObjectEncryptionAlgValuesSupported;
	}

	public List<EncryptionMethod> getRequestObjectEncryptionEncValuesSupported() {
		return requestObjectEncryptionEncValuesSupported;
	}

	public void setRequestObjectEncryptionEncValuesSupported(List<EncryptionMethod> requestObjectEncryptionEncValuesSupported) {
		this.requestObjectEncryptionEncValuesSupported = requestObjectEncryptionEncValuesSupported;
	}

	public List<String> getTokenEndpointAuthMethodsSupported() {
		return tokenEndpointAuthMethodsSupported;
	}

	public void setTokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
		this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
	}

	public List<JWSAlgorithm> getTokenEndpointAuthSigningAlgValuesSupported() {
		return tokenEndpointAuthSigningAlgValuesSupported;
	}

	public void setTokenEndpointAuthSigningAlgValuesSupported(List<JWSAlgorithm> tokenEndpointAuthSigningAlgValuesSupported) {
		this.tokenEndpointAuthSigningAlgValuesSupported = tokenEndpointAuthSigningAlgValuesSupported;
	}

	public List<String> getDisplayValuesSupported() {
		return displayValuesSupported;
	}

	public void setDisplayValuesSupported(List<String> displayValuesSupported) {
		this.displayValuesSupported = displayValuesSupported;
	}

	public List<String> getClaimTypesSupported() {
		return claimTypesSupported;
	}

	public void setClaimTypesSupported(List<String> claimTypesSupported) {
		this.claimTypesSupported = claimTypesSupported;
	}

	public List<String> getClaimsSupported() {
		return claimsSupported;
	}

	public void setClaimsSupported(List<String> claimsSupported) {
		this.claimsSupported = claimsSupported;
	}

	public String getServiceDocumentation() {
		return serviceDocumentation;
	}

	public void setServiceDocumentation(String serviceDocumentation) {
		this.serviceDocumentation = serviceDocumentation;
	}

	public List<String> getClaimsLocalesSupported() {
		return claimsLocalesSupported;
	}

	public void setClaimsLocalesSupported(List<String> claimsLocalesSupported) {
		this.claimsLocalesSupported = claimsLocalesSupported;
	}

	public List<String> getUiLocalesSupported() {
		return uiLocalesSupported;
	}

	public void setUiLocalesSupported(List<String> uiLocalesSupported) {
		this.uiLocalesSupported = uiLocalesSupported;
	}

	public Boolean getClaimsParameterSupported() {
		return claimsParameterSupported;
	}

	public void setClaimsParameterSupported(Boolean claimsParameterSupported) {
		this.claimsParameterSupported = claimsParameterSupported;
	}

	public Boolean getRequestParameterSupported() {
		return requestParameterSupported;
	}

	public void setRequestParameterSupported(Boolean requestParameterSupported) {
		this.requestParameterSupported = requestParameterSupported;
	}

	public Boolean getRequestUriParameterSupported() {
		return requestUriParameterSupported;
	}

	public void setRequestUriParameterSupported(Boolean requestUriParameterSupported) {
		this.requestUriParameterSupported = requestUriParameterSupported;
	}

	public Boolean getRequireRequestUriRegistration() {
		return requireRequestUriRegistration;
	}

	public void setRequireRequestUriRegistration(Boolean requireRequestUriRegistration) {
		this.requireRequestUriRegistration = requireRequestUriRegistration;
	}

	public String getOpPolicyUri() {
		return opPolicyUri;
	}

	public void setOpPolicyUri(String opPolicyUri) {
		this.opPolicyUri = opPolicyUri;
	}

	public String getOpTosUri() {
		return opTosUri;
	}

	public void setOpTosUri(String opTosUri) {
		this.opTosUri = opTosUri;
	}

	public String getRevocationEndpointUri() {
		return revocationEndpointUri;
	}

	public void setRevocationEndpointUri(String revocationEndpointUri) {
		this.revocationEndpointUri = revocationEndpointUri;
	}

	public UserInfoTokenMethod getUserInfoTokenMethod() {
		return userInfoTokenMethod;
	}

	public void setUserInfoTokenMethod(UserInfoTokenMethod userInfoTokenMethod) {
		this.userInfoTokenMethod = userInfoTokenMethod;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((acrValuesSupported == null) ? 0 : acrValuesSupported
						.hashCode());
		result = prime
				* result
				+ ((authorizationEndpointUri == null) ? 0
						: authorizationEndpointUri.hashCode());
		result = prime
				* result
				+ ((checkSessionIframe == null) ? 0 : checkSessionIframe
						.hashCode());
		result = prime
				* result
				+ ((claimTypesSupported == null) ? 0 : claimTypesSupported
						.hashCode());
		result = prime
				* result
				+ ((claimsLocalesSupported == null) ? 0
						: claimsLocalesSupported.hashCode());
		result = prime
				* result
				+ ((claimsParameterSupported == null) ? 0
						: claimsParameterSupported.hashCode());
		result = prime * result
				+ ((claimsSupported == null) ? 0 : claimsSupported.hashCode());
		result = prime
				* result
				+ ((displayValuesSupported == null) ? 0
						: displayValuesSupported.hashCode());
		result = prime
				* result
				+ ((endSessionEndpoint == null) ? 0 : endSessionEndpoint
						.hashCode());
		result = prime
				* result
				+ ((grantTypesSupported == null) ? 0 : grantTypesSupported
						.hashCode());
		result = prime
				* result
				+ ((idTokenEncryptionAlgValuesSupported == null) ? 0
						: idTokenEncryptionAlgValuesSupported.hashCode());
		result = prime
				* result
				+ ((idTokenEncryptionEncValuesSupported == null) ? 0
						: idTokenEncryptionEncValuesSupported.hashCode());
		result = prime
				* result
				+ ((idTokenSigningAlgValuesSupported == null) ? 0
						: idTokenSigningAlgValuesSupported.hashCode());
		result = prime
				* result
				+ ((introspectionEndpointUri == null) ? 0
						: introspectionEndpointUri.hashCode());
		result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
		result = prime * result + ((jwksUri == null) ? 0 : jwksUri.hashCode());
		result = prime * result
				+ ((opPolicyUri == null) ? 0 : opPolicyUri.hashCode());
		result = prime * result
				+ ((opTosUri == null) ? 0 : opTosUri.hashCode());
		result = prime
				* result
				+ ((registrationEndpointUri == null) ? 0
						: registrationEndpointUri.hashCode());
		result = prime
				* result
				+ ((requestObjectEncryptionAlgValuesSupported == null) ? 0
						: requestObjectEncryptionAlgValuesSupported.hashCode());
		result = prime
				* result
				+ ((requestObjectEncryptionEncValuesSupported == null) ? 0
						: requestObjectEncryptionEncValuesSupported.hashCode());
		result = prime
				* result
				+ ((requestObjectSigningAlgValuesSupported == null) ? 0
						: requestObjectSigningAlgValuesSupported.hashCode());
		result = prime
				* result
				+ ((requestParameterSupported == null) ? 0
						: requestParameterSupported.hashCode());
		result = prime
				* result
				+ ((requestUriParameterSupported == null) ? 0
						: requestUriParameterSupported.hashCode());
		result = prime
				* result
				+ ((requireRequestUriRegistration == null) ? 0
						: requireRequestUriRegistration.hashCode());
		result = prime
				* result
				+ ((responseTypesSupported == null) ? 0
						: responseTypesSupported.hashCode());
		result = prime
				* result
				+ ((revocationEndpointUri == null) ? 0 : revocationEndpointUri
						.hashCode());
		result = prime * result
				+ ((scopesSupported == null) ? 0 : scopesSupported.hashCode());
		result = prime
				* result
				+ ((serviceDocumentation == null) ? 0 : serviceDocumentation
						.hashCode());
		result = prime
				* result
				+ ((subjectTypesSupported == null) ? 0 : subjectTypesSupported
						.hashCode());
		result = prime
				* result
				+ ((tokenEndpointAuthMethodsSupported == null) ? 0
						: tokenEndpointAuthMethodsSupported.hashCode());
		result = prime
				* result
				+ ((tokenEndpointAuthSigningAlgValuesSupported == null) ? 0
						: tokenEndpointAuthSigningAlgValuesSupported.hashCode());
		result = prime
				* result
				+ ((tokenEndpointUri == null) ? 0 : tokenEndpointUri.hashCode());
		result = prime
				* result
				+ ((uiLocalesSupported == null) ? 0 : uiLocalesSupported
						.hashCode());
		result = prime * result
				+ ((userInfoUri == null) ? 0 : userInfoUri.hashCode());
		result = prime
				* result
				+ ((userinfoEncryptionAlgValuesSupported == null) ? 0
						: userinfoEncryptionAlgValuesSupported.hashCode());
		result = prime
				* result
				+ ((userinfoEncryptionEncValuesSupported == null) ? 0
						: userinfoEncryptionEncValuesSupported.hashCode());
		result = prime
				* result
				+ ((userinfoSigningAlgValuesSupported == null) ? 0
						: userinfoSigningAlgValuesSupported.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ServerConfiguration other = (ServerConfiguration) obj;
		if (acrValuesSupported == null) {
			if (other.acrValuesSupported != null) {
				return false;
			}
		} else if (!acrValuesSupported.equals(other.acrValuesSupported)) {
			return false;
		}
		if (authorizationEndpointUri == null) {
			if (other.authorizationEndpointUri != null) {
				return false;
			}
		} else if (!authorizationEndpointUri
				.equals(other.authorizationEndpointUri)) {
			return false;
		}
		if (checkSessionIframe == null) {
			if (other.checkSessionIframe != null) {
				return false;
			}
		} else if (!checkSessionIframe.equals(other.checkSessionIframe)) {
			return false;
		}
		if (claimTypesSupported == null) {
			if (other.claimTypesSupported != null) {
				return false;
			}
		} else if (!claimTypesSupported.equals(other.claimTypesSupported)) {
			return false;
		}
		if (claimsLocalesSupported == null) {
			if (other.claimsLocalesSupported != null) {
				return false;
			}
		} else if (!claimsLocalesSupported.equals(other.claimsLocalesSupported)) {
			return false;
		}
		if (claimsParameterSupported == null) {
			if (other.claimsParameterSupported != null) {
				return false;
			}
		} else if (!claimsParameterSupported
				.equals(other.claimsParameterSupported)) {
			return false;
		}
		if (claimsSupported == null) {
			if (other.claimsSupported != null) {
				return false;
			}
		} else if (!claimsSupported.equals(other.claimsSupported)) {
			return false;
		}
		if (displayValuesSupported == null) {
			if (other.displayValuesSupported != null) {
				return false;
			}
		} else if (!displayValuesSupported.equals(other.displayValuesSupported)) {
			return false;
		}
		if (endSessionEndpoint == null) {
			if (other.endSessionEndpoint != null) {
				return false;
			}
		} else if (!endSessionEndpoint.equals(other.endSessionEndpoint)) {
			return false;
		}
		if (grantTypesSupported == null) {
			if (other.grantTypesSupported != null) {
				return false;
			}
		} else if (!grantTypesSupported.equals(other.grantTypesSupported)) {
			return false;
		}
		if (idTokenEncryptionAlgValuesSupported == null) {
			if (other.idTokenEncryptionAlgValuesSupported != null) {
				return false;
			}
		} else if (!idTokenEncryptionAlgValuesSupported
				.equals(other.idTokenEncryptionAlgValuesSupported)) {
			return false;
		}
		if (idTokenEncryptionEncValuesSupported == null) {
			if (other.idTokenEncryptionEncValuesSupported != null) {
				return false;
			}
		} else if (!idTokenEncryptionEncValuesSupported
				.equals(other.idTokenEncryptionEncValuesSupported)) {
			return false;
		}
		if (idTokenSigningAlgValuesSupported == null) {
			if (other.idTokenSigningAlgValuesSupported != null) {
				return false;
			}
		} else if (!idTokenSigningAlgValuesSupported
				.equals(other.idTokenSigningAlgValuesSupported)) {
			return false;
		}
		if (introspectionEndpointUri == null) {
			if (other.introspectionEndpointUri != null) {
				return false;
			}
		} else if (!introspectionEndpointUri
				.equals(other.introspectionEndpointUri)) {
			return false;
		}
		if (issuer == null) {
			if (other.issuer != null) {
				return false;
			}
		} else if (!issuer.equals(other.issuer)) {
			return false;
		}
		if (jwksUri == null) {
			if (other.jwksUri != null) {
				return false;
			}
		} else if (!jwksUri.equals(other.jwksUri)) {
			return false;
		}
		if (opPolicyUri == null) {
			if (other.opPolicyUri != null) {
				return false;
			}
		} else if (!opPolicyUri.equals(other.opPolicyUri)) {
			return false;
		}
		if (opTosUri == null) {
			if (other.opTosUri != null) {
				return false;
			}
		} else if (!opTosUri.equals(other.opTosUri)) {
			return false;
		}
		if (registrationEndpointUri == null) {
			if (other.registrationEndpointUri != null) {
				return false;
			}
		} else if (!registrationEndpointUri
				.equals(other.registrationEndpointUri)) {
			return false;
		}
		if (requestObjectEncryptionAlgValuesSupported == null) {
			if (other.requestObjectEncryptionAlgValuesSupported != null) {
				return false;
			}
		} else if (!requestObjectEncryptionAlgValuesSupported
				.equals(other.requestObjectEncryptionAlgValuesSupported)) {
			return false;
		}
		if (requestObjectEncryptionEncValuesSupported == null) {
			if (other.requestObjectEncryptionEncValuesSupported != null) {
				return false;
			}
		} else if (!requestObjectEncryptionEncValuesSupported
				.equals(other.requestObjectEncryptionEncValuesSupported)) {
			return false;
		}
		if (requestObjectSigningAlgValuesSupported == null) {
			if (other.requestObjectSigningAlgValuesSupported != null) {
				return false;
			}
		} else if (!requestObjectSigningAlgValuesSupported
				.equals(other.requestObjectSigningAlgValuesSupported)) {
			return false;
		}
		if (requestParameterSupported == null) {
			if (other.requestParameterSupported != null) {
				return false;
			}
		} else if (!requestParameterSupported
				.equals(other.requestParameterSupported)) {
			return false;
		}
		if (requestUriParameterSupported == null) {
			if (other.requestUriParameterSupported != null) {
				return false;
			}
		} else if (!requestUriParameterSupported
				.equals(other.requestUriParameterSupported)) {
			return false;
		}
		if (requireRequestUriRegistration == null) {
			if (other.requireRequestUriRegistration != null) {
				return false;
			}
		} else if (!requireRequestUriRegistration
				.equals(other.requireRequestUriRegistration)) {
			return false;
		}
		if (responseTypesSupported == null) {
			if (other.responseTypesSupported != null) {
				return false;
			}
		} else if (!responseTypesSupported.equals(other.responseTypesSupported)) {
			return false;
		}
		if (revocationEndpointUri == null) {
			if (other.revocationEndpointUri != null) {
				return false;
			}
		} else if (!revocationEndpointUri.equals(other.revocationEndpointUri)) {
			return false;
		}
		if (scopesSupported == null) {
			if (other.scopesSupported != null) {
				return false;
			}
		} else if (!scopesSupported.equals(other.scopesSupported)) {
			return false;
		}
		if (serviceDocumentation == null) {
			if (other.serviceDocumentation != null) {
				return false;
			}
		} else if (!serviceDocumentation.equals(other.serviceDocumentation)) {
			return false;
		}
		if (subjectTypesSupported == null) {
			if (other.subjectTypesSupported != null) {
				return false;
			}
		} else if (!subjectTypesSupported.equals(other.subjectTypesSupported)) {
			return false;
		}
		if (tokenEndpointAuthMethodsSupported == null) {
			if (other.tokenEndpointAuthMethodsSupported != null) {
				return false;
			}
		} else if (!tokenEndpointAuthMethodsSupported
				.equals(other.tokenEndpointAuthMethodsSupported)) {
			return false;
		}
		if (tokenEndpointAuthSigningAlgValuesSupported == null) {
			if (other.tokenEndpointAuthSigningAlgValuesSupported != null) {
				return false;
			}
		} else if (!tokenEndpointAuthSigningAlgValuesSupported
				.equals(other.tokenEndpointAuthSigningAlgValuesSupported)) {
			return false;
		}
		if (tokenEndpointUri == null) {
			if (other.tokenEndpointUri != null) {
				return false;
			}
		} else if (!tokenEndpointUri.equals(other.tokenEndpointUri)) {
			return false;
		}
		if (uiLocalesSupported == null) {
			if (other.uiLocalesSupported != null) {
				return false;
			}
		} else if (!uiLocalesSupported.equals(other.uiLocalesSupported)) {
			return false;
		}
		if (userInfoUri == null) {
			if (other.userInfoUri != null) {
				return false;
			}
		} else if (!userInfoUri.equals(other.userInfoUri)) {
			return false;
		}
		if (userinfoEncryptionAlgValuesSupported == null) {
			if (other.userinfoEncryptionAlgValuesSupported != null) {
				return false;
			}
		} else if (!userinfoEncryptionAlgValuesSupported
				.equals(other.userinfoEncryptionAlgValuesSupported)) {
			return false;
		}
		if (userinfoEncryptionEncValuesSupported == null) {
			if (other.userinfoEncryptionEncValuesSupported != null) {
				return false;
			}
		} else if (!userinfoEncryptionEncValuesSupported
				.equals(other.userinfoEncryptionEncValuesSupported)) {
			return false;
		}
		if (userinfoSigningAlgValuesSupported == null) {
			return other.userinfoSigningAlgValuesSupported == null;
		} else return userinfoSigningAlgValuesSupported
				.equals(other.userinfoSigningAlgValuesSupported);
	}

}
