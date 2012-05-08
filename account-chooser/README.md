# Account Choooser UI Application

## Overview

This is Web application created in response to [Issue #39] to permit the Client AuthenticationFilter to speak to multiple OpenID Connect servers.


## Configuration

Configure a bean configuration to the spring-servlet.xml like so:

	<bean class="org.mitre.account_chooser.OIDCServers">
		<property name="servers">
			<map>
				<entry key="1">
					<bean class="org.mitre.account_chooser.OIDCServer">
						<property name="name" value="OIDC Server 1" />
					</bean>
				</entry>
				<entry key="2">
					<bean class="org.mitre.account_chooser.OIDCServer">
						<property name="name" value="OIDC Server 2" />
					</bean>
				</entry>
				<entry key="3">
					<bean class="org.mitre.account_chooser.OIDCServer">
						<property name="name" value="OIDC Server 3" />
					</bean>
				</entry>
			</map>
		</property>
	</bean>


The keys must match those found in the OpenIdConnectAuthenticationFilter's configuration like so:

	<bean id="openIdConnectAuthenticationFilter"
		class="org.mitre.openid.connect.client.OpenIdConnectAuthenticationFilter">
		<property name="OIDCServers">
			<map>
				<entry key="1">
					<property name="authorizationEndpointURI" 
						value="http://sever.example.com:8080/openid-connect-server/openidconnect/auth" />
					<property name="tokenEndpointURI" 
						value="http://sever.example.com:8080/openid-connect-server/checkid" />
					<property name="checkIDEndpointURI" 
						value="http://sever.example.com:8080/openid-connect-server/checkid" />
					<property name="clientId" 
						value="someClientId" /> 
					<property name="clientSecret" value="someClientSecret" />
				</entry>

[Issue #39]: http://github.com/jricher/OpenID-Connect-Java-Spring-Server/issues/39 "Issue #39 -- Multiple Point Client"