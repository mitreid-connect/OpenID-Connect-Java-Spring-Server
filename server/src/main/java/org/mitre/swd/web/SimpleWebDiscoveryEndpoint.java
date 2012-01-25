package org.mitre.swd.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SimpleWebDiscoveryEndpoint {

	@RequestMapping(value="/.well-known/simple-web-discovery", 
					params={"principal", "service=http://openid.net/specs/connect/1.0/issuer"})
	public ModelAndView openIdConnectIssuerDiscovery(@RequestParam("principal") String principal, ModelAndView modelAndView) {
		return modelAndView;
	}
	

	/*	
	 * version 	string 	Version of the provider response. "3.0" is the default.
	 * issuer 	string 	The https: URL with no path component that the OP asserts as its Issuer Identifier
	 * authorization_endpoint 	string 	URL of the OP's Authentication and Authorization Endpoint [OpenID.Messages]
	 * 	token_endpoint 	string 	URL of the OP's OAuth 2.0 Token Endpoint [OpenID.Messages]
	 * 	userinfo_endpoint 	string 	URL of the OP's UserInfo Endpoint [OpenID.Messages]
	 * 	check_id_endpoint 	string 	URL of the OP's Check ID Endpoint [OpenID.Messages]
	 * 	refresh_session_endpoint 	string 	URL of the OP's Refresh Session Endpoint [OpenID.Session]
	 * 	end_session_endpoint 	string 	URL of the OP's End Session Endpoint [OpenID.Session]
	 * 	jwk_url 	string 	URL of the OP's JSON Web Key [JWK] document. Server's signing Key
	 * 	jwk_encryption_url 	string 	URL of the OP's JSON Web Key [JWK] document. Server's Encryption Key, if not present, its value is the same as the URL provided by jwk_url
	 * 	x509_url 	string 	URL of the OP's X.509 certificates in PEM format.
	 * 	x509_encryption_url 	string 	URL of the OP's X.509 certificates in PEM format. Server's Encryption Key, if not present its value is the same as the URL provided by x509_url
	 * 	registration_endpoint 	string 	URL of the OP's Dynamic Client Registration Endpoint [OpenID.Registration]
	 * 	scopes_supported 	array 	A JSON array containing a list of the OAuth 2.0 [OAuth2.0] scope values that this server supports. The server MUST support the openid scope value.
	 * 	response_types_supported 	array 	A JSON array containing a list of the OAuth 2.0 response_type that this server supports. The server MUST support the code response_type.
	 * 	acrs_supported 	array 	A JSON array containing a list of the Authentication Context Class References that this server supports.
	 * 	user_id_types_supported 	array 	A JSON array containing a list of the user identifier types that this server supports. Valid types include pairwise and public.
	 * 	userinfo_algs_supported 	array 	A JSON array containing a list of the JWS [JWS] and JWE [JWE] signing and encryption algorithms supported by the UserInfo Endpoint to encode the JWT [JWT].
	 * 	id_token_algs_supported 	array 	A JSON array containing a list of the JWS [JWS] and JWE [JWE] signing and encryption algorithms supported by the Authorization Server for the ID Token to encode the JWT [JWT].
	 * 	request_object_algs_supported 	array 	A JSON array containing a list of the JWS [JWS] and JWE [JWE] signing and encryption algorithms supported by the Authorization Server for the OpenID Request Object described in Section 2.1.2.1 of OpenID Connect Messages 1.0 [OpenID.Messages] to encode the JWT [JWT]. Servers SHOULD support HS256.
	 * 	token_endpoint_auth_types_supported 	array 	A JSON array containing a list of authentication types supported by this Token Endpoint. The options are client_secret_post, client_secret_basic, client_secret_jwt, and private_key_jwt, as described in Section 2.2.1 of OpenID Connect Messages 1.0 [OpenID.Messages]. Other Authentication types may be defined by extension. If unspecified or omitted, the default is client_secret_basic HTTP Basic Authentication Scheme as specified in section 2.3.1 of OAuth 2.0 [OAuth2.0].
	 * 	token_endpoint_auth_algs_supported 	array 	A JSON array containing a list of the JWS [JWS] signing algorithms supported by the Token Endpoint for the private_key_jwt method to encode the JWT [JWT]. Servers SHOULD support RS256.
	 */
	@RequestMapping("/.well-known/openid-configuration")
	public ModelAndView providerConfiguration(ModelAndView modelAndView) {

		Map m = modelAndView.getModel();
		m.put("version", "3.0");
		// TODO: everything in the list up there
		
		return modelAndView;
	}
	
}
