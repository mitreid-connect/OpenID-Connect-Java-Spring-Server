package cz.muni.ics.oidc.models.enums;

public enum PerunAttrValueType {

	STRING,
	INTEGER,
	BOOLEAN,
	ARRAY,
	MAP_JSON,
	MAP_KEY_VALUE;

	public static PerunAttrValueType parse(String str){
		if (str == null) {
			return STRING;
		}

		switch (str.toLowerCase()) {
			case "integer": return INTEGER;
			case "boolean": return BOOLEAN;
			case "array":
			case "list": return ARRAY;
			case "map_json": return MAP_JSON;
			case "map_key_value": return MAP_KEY_VALUE;
			default: return STRING;
		}
	}

}
