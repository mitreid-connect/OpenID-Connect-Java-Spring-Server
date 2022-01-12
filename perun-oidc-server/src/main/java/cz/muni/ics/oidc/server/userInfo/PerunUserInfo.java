package cz.muni.ics.oidc.server.userInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cz.muni.ics.openid.connect.model.DefaultUserInfo;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements UserInfo by inheriting from DefaultUserInfo and adding more claims.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Getter
@Setter
@Slf4j
public class PerunUserInfo extends DefaultUserInfo {

	private final Map<String, JsonNode> customClaims = new LinkedHashMap<>();

	private long updatedAt;

	private JsonObject renderedObject;

	@Override
	public JsonObject toJson() {
		//TODO: include updatedAd in the object
		if (renderedObject == null) {
			//delegate standard claims to DefaultUserInfo
			renderedObject = super.toJson();
			//add custom claims
			for (Map.Entry<String, JsonNode> entry : customClaims.entrySet()) {
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				if (value == null || value.isNull()) {
					renderedObject.addProperty(key, (String) null);
					log.debug("adding null claim {}=null", key);
				} else if (value.isTextual() || value.isBoolean()) {
					renderedObject.addProperty(key, value.asText());
					log.debug("adding string claim {}={}", key, value.asText());
				} else if (value.isNumber()) {
					renderedObject.addProperty(key, value.asLong());
					log.debug("adding long claim {}={}", key, value.asText());
				} else if (value.isContainerNode()) {
					try {
						//convert from Jackson to GSon
						String rawJson = new ObjectMapper().writeValueAsString(value);
						renderedObject.add(key, new JsonParser().parse(rawJson));
						log.debug("adding JSON claim {}={}", key, rawJson);
					} catch (JsonProcessingException | JsonSyntaxException e) {
						log.error("cannot convert Jackson/Gson value " + value, e);
					}
				} else {
					log.warn("claim {} is of unknown type {}, skipping", key, value.getNodeType().toString());
				}
			}
		} else {
			log.debug("already rendered to JSON");
		}
		return renderedObject;
	}

}
