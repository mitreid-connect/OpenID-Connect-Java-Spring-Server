/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.concurrent.ExecutionException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Dynamically fetches OpenID Connect server configurations based on the issuer. Caches the server configurations.
 * 
 * @author jricher
 *
 */
public class DynamicServerConfigurationService implements ServerConfigurationService {

	private static Logger logger = LoggerFactory.getLogger(DynamicServerConfigurationService.class);
	
	// map of issuer -> server configuration, loaded dynamically from service discovery
	private LoadingCache<String, ServerConfiguration> servers;
	
	public DynamicServerConfigurationService() {
		// initialize the cache
		servers = CacheBuilder.newBuilder().build(new OpenIDConnectServiceConfigurationFetcher());
	}
	
	@Override
	public ServerConfiguration getServerConfiguration(String issuer) {
		try {
	        return servers.get(issuer);
        } catch (ExecutionException e) {
        	logger.warn("Couldn't load configuration for " + issuer, e);
	        return null; 
        }
		
	}

	/**
     * @author jricher
     *
     */
    private class OpenIDConnectServiceConfigurationFetcher extends CacheLoader<String, ServerConfiguration> {
    	private HttpClient httpClient = new DefaultHttpClient();
    	private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		private JsonParser parser = new JsonParser();

    	@Override
        public ServerConfiguration load(String issuer) throws Exception {
    		RestTemplate restTemplate = new RestTemplate(httpFactory);

    		// data holder
    		ServerConfiguration conf = new ServerConfiguration();
    		
    		// construct the well-known URI
    		String url = issuer + "/.well-known/openid-configuration";
        	
    		// fetch the value
        	String jsonString = restTemplate.getForObject(url, String.class);

        	JsonElement parsed = parser.parse(jsonString);
        	if (parsed.isJsonObject()) {
        		
        		JsonObject o = parsed.getAsJsonObject();
        		
        		// sanity checks
        		if (!issuer.equals(o.get("issuer").getAsString())) {
        			throw new IllegalStateException("Discovered issuers didn't match, expected " + issuer + " got " + o.get("issuer").getAsString());
        		}
        		
        		conf.setIssuer(o.get("issuer").getAsString());
        		conf.setAuthorizationEndpointUri(o.get("authorization_endpoint").getAsString());
        		conf.setTokenEndpointUri(o.get("token_endpoint").getAsString());
        		conf.setJwksUri(o.get("jwks_uri").getAsString());
        		conf.setUserInfoUri(o.get("userinfo_endpoint").getAsString());

        		return conf;
        	} else {
        		throw new IllegalStateException("Couldn't parse server discovery results for " + url); 
        	}
        	
        }

    }

}
