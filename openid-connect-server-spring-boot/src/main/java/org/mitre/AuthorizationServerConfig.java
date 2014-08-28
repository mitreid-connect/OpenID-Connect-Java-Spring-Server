package org.mitre;

import java.util.Arrays;

import org.mitre.oauth2.service.impl.DefaultOAuth2AuthorizationCodeService;
import org.mitre.oauth2.service.impl.DefaultOAuth2ClientDetailsEntityService;
import org.mitre.oauth2.service.impl.DefaultOAuth2ProviderTokenService;
import org.mitre.oauth2.token.ChainedTokenGranter;
import org.mitre.oauth2.token.JwtAssertionTokenGranter;
import org.mitre.oauth2.token.StructuredScopeAwareOAuth2RequestValidator;
import org.mitre.openid.connect.ConnectOAuth2RequestFactory;
import org.mitre.openid.connect.token.TofuUserApprovalHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestValidator;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;

@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private DefaultOAuth2ClientDetailsEntityService clientDetailsService;
	
	@Autowired
	private DefaultOAuth2ProviderTokenService tokenServices;
	
	@Autowired
	private TofuUserApprovalHandler tofuUserApprovalHandler;
	
	@Autowired
	private ConnectOAuth2RequestFactory requestFactory;
	
	@Autowired
	private DefaultOAuth2AuthorizationCodeService authorizationCodeServices;
	
	@Autowired
	private ChainedTokenGranter chainedTokenGranter;
	
	@Autowired JwtAssertionTokenGranter jwtAssertionTokenGranter;
	
	@Bean
	OAuth2RequestValidator requestValidator() {
		return new StructuredScopeAwareOAuth2RequestValidator();
	}
	
	private TokenGranter tokenGranter() {
		return new CompositeTokenGranter(Arrays.<TokenGranter>asList(
				new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices, clientDetailsService, requestFactory),
				new ImplicitTokenGranter(tokenServices, clientDetailsService, requestFactory),
				new RefreshTokenGranter(tokenServices, clientDetailsService, requestFactory),
				new ClientCredentialsTokenGranter(tokenServices, clientDetailsService, requestFactory),
				chainedTokenGranter,
				jwtAssertionTokenGranter
				));
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints
			.requestValidator(requestValidator())
			.pathMapping("/oauth/token", "/token")
			.pathMapping("/oauth/authorize", "/authorize")
			.tokenServices(tokenServices)
			.userApprovalHandler(tofuUserApprovalHandler)
			.requestFactory(requestFactory)
			.clientDetailsService(clientDetailsService)
			.tokenGranter(tokenGranter())
			.authorizationCodeServices(authorizationCodeServices)
		;
	}
	
	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
	}
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetailsService);
	}
}
