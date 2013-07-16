/**
 * 
 */
package org.mitre.oauth2.introspectingfilter;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * 
 * Parses the incoming accesstoken as a JWT and determines the issuer based on
 * the "iss" field inside the JWT. Uses the ServerConfigurationService to determine
 * the introspection URL for that issuer.
 * 
 * @author jricher
 *
 */
public class JWTParsingIntrospectionUrlProvider implements IntrospectionUrlProvider {

	private ServerConfigurationService serverConfigurationService;
	
	/**
	 * @return the serverConfigurationService
	 */
	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	/**
	 * @param serverConfigurationService the serverConfigurationService to set
	 */
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.introspectingfilter.IntrospectionUrlProvider#getIntrospectionUrl(java.lang.String)
	 */
	@Override
	public String getIntrospectionUrl(String accessToken) {
		
		try {
	        JWT jwt = JWTParser.parse(accessToken);
	        
	        String issuer = jwt.getJWTClaimsSet().getIssuer();
	        if (!Strings.isNullOrEmpty(issuer)) {
	        	
	        	
	        	
	        	ServerConfiguration server = serverConfigurationService.getServerConfiguration(issuer);
	        	if (server != null) {
	        		if (!Strings.isNullOrEmpty(server.getIntrospectionEndpointUri())) {
	        			return server.getIntrospectionEndpointUri();
	        		} else {
	        			throw new IllegalArgumentException("Server does not have Introspection Endpoint defined");
	        		}
	        	} else {
	        		throw new IllegalArgumentException("Could not find server configuration for issuer " + issuer);
	        	}
	        } else {
	        	throw new IllegalArgumentException("No issuer claim found in JWT");
	        }
	        
        } catch (ParseException e) {
        	throw new IllegalArgumentException("Unable to parse JWT", e);
        }
		
	}

}
