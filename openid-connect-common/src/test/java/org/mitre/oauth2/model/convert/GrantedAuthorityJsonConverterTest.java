package org.mitre.oauth2.model.convert;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GrantedAuthorityJsonConverterTest {
    
    @Test
    public void parse() {
        
        final JsonParser parser = new JsonParser();
        final JsonObject jsonObject = parser.parse("{\"role\": \"ROLE_CLIENT\"}").getAsJsonObject();
        
        final GrantedAuthority authority = GrantedAuthorityJsonConverter.parse(jsonObject);
        
        assertEquals("ROLE_CLIENT", authority.getAuthority());
        
    }
    
}
