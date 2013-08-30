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
package org.bbplus;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.google.gson.JsonObject;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author Josh Mandel
 *
 */
public class PreregistrationToken extends OAuth2Authentication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JsonObject clientDefinitionFromTrustedRegistry;
	private SignedJWT jwt;
	
	public PreregistrationToken(OAuth2Request authorizationRequest,
			Authentication userAuthentication) {
		super(authorizationRequest, userAuthentication);
	}

	public static OAuth2Request emptyReq(String subject, boolean approved){
		Collection<String> scopes = new HashSet<String>();
		AuthorizationRequest req = new AuthorizationRequest(subject, scopes);
		req.setApproved(true);
		return req.createOAuth2Request();
	}
	
	public PreregistrationToken(JsonObject clientDefinition) {
		super(emptyReq(null, false), null);
		setClientDefinitionFromTrustedRegistry(clientDefinition);
	}

	public PreregistrationToken(String subject, JsonObject clientDefinition, boolean approved) {
		super(emptyReq(subject, approved), null);
		setClientDefinitionFromTrustedRegistry(clientDefinition);
	}

	public JsonObject getClientDefinitionFromTrustedRegistry() {
		return clientDefinitionFromTrustedRegistry;
	}

	public void setClientDefinitionFromTrustedRegistry(
			JsonObject clientDefinitionFromTrustedRegistry) {
		this.clientDefinitionFromTrustedRegistry = clientDefinitionFromTrustedRegistry;
	}

	public SignedJWT getJwt() {
		return jwt;
	}

	public void setJwt(SignedJWT jwt) {
		this.jwt = jwt;
	}


}
