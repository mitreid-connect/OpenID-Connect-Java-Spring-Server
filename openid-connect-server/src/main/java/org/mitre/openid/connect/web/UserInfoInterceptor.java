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

    	// get our principal
    	Principal p = request.getUserPrincipal();
    	
    	if (p != null && p.getName() != null) {
    	
	    	// try to look up a user based on it
	    	UserInfo user = userInfoRepository.getByUserId(p.getName());
	    	
	    	// if we have one, inject it so views can use it
	    	if (user != null) {
	    		modelAndView.addObject("userInfo", user);
	    	}
    	}
    	
    }
	
	
	

}
