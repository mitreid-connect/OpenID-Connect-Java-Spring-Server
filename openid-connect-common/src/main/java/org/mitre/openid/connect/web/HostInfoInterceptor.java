package org.mitre.openid.connect.web;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.host.util.HostUtils;
import org.mitre.util.HttpUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class HostInfoInterceptor extends HandlerInterceptorAdapter {

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		URL url = HttpUtils.getHost(request);
		
		HostUtils.setCurrentHost(url);
		return super.preHandle(request, response, handler);
	}
	
}
