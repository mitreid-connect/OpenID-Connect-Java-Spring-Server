/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.jwt.encryption.service.JwtEncryptionAndDecryptionService;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.jwt.signer.service.impl.SymmetricCacheService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author jricher
 *
 */
@Component("userInfoJwtView")
public class UserInfoJwtView extends UserInfoView {

	private static Logger logger = LoggerFactory.getLogger(UserInfoJwtView.class);

	@Autowired
	private JwtSigningAndValidationService jwtService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@Autowired
	private JWKSetCacheService encrypters;

	@Autowired
	private SymmetricCacheService symmetricCacheService;

	@Override
	protected void writeOut(JsonObject json, Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) {

		try {
			ClientDetailsEntity client = (ClientDetailsEntity)model.get("client");

			// use the parser to import the user claims into the object
			StringWriter writer = new StringWriter();
			gson.toJson(json, writer);

			response.setContentType("application/jwt");
			
			JWTClaimsSet claims = JWTClaimsSet.parse(writer.toString());

			claims.setAudience(Lists.newArrayList(client.getClientId()));

			claims.setIssuer(config.getIssuer());

			claims.setIssueTime(new Date());

			claims.setJWTID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it


			if (client.getIdTokenEncryptedResponseAlg() != null && !client.getIdTokenEncryptedResponseAlg().equals(Algorithm.NONE)
					&& client.getIdTokenEncryptedResponseEnc() != null && !client.getIdTokenEncryptedResponseEnc().equals(Algorithm.NONE)
					&& !Strings.isNullOrEmpty(client.getJwksUri())) {

				// encrypt it to the client's key

				JwtEncryptionAndDecryptionService encrypter = encrypters.getEncrypter(client.getJwksUri());

				if (encrypter != null) {

					EncryptedJWT encrypted = new EncryptedJWT(new JWEHeader(client.getIdTokenEncryptedResponseAlg(), client.getIdTokenEncryptedResponseEnc()), claims);

					encrypter.encryptJwt(encrypted);


					Writer out = response.getWriter();
					out.write(encrypted.serialize());

				} else {
					logger.error("Couldn't find encrypter for client: " + client.getClientId());
				}
			} else {

				JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm(); // default to the server's preference
				if (client.getUserInfoSignedResponseAlg() != null) {
					signingAlg = client.getUserInfoSignedResponseAlg(); // override with the client's preference if available
				}

				SignedJWT signed = new SignedJWT(new JWSHeader(signingAlg), claims);

				if (signingAlg.equals(JWSAlgorithm.HS256)
						|| signingAlg.equals(JWSAlgorithm.HS384)
						|| signingAlg.equals(JWSAlgorithm.HS512)) {

					// sign it with the client's secret
					JwtSigningAndValidationService signer = symmetricCacheService.getSymmetricValidtor(client);
					signer.signJwt(signed);

				} else {
					// sign it with the server's key
					jwtService.signJwt(signed);
				}

				Writer out = response.getWriter();
				out.write(signed.serialize());
			}
		} catch (IOException e) {
			logger.error("IO Exception in UserInfoJwtView", e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
