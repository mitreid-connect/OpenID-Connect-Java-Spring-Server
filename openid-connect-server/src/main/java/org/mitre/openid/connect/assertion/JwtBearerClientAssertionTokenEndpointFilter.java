/**
 * 
 */
package org.mitre.openid.connect.assertion;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * Filter to check client authentication via JWT Bearer assertions.
 * 
 * @author jricher
 *
 */
public class JwtBearerClientAssertionTokenEndpointFilter extends ClientCredentialsTokenEndpointFilter {

	public JwtBearerClientAssertionTokenEndpointFilter() {
	    super();
	    // TODO Auto-generated constructor stub
    }

	public JwtBearerClientAssertionTokenEndpointFilter(String path) {
	    super(path);
	    // TODO Auto-generated constructor stub
    }

	/**
	 * Pull the assertion out of the request and send it up to the auth manager for processing.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

    	// check for appropriate parameters
    	String assertionType = request.getParameter("client_assertion_type");
    	String assertion = request.getParameter("client_assertion");
    	
    	try {
    		JWT jwt = JWTParser.parse(assertion);
    	
    		String clientId = jwt.getJWTClaimsSet().getSubject();
    		
    		Authentication authRequest = new JwtBearerAssertionAuthenticationToken(clientId, jwt);
    	
    		return this.getAuthenticationManager().authenticate(authRequest);
    	} catch (ParseException e) {
    		throw new BadCredentialsException("Invalid JWT credential: " + assertion);
    	}
    }

	/**
	 * Check to see if the "client_assertion_type" and "client_assertion" parameters are present and contain the right values.
     */
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
    	// check for appropriate parameters
    	String assertionType = request.getParameter("client_assertion_type");
    	String assertion = request.getParameter("client_assertion");

    	if (Strings.isNullOrEmpty(assertionType) || Strings.isNullOrEmpty(assertion)) {
    		return false;
    	} else if (!assertionType.equals("urn:ietf:params:oauth:client-assertion-type:jwt-bearer")) {
    		return false;
    	}
    	
    	
    	// Can't call to superclass here b/c client creds would break for lack of client_id
//	    return super.requiresAuthentication(request, response);
    	
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        if ("".equals(request.getContextPath())) {
            return uri.endsWith(getFilterProcessesUrl());
        }

        return uri.endsWith(request.getContextPath() + getFilterProcessesUrl());

    }


	
	
}
