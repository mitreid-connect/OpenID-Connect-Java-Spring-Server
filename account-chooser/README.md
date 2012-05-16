# Account Choooser UI Application

## Overview

This is Web application created in response to [Issue #39] to permit the Client AuthenticationFilter to speak to multiple OpenID Connect Servers.  The protocol between the Clinent and the Account Chooser UI application is documented the README.md of the openid-connect-client submodule.


## Configuration

Configure AccountChooserController via configuring a AccountChooserConfig bean in the  spring-servlet.xml like so:

	<bean name="AccountChooserConfig" class="org.mitre.account_chooser.AccountChooserConfig">
		<property name="issuers">
			<map>
				<entry key="http://sever.example.com:8080/openid-connect-server">
					<bean class="org.mitre.account_chooser.OIDCServer">
						<property name="name" value="Example Server" />
					</bean>
				</entry>
			</map>
		</property>
		<property name="validClientIds" value="FGWEUIASJK, IUYTTYEV, GFHDSFYD" />
	</bean>
 

The keys must match those found in the OpenIdConnectAuthenticationFilter's configuration like so:

	<bean id="openIdConnectAuthenticationFilter"
		class="org.mitre.openid.connect.client.OpenIdConnectAuthenticationFilter">
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
				. . . 


## Test the Default Configuration
				
To test the default config, deploy to a servlet container, and request:

http://localhost:8080/account-chooser/?redirect_uri=http://www.google.com&client_id=FGWEUIASJK

Click **Submit** or **Cancel**, and you will be Google will open.  Study the URL parameters of each.

[Issue #39]: http://github.com/jricher/OpenID-Connect-Java-Spring-Server/issues/39 "Issue #39 -- Multiple Point Client"