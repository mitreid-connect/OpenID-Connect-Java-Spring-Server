package org.mitre;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author woltere
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "org.mitre")
@EnableAutoConfiguration
@EnableConfigurationProperties(Application.OpenIdConnectConfigurationProperties.class)
@EnableResourceServer
@EnableAuthorizationServer
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @ConfigurationProperties(prefix = "openid.connect.server")
	public static class OpenIdConnectConfigurationProperties extends ConfigurationPropertiesBean {
		
	}
    
    @Bean
	public Http403ForbiddenEntryPoint http403ForbiddenEntryPoint() {
		return new Http403ForbiddenEntryPoint();
	}
    
	/**
	 * This seems to be the only way to do the same as <code><mvc:annotation-driven ignoreDefaultModelOnRedirect="true" /></code>
	 * 
	 */
	@Configuration
	protected static class WorkAroundDelegatingWebMvcConfiguration extends DelegatingWebMvcConfiguration {
		@Override
		public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
			RequestMappingHandlerAdapter adapter = super.requestMappingHandlerAdapter();
			adapter.setIgnoreDefaultModelOnRedirect(true);
			return adapter;
		}
	}
	
	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
	public static class SecurityConfiguration extends GlobalMethodSecurityConfiguration {
		
	    @Override
	    protected MethodSecurityExpressionHandler createExpressionHandler() {
	        return new OAuth2MethodSecurityExpressionHandler();
	    }		
	}
		
	@Bean
	public OAuth2WebSecurityExpressionHandler oauthWebExpressionHandler() {
		return new OAuth2WebSecurityExpressionHandler();
	}
	
	@Bean
	public OAuth2AuthenticationEntryPoint oauth2AuthenticationEntryPoint() {
		OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint();
		entryPoint.setRealmName("openidconnect");
		return entryPoint;
	}
}
