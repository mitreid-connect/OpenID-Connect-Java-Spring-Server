/**
 * 
 */
package org.mitre.openid.connect.client.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * Gets an issuer for the given request. Might do dynamic discovery, or might be statically configured.
 * 
 * @author jricher
 *
 */
public interface IssuerService {

	public String getIssuer(HttpServletRequest request);
	
}
