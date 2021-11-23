package cz.muni.ics.oauth2.model.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthMethod {
    SECRET_POST("client_secret_post"),
    SECRET_BASIC("client_secret_basic"),
    SECRET_JWT("client_secret_jwt"),
    PRIVATE_KEY("private_key_jwt"),
    NONE("none");

    private final String value;

    // map to aid reverse lookup
    private static final Map<String, AuthMethod> lookup = new HashMap<>();
    static {
        for (AuthMethod a : AuthMethod.values()) {
            lookup.put(a.getValue(), a);
        }
    }

    public static AuthMethod getByValue(String value) {
        return lookup.get(value);
    }

}
