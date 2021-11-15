package cz.muni.ics.oidc.server.filters;

import cz.muni.ics.oidc.BeanUtil;
import java.util.Properties;

/**
 * Class holding parameters for filter instantiation
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class PerunRequestFilterParams {

	private final String filterName;

	private final String propertyPrefix;
	private final Properties properties;
	private final BeanUtil beanUtil;

	public PerunRequestFilterParams(String filterName, String propertyPrefix, Properties properties, BeanUtil beanUtil) {
		this.filterName = filterName;
		this.propertyPrefix = propertyPrefix;
		this.properties = properties;
		this.beanUtil = beanUtil;
	}

	public boolean hasProperty(String name) {
		return this.properties.containsKey(propertyPrefix + '.' + name);
	}

	public String getProperty(String name) {
		return this.properties.getProperty(propertyPrefix + '.' + name);
	}

	public BeanUtil getBeanUtil() {
		return beanUtil;
	}

	public String getFilterName() {
		return filterName;
	}

	public Properties getProperties() {
		return properties;
	}
}
