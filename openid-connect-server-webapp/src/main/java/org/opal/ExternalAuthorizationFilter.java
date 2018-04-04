package org.opal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.nimbusds.jwt.JWT;

public class ExternalAuthorizationFilter extends AbstractAuthenticationProcessingFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthorizationFilter.class);

    @Autowired
	private UserInfoService userService;
    
	public ExternalAuthorizationFilter() {
		super(new AntPathRequestMatcher("/login/facebook"));
	}

	
	public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response ) throws AuthenticationException{

        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if ( code == null ) {
            return null; // no header found, continue on to other security filters
        }
        logger.debug("***filter: code: "+code+" state:"+state);
        
        
        UserInfo user = userService.getByUsername("admin");
        ArrayList<GrantedAuthority> temp = new ArrayList<GrantedAuthority>();
		temp.add(new GrantedAuthority() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getAuthority() {
				return "ROLE_ADMIN";
			}
			
		});
		ExternalAuthenticationToken t1 = new ExternalAuthenticationToken(user.getPreferredUsername(), "99", Collections.unmodifiableList(temp));
		t1.setAuthenticated(false);
        return t1;
    }
    
    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
}