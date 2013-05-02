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
public interface ClientConfigurationService {

	public ClientDetails getClientConfiguration(ServerConfiguration issuer);

}
