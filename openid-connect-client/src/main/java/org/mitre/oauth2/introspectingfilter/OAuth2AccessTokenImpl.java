package org.mitre.oauth2.introspectingfilter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;


public class OAuth2AccessTokenImpl implements OAuth2AccessToken {

    private JsonObject token;
    private String tokenString;
    private Set<String> scopes = null;
    private Date expireDate;
    
    
    public OAuth2AccessTokenImpl(JsonObject token, String tokenString) {
        this.token = token;
        this.tokenString = tokenString;
        scopes = new HashSet<String>();
        for (JsonElement e : token.get("scope").getAsJsonArray()) {
            scopes.add(e.getAsString());
        }
        
        DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            expireDate = dateFormater.parse(token.get("expires_at").getAsString());
        } catch (ParseException ex) {
            Logger.getLogger(IntrospectingTokenService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public Map<String, Object> getAdditionalInformation() {
        return null;
    }

    @Override
    public Set<String> getScope() {
        return scopes;
    }

    @Override
    public OAuth2RefreshToken getRefreshToken() {
        return null;
    }

    @Override
    public String getTokenType() {
        return BEARER_TYPE;
    }

    @Override
    public boolean isExpired() {
        if (expireDate != null && expireDate.before(new Date())) {
            return true;
        }
        return false;
    }

    @Override
    public Date getExpiration() {
        return expireDate;
    }

    @Override
    public int getExpiresIn() {
        if (expireDate != null) {
            return (int)TimeUnit.MILLISECONDS.toSeconds(expireDate.getTime() - (new Date()).getTime());
        }
        return 0;
    }

    @Override
    public String getValue() {
        return tokenString;
    }

}
