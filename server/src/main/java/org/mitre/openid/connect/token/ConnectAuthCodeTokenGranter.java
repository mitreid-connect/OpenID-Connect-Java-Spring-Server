/**
 * 
 */
package org.mitre.openid.connect.token;

import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

/**
 * @author AANGANES
 *
 */
public class ConnectAuthCodeTokenGranter extends AuthorizationCodeTokenGranter {

	/**
	 * @param tokenServices
	 * @param authorizationCodeServices
	 * @param clientDetailsService
	 */
	public ConnectAuthCodeTokenGranter(
			AuthorizationServerTokenServices tokenServices,
			AuthorizationCodeServices authorizationCodeServices,
			ClientDetailsService clientDetailsService) {
		super(tokenServices, authorizationCodeServices, clientDetailsService);
		// TODO Auto-generated constructor stub
	}

}
