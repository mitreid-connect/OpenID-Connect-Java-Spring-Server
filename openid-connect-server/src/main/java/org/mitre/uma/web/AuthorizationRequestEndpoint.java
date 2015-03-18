/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/

package org.mitre.uma.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.oauth2.web.AuthenticationUtilities;
import org.mitre.openid.connect.service.OIDCTokenService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.uma.model.Claim;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + AuthorizationRequestEndpoint.URL)
public class AuthorizationRequestEndpoint {
	// Logger for this class
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationRequestEndpoint.class);

	public static final String RPT = "rpt";
	public static final String TICKET = "ticket";
	public static final String URL = "authz_request";

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private OAuth2TokenEntityService tokenService;
	
	@Autowired
	private OIDCTokenService oidcTokenService;

	@Autowired
	private WebResponseExceptionTranslator providerExceptionHandler;

	@RequestMapping(method = RequestMethod.POST, consumes = MimeTypeUtils.APPLICATION_JSON_VALUE, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public String authorizationRequest(@RequestBody String jsonString, Model m, Authentication auth) {
		
		AuthenticationUtilities.ensureOAuthScope(auth, SystemScopeService.UMA_AUTHORIZATION_SCOPE);
		
		JsonParser parser = new JsonParser();
		JsonElement e = parser.parse(jsonString);
		
		if (e.isJsonObject()) {
			JsonObject o = e.getAsJsonObject();
			
			if (o.has(TICKET)) {
				
				OAuth2AccessTokenEntity rpt = null;
				if (o.has(RPT)) {
					String rptValue = o.get(RPT).getAsString();
					rpt = tokenService.readAccessToken(rptValue);
				}				
				
				String ticketValue = o.get(TICKET).getAsString();
				
				PermissionTicket ticket = permissionService.getByTicket(ticketValue);
				
				if (ticket != null) {
					// found the ticket, see if it's any good
					
					ResourceSet rs = ticket.getPermission().getResourceSet();
					Collection<Claim> claimsRequired = rs.getClaimsRequired();
					
					Collection<Claim> claimsSupplied = ticket.getClaimsSupplied();
					
					Collection<Claim> claimsUnmatched = new HashSet<>(claimsRequired);
					
					// see if each of the required claims has a counterpart in the supplied claims set
					for (Claim required : claimsRequired) {
						for (Claim supplied : claimsSupplied) {
							
							if (required.getIssuer().containsAll(supplied.getIssuer())) {
								// it's from the right issuer
								
								if (required.getName().equals(supplied.getName()) &&
										required.getValue().equals(supplied.getValue())) {
									
									// the claim matched, pull it from the set
									claimsUnmatched.remove(required);
									
								}
								
							}
							
						}
					}
					
					if (claimsUnmatched.isEmpty()) {
						// we matched all the claims, create and return the token
						//OAuth2AccessTokenEntity accessToken = oidcTokenService.createPermissionedToken(auth, perm);
						
						// PUNT!
						return JsonErrorView.VIEWNAME;
						
					} else {
						
						// if we got here, the claim didn't match, forward the user to the claim gathering endpoint
						JsonObject entity = new JsonObject();
						
						entity.addProperty("error", "need_info");
						JsonObject details = new JsonObject();
						
						JsonObject rpClaims = new JsonObject();
						rpClaims.addProperty("redirect_user", true);
						rpClaims.addProperty("ticket", ticketValue);
						JsonArray req = new JsonArray();
						for (Claim claim : claimsUnmatched) {
							JsonObject c = new JsonObject();
							c.addProperty("name", claim.getName());
							c.addProperty("friendly_name", claim.getFriendlyName());
							c.addProperty("claim_type", claim.getClaimType());
							JsonArray f = new JsonArray();
							for (String format : claim.getClaimTokenFormat()) {
								f.add(new JsonPrimitive(format));
							}
							c.add("claim_token_format", f);
							JsonArray i = new JsonArray();
							for (String issuer : claim.getIssuer()) {
								i.add(new JsonPrimitive(issuer));
							}
							c.add("issuer", i);
							req.add(c);
						}
						rpClaims.add("required_claims", req);
						details.add("requesting_party_claims", rpClaims);
						entity.add("error_details", details);
						
						m.addAttribute(JsonEntityView.ENTITY, entity);
						return JsonEntityView.VIEWNAME;
					}						
					
					
				} else {
					// ticket wasn't found, return an error
					m.addAttribute(HttpStatus.BAD_REQUEST);
					m.addAttribute(JsonErrorView.ERROR, "invalid_ticket");
					return JsonErrorView.VIEWNAME;
				}
				
			} else {
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Missing JSON elements.");
				return JsonErrorView.VIEWNAME;
			}
			
			
		} else {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "Malformed JSON request.");
			return JsonErrorView.VIEWNAME;
		}
		
	}
	


	@ExceptionHandler(OAuth2Exception.class)
	public ResponseEntity<OAuth2Exception> handleException(Exception e) throws Exception {
		logger.info("Handling error: " + e.getClass().getSimpleName() + ", " + e.getMessage());
		return providerExceptionHandler.translate(e);
	}
	
}
