/**
 * 
 */
package org.mitre.discovery.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

/**
 * A collection of null-safe converters from common classes and JSON elements, using GSON.
 * 
 * @author jricher
 *
 */
public class JsonUtils {

	private static Gson gson = new Gson();
	
	/**
	 * Translate a set of strings to a JSON array
	 * @param value
	 * @return
	 */
	public static JsonElement getAsArray(Set<String> value) {
		return gson.toJsonTree(value, new TypeToken<Set<String>>(){}.getType());
	}

	/**
	 * Gets the value of the given member (expressed as integer seconds since epoch) as a Date
	 */
	public static Date getAsDate(JsonObject o, String member) {
		if (o.has(member)) {
			JsonElement e = o.get(member);
			if (e != null && e.isJsonPrimitive()) {
				return new Date(e.getAsInt() * 1000L);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Gets the value of the given member as a JWE Algorithm, null if it doesn't exist
	 */
	public static JWEAlgorithm getAsJweAlgorithm(JsonObject o, String member) {
		String s = getAsString(o, member);
		if (s != null) {
			return JWEAlgorithm.parse(s);
		} else {
			return null;
		}
	}

	/**
	 * Gets the value of the given member as a JWE Encryption Method, null if it doesn't exist
	 */
	public static EncryptionMethod getAsJweEncryptionMethod(JsonObject o, String member) {
		String s = getAsString(o, member);
		if (s != null) {
			return EncryptionMethod.parse(s);
		} else {
			return null;
		}
	}

	/**
	 * Gets the value of the given member as a JWS Algorithm, null if it doesn't exist
	 */
	public static JWSAlgorithm getAsJwsAlgorithm(JsonObject o, String member) {
		String s = getAsString(o, member);
		if (s != null) {
			return JWSAlgorithm.parse(s);
		} else {
			return null;
		}
	}

	/**
	 * Gets the value of the given member as a string, null if it doesn't exist
	 */
	public static String getAsString(JsonObject o, String member) {
		if (o.has(member)) {
			JsonElement e = o.get(member);
			if (e != null && e.isJsonPrimitive()) {
				return e.getAsString();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Gets the value of the given member as a boolean, null if it doesn't exist
	 */
	public static Boolean getAsBoolean(JsonObject o, String member) {
		if (o.has(member)) {
			JsonElement e = o.get(member);
			if (e != null && e.isJsonPrimitive()) {
				return e.getAsBoolean();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the value of the given given member as a set of strings, null if it doesn't exist
	 */
	public static Set<String> getAsStringSet(JsonObject o, String member) throws JsonSyntaxException {
		if (o.has(member)) {
			return gson.fromJson(o.get(member), new TypeToken<Set<String>>(){}.getType());
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the value of the given given member as a set of strings, null if it doesn't exist
	 */
	public static List<String> getAsStringList(JsonObject o, String member) throws JsonSyntaxException {
		if (o.has(member)) {
			return gson.fromJson(o.get(member), new TypeToken<List<String>>(){}.getType());
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the value of the given member as a list of JWS Algorithms, null if it doesn't exist
	 */
	public static List<JWSAlgorithm> getAsJwsAlgorithmList(JsonObject o, String member) {
		List<String> strings = getAsStringList(o, member);
		if (strings != null) {
			List<JWSAlgorithm> algs = new ArrayList<JWSAlgorithm>();
			for (String alg : strings) {
	            algs.add(JWSAlgorithm.parse(alg));
            }
			return algs;
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the value of the given member as a list of JWS Algorithms, null if it doesn't exist
	 */
	public static List<JWEAlgorithm> getAsJweAlgorithmList(JsonObject o, String member) {
		List<String> strings = getAsStringList(o, member);
		if (strings != null) {
			List<JWEAlgorithm> algs = new ArrayList<JWEAlgorithm>();
			for (String alg : strings) {
	            algs.add(JWEAlgorithm.parse(alg));
            }
			return algs;
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the value of the given member as a list of JWS Algorithms, null if it doesn't exist
	 */
	public static List<EncryptionMethod> getAsEncryptionMethodList(JsonObject o, String member) {
		List<String> strings = getAsStringList(o, member);
		if (strings != null) {
			List<EncryptionMethod> algs = new ArrayList<EncryptionMethod>();
			for (String alg : strings) {
	            algs.add(EncryptionMethod.parse(alg));
            }
			return algs;
		} else {
			return null;
		}
	}
	
}
