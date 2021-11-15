package cz.muni.ics.oidc.server.filters;

import cz.muni.ics.oidc.BeanUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Class that contains all custom Perun request filters. Filters are stored in the LinkedList
 * and executed in the order they are added to the list.
 *
 * Filters are configured from configuration file in following way:
 * filter.names=filterName1,filterName2,...
 *
 * @see PerunRequestFilter for configuration of filter
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunFiltersContext {

	private static final String FILTER_NAMES = "filter.names";
	private static final String FILTER_CLASS = ".class";
	private static final String PREFIX = "filter.";

	private List<PerunRequestFilter> filters;
	private Properties properties;
	private BeanUtil beanUtil;

	public PerunFiltersContext(Properties properties, BeanUtil beanUtil) {
		this.properties = properties;
		this.beanUtil = beanUtil;
		this.filters = new LinkedList<>();

		String filterNames = properties.getProperty(FILTER_NAMES);
		log.debug("Filters to be initialized '{}'", filterNames);

		log.debug("--------------------------------");
		for (String filterName: filterNames.split(",")) {
			PerunRequestFilter requestFilter = loadFilter(filterName);
			filters.add(requestFilter);
			log.debug("--------------------------------");
		}
	}

	public List<PerunRequestFilter> getFilters() {
		return filters;
	}

	private PerunRequestFilter loadFilter(String filterName) {
		String propPrefix = PerunFiltersContext.PREFIX + filterName;
		String filterClass = properties.getProperty(propPrefix + FILTER_CLASS, null);
		if (!StringUtils.hasText(filterClass)) {
			log.warn("{} - failed to initialized filter: no class has ben configured", filterName);
			return null;
		}
		log.trace("{} - loading class '{}'", filterName, filterClass);

		try {
			Class<?> rawClazz = Class.forName(filterClass);
			if (!PerunRequestFilter.class.isAssignableFrom(rawClazz)) {
				log.warn("{} - failed to initialized filter: class '{}' does not extend PerunRequestFilter",
						filterName, filterClass);
				return null;
			}
			
			@SuppressWarnings("unchecked") Class<PerunRequestFilter> clazz = (Class<PerunRequestFilter>) rawClazz;
			Constructor<PerunRequestFilter> constructor = clazz.getConstructor(PerunRequestFilterParams.class);
			PerunRequestFilterParams params = new PerunRequestFilterParams(filterName, propPrefix, properties, beanUtil);
			return constructor.newInstance(params);
		} catch (ClassNotFoundException e) {
			log.warn("{} - failed to initialize filter: class '{}' was not found", filterName, filterClass);
			log.trace("{} - details:", filterName, e);
			return null;
		} catch (NoSuchMethodException e) {
			log.warn("{} - failed to initialize filter: class '{}' does not have proper constructor",
					filterName, filterClass);
			log.trace("{} - details:", filterName, e);
			return null;
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			log.warn("{} - failed to initialize filter: class '{}' cannot be instantiated", filterName, filterClass);
			log.trace("{} - details:", filterName, e);
			return null;
		}
	}

}
