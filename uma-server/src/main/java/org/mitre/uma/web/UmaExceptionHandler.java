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

import java.util.HashMap;
import java.util.Map;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.uma.exception.InvalidTicketException;
import org.mitre.uma.exception.NeedInfoException;
import org.mitre.uma.exception.NotAuthorizedException;
import org.mitre.uma.model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author jricher
 *
 */
@ControllerAdvice
public class UmaExceptionHandler {
	
	@Autowired
	private ConfigurationPropertiesBean config;
	
	@ExceptionHandler(NeedInfoException.class)
	public ModelAndView handleUmaException(Exception e) {
		// if we got here, the claim didn't match, forward the user to the claim gathering endpoint
		
		NeedInfoException nie = (NeedInfoException)e;
		
		JsonObject entity = new JsonObject();

		entity.addProperty(JsonErrorView.ERROR, "need_info");
		JsonObject details = new JsonObject();
		details.addProperty("redirect_user", true);
		details.addProperty("ticket", nie.getTicketValue());
		details.addProperty("claims_endpoint", config.getIssuer() + ClaimsCollectionEndpoint.URL);

		JsonObject rpClaims = new JsonObject();
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
