/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.view;

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
import cz.muni.ics.jwt.encryption.service.JWTEncryptionAndDecryptionService;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.jwt.signer.service.impl.ClientKeyCacheService;
import cz.muni.ics.jwt.signer.service.impl.SymmetricKeyJWTValidatorCacheService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * @author jricher
 *
 */
@Component(UserInfoJWTView.VIEWNAME)
@Slf4j
public class UserInfoJWTView extends UserInfoView {

	public static final String CLIENT = "client";

	public static final String VIEWNAME = "userInfoJwtView";

	public static final String JOSE_MEDIA_TYPE_VALUE = "application/jwt";
	public static final MediaType JOSE_MEDIA_TYPE = new MediaType("application", "jwt");


	@Autowired
	private JWTSigningAndValidationService jwtService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@Autowired
	private ClientKeyCacheService encrypters;

	@Autowired
	private SymmetricKeyJWTValidatorCacheService symmetricCacheService;

	@Override
	protected void writeOut(JsonObject json, Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) {

		try {
			ClientDetailsEntity client = (ClientDetailsEntity)model.get(CLIENT);

			// use the parser to import the user claims into the object
			StringWriter writer = new StringWriter();
			gson.toJson(json, writer);

			response.setContentType(JOSE_MEDIA_TYPE_VALUE);

			JWTClaimsSet claims = new JWTClaimsSet.Builder(JWTClaimsSet.parse(writer.toString()))
					.audience(Lists.newArrayList(client.getClientId()))
					.issuer(config.getIssuer())
					.issueTime(new Date())
					.jwtID(UUID.randomUUID().toString()) // set a random NONCE in the middle of it
					.build();


			if (client.getUserInfoEncryptedResponseAlg() != null && !client.getUserInfoEncryptedResponseAlg().equals(Algorithm.NONE)
					&& client.getUserInfoEncryptedResponseEnc() != null && !client.getUserInfoEncryptedResponseEnc().equals(Algorithm.NONE)
					&& (!Strings.isNullOrEmpty(client.getJwksUri()) || client.getJwks() != null)) {

				// encrypt it to the client's key

				JWTEncryptionAndDecryptionService encrypter = encrypters.getEncrypter(client);

				if (encrypter != null) {

					EncryptedJWT encrypted = new EncryptedJWT(new JWEHeader(client.getUserInfoEncryptedResponseAlg(), client.getUserInfoEncryptedResponseEnc()), claims);

					encrypter.encryptJwt(encrypted);


					Writer out = response.getWriter();
					out.write(encrypted.serialize());

				} else {
					log.error("Couldn't find encrypter for client: " + client.getClientId());
				}
			} else {

				JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm(); // default to the server's preference
				if (client.getUserInfoSignedResponseAlg() != null) {
					signingAlg = client.getUserInfoSignedResponseAlg(); // override with the client's preference if available
				}
				JWSHeader header = new JWSHeader(signingAlg, null, null, null, null, null, null, null, null, null,
						jwtService.getDefaultSignerKeyId(),
						null, null);
				SignedJWT signed = new SignedJWT(header, claims);

				if (signingAlg.equals(JWSAlgorithm.HS256)
						|| signingAlg.equals(JWSAlgorithm.HS384)
						|| signingAlg.equals(JWSAlgorithm.HS512)) {

					// sign it with the client's secret
					JWTSigningAndValidationService signer = symmetricCacheService.getSymmetricValidator(client);
					signer.signJwt(signed);

				} else {
					// sign it with the server's key
					jwtService.signJwt(signed);
				}

				Writer out = response.getWriter();
				out.write(signed.serialize());
			}
		} catch (IOException e) {
			log.error("IO Exception in UserInfoJwtView", e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
