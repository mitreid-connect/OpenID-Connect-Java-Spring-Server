# OpenID Connect Client #

## Overview ##

This project contains an OpenID Connect Client implemented as a Spring Security AuthenticationFilter.  The client facilitates a user's authentication into the secured application to an OpenID Connect Java Spring Server following the OpenID Connect Standard protocol.

For an example of the Client configuration, see the [Simple Web App] project.

## Configuring ##

Configure the client by adding the following XML to your application context security making changes where necessary for your specific deployment.

Open and define an HTTP security configuration with a reference to a custom ***AuthenticationEntryPoint***, described below:

	<security:http auto-config="false" use-expressions="true" disable-url-rewriting="true" entry-point-ref="authenticationEntryPoint" pattern="/**">

Specify the access attributes and/or filter list for a particular set of URLs needing protection:

		<security:intercept-url pattern="/**" access="hasAnyRole('ROLE_USER','ROLE_ADMIN')" /> 

Indicate that ***OIDCAuthenticationFilter*** authentication filter should be incorporated into the security filter chain:

		<security:custom-filter before="PRE_AUTH_FILTER" ref="openIdConnectAuthenticationFilter" />

Then close the HTTP security configuration:
		
	</security:http>

Define a custom ***AuthenticationEntryPoint*** to use a login URL via a bean declaration:
	
	<bean id="authenticationEntryPoint" class="org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint"> 
		<property name="loginFormUrl" value="/openid_connect_login" />
	</bean>
	
NOTE:  The ***loginFormUrl*** value is post-pended to the URI of the application being secured to define the ***redirect_uri***, the value passed to the OIDC Server and, if the ***OIDCAuthenticationUsingChooserFilter*** is configured, also the Account Chooser Application.  
		
Define an ***AuthenticationManager*** with a reference to a custom authentication provider, ***OpenIDConnectAuthenticationProvider***:
		
	<security:authentication-manager alias="authenticationManager">
		<security:authentication-provider ref="openIDConnectAuthenticationProvider" />
	</security:authentication-manager> 

Define the custom authentication provider. Note that it does not take a UserDetailsService as input at this time but instead makes a call to the UserInfoEndpoint to fill in user information.

	<bean id="openIdConnectAuthenticationProvider" class="org.mitre.openid.connect.client.OIDCAuthenticationProvider" />

### Configuring the OIDCAuthenticationFilter ###

The ***OIDCAuthenticationFilter*** filter is defined with the following properties:

*	***authenticationManager*** -- a reference to the  ***AuthenticationManager***
*	***errorRedirectURI*** -- the URI of the Error redirect

Additionally, it contains a set of convenience methods to pass through to parameters on the ***OIDCServerConfiguration*** object that defines attributes of the server that it connects to:

*   ***issuer*** -- the root issuer string of this server (required)
*   ***authorizationEndpointUrl*** -- the URL of the Authorization Endpoint (required)
*   ***tokenEndpointUrl*** -- the URL of the Token Endpoint (required)
*   ***jwkSigningUrl*** -- the URL of the JWK (public key) Endpoint for token verification 
*   ***clientId*** -- the registered client identifier (required)
*   ***clientSecret*** -- the registered client secret
*   ***userInfoUrl*** -- the URL of the User Info Endpoint
*   ***scope*** -- space-separated list of scopes; the required value "openid" will always be prepended to the list given here

Configure like so: 

	<bean id="openIdConnectAuthenticationFilter"
		class="org.mitre.openid.connect.client.PlainOIDCAuthenticationFilter">
		<property name="authenticationManager" ref="authenticationManager" />
		<property name="errorRedirectURI" value="/login.jsp?authfail=openid" />
		<property name="issuer" value="http://server.example.com:8080/openid-connect-server/" />
		<property name="authorizationEndpointUrl" value="http://sever.example.com:8080/openid-connect-server/openidconnect/auth" />
		<property name="tokenEndpointUrl" value="http://sever.example.com:8080/openid-connect-server/openidconnect/token" />
		<property name="jwkSigningUrl" value="http://server.example.com:8080/openid-connect-server/jwk" />
		<property name="clientId" value="someClientId" /> 
		<property name="clientSecret" value="someClientSecret" /> 
		<property name="userInfoUrl" value="http://server.example.com:8080/open-id-connect-server/userinfo" />
		<property name="scope" value="profile email address phone" />
	</bean>	

### Configuring the OIDCAuthenticationUsingChooserFilter ###

For talking to multiple IdPs using an Account chooser, the ***OIDCAuthenticationUsingChooserFilter*** can be configured and used.  [The Client -- Account Chooser protocol] documentation details the protocol used between the Client and an Account Chooser application.  

The ***OIDCAuthenticationUsingChooserFilter*** Authentication Filter has the following properties:

*	***authenticationManager*** -- a reference to the  ***AuthenticationManager***,
*	***errorRedirectURI*** -- the URI of the Error redirect,
*   ***accountChooserURI*** -- to denote the URI of the Account Chooser, and 
*   ***accountChooserClient*** -- to identify the Client to the Account Chooser UI application. 
*   ***oidcServerConfigs*** -- a map of ***OIDCserverConfiguration***s to encapsulate the settings necesary for the client to communicate with each respective OIDC server,

Each ***OIDCServerConfiguration*** entry in ***OIDCserverConfiguration*** map is keyed to the ***issuer*** returned from the Account Chooser Application and enumerates the following properties: 

*	***authenticationManager*** -- a reference to the  ***AuthenticationManager***,
*   ***issuer*** -- the root issuer string of this server (required)
*   ***authorizationEndpointUrl*** -- the URL of the Authorization Endpoint (required)
*   ***tokenEndpointUrl*** -- the URL of the Token Endpoint (required)
*   ***jwkSigningUrl*** -- the URL of the JWK (public key) Endpoint for token verification 
*   ***clientId*** -- the registered client identifier (required)
*   ***clientSecret*** -- the registered client secret
*   ***userInfoUrl*** -- the URL of the User Info Endpoint
*   ***scope*** -- space-separated list of scopes; the required value "openid" will always be prepended to the list given here

Configure like so: 
	
	<bean id="openIdConnectAuthenticationFilter"
		class="org.mitre.openid.connect.client.OIDCAuthenticationUsingChooserFilter">
		<property name="errorRedirectURI" value="/login.jsp?authfail=openid" /> 
		<property name="authenticationManager" ref="authenticationManager" />
		<property name="accountChooserURI" value="http://sever.example.com:8080/account-chooser" />
		<property name="accountChooserClientID" value="FGWEUIASJK" />
		<property name="oidcServerConfigs">
			<map>
				<entry key="http://sever.example.com:8080/Fopenid-connect-server">
					<bean class="org.mitre.openid.connect.client.OIDCServerConfiguration">
						<property name="issuer" value="http://server.example.com:8080/openid-connect-server/" />
						<property name="authorizationEndpointUrl" value="http://sever.example.com:8080/openid-connect-server/openidconnect/auth" />
						<property name="tokenEndpointUrl" value="http://sever.example.com:8080/openid-connect-server/openidconnect/token" />
						<property name="jwkSigningUrl" value="http://server.example.com:8080/openid-connect-server/jwk" />
						<property name="clientId" value="someClientId" /> 
						<property name="clientSecret" value="someClientSecret" /> 
						<property name="userInfoUrl" value="http://server.example.com:8080/open-id-connect-server/userinfo" />
						<property name="scope" value="profile email address phone" />
					</bean>
				</entry>
				<entry key=". . .
			</map>
		</property>
	</bean>
	
[The Client -- Account Chooser protocol]: https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/blob/master/account-chooser/docs/protocol.md
[Simple Web App]: https://github.com/mitreid-connect/simple-web-app