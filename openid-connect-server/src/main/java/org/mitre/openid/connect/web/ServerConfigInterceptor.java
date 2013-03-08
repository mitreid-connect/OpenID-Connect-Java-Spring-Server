/**
 * 
 */
package org.mitre.openid.connect.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 
 * Injects the server configuration bean into the Model context, if it exists. Allows JSPs and the like to call "config.logoUrl" among others. 
 * 
 * @author jricher
 *
 */
public class ServerConfigInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private ConfigurationPropertiesBean config;
	
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    	if (modelAndView != null) { // skip checking at all if we have no model and view to hand the config to
    		modelAndView.addObject("config", config);
    	}
    }

}
