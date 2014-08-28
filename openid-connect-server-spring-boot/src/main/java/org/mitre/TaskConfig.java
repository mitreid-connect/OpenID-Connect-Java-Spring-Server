package org.mitre;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.mitre.oauth2.service.impl.DefaultOAuth2ProviderTokenService;
import org.mitre.openid.connect.service.ApprovedSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class TaskConfig implements SchedulingConfigurer {
	@Autowired
    private DefaultOAuth2ProviderTokenService defaultOAuth2ProviderTokenService;
	
	@Autowired
    private ApprovedSiteService approvedSiteService;
	
	@Bean(destroyMethod="shutdown")
	public Executor taskScheduler() {
		return Executors.newScheduledThreadPool(5); // TODO make configureable
	}
	
	// TODO what's exaclty the difference between, taskScheduler and taskExecutor?
	
	@Scheduled(fixedDelay = 30000, initialDelay = 60000)
	public void clearExpiredTokens() {
		defaultOAuth2ProviderTokenService.clearExpiredTokens();
	}
	
	@Scheduled(fixedDelay = 30000, initialDelay = 60000)
	public void clearExpiredSites() {
		approvedSiteService.clearExpiredSites();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler());
	}
}