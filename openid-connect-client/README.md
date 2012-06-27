# OpenID Connect Client #

## Overview ##

You are reading the documentation for the OIDC Client implemented as a Spring Security AuthenticationFilter.  The client facilitates a user's authentication into the secured application to an OpenID Connect Java Spring Server following the [OpenID Connect Standard] described protocol.

## Configuring ##

Configure the client by adding the following XML to your application context security making changes where necessary for your specific deployment.

Open and define an HTTP security configuration with a reference to a bean defined custom ***AuthenticationEntryPoint***:

	<security:http auto-config="false" 
		use-expressions="true"
		disable-url-rewriting="true" 
		entry-point-ref="authenticationEntryPoint" 
		pattern="/**">

Specify the access attributes and/or filter list for a particular set of URLs needing protection:

		<security:intercept-url 
			pattern="/**" 
			access="hasAnyRole('ROLE_USER','ROLE_ADMIN')" /> 

Indicate that ***OpenIdConnectAuthenticationFilter*** authentication filter should be incorporated into the security filter chain:

		<security:custom-filter 
			before="PRE_AUTH_FILTER 
			ref="openIdConnectAuthenticationFilter" />

Set up remember-me authentication referencing the yet to be defined ***UserDetailsService***:
		
		<security:remember-me user-service-ref="myUserDetailsService"
		
NOTE:  See the last section as how to implement your own ***UserDetailsService*** necessary to complete authentication.
		
Then close the HTTP security configuration:
		
	</security:http>

Define a custom ***AuthenticationEntryPoint*** via a bean declaration:
	
	<bean id="authenticationEntryPoint" 
		class="org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint"> 
		<property name="loginFormUrl" 
			value="/openid_connect_login"/> 
	</bean>
	
NOTE:  The ***loginFormUrl*** value is post-pended to the URI of the application being secured to define the ***redirect_uri***, the value passed to the OIDC Server and, if the ***OIDCAuthenticationUsingChooserFilter*** is configured, also the Account Chooser Application.  
		
Define an ***AuthenticationManager*** with a reference to a custom authentication provider, ***OpenIDConnectAuthenticationProvider***:
		
	<security:authentication-manager alias="authenticationManager">
		<security:authentication-provider ref="openIDConnectAuthenticationProvider" />
	</security:authentication-manager> 

Define the custom authentication provider referencing the your yet to be defined implementation of a ***UserDetailsService***:

	<bean id="openIdConnectAuthenticationProvider"
		class='org.mitre.openid.connect.client.OIDCAuthenticationProvider">
		<property name="userDetailsService" ref="myUserDetailsService"/>
	</bean>

### Configuring the OIDCAuthenticationFilter ###

The ***OpenIdConnectAuthenticationFilter*** filter is defined with the following properties:

*	***authenticationManager*** -- a reference to the  ***AuthenticationManager***,
*	***errorRedirectURI*** -- the URI of the Error redirect,
*   ***authorizationEndpointURI*** -- the URI of the Authorization Endpoint, 
*   ***tokenEndpointURI*** -- the URI of the Token Endpoint, 
*   ***clientId*** -- the registered client identifier, and 
*   ***clientSecret*** -- the registered client secret.

Configure like so: 

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
		<property name="clientId" 
			value="someClientId" /> 
		<property name="clientSecret" value="someClientSecret" /> 
	</bean>	

NOTE:  Again, you will need your own implementation of a ***UserDetailsService*** specific to your deployment. See the last section of this document.

### Or Alternatively, Configuring the OIDCAuthenticationUsingChooserFilter ###

Alternatively, the ***OIDCAuthenticationUsingChooserFilter*** can be configured and used.  It was written in response to [Issue #39]. [The Client -- Account Chooser protocol] documentation details the protocol used between the Client and an Account Chooser application.  

The ***OIDCAuthenticationUsingChooserFilter*** Authentication Filter has the following properties:

*   ***oidcServerConfigs*** -- a map of ***OIDCserverConfiguration***s to encapsulate the settings necesary for the client to communicate with each respective OIDC server,
*   ***accountChooserURI*** -- to denote the URI of the Account Chooser, and 
*   ***accountChooserClient*** -- to identify the Client to the Account Chooser UI application. 

Each ***OIDCServerConfiguration*** entry in ***OIDCserverConfiguration*** map is keyed to the ***issuer*** returned from the Account Chooser Application and enumerates the following properties: 

*   ***authorizationEndpointURI*** -- the URI of the Authorization Endpoint, 
*   ***tokenEndpointURI*** -- the URI of the Token Endpoint, 
*   ***clientId*** -- the registered client identifier, and 
*   ***clientSecret*** -- the registered client secret.

Configure like so: 
	
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
						<property name="clientId" 
							value="someClientId" /> 
						<property name="clientSecret" value="someClientSecret" />
					</bean>
				</entry>
				<entry key=". . .
			</map>
		</property>
	</bean>
	
Again, you will need your own implementation of a ***UserDetailsService***. See the next section.	
## Implementing your own UserDetailsService ##

You need to implement your own ***UserDetailsService*** to complete the authentication.

An example ***UserDetailsService*** for the Rave Portal follows:

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
[The Client -- Account Chooser protocol]: https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/blob/master/account-chooser/docs/protocol.md
