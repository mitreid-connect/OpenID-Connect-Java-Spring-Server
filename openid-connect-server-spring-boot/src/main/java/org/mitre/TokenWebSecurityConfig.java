package org.mitre;

import java.util.Collections;

import org.mitre.oauth2.web.CorsFilter;
import org.mitre.openid.connect.assertion.JwtBearerAuthenticationProvider;
import org.mitre.openid.connect.assertion.JwtBearerClientAssertionTokenEndpointFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@Order(101)
public class TokenWebSecurityConfig extends WebSecurityConfigurerAdapter {
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
		ClientCredentialsTokenEndpointFilter filter = new ClientCredentialsTokenEndpointFilter("/token");
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}
	
	@Bean
	public JwtBearerClientAssertionTokenEndpointFilter clientAssertiontokenEndpointFilter() {
		JwtBearerClientAssertionTokenEndpointFilter filter = new JwtBearerClientAssertionTokenEndpointFilter("/token");
		filter.setAuthenticationManager(new ProviderManager(Collections.<AuthenticationProvider>singletonList(new JwtBearerAuthenticationProvider())));
		return filter;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.requestMatchers()
				.antMatchers("/token")
				.and()
			.httpBasic()
				.authenticationEntryPoint(authenticationEntryPoint)
				.and()
			.authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/token").permitAll()
				.antMatchers("/token").authenticated()
				.and()
			.addFilterBefore(clientAssertiontokenEndpointFilter(), AbstractPreAuthenticatedProcessingFilter.class)
			.addFilterBefore(clientCredentialsIntrospectionEndpointFilter(), BasicAuthenticationFilter.class)	
			.addFilterBefore(corsFilter, SecurityContextPersistenceFilter.class)
			.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(new OAuth2AccessDeniedHandler())
				.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//				.and()
//			.csrf().disable()
		;
		// @formatter:on
	}
}
