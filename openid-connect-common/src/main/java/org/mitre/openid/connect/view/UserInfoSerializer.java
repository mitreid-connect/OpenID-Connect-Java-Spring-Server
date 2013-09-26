package org.mitre.openid.connect.view;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.ScopeClaimTranslationService;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UserInfoSerializer {

	private static ScopeClaimTranslationService translator = new ScopeClaimTranslationService();
	
	/**
	 * Filter the UserInfo object by scope, using our ScopeClaimTranslationService to determine
	 * which claims are allowed for each given scope.
	 * 
	 * @param ui the UserInfo to filter
	 * @param scope the allowed scopes to filter by
	 * @return the filtered JsonObject result
	 */
	public static JsonObject filterByScope(UserInfo ui, Set<String> scope) {

		JsonObject uiJson = ui.toJson();
		List<String> filteredClaims = translator.getClaimsForScopeSet(scope);
		JsonObject result = new JsonObject();
		
		for (String claim : filteredClaims) {
			if (uiJson.has(claim)) {
				result.add(claim, uiJson.get(claim));
			}
		}
		
		return result;
	}
	
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

		// Only proceed if we have both requested claims and authorized claims list. Otherwise just return
		// the scope-filtered claim set.
		if (requestedClaims == null || authorizedClaims == null) {
			return filterByScope(ui, scope);
		}
		
		// get the base object
		JsonObject obj = ui.toJson();
		
		List<String> allowedByScope = translator.getClaimsForScopeSet(scope);
		JsonObject userinfoAuthorized = authorizedClaims.getAsJsonObject().get("userinfo").getAsJsonObject();
		JsonObject userinfoRequested = requestedClaims.getAsJsonObject().get("userinfo").getAsJsonObject();
		
		if (userinfoAuthorized == null || !userinfoAuthorized.isJsonObject()) {
			return obj;
		}

		// Filter claims by performing a manual intersection of claims that are allowed by the given scope, requested, and authorized.
		// We cannot use Sets.intersection() or similar because Entry<> objects will evaluate to being unequal if their values are
		// different, whereas we are only interested in matching the Entry<>'s key values.
		JsonObject result = new JsonObject();		
		for (Entry<String, JsonElement> entry : userinfoAuthorized.getAsJsonObject().entrySet()) {
			if (userinfoRequested.has(entry.getKey()) && allowedByScope.contains(entry.getKey())) {
				result.add(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
}
