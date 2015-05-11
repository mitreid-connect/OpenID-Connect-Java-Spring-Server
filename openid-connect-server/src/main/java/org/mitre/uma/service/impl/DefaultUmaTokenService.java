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

package org.mitre.uma.service.impl;

import java.util.Date;
import java.util.UUID;

import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.service.UmaTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
@Service("defaultUmaTokenService")
public class DefaultUmaTokenService implements UmaTokenService {

	@Autowired 
	private AuthenticationHolderRepository authenticationHolderRepository;
	
	@Autowired
	private OAuth2TokenEntityService tokenService;
		
	@Autowired 
	private ClientDetailsEntityService clientService;
	
	@Autowired 
	private ConfigurationPropertiesBean config;
	
	@Autowired 
	private JWTSigningAndValidationService jwtService;

	
	@Override
	public OAuth2AccessTokenEntity createRequestingPartyToken(OAuth2Authentication o2auth, PermissionTicket ticket) {
		OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setAuthentication(o2auth);
		authHolder = authenticationHolderRepository.save(authHolder);
		
		token.setAuthenticationHolder(authHolder);
		
		ClientDetailsEntity client = clientService.loadClientByClientId(o2auth.getOAuth2Request().getClientId());
		token.setClient(client);
		
		token.setPermissions(Sets.newHashSet(ticket.getPermission()));
		
		
		JWTClaimsSet claims = new JWTClaimsSet();
		
		claims.setAudience(Lists.newArrayList(ticket.getPermission().getResourceSet().getId().toString()));
		claims.setIssuer(config.getIssuer());
		claims.setJWTID(UUID.randomUUID().toString());
		
		if (config.getRqpTokenLifeTime() != null) {
			Date exp = new Date(System.currentTimeMillis() + config.getRqpTokenLifeTime() * 1000L);
			
			claims.setExpirationTime(exp);
			token.setExpiration(exp);
		}
		
		
		JWSAlgorithm signingAlgorithm = jwtService.getDefaultSigningAlgorithm();
		SignedJWT signed = new SignedJWT(new JWSHeader(signingAlgorithm), claims);
		
		jwtService.signJwt(signed);
		
		token.setJwt(signed);
		
		tokenService.saveAccessToken(token);

		return token;
	}

}
