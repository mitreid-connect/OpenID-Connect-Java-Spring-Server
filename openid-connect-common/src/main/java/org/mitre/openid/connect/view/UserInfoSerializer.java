package org.mitre.openid.connect.view;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.ScopeClaimTranslationService;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UserInfoSerializer {

	private static ScopeClaimTranslationService translator = new ScopeClaimTranslationService();
	
	/**
	 * Build a JSON response according to the request object received.
	 * 
	 * Claims requested in requestObj.userinfo.claims are added to any
	 * claims corresponding to requested scopes, if any.
	 * 
	 * @param ui the UserInfo to filter
	 * @param scope the allowed scopes to filter by
	 * @param authorizedClaims the claims authorized by the client or user
	 * @param requestedClaims the claims requested in the RequestObject
	 * @return the filtered JsonObject result
	 */
	public static JsonObject toJsonFromRequestObj(UserInfo ui, Set<String> scope, JsonObject authorizedClaims, JsonObject requestedClaims) {

		// get the base object
		JsonObject obj = ui.toJson();
		
		Set<String> allowedByScope = translator.getClaimsForScopeSet(scope);
		Set<String> authorizedByClaims = new HashSet<String>();
		Set<String> requestedByClaims = new HashSet<String>();
		
		if (authorizedClaims != null) {
			JsonObject userinfoAuthorized = authorizedClaims.getAsJsonObject().get("userinfo").getAsJsonObject();
			for (Entry<String, JsonElement> entry : userinfoAuthorized.getAsJsonObject().entrySet()) {
				authorizedByClaims.add(entry.getKey());
			}
		}
		if (requestedClaims != null) {
			JsonObject userinfoRequested = requestedClaims.getAsJsonObject().get("userinfo").getAsJsonObject();
			for (Entry<String, JsonElement> entry : userinfoRequested.getAsJsonObject().entrySet()) {
				requestedByClaims.add(entry.getKey());
			}
		}
		
		// Filter claims by performing a manual intersection of claims that are allowed by the given scope, requested, and authorized.
		// We cannot use Sets.intersection() or similar because Entry<> objects will evaluate to being unequal if their values are
		// different, whereas we are only interested in matching the Entry<>'s key values.
		JsonObject result = new JsonObject();		
		for (Entry<String, JsonElement> entry : obj.entrySet()) {
			
			if (allowedByScope.contains(entry.getKey())
					|| authorizedByClaims.contains(entry.getKey())) {
				// it's allowed either by scope or by the authorized claims (either way is fine with us)
				
				if (requestedByClaims.isEmpty() || requestedByClaims.contains(entry.getKey())) {
					// the requested claims are empty (so we allow all), or they're not empty and this claim was specifically asked for
					result.add(entry.getKey(), entry.getValue());
				} // otherwise there were specific claims requested and this wasn't one of them
			}
		}
		
		return result;
	}
}
