/**
 * 
 */
package org.mitre.openid.connect.client.service;

import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * @author jricher
 *
 */
public interface ServerConfigurationService {

	public ServerConfiguration getServerConfiguration(String issuer);

}
