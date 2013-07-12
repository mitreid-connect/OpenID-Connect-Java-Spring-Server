/**
 * 
 */
package org.mitre.openid.connect.client.service;

import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * @author jricher
 *
 */
public interface AuthRequestUrlBuilder {

	/**
	 * @param serverConfig
	 * @param clientConfig
	 * @param redirectUri
	 * @param nonce
	 * @param state
	 * @return
	 */
	public String buildAuthRequestUrl(ServerConfiguration serverConfig, ClientDetails clientConfig, String redirectUri, String nonce, String state);

}
