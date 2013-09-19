package org.mitre.openid.connect.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.Set;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.ScopeClaimTranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UserInfoSerializer {

	private static Logger logger = LoggerFactory.getLogger(UserInfoSerializer.class);
	
	private ScopeClaimTranslationService translator = new ScopeClaimTranslationService();
	
	/**
	 * Build a JSON response according to the request object received.
	 * 
	 * Claims requested in requestObj.userinfo.claims are added to any
	 * claims corresponding to requested scopes, if any.
	 * 
	 * @param ui
	 * @param scope
	 * @param requestObj
	 * @param claimsRequest the claims request parameter object.
	 * @return
	 */
	public static JsonObject toJsonFromRequestObj(UserInfo ui, Set<String> scope, JsonObject requestObj, JsonObject claimsRequest) {

		JsonObject obj = toJson(ui, scope);

		//Process list of requested claims out of the request object
		JsonElement claims = requestObj.get("claims");
		if (claims == null || !claims.isJsonObject()) {
			return obj;
		}

		JsonElement userinfo = claims.getAsJsonObject().get("userinfo");
		if (userinfo == null || !userinfo.isJsonObject()) {
			return obj;
		}

		// Filter claims from the request object with the claims from the claims request parameter, if it exists
		
		// Doing the set intersection manually because the claim entries may be referring to
		// the same claim but have different 'individual claim values', causing the Entry<> to be unequal, 
		// which doesn't allow the use of the more compact Sets.intersection() type method.
		Set<Entry<String, JsonElement>> requestClaimsSet = Sets.newHashSet();
		if (claimsRequest != null) {
			
			for (Entry<String, JsonElement> entry : userinfo.getAsJsonObject().entrySet()) {
				if (claimsRequest.has(entry.getKey())) {
					requestClaimsSet.add(entry);
				}
			}
			
		}
		
		//TODO: is there a way to use bean processors to do bean.getfield(name)?
		//Method reflection is OK, but need a service to translate scopes into claim names => field names
		
		// TODO: this method is likely to be fragile if the data model changes at all

		//For each claim found, add it if not already present
		for (Entry<String, JsonElement> i : requestClaimsSet) {
			String claimName = i.getKey();
			if (!obj.has(claimName)) {
				String value = "";

				//Process claim names to go from "claim_name" to "ClaimName"
				String camelClaimName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, claimName);
				//Now we have "getClaimName"
				String methodName = "get" + camelClaimName;
				Method getter = null;
				try {
					getter = ui.getClass().getMethod(methodName);
					value = (String) getter.invoke(ui);
					obj.addProperty(claimName, value);
				} catch (SecurityException e) {
					logger.error("SecurityException in UserInfoView.java: ", e);
				} catch (NoSuchMethodException e) {
					logger.error("NoSuchMethodException in UserInfoView.java: ", e);
				} catch (IllegalArgumentException e) {
					logger.error("IllegalArgumentException in UserInfoView.java: ", e);
				} catch (IllegalAccessException e) {
					logger.error("IllegalAccessException in UserInfoView.java: ", e);
				} catch (InvocationTargetException e) {
					logger.error("InvocationTargetException in UserInfoView.java: ", e);
				}
			}
		}
		return obj;
	}
	
	public static JsonObject toJson(UserInfo ui, Set<String> scope) {

		JsonObject obj = new JsonObject();
		
		//TODO: This is a hack: the UserInfoInterceptor should use a serializer from this class, but it doesn't
		//have access to a scope set. It wants to just serialize whatever fields are present?
		if (scope == null) {
			Set<String> allScopes = Sets.newHashSet("openid", "profile", "email", "phone", "address");
			scope = allScopes;
		}

		if (scope.contains("openid")) {
			obj.addProperty("sub", ui.getSub());
		}

		if (scope.contains("profile")) {
			obj.addProperty("name", ui.getName());
			obj.addProperty("preferred_username", ui.getPreferredUsername());
			obj.addProperty("given_name", ui.getGivenName());
			obj.addProperty("family_name", ui.getFamilyName());
			obj.addProperty("middle_name", ui.getMiddleName());
			obj.addProperty("nickname", ui.getNickname());
			obj.addProperty("profile", ui.getProfile());
			obj.addProperty("picture", ui.getPicture());
			obj.addProperty("website", ui.getWebsite());
			obj.addProperty("gender", ui.getGender());
			obj.addProperty("zone_info", ui.getZoneinfo());
			obj.addProperty("locale", ui.getLocale());
			obj.addProperty("updated_time", ui.getUpdatedTime());
			obj.addProperty("birthdate", ui.getBirthdate());
		}

		if (scope.contains("email")) {
			obj.addProperty("email", ui.getEmail());
			obj.addProperty("email_verified", ui.getEmailVerified());
		}

		if (scope.contains("phone")) {
			obj.addProperty("phone_number", ui.getPhoneNumber());
			obj.addProperty("phone_number_verified", ui.getPhoneNumberVerified());
		}

		if (scope.contains("address") && ui.getAddress() != null) {

			JsonObject addr = new JsonObject();
			addr.addProperty("formatted", ui.getAddress().getFormatted());
			addr.addProperty("street_address", ui.getAddress().getStreetAddress());
			addr.addProperty("locality", ui.getAddress().getLocality());
			addr.addProperty("region", ui.getAddress().getRegion());
			addr.addProperty("postal_code", ui.getAddress().getPostalCode());
			addr.addProperty("country", ui.getAddress().getCountry());

			obj.add("address", addr);
		}

		return obj;
	}
	
}
