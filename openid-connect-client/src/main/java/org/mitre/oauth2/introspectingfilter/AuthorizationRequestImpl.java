package org.mitre.oauth2.introspectingfilter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizationRequest;

public class AuthorizationRequestImpl implements AuthorizationRequest {

    private JsonObject token;
    private String clientId;
    private Set<String> scopes = null;
    
    public AuthorizationRequestImpl(JsonObject token) {
        this.token = token;
        clientId = token.get("client_id").getAsString();
        scopes = new HashSet<String>();
        for (JsonElement e : token.get("scope").getAsJsonArray()) {
            scopes.add(e.getAsString());
        }        
    }

    @Override
    public Map<String, String> getAuthorizationParameters() {
        return null;
    }

    @Override
    public Map<String, String> getApprovalParameters() {
        return null;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public Set<String> getScope() {

        return scopes;
    }

    @Override
    public Set<String> getResourceIds() {
        return null;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isApproved() {
        return true;
    }

    @Override
    public boolean isDenied() {
        return false;
    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public String getRedirectUri() {
        return null;
    }

    @Override
    public Set<String> getResponseTypes() {
        return null;
    }

}
