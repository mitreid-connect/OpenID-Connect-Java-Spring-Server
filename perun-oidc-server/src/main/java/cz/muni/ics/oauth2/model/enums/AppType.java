package cz.muni.ics.oauth2.model.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppType {
    WEB("web"), NATIVE("native");

    private final String value;

    // map to aid reverse lookup
    private static final Map<String, AppType> lookup = new HashMap<>();
    static {
        for (AppType a : AppType.values()) {
            lookup.put(a.getValue(), a);
        }
    }

    public static AppType getByValue(String value) {
        return lookup.get(value);
    }

}
