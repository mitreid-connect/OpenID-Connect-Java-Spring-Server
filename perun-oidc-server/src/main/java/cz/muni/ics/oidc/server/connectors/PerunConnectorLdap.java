package cz.muni.ics.oidc.server.connectors;

import com.google.common.base.Strings;
import cz.muni.ics.oidc.aop.LogTimes;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.NoVerificationTrustManager;
import org.apache.directory.ldap.client.api.search.FilterBuilder;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.springframework.beans.factory.DisposableBean;

/**
 * Connector for calling Perun LDAP
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunConnectorLdap implements DisposableBean {

	private final String baseDN;
	private final LdapConnectionPool pool;
	private final LdapConnectionTemplate ldap;

	public PerunConnectorLdap(String ldapHost, String ldapUser, String ldapPassword, int port, boolean useTLS,
							  boolean useSSL, boolean allowUntrustedSsl, long timeoutSecs, String baseDN) {
		if (ldapHost == null || ldapHost.trim().isEmpty()) {
			throw new IllegalArgumentException("Host cannot be null or empty");
		} else if (baseDN == null || baseDN.trim().isEmpty()) {
			throw new IllegalArgumentException("baseDN cannot be null or empty");
		}

		this.baseDN = baseDN;
		LdapConnectionConfig config = getConfig(ldapHost, port, useTLS, useSSL, allowUntrustedSsl);
		if (ldapUser != null && !ldapUser.isEmpty()) {
			log.debug("setting ldap user to {}", ldapUser);
			config.setName(ldapUser);
		}
		if (ldapPassword != null && !ldapPassword.isEmpty()) {
			log.debug("setting ldap password");
			config.setCredentials(ldapPassword);
		}
		DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(config);
		factory.setTimeOut(timeoutSecs * 1000L);

		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setTestOnBorrow(true);

		pool = new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(factory), poolConfig);
		ldap = new LdapConnectionTemplate(pool);
		log.debug("initialized LDAP connector");
	}

	public String getBaseDN() {
		return baseDN;
	}

	private LdapConnectionConfig getConfig(String host, int port, boolean useTLS, boolean useSSL,
										   boolean allowUntrustedSsl) {
		LdapConnectionConfig config = new LdapConnectionConfig();
		config.setLdapHost(host);
		config.setLdapPort(port);
		config.setUseSsl(useSSL);
		config.setUseTls(useTLS);
		if (allowUntrustedSsl) {
			config.setTrustManagers(new NoVerificationTrustManager());
		}

		return config;
	}

	@Override
	public void destroy() {
		if (!pool.isClosed()) {
			pool.close();
		}
	}

	/**
	 * Search for the first entry that satisfies criteria.
	 * @param dnPrefix Prefix to be added to the base DN. (i.e. ou=People) !DO NOT END WITH A COMMA!
	 * @param filter Filter for entries
	 * @param scope Search scope
	 * @param attributes Attributes to be fetch for entry
	 * @param entryMapper Mapper of entries to the target class T
	 * @param <T> Class that the result should be mapped to.
	 * @return Found entry mapped to target class
	 */
	@LogTimes
	public <T> T searchFirst(String dnPrefix, FilterBuilder filter, SearchScope scope, String[] attributes,
							 EntryMapper<T> entryMapper)
	{
		Dn fullDn = getFullDn(dnPrefix);
		return ldap.searchFirst(fullDn, filter, scope, attributes, entryMapper);
	}

	/**
	 * Perform lookup for the entry that satisfies criteria.
	 * @param dnPrefix Prefix to be added to the base DN. (i.e. ou=People) !DO NOT END WITH A COMMA!
	 * @param attributes Attributes to be fetch for entry
	 * @param entryMapper Mapper of entries to the target class T
	 * @param <T> Class that the result should be mapped to.
	 * @return Found entry mapped to target class
	 */
	@LogTimes
	public <T> T lookup(String dnPrefix, String[] attributes, EntryMapper<T> entryMapper) {
		Dn fullDn = getFullDn(dnPrefix);
		return ldap.lookup(fullDn, attributes, entryMapper);
	}

	/**
	 * Search for the entries satisfy criteria.
	 * @param dnPrefix Prefix to be added to the base DN. (i.e. ou=People) !DO NOT END WITH A COMMA!
	 * @param filter Filter for entries
	 * @param scope Search scope
	 * @param attributes Attributes to be fetch for entry
	 * @param entryMapper Mapper of entries to the target class T
	 * @param <T> Class that the result should be mapped to.
	 * @return List of found entries mapped to target class
	 */
	@LogTimes
	public <T> List<T> search(String dnPrefix, FilterBuilder filter, SearchScope scope, String[] attributes,
							  EntryMapper<T> entryMapper)
	{
		Dn fullDn = getFullDn(dnPrefix);
		return ldap.search(fullDn, filter, scope, attributes, entryMapper);
	}

	private Dn getFullDn(String prefix) {
		String dn = baseDN;
		if (!Strings.isNullOrEmpty(prefix)) {
			dn = prefix + "," + baseDN;
		}

		return ldap.newDn(dn);
	}

}
