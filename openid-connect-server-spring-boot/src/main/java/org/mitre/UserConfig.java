package org.mitre;

import javax.sql.DataSource;

import org.mitre.openid.connect.filter.PromptFilter;
import org.mitre.openid.connect.web.AuthenticationTimeStamper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class UserConfig {
	
	@Order(106)
	@Configuration
	public static class EndUserLoginSecurityConfiguration extends WebSecurityConfigurerAdapter {
		@Autowired
		private Http403ForbiddenEntryPoint http403ForbiddenEntryPoint;
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.requestMatchers()
					.antMatchers("/login**")
					.and()
				.authorizeRequests()
					.antMatchers("/login**")
					.permitAll()
					.and()
				.exceptionHandling()
					.authenticationEntryPoint(http403ForbiddenEntryPoint);
			;
			// @formatter:on
		}
	}
	
	@Order(107)
	@Configuration
	public static class EndUserSecurityConfiguration extends WebSecurityConfigurerAdapter {
		
		@Autowired
		private AuthenticationTimeStamper authenticationTimeStamper;
		
		@Autowired
		private OAuth2WebSecurityExpressionHandler oAuth2WebSecurityExpressionHandler;
		
		@Autowired
		private PromptFilter promptFilter;
		
		@Autowired
		private DataSource dataSource;
		
		@Autowired
		public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
			// auth.inMemoryAuthentication().withUser("barry").password("password").roles("USER"); // ... etc.
			auth.jdbcAuthentication().dataSource(dataSource);
		}

		@Override
		public void configure(WebSecurity web) throws Exception {
			web.expressionHandler(oAuth2WebSecurityExpressionHandler);
		}
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.sessionManagement()
					.enableSessionUrlRewriting(false)
					.and()
				.formLogin()
					.loginPage("/login")
					.loginProcessingUrl("/j_spring_security_check")
					.failureUrl("/login?error=failure")
					.successHandler(authenticationTimeStamper)
					.permitAll()
					.and()
				.authorizeRequests()
					.antMatchers("/**")
					.permitAll()
					.and()
				.addFilterBefore(promptFilter, SecurityContextPersistenceFilter.class)
				.logout()
					.logoutUrl("/logout")
					.and()
				.anonymous()
				.and()
				.csrf().requireCsrfProtectionMatcher(new AntPathRequestMatcher("/authorize")).disable();
			;
			// @formatter:on
		}

		@Bean(name = "authenticationManager")
		public AuthenticationManager authenticationManagerBean()
				throws Exception {
			return super.authenticationManagerBean();
		}
		
		@Bean
		public OAuth2WebSecurityExpressionHandler oAuth2WebSecurityExpressionHandler() {
			return new OAuth2WebSecurityExpressionHandler();
		}
	}
}
