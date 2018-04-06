package org.opal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;


public class ExternalAuthorizationFilter extends AbstractAuthenticationProcessingFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthorizationFilter.class);

    private String issuer;
    
    public ExternalAuthorizationFilter(RequestMatcher requestMatcher, String issuer) {
    	super(requestMatcher);
    	this.issuer = issuer;
    }

	
	public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response ) throws AuthenticationException{

        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if ( code == null ) {
            return null; // no header found, continue on to other security filters
        }
        logger.debug("***filter: code: "+code+" state:"+state);
        
        ExternalAuthenticationToken t1 = new ExternalAuthenticationToken(null, null, null);
		t1.setAuthenticated(false);
		t1.setCode(code);
		t1.setState(state);
		t1.setIssuer(issuer);
        return t1;
    }
    
    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
}