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

package org.mitre.uma.token;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.service.OIDCTokenService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.uma.exception.InvalidTicketException;
import org.mitre.uma.exception.NeedInfoException;
import org.mitre.uma.exception.NotAuthorizedException;
import org.mitre.uma.model.Claim;
import org.mitre.uma.model.ClaimProcessingResult;
import org.mitre.uma.model.Permission;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.ClaimsProcessingService;
import org.mitre.uma.service.PermissionService;
import org.mitre.uma.service.UmaTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
@Component("requestingPartyTokenGranter")
public class RequestingPartyTokenGranter extends AbstractTokenGranter {

	private static final String grantType = "urn:ietf:params:oauth:grant_type:multiparty-delegation";

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Autowired
	private OIDCTokenService oidcTokenService;

	@Autowired
	private ClaimsProcessingService claimsProcessingService;

	@Autowired
	private UmaTokenService umaTokenService;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@Autowired
	private JWTSigningAndValidationService jwtService;

	
	/**
	 * @param tokenServices
	 * @param clientDetailsService
	 * @param requestFactory
	 * @param grantType
	 */
	@Autowired
	protected RequestingPartyTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, clientDetailsService, requestFactory, grantType);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getOAuth2Authentication(org.springframework.security.oauth2.provider.ClientDetails, org.springframework.security.oauth2.provider.TokenRequest)
	 */
	@Override
	protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {

		//String incomingTokenValue = tokenRequest.getRequestParameters().get("token");
		
		String ticketValue = tokenRequest.getRequestParameters().get("ticket");

		PermissionTicket ticket = permissionService.getByTicket(ticketValue);
		OAuth2AccessTokenEntity incomingRpt = null;
		String rptValue = tokenRequest.getRequestParameters().get("rpt");
		if (!Strings.isNullOrEmpty(rptValue)) {
			incomingRpt = tokenService.readAccessToken(rptValue);
		}

		if (ticket != null) {
			// found the ticket, see if it's any good

			ResourceSet rs = ticket.getPermission().getResourceSet();

			if (rs.getPolicies() == null || rs.getPolicies().isEmpty()) {
				// the required claims are empty, this resource has no way to be authorized

				throw new NotAuthorizedException();
			} else {
				// claims weren't empty or missing, we need to check against what we have

				ClaimProcessingResult result = claimsProcessingService.claimsAreSatisfied(rs, ticket);


				if (result.isSatisfied()) {
					// the service found what it was looking for, issue a token

					OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
					
					OAuth2Request clientAuth = tokenRequest.createOAuth2Request(client);
					OAuth2Authentication o2auth = new OAuth2Authentication(clientAuth, null);
					AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
					authHolder.setAuthentication(o2auth);

					token.setAuthenticationHolder(authHolder);

					ClientDetailsEntity clientEntity = clientService.loadClientByClientId(client.getClientId());
					token.setClient(clientEntity);

					Set<String> ticketScopes = ticket.getPermission().getScopes();
					Set<String> policyScopes = result.getMatched().getScopes();

					Permission perm = new Permission();
					perm.setResourceSet(ticket.getPermission().getResourceSet());
					perm.setScopes(new HashSet<>(Sets.intersection(ticketScopes, policyScopes)));

					token.setPermissions(Sets.newHashSet(perm));

					JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder();

					claims.audience(Lists.newArrayList(ticket.getPermission().getResourceSet().getId().toString()));
					claims.issuer(config.getIssuer());
					claims.jwtID(UUID.randomUUID().toString());

					if (config.getRqpTokenLifeTime() != null) {
						Date exp = new Date(System.currentTimeMillis() + config.getRqpTokenLifeTime() * 1000L);

						claims.expirationTime(exp);
						token.setExpiration(exp);
					}


					JWSAlgorithm signingAlgorithm = jwtService.getDefaultSigningAlgorithm();
					JWSHeader header = new JWSHeader(signingAlgorithm, null, null, null, null, null, null, null, null, null,
							jwtService.getDefaultSignerKeyId(),
							null, null);
					SignedJWT signed = new SignedJWT(header, claims.build());

					jwtService.signJwt(signed);

					token.setJwt(signed);

					tokenService.saveAccessToken(token);

					
					// if we have an inbound RPT, throw it out because we're replacing it
					if (incomingRpt != null) {
						tokenService.revokeAccessToken(incomingRpt);
					}

					return token;
				} else {

					throw new NeedInfoException(ticketValue, result.getUnmatched());
					
				}


			}
		} else {
			throw new InvalidTicketException();
		}

		
		
	
	}

	
	@ExceptionHandler(NeedInfoException.class)
	public ModelAndView handleUmaException(Exception e) {
		// if we got here, the claim didn't match, forward the user to the claim gathering endpoint
		
		NeedInfoException nie = (NeedInfoException)e;
		
		JsonObject entity = new JsonObject();

		entity.addProperty(JsonErrorView.ERROR, "need_info");
		JsonObject details = new JsonObject();

		JsonObject rpClaims = new JsonObject();
		rpClaims.addProperty("redirect_user", true);
		rpClaims.addProperty("ticket", nie.getTicketValue());
		JsonArray req = new JsonArray();
		for (Claim claim : nie.getUnmatched()) {
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

		Map<String, Object> m = new HashMap<>();
		m.put(JsonEntityView.ENTITY, entity);
		return new ModelAndView(JsonEntityView.VIEWNAME, m);
	}

	@ExceptionHandler(InvalidTicketException.class)
	public ModelAndView handleInvalidTicketException(Exception e) {
		// ticket wasn't found, return an error
		Map<String, Object> m = new HashMap<>();
		m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
		m.put(JsonErrorView.ERROR, "invalid_ticket");
		return new ModelAndView(JsonErrorView.VIEWNAME, m);
	}

	@ExceptionHandler(NotAuthorizedException.class)
	public ModelAndView handleNotAuthorizedException(Exception e) {
		Map<String, Object> m = new HashMap<>();
		m.put(JsonErrorView.ERROR, "not_authorized");
		m.put(JsonErrorView.ERROR_MESSAGE, "This resource set can not be accessed.");
		m.put(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
		return new ModelAndView(JsonErrorView.VIEWNAME, m);
	}
	
}
