# OpenID Connect Client

## Overview 

This is the Client, a Spring Security AuthenticationFilter, to OpenID Connect Java Spring Server described by [OpenID Connect Standard].

## Configure

Configure the OpenIDConnectAuthenticationFilter by adding the XML to your application context security like so:

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
		class='org.mitre.openid.connect.client.OpenIdConnectAuthenticationProvider">
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

## Proposed Account Chooser UI Application Extension

The following proposed extension is in response to [Issue #39].

### Account Chooser Protocol

The following describes the protocol between the Client and Account Chooser UI application introduced in [Issue #39]. 

#### Authorization when using Account Chooser Code Flow

The Authorization when using Account Chooser Code Flow goes through the following steps. 

1. Client prepares an Account Chooser Request containing the desired request parameters.
2. Client sends a request to the Account Chooser.
3. Account Chooser presents a selection of OpenID Connect (OIDC) Servers from which the End-User must select from.
4. End-User selects an OIDC.
5. Account Chooser Sends the End-User back to the Client with key value of the OIDC End-User selected.
6. The Client begins the Authorization flow desrcribed in [Authorization Code Flow][OpenID Connect Standard] of the [OpenID Connect Standard].

#### Account Chooser Request

When the End-User wishes to access a Protected Resource, and the End-User Authorization has not yet been obtained, the Client will redirect the End-User to Account Chooser.

Account Chooser MUST support the use of the HTTP "GET" and "POST" methods defined in RFC 2616 [RFC2616]. 

Clients MAY use the HTTP "GET" or "POST" method to send the Account Chooser Request to the Account Chooser. If using the HTTP "GET" method, the request parameters are serialized using URI query string serialization. If using the HTTP "POST" method, the request parameters are serialized using form serialization. 

#### Client Prepares an Account Chooser Request

The Client prepares an Account Chooser Request to the Account Chooser with the request parameters using the HTTP "GET" or "POST" method.

The required Account Chooser Request parameters are as follows: 

* redirect_uri - A redirection URI where the response will be sent.

There is one method to construct and send the request to the Account Chooser:

a. Simple Request Method 

#### Simple Request Method

The Client prepares an Account Chooser Request to the Account Chooser using the appropriate parameters. If using the HTTP "GET" method, the request parameters are serialized using URI query string serialization. If using the HTTP "POST" method, the request parameters are serialized using form serialization. 

The following is a non-normative example of an Account Chooser Request URL. Line wraps are for display purposes only. 

	http://server.example.com/chooser?
	redirect_uri=https%3A%2F%2Fclient.example.com%2Fopenid_connect_login
	
#### Client sends a request to the Account Chooser

Having constructed the Account Chooser Request, the Client sends it to the Account Chooser. This MAY happen via redirect, hyperlinking, or any other means of directing the User-Agent to the Account Chooser URL.

Following is a non-normative example using HTTP redirect. Line wraps are for display purposes only.

	HTTP/1.1 302 Found
	Location: https://server.example.com/chooser?
	redirect_uri=https%3A%2F%2Fclient.example.com%2Fopenid_connect_login

#### Account Chooser Sends the End-User back to the Client

After the End-User has select an OpenID Connect Server, it issues an Account Chooser Response and delivers it to the Client by adding the response parameters to redirect_uri specified in the Account Choose Request using the "application/x-www-form-urlencoded" format. 

The following response parameters are included:

* oidc_alias - REQUIRED. The key used to configure the Client for its request of the selected OIDC server. 

The following is non-normative example of a responses. Line wraps are for display purposes only. 

	HTTP/1.1 302 Found
	Location: https://client.example.com/openid_connect_login?
	oidc_alias=OIDC%20Server%201

#### End-User refuses to select an OIDC Server

If the End-User refuses to select an OIDC server, the Account Chooser MUST return an error response. The Account Chooser returns the Client via the redirection URI specified in the Account Chooser Request with the appropriate error parameters. No other parameters SHOULD be returned.

The error response parameters are the following:

* error - REQUIRED. The error code. 
* error_description - OPTIONAL. A human-readable UTF-8 encoded text description of the error. 

The response parameters are added to the query component of the redirection URI.

The following is a non-normative example. Line wraps after the second line are for the display purposes only.

	HTTP/1.1 302 Found
	Location: https://client.example.com/openid_connect_login?
	error=end_user_cancelled
	&error_description=The%20end%20user%20refused%20to%20select%20an%20OIDC%20server

### Modification to existing Client

#### Modifications to Client Configuration

The configuration of the filter would change by adding a OIDCServers property to the Client containing a map of OIDC servers, and a AccountChooserURI to denote the URI of the Account Chooser like so:
	
	<bean id="openIdConnectAuthenticationFilter"
		class="org.mitre.openid.connect.client.OpenIdConnectAuthenticationFilter">
		<property name="errorRedirectURI" value="/login.jsp?authfail=openid" /> 
		<property name="authenticationManager" ref="authenticationManager" />
		<property name="AccountChooserURI"
			value="http://sever.example.com:8080/account-chooser" />
		<property name="oidcServerConfigs">
			<map>
				<entry key="OIDC Server 1">
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
				<entry key="OIDC Server 2">
				...
			</map>
		</property>
	</bean>
	
	
	

In cases where the Account Chooser will not be used, the Client will be configured with authorizationEndpointURI, tokenEndpointURI, checkIDEndpointURI, clientId, and clientSecret as the Client is presently.

[OpenID Connect Standard]: http://openid.net/specs/openid-connect-standard-1_0.html "OpenID Connect Standard 1.0"
[OpenID Connect Standard]: http://openid.net/specs/openid-connect-standard-1_0.html#code_flow "Authorization Code Flow, OpenID Connect Standard"
[Issue #39]: http://github.com/jricher/OpenID-Connect-Java-Spring-Server/issues/39 "Issue #39 -- Multiple Point Client"