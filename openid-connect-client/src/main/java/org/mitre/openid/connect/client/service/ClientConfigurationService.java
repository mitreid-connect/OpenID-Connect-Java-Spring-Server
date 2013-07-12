/**
 * 
 */
package org.mitre.openid.connect.client.service;

import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * @author jricher
 *
 */
public interface ClientConfigurationService {

	public ClientDetails getClientConfiguration(String issuer);

}
