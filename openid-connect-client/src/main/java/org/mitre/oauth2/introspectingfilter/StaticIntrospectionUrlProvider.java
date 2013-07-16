/**
 * 
 */
package org.mitre.oauth2.introspectingfilter;

/**
 * 
 * Always provides the (configured) IntrospectionURL regardless of token. Useful for talking to
 * a single, trusted authorization server.
 * 
 * @author jricher
 *
 */
public class StaticIntrospectionUrlProvider implements IntrospectionUrlProvider {

	private String introspectionUrl; 
	
	/**
	 * @return the introspectionUrl
	 */
	public String getIntrospectionUrl() {
		return introspectionUrl;
	}

	/**
	 * @param introspectionUrl the introspectionUrl to set
	 */
	public void setIntrospectionUrl(String introspectionUrl) {
		this.introspectionUrl = introspectionUrl;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionUrlProvider#getIntrospectionUrl(java.lang.String)
	 */
	@Override
	public String getIntrospectionUrl(String accessToken) {
		return getIntrospectionUrl();
	}

}
