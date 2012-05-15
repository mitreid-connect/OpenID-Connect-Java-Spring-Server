# OpenID Connect Client

## Overview 

This is the Client, a Spring Security AuthenticationFilter, to OpenID Connect Java Spring Server described by [OpenID Connect Standard].

## Configuration of OIDCAuthenticationFilter

Configure the OIDCAuthenticationFilter by adding the XML to your application context security like so:

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

Th Authentication Filter use the *oidcServerConfigs* property, a map of OIDC servers, an *accountChooserURI* property to denote the URI of the Account Chooser, and an *accountChooserClient* property to identify the Client to the Account Chooser UI application like so:
	
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

[OpenID Connect Standard]: http://openid.net/specs/openid-connect-standard-1_0.html "OpenID Connect Standard 1.0"
[OpenID Connect Standard]: http://openid.net/specs/openid-connect-standard-1_0.html#code_flow "Authorization Code Flow, OpenID Connect Standard"
[Issuer Identifier]: http://openid.net/specs/openid-connect-messages-1_0.html#issuer_identifier "Issuer Identifier"
[Issue #39]: http://github.com/jricher/OpenID-Connect-Java-Spring-Server/issues/39 "Issue #39 -- Multiple Point Client"