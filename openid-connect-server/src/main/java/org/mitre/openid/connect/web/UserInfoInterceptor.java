/**
 * 
 */
package org.mitre.openid.connect.web;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author jricher
 *
 */
public class UserInfoInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private UserInfoRepository userInfoRepository;
	
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    	if (modelAndView != null) { // skip checking at all if we have no model and view to hand the user to
	    	// get our principal from the security context
	    	Principal p = request.getUserPrincipal();
	    	
	    	if (p != null && p.getName() != null) { // don't bother checking if we don't have a principal
	    	
		    	// try to look up a user based on it
		    	UserInfo user = userInfoRepository.getByUserId(p.getName());
		    	
		    	// if we have one, inject it so views can use it
		    	if (user != null) {
		    		modelAndView.addObject("userInfo", user);
		    	}
	    	}
    	}
    	
    }
	
	
	

}
