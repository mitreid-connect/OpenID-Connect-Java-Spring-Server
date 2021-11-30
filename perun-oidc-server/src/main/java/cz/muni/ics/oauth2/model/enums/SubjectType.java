package cz.muni.ics.oauth2.model.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubjectType {

    PAIRWISE("pairwise"), PUBLIC("public");

    private final String value;

    // map to aid reverse lookup
    private static final Map<String, SubjectType> lookup = new HashMap<>();
    static {
        for (SubjectType u : SubjectType.values()) {
            lookup.put(u.getValue(), u);
        }
    }

    public static SubjectType getByValue(String value) {
        return lookup.get(value);
    }
}
