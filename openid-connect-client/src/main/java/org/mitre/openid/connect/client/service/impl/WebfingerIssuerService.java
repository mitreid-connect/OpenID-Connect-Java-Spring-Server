/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.IssuerService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Use Webfinger to discover the appropriate issuer for a user-given input string.
 * @author jricher
 *
 */
public class WebfingerIssuerService implements IssuerService {

	private static Logger logger = LoggerFactory.getLogger(WebfingerIssuerService.class);
	
	// map of user input -> issuer, loaded dynamically from webfinger discover
	private LoadingCache<String, String> issuers;

	private String parameterName = "login";
	
	public WebfingerIssuerService() {
		issuers = CacheBuilder.newBuilder().build(new WebfingerIssuerFetcher());
	}
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.IssuerService#getIssuer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public IssuerServiceResponse getIssuer(HttpServletRequest request) {
		
		String login = request.getParameter(parameterName);
		if (!Strings.isNullOrEmpty(login)) {
            try {
            	String issuer = issuers.get(normalizeResource(login));
            	return new IssuerServiceResponse(issuer, null, null);
            } catch (ExecutionException e) {
            	logger.warn("Issue fetching issuer for user input: " + login, e);
	            return null;
            }
            
		} else {
			logger.warn("No user input given.");
			return null;
		}
	}
	
	/**
	 * Normalize the resource string as per OIDC Discovery.
	 * @param resource
	 * @return
	 */
	private String normalizeResource(String resource) {
		// TODO
		return null;
	}

	
	/**
	 * @return the parameterName
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * @param parameterName the parameterName to set
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}


	/**
     * @author jricher
     *
     */
    private class WebfingerIssuerFetcher extends CacheLoader<String, String> {
    	private HttpClient httpClient = new DefaultHttpClient();
    	private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    	private RestTemplate restTemplate = new RestTemplate(httpFactory);
		/* (non-Javadoc)
		 * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
		 */
        @Override
        public String load(String key) throws Exception {
	        // TODO Auto-generated method stub
	        return null;
        }

    }

	
	
}
