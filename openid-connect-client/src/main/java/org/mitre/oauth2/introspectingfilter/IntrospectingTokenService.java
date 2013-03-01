package org.mitre.oauth2.introspectingfilter;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class IntrospectingTokenService implements ResourceServerTokenServices {

    
    private String clientId;
    private String clientSecret;
    private String introspectionUrl;
    
    // Inner class to store in the hash map
    private class TokenCacheObject { OAuth2AccessToken token; OAuth2Authentication auth;
        private TokenCacheObject(OAuth2AccessToken token, OAuth2Authentication auth) {
            this.token = token;
            this.auth = auth;
        }
    }
    private Map<String, TokenCacheObject> authCache = new HashMap<String, TokenCacheObject>();

    public String getIntrospectionUrl() {
        return introspectionUrl;
    }

    public void setIntrospectionUrl(String introspectionUrl) {
        this.introspectionUrl = introspectionUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    // Check if there is a token and authentication in the cache
    //   and check if it is not expired.
    private TokenCacheObject checkCache(String key) {
        if(authCache.containsKey(key)) {
            TokenCacheObject tco = authCache.get(key);
            if (tco.token.getExpiration().after(new Date())) {
                return tco;
            } else {
                // if the token is expired, don't keep things around.
                authCache.remove(key);
            }
        }
        return null;
    }
    
    private AuthorizationRequest createAuthRequest(final JsonObject token) {
        AuthorizationRequest authReq = new AuthorizationRequestImpl(token);
        return authReq;
    }
    
    // create a default authentication object with authority ROLE_API
    private Authentication createAuthentication(JsonObject token){
    	// TODO: make role/authority configurable somehow
        return new PreAuthenticatedAuthenticationToken(token.get("subject").getAsString(), null, AuthorityUtils.createAuthorityList("ROLE_API"));
    }
    
    private OAuth2AccessToken createAccessToken(final JsonObject token, final String tokenString){
        OAuth2AccessToken accessToken = new OAuth2AccessTokenImpl(token, tokenString);               
        return accessToken;
    }
    
    // Validate a token string against the introspection endpoint, 
    //   then parse it and store it in the local cache. Return true on 
    //   sucess, false otherwise.
    private boolean parseToken(String accessToken) {
        String validatedToken = null;
        // Use the SpringFramework RestTemplate to send the request to the endpoint
        
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("token",accessToken);
        form.add("client_id", this.clientId);
        form.add("client_secret", this.clientSecret);
        
        try {
            validatedToken = restTemplate.postForObject(introspectionUrl, form, String.class);
        } catch (RestClientException rce) {
            // TODO: LOG THIS!?
            LoggerFactory.getLogger(IntrospectingTokenService.class).error("validateToken", rce);
        }
        if (validatedToken != null) {
            // parse the json
            JsonElement jsonRoot = new JsonParser().parse(validatedToken);
            if (!jsonRoot.isJsonObject()) {
                return false; // didn't get a proper JSON object
            }
            
            JsonObject tokenResponse = jsonRoot.getAsJsonObject();
            
            if (tokenResponse.get("error") != null) {
                // report an error?
                return false;
            }
            
            if (!tokenResponse.get("valid").getAsBoolean()){
                // non-valid token
                return false;
            }
            // create an OAuth2Authentication
            OAuth2Authentication auth = new OAuth2Authentication(createAuthRequest(tokenResponse), createAuthentication(tokenResponse));
            // create an OAuth2AccessToken
            OAuth2AccessToken token = createAccessToken(tokenResponse, accessToken);
            
            if (token.getExpiration().after(new Date())){
            // Store them in the cache
                authCache.put(accessToken, new TokenCacheObject(token,auth));
       
                return true;
            }
        }
        
        // If we never put a token and an authentication in the cache... 
        return false;
    }
    
    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException {
        // First check if the in memory cache has an Authentication object, and that it is still valid
        // If Valid, return it
        TokenCacheObject cacheAuth = checkCache(accessToken);
        if (cacheAuth != null) {
            return cacheAuth.auth;
        } else {
            if (parseToken(accessToken)) {
                cacheAuth = authCache.get(accessToken);
                if (cacheAuth != null && (cacheAuth.token.getExpiration().after(new Date()))) {
                    return cacheAuth.auth;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        // First check if the in memory cache has a Token object, and that it is still valid
        // If Valid, return it
        TokenCacheObject cacheAuth = checkCache(accessToken);
        if (cacheAuth != null) {
            return cacheAuth.token;
        } else {
            if (parseToken(accessToken)) {
                cacheAuth = authCache.get(accessToken);
                if (cacheAuth != null && (cacheAuth.token.getExpiration().after(new Date()))) {
                    return cacheAuth.token;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }        
    }

}
