package org.mitre.oauth2.model.convert;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.gson.JsonElement;

public class GrantedAuthorityJsonConverter {

    private static final String ROLE_MEMBER_NAME = "role";

    private GrantedAuthorityJsonConverter() { }

    public static GrantedAuthority parse(JsonElement jsonElement) {
        return new SimpleGrantedAuthority(jsonElement.getAsJsonObject().get(ROLE_MEMBER_NAME).getAsString());
    }
}
