/**
 * 
 */
package org.mitre.openid.connect.client.service;

import org.mitre.oauth2.model.RegisteredClient;

/**
 * @author jricher
 *
 */
public interface RegisteredClientService {

	/**
	 * Get a remembered client (if one exists) to talk to the given issuer. This 
	 * client likely doesn't have its full configuration information but contains 
	 * the information needed to fetch it.
	 * @param issuer
	 * @return
	 */
    RegisteredClient getByIssuer(String issuer);

	/**
	 * Save this client's information for talking to the given issuer. This will
	 * save only enough information to fetch the client's full configuration from
	 * the server.
	 * @param client
	 */
    void save(String issuer, RegisteredClient client);

}
