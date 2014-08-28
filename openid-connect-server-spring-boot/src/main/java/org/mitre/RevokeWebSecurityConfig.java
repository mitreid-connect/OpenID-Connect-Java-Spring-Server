package org.mitre;

import org.mitre.oauth2.web.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Order(110)
@Configuration
public class RevokeWebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private CorsFilter corsFilter;
	
	@Autowired
	private OAuth2AuthenticationEntryPoint authenticationEntryPoint;
	
	@Autowired
	@Qualifier("clientUserDetailsService")
	private  UserDetailsService userDetailsService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}
	
	@Bean
	public ClientCredentialsTokenEndpointFilter clientCredentialsIntrospectionEndpointFilter() throws Exception {
		ClientCredentialsTokenEndpointFilter filter = new ClientCredentialsTokenEndpointFilter("/revoke");
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.requestMatchers()
				.antMatchers("/revoke**")
				.and()
			.httpBasic()
				.authenticationEntryPoint(authenticationEntryPoint)
				.and()
			.addFilterBefore(corsFilter, SecurityContextPersistenceFilter.class)
			.addFilterBefore(clientCredentialsIntrospectionEndpointFilter(), BasicAuthenticationFilter.class)	
			.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint)
				.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		;
		// @formatter:on
	}
}