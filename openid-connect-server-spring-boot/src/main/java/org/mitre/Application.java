package org.mitre;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "org.mitre")
@EnableAutoConfiguration
@EnableConfigurationProperties(Application.OpenIdConnectConfigurationProperties.class)
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
}
