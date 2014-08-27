package org.mitre;

import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties(Application.OpenIdConnectConfigurationProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @ConfigurationProperties(prefix = "openid.connect.server")
	public static class OpenIdConnectConfigurationProperties extends ConfigurationPropertiesBean {
		
	}
}
