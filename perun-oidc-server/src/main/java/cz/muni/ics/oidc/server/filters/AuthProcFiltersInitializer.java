package cz.muni.ics.oidc.server.filters;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.exceptions.ConfigurationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Initialization class for AuthProcFilters. Takes care of loading the filters and putting them into the custom
 * authentication processing chain.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class AuthProcFiltersInitializer {

    private static final String FILTER_NAMES = "filter.names";
    private static final String FILTERS_PROP_BASE_PREFIX = "filter.";

    public static List<AuthProcFilter> initialize(Properties coreProperties, BeanUtil beanUtil) throws ConfigurationException {
        List<AuthProcFilter> filters = new LinkedList<>();

        String filterNames = coreProperties.getProperty(FILTER_NAMES);
        log.debug("Filters to be initialized '{}'", filterNames);

        log.debug("--------------------------------");
        for (String filterName: filterNames.split(",")) {
            String filterPropertyPrefix = FILTERS_PROP_BASE_PREFIX + filterName;
            AuthProcFilterInitContext ctx = new AuthProcFilterInitContext(filterName, filterPropertyPrefix, coreProperties, beanUtil);
            AuthProcFilter requestFilter = loadFilter(ctx);
            filters.add(requestFilter);
            log.debug("--------------------------------");
        }
        return filters;
    }

    private static AuthProcFilter loadFilter(AuthProcFilterInitContext ctx) throws ConfigurationException {
        String filterClass = ctx.getFilterClass();
        if (!StringUtils.hasText(filterClass)) {
            log.warn("{} - failed to initialized filter: no class has ben configured", ctx.getFilterName());
            throw new ConfigurationException("Failed to initialize filter '" + ctx.getFilterName() + "' ("  + filterClass + ")");
        }
        log.debug("{} - loading class '{}'", ctx.getFilterName(), filterClass);

        try {
            Class<?> rawClazz = Class.forName(filterClass);
            if (!AuthProcFilter.class.isAssignableFrom(rawClazz)) {
                log.warn("{} - failed to initialized filter: class '{}' does not extend AuthProcFilter",
                        ctx.getFilterName(), filterClass);
                throw new ConfigurationException("Failed to initialize filter '" + ctx.getFilterName() + "' ("  + filterClass + ")");
            }

            @SuppressWarnings("unchecked") Class<AuthProcFilter> clazz = (Class<AuthProcFilter>) rawClazz;
            Constructor<AuthProcFilter> constructor = clazz.getConstructor(AuthProcFilterInitContext.class);
            AuthProcFilter filter = constructor.newInstance(ctx);
            log.debug("Initialized AuthProcFilter - {}", filter);
            return filter;
        } catch (ClassNotFoundException e) {
            log.warn("{} - failed to initialize filter: class '{}' was not found", ctx.getFilterName(), filterClass);
            log.debug("{} - details:", ctx.getFilterName(), e);
            throw new ConfigurationException("Failed to initialize filter '" + ctx.getFilterName() + "' ("  + filterClass + ")");
        } catch (NoSuchMethodException e) {
            log.warn("{} - failed to initialize filter: class '{}' does not have proper constructor",
                    ctx.getFilterName(), filterClass);
            log.debug("{} - details:", ctx.getFilterName(), e);
            throw new ConfigurationException("Failed to initialize filter '" + ctx.getFilterName() + "' ("  + filterClass + ")");
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.warn("{} - failed to initialize filter: class '{}' cannot be instantiated", ctx.getFilterName(), filterClass);
            log.debug("{} - details:", ctx.getFilterName(), e);
            throw new ConfigurationException("Failed to initialize filter '" + ctx.getFilterName() + "' ("  + filterClass + ")");
        }
    }

}
