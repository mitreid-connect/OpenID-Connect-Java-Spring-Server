package cz.muni.ics.oidc.server;

import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * A custom scheduler for tasks with usage of ShedLock.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "30s")
@Slf4j
public class CustomTaskScheduler {

	private static final long ONE_MINUTE = 60000L;

	private final CustomClearTasks customClearTasks;
	private final DataSource dataSource;

	@Autowired
	public CustomTaskScheduler(CustomClearTasks customClearTasks,
							   @Qualifier("dataSource") DataSource dataSource)
	{
		this.customClearTasks = customClearTasks;
		this.dataSource = dataSource;
	}

	@Bean
	public LockProvider lockProvider() {
		return new JdbcTemplateLockProvider(this.dataSource);
	}

	@Transactional(value = "defaultTransactionManager")
	@Scheduled(fixedDelay = 60 * ONE_MINUTE, initialDelay = ONE_MINUTE)
	@SchedulerLock(name = "clearExpiredSites", lockAtMostFor = "3590s", lockAtLeastFor = "3590s")
	public void clearExpiredSites() {
		try {
			LockAssert.assertLocked();
		} catch (IllegalArgumentException e) {
			return;
		}
		long start = System.currentTimeMillis();
		int count = this.customClearTasks.clearExpiredSites(TimeUnit.MINUTES.toMillis(15));
		long execution = System.currentTimeMillis() - start;
		log.info("clearExpiredSites took {}ms, deleted {} records", execution, count);
	}

	@Transactional(value = "defaultTransactionManager")
	@Scheduled(fixedDelay = 60 * ONE_MINUTE, initialDelay = 12 * ONE_MINUTE)
	@SchedulerLock(name = "clearExpiredTokens", lockAtMostFor = "3590s", lockAtLeastFor = "3590s")
	public void clearExpiredTokens() {
		try {
			LockAssert.assertLocked();
		} catch (IllegalArgumentException e) {
			return;
		}
		long start = System.currentTimeMillis();
		int count = this.customClearTasks.clearExpiredTokens(TimeUnit.MINUTES.toMillis(15));
		long execution = System.currentTimeMillis() - start;
		log.info("clearExpiredTokens took {}ms, deleted {} records", execution, count);
	}

	@Transactional(value = "defaultTransactionManager")
	@Scheduled(fixedDelay = 60 * ONE_MINUTE, initialDelay = 24 * ONE_MINUTE)
	@SchedulerLock(name = "clearExpiredAuthorizationCodes", lockAtMostFor = "3590s", lockAtLeastFor = "3590s")
	public void clearExpiredAuthorizationCodes() {
		try {
			LockAssert.assertLocked();
		} catch (IllegalArgumentException e) {
			return;
		}
		long start = System.currentTimeMillis();
		int count = this.customClearTasks.clearExpiredAuthorizationCodes(TimeUnit.MINUTES.toMillis(15));
		long execution = System.currentTimeMillis() - start;
		log.info("clearExpiredAuthorizationCodes took {}ms, deleted {} records", execution, count);
	}

	@Transactional(value = "defaultTransactionManager")
	@Scheduled(fixedDelay = 60 * ONE_MINUTE, initialDelay = 36 * ONE_MINUTE)
	@SchedulerLock(name = "clearExpiredDeviceCodes", lockAtMostFor = "3590s", lockAtLeastFor = "3590s")
	public void clearExpiredDeviceCodes() {
		try {
			LockAssert.assertLocked();
		} catch (IllegalArgumentException e) {
			return;
		}
		long start = System.currentTimeMillis();
		int count = this.customClearTasks.clearExpiredDeviceCodes(TimeUnit.MINUTES.toMillis(15));
		long execution = System.currentTimeMillis() - start;
		log.info("clearExpiredDeviceCodes took {}ms, deleted {} records", execution, count);
	}

	@Transactional(value = "defaultTransactionManager")
	@Scheduled(fixedDelay = 60 * ONE_MINUTE, initialDelay = 48 * ONE_MINUTE)
	@SchedulerLock(name = "clearExpiredAcrs", lockAtMostFor = "3590s", lockAtLeastFor = "3590s")
	public void clearExpiredAcrs() {
		try {
			LockAssert.assertLocked();
		} catch (IllegalArgumentException e) {
			return;
		}
		long start = System.currentTimeMillis();
		int count = this.customClearTasks.clearExpiredAcrs(TimeUnit.MINUTES.toMillis(15));
		long execution = System.currentTimeMillis() - start;
		log.info("clearExpiredAcrs took {}ms, deleted {} records", execution, count);
	}

	@Transactional(value = "defaultTransactionManager")
	@Scheduled(fixedDelay = 60 * ONE_MINUTE, initialDelay = 48 * ONE_MINUTE)
	@SchedulerLock(name = "clearExpiredDeviceAcrs", lockAtMostFor = "3590s", lockAtLeastFor = "3590s")
	public void clearExpiredDeviceAcrs() {
		try {
			LockAssert.assertLocked();
		} catch (IllegalArgumentException e) {
			return;
		}
		long start = System.currentTimeMillis();
		int count = this.customClearTasks.clearExpiredAcrs(TimeUnit.MINUTES.toMillis(15));
		long execution = System.currentTimeMillis() - start;
		log.info("clearExpiredDeviceAcrs took {}ms, deleted {} records", execution, count);
	}

}
