/**
 * 
 */
package org.mitre.oauth2.introspectingfilter;

/**
 * @author jricher
 *
 */
public interface IntrospectionUrlProvider {

	/**
	 * Get the introspection URL based on the access token.
	 * @param accessToken
	 * @return
	 */
	public String getIntrospectionUrl(String accessToken);
	
}
