/**
 * 
 */
package org.mitre.openid.connect.client.service.impl;

import java.util.Map;
import java.util.Set;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.RegisteredClientService;
import org.mitre.openid.connect.config.ServerConfiguration;

/**
 * Houses both a static client configuration and a dynamic client configuration
 * service in one object. Checks the static service first, then falls through to
 * the dynamic service.
 * 
 * Provides configuration passthrough for the template, registered client service, whitelist,
 * and blacklist for the dynamic service, and to the static service's client map.
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

	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#getRegisteredClientService()
	 */
    public RegisteredClientService getRegisteredClientService() {
	    return dynamicClientService.getRegisteredClientService();
    }

	/**
	 * @param registeredClientService
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#setRegisteredClientService(org.mitre.openid.connect.client.service.RegisteredClientService)
	 */
    public void setRegisteredClientService(RegisteredClientService registeredClientService) {
	    dynamicClientService.setRegisteredClientService(registeredClientService);
    }

	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#getWhitelist()
	 */
    public Set<String> getWhitelist() {
	    return dynamicClientService.getWhitelist();
    }

	/**
	 * @param whitelist
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#setWhitelist(java.util.Set)
	 */
    public void setWhitelist(Set<String> whitelist) {
	    dynamicClientService.setWhitelist(whitelist);
    }

	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#getBlacklist()
	 */
    public Set<String> getBlacklist() {
	    return dynamicClientService.getBlacklist();
    }

	/**
	 * @param blacklist
	 * @see org.mitre.openid.connect.client.service.impl.DynamicRegistrationClientConfigurationService#setBlacklist(java.util.Set)
	 */
    public void setBlacklist(Set<String> blacklist) {
	    dynamicClientService.setBlacklist(blacklist);
    }

}
