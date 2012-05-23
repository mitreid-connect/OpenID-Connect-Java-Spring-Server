# OpenID Connect Client

## Overview 

This is the Client, a Spring Security AuthenticationFilter, to the OpenID Connect Java Spring Server following the [OpenID Connect Standard] described protocol.

## Configuration of OIDCAuthenticationFilter

Configure the OIDCAuthenticationFilter by adding the XML to your application context security like so making changes where necessary for your deployment:

	<security:http auto-config="false" 
		use-expressions="true"
		disable-url-rewriting="true" 
		entry-point-ref="authenticationEntryPoint" 
		pattern="/**">

		<security:intercept-url 
			pattern="/somepath/**" 
			access="denyAll" />

		<security:custom-filter 
			before="PRE_AUTH_FILTER 
			ref="openIdConnectAuthenticationFilter" />
	
		<security:intercept-url 
			pattern="/**" 
			access="hasAnyRole('ROLE_USER','ROLE_ADMIN')" /> 
		
		<security:logout />
		
		<securityLremember-me user-service-ref="myUserDetailsService"
	</security:http>
	
	<bean id="authenticationEntryPoint" 
		class="org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint"> 
		<property name="loginFormUrl" 
			value="/openid_connect_login"/> 
	</bean>
	
	<security:authentication-manager alias="authenticationManager" /> 

	<bean id="openIdConnectAuthenticationProvider"
		class='org.mitre.openid.connect.client.OIDCAuthenticationProvider">
		<property name="userDetaulsService" ref="myUserDetailsService"/>
	</bean>

	<bean id="openIdConnectAuthenticationFilter"
		class="org.mitre.openid.connect.client.OpenIdConnectAuthenticationFilter">
		<property name="authenticationManager"
			ref="authenticationManager" />
		<property name="errorRedirectURI" 
			value="/login.jsp?authfail=openid" />
		<property name="authorizationEndpointURI" 
			value="http://sever.example.com:8080/openid-connect-server/oauth/authorize" />
		<property name="tokenEndpointURI" 
			value="http://sever.example.com:8080/openid-connect-server/oauth/token" />
		<property name="checkIDEndpointURI" 
			value="http://sever.example.com:8080/openid-connect-server/checkid" />
		<property name="clientId" 
			value="someClientId" /> 
		<property name="clientSecret" value="someClientSecret" /> 
	</bean>

You will need to implement your own UserDetailsService and configure as the above does with the reference to *myUserDetailsService*.

## Configuration of OIDCAuthenticationUsingChooserFilter

The OIDCAuthenticationUsingChooserFilter was written in response to [Issue #39].

The Authentication Filter uses the *oidcServerConfigs* property, a map of OIDC servers; an *accountChooserURI* property to denote the URI of the Account Chooser; and an *accountChooserClient* property to identify the Client to the Account Chooser UI application like so with modifications specific to your deployment:
	
	<bean id="openIdConnectAuthenticationFilter"
		class="org.mitre.openid.connect.client.OIDCAuthenticationUsingChooserFilter">
		<property name="errorRedirectURI" value="/login.jsp?authfail=openid" /> 
		<property name="authenticationManager" ref="authenticationManager" />
		<property name="accountChooserURI"
			value="http://sever.example.com:8080/account-chooser" />
		<property name="accountChooserClientID" value="FGWEUIASJK" />
		<property name="oidcServerConfigs">
			<map>
				<entry key="http://sever.example.com:8080/Fopenid-connect-server">
					<bean class="org.mitre.openid.connect.client.OIDCServerConfiguration">
						<property name="authorizationEndpointURI" 
							value="http://sever.example.com:8080/openid-connect-server/oauth/authorize" />
						<property name="tokenEndpointURI" 
							value="http://sever.example.com:8080/openid-connect-server/oauth/token" />
						<property name="checkIDEndpointURI" 
							value="http://sever.example.com:8080/openid-connect-server/checkid" />
						<property name="clientId" 
							value="someClientId" /> 
						<property name="clientSecret" value="someClientSecret" />
					</bean>
				</entry>
				<entry key=". . .
			</map>
		</property>
	</bean>
	
Again, you will need to implement your own UserDetailsService and configure as the above does with the reference to *myUserDetailsService*.	

## Implementing your own UserDetailsService

An example UserDetailsService for the Rave Portal follows:

        package org.mitre.mpn.service.impl;
        
        import org.apache.rave.portal.model.NewUser;
        import org.apache.rave.portal.model.User;
        import org.apache.rave.portal.service.NewAccountService;
        import org.apache.rave.portal.service.UserService;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
        import org.springframework.security.core.userdetails.UserDetails;
        import org.springframework.security.core.userdetails.UserDetailsService;
        import org.springframework.security.core.userdetails.UsernameNotFoundException;
        
        import org.mitre.openid.connect.client.OpenIdConnectAuthenticationToken;
        
        import org.springframework.stereotype.Service;
        
        import java.util.UUID;
        
        @Service(value = "myUserDetailsService")
        public class MyUserDetailsService implements UserDetailsService,
                AuthenticationUserDetailsService<OpenIdConnectAuthenticationToken> {
        
            private static final Logger log = LoggerFactory.getLogger(MpnUserDetailsService.class);

            private final UserService userService;
            private final NewAccountService newAccountService;

            //TODO: This is temporarily hard-coded while we wait for the concept of Page Templates to be implemented in Rave
            private static final String DEFAULT_LAYOUT_CODE = "columns_3";
        
            @Autowired
            public MyUserDetailsService(UserService userService, NewAccountService newAccountService) {
                this.userService = userService;
                this.newAccountService = newAccountService;
            }
        
            /* (non-Javadoc)
             * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
             */
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                
            	log.debug("loadUserByUsername called with: {}", username);
                
            	User user = userService.getUserByUsername(username);
                
                if (user == null) {
                    throw new UsernameNotFoundException("User with username '" + username + "' was not found!");
                }
                
                return user;
            }
        
            /* (non-Javadoc)
             * @see org.springframework.security.core.userdetails.AuthenticationUserDetailsService#loadUserDetails(org.springframework.security.core.Authentication)
             */
            public UserDetails loadUserDetails(OpenIdConnectAuthenticationToken token) throws UsernameNotFoundException {
                log.debug("loadUserDetails called with: {}", token);
        
                User user = userService.getUserByUsername(token.getUserId());
        
                if (user == null) {
                	
                    NewUser newUser = new NewUser();
                    newUser.setUsername(token.getUserId());
                    newUser.setEmail(token.getUserId() + "@example.com");
                    newUser.setPageLayout(DEFAULT_LAYOUT_CODE);
                    newUser.setPassword(UUID.randomUUID().toString());
                    
                    try {
                        newAccountService.createNewAccount(newUser);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
        
                    user = userService.getUserByUsername(token.getName());
        
                }
                
                return user;
            }
        }


[OpenID Connect Standard]: http://openid.net/specs/openid-connect-standard-1_0.html "OpenID Connect Standard 1.0"
[OpenID Connect Standard]: http://openid.net/specs/openid-connect-standard-1_0.html#code_flow "Authorization Code Flow, OpenID Connect Standard"
[Issuer Identifier]: http://openid.net/specs/openid-connect-messages-1_0.html#issuer_identifier "Issuer Identifier"
[Issue #39]: http://github.com/jricher/OpenID-Connect-Java-Spring-Server/issues/39 "Issue #39 -- Multiple Point Client"
