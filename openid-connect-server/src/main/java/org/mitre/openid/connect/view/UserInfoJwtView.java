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
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
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
	
	@Override
	protected void writeOut(JsonObject json, Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) {

		try {
			ClientDetailsEntity client = (ClientDetailsEntity)model.get("client");
			
			// use the parser to import the user claims into the object
			StringWriter writer = new StringWriter();
			gson.toJson(json, writer);
			
			JWTClaimsSet claims = JWTClaimsSet.parse(writer.toString());
					
			claims.setAudience(Lists.newArrayList(client.getClientId()));
	
			claims.setIssuer(config.getIssuer());
	
			claims.setIssueTime(new Date());
	
			claims.setJWTID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it
			
			JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();
			if (client.getUserInfoSignedResponseAlg() != null) {
				signingAlg = client.getUserInfoSignedResponseAlg();
			}
			
			SignedJWT signed = new SignedJWT(new JWSHeader(signingAlg), claims);
	
			jwtService.signJwt(signed);
		
			Writer out = response.getWriter();
			out.write(signed.serialize());
		} catch (IOException e) {
			logger.error("IO Exception in UserInfoJwtView", e);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
