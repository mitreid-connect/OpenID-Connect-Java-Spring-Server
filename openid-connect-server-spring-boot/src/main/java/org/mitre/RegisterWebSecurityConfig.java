package org.mitre;

import org.mitre.oauth2.web.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
public class RegisterWebSecurityConfig extends ResourceServerConfigurerAdapter {

	@Autowired
	private CorsFilter corsFilter;
	
	@Autowired
	private OAuth2AuthenticationEntryPoint authenticationEntryPoint;
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.requestMatchers()
				.antMatchers("/register/**")
				.and()
			.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
				.and()
			.addFilterBefore(corsFilter, SecurityContextPersistenceFilter.class)
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.authorizeRequests()
				.antMatchers("/register/**")
				.permitAll()
		;
		// @formatter:on
	}

}