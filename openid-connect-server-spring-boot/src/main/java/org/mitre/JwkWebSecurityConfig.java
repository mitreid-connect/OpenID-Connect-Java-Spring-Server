package org.mitre;

import org.mitre.oauth2.web.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

@Order(102)
@Configuration
public class JwkWebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private CorsFilter corsFilter;
	
	@Autowired
	private Http403ForbiddenEntryPoint http403ForbiddenEntryPoint;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.requestMatchers()
				.antMatchers("/jwk**")
				.and()
			.exceptionHandling()
				.authenticationEntryPoint(http403ForbiddenEntryPoint)
				.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
		.authorizeRequests()
			.antMatchers("/jwk**")
			.permitAll()
		;
		// @formatter:on
	}
}
