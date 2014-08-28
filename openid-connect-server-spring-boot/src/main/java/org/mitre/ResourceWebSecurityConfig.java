package org.mitre;

import org.mitre.oauth2.web.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Order(106)
@Configuration
public class ResourceWebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private CorsFilter corsFilter;
	
	@Autowired
	private OAuth2AuthenticationEntryPoint authenticationEntryPoint;
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.requestMatchers()
				.antMatchers("/resource/**")
				.and()
			.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
				.and()
			.addFilterBefore(corsFilter, SecurityContextPersistenceFilter.class)
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.authorizeRequests()
				.antMatchers("/resource/**")
				.permitAll()
		;
		// @formatter:on
	}

}