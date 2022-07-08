package cz.muni.ics.oidc.server.filters;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class holding parameters for AuthProcFilter instantiation.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@AllArgsConstructor
public class AuthProcFilterInitContext {

	public static final String PROP_CLASS = "class";

	private final String filterName;
	private final String filterPropertyPrefix;
	private final Properties properties;
	private final BeanUtil beanUtil;

	public boolean hasProperty(String name) {
		return this.properties.containsKey(filterPropertyPrefix + '.' + name);
	}

	public String getProperty(String name) {
		return this.properties.getProperty(filterPropertyPrefix + '.' + name);
	}

	public String getProperty(String name, String defaultValue) {
		if (this.properties.containsKey(filterPropertyPrefix + '.' + name)) {
			return this.properties.getProperty(filterPropertyPrefix + '.' + name);
		}
		return defaultValue;
	}

	public String getFilterClass() {
		return (String) properties.getOrDefault(filterPropertyPrefix + '.' + PROP_CLASS, null);
	}

	public PerunAdapter getPerunAdapterBean() {
		return beanUtil.getBean(PerunAdapter.class);
	}

	public PerunOidcConfig getPerunOidcConfigBean() {
		return beanUtil.getBean(PerunOidcConfig.class);
	}

}
