package org.bbplus;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BBTrustedRegistrationValidator implements TrustedRegistrationValidator {

	@Autowired
	private TrustedRegistryService registries;
	
	@Override
	public boolean validate(String postBody, Authentication auth) {

		// BlueButton permits open registration, so no auth = A-OK.
		if (auth  == null || !auth.isAuthenticated())
			return false;
		
		PreregistrationToken preregToken = (PreregistrationToken) auth;
		String issuer = preregToken.getJwt().getPayload().toJSONObject().get("iss").toString();
		
		if (!registries.isTrusted(issuer)){
			throw new AuthenticationServiceException(
					"Registry " + issuer + " isn't trusted by this server. "+
					"The followin registries are trusted: " + registries.allTrustedRegistries());
		}
		
		JsonObject regRequest = (JsonObject) new JsonParser().parse(postBody);
		
		
		JsonObject bbClient = preregToken.getClientDefinitionFromTrustedRegistry();
		JsonObject fixedParams = (JsonObject) bbClient.get("fixed_registration_parameters");

		for (Map.Entry<String,JsonElement> entry  : fixedParams.entrySet()){
			String claimed = entry.getValue().toString();
			String requested = regRequest.get(entry.getKey()).toString();
			if (!(claimed.equals(requested)))
				throw new AuthenticationServiceException("App preregistered a claim for " +
						entry.getKey() +"="+claimed + 
						" but os asking for " +
						entry.getKey() +"="+requested);						
			}
		
		return true;
	}

}
