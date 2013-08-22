/**
 * 
 */
package org.mitre.openid.connect.client.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * 
 * This service provides any extra options that need to be passed to the authentication request. 
 * These options may depend on the server configuration, client configuration, or HTTP request.
 * 
 * @author jricher
 *
 */
public interface AuthRequestOptionsService {

	public Map<String, String> getOptions(ServerConfiguration server, RegisteredClient client, HttpServletRequest request);
	
}
