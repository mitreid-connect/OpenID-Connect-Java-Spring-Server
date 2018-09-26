package org.mitre.oauth2.web;

import java.io.IOException;
import java.net.URL;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.host.util.HostUtils;
import org.mitre.util.HttpUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component("hostFilter")
public class HostFilter extends OncePerRequestFilter {

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

	URL url = HttpUtils.getHost(request);
		
		HostUtils.setCurrentHost(url);
		filterChain.doFilter(request, response);
	}

}
