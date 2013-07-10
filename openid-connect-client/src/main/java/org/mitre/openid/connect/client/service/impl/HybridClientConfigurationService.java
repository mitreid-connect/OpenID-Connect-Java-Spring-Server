/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.Map;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * Houses both a static client configuration and a dynamic client configuration
 * service in one object. Checks the static service first, then falls through to
 * the dynamic service.
 * 
 * Provides configuration passthrough for the template and the static
 * client map.
 * 
 * @author jricher
 * 
 */
public class HybridClientConfigurationService implements ClientConfigurationService {

	private StaticClientConfigurationService staticClientService = new StaticClientConfigurationService();
	
	private DynamicRegistrationClientConfigurationService dynamicClientService = new DynamicRegistrationClientConfigurationService();
	
	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.client.service.ClientConfigurationService#getClientConfiguration(org.mitre.openid.connect.config.ServerConfiguration)
	 */
	@Override
	public RegisteredClient getClientConfiguration(ServerConfiguration issuer) {
		
		RegisteredClient client = staticClientService.getClientConfiguration(issuer);
		if (client != null) {
			return client;
		} else {
			return dynamicClientService.getClientConfiguration(issuer);
		}
		
	}

	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.StaticClientConfigurationService#getClients()
	 */
    public Map<String, RegisteredClient> getClients() {
	    return staticClientService.getClients();
    }

	/**
	 * @param clients
	 * @see org.mitre.openid.connect.client.service.impl.StaticClientConfigurationService#setClients(java.util.Map)
	 */
    public void setClients(Map<String, RegisteredClient> clients) {
	    staticClientService.setClients(clients);
    }

	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#getTemplate()
	 */
    public RegisteredClient getTemplate() {
	    return dynamicClientService.getTemplate();
    }

	/**
	 * @param template
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#setTemplate(org.mitre.oauth2.model.RegisteredClient)
	 */
    public void setTemplate(RegisteredClient template) {
	    dynamicClientService.setTemplate(template);
    }

}
