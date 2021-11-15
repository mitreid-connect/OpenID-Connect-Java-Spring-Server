package cz.muni.ics.oidc.server.filters;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.AUTHORIZE_REQ_PATTERN;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Abstract class for Perun filters. All filters called in CallPerunFiltersFilter has to extend this.
 *
 * Configuration of filter names:
 * <ul>
 *     <li><b>filter.names</b> - comma separated list of names of the request filters</li>
 * </ul>
 *
 * Configuration of filter (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].class</b> - Class the filter instantiates</li>
 *     <li><b>filter.[name].subs</b> - comma separated list of sub values for which execution of filter will be skipped
 *         if user's SUB is in the list</li>
 *     <li><b>filter.[name].clientIds</b> - comma separated list of client_id values for which execution of filter
 *         will be skipped if client_id is in the list</li>
 * </ul>
 *
 * @see cz.muni.ics.oidc.server.filters.impl package for specific filters and their configuration
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public abstract class PerunRequestFilter {

    private static final String DELIMITER = ",";
    private static final String CLIENT_IDS = "clientIds";
    private static final String SUBS = "subs";

    private static final RequestMatcher requestMatcher = new AntPathRequestMatcher(AUTHORIZE_REQ_PATTERN);

    private final String filterName;
    private Set<String> clientIds = new HashSet<>();
    private Set<String> subs = new HashSet<>();

    public PerunRequestFilter(PerunRequestFilterParams params) {
        filterName = params.getFilterName();

        if (params.hasProperty(CLIENT_IDS)) {
            this.clientIds = new HashSet<>(Arrays.asList(params.getProperty(CLIENT_IDS).split(DELIMITER)));
        }

        if (params.hasProperty(SUBS)) {
            this.subs = new HashSet<>(Arrays.asList(params.getProperty(SUBS).split(DELIMITER)));
        }

        log.debug("{} - filter initialized", filterName);
        log.debug("{} - skip execution for users with SUB in: {}", filterName, subs);
        log.debug("{} - skip execution for clients with CLIENT_ID in: {}", filterName, clientIds);
    }

    /**
     * In this method is done whole logic of filer
     *
     * @param request request
     * @param response response
     * @return boolean if filter was successfully done
     * @throws IOException this exception could be thrown because of failed or interrupted I/O operation
     */
    protected abstract boolean process(ServletRequest request, ServletResponse response, FilterParams params)
            throws IOException;

    public boolean doFilter(ServletRequest req, ServletResponse res, FilterParams params) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        // skip everything that's not an authorize URL
        if (!requestMatcher.matches(request)) {
            log.debug("{} - filter has been skipped, did not match '/authorize' the request", filterName);
            return true;
        }
        if (!skip(request)) {
            log.trace("{} - executing filter", filterName);
            return this.process(req, res, params);
        } else {
            return true;
        }
    }

    private boolean skip(HttpServletRequest request) {
        String sub = (request.getUserPrincipal() != null) ? request.getUserPrincipal().getName() : null;
        String clientId = request.getParameter(PerunFilterConstants.PARAM_CLIENT_ID);

        if (sub != null && subs.contains(sub)) {
            log.debug("{} - skip filter execution: matched one of the ignored SUBS ({})", filterName, sub);
            return true;
        } else if (clientId != null && clientIds.contains(clientId)){
            log.debug("{} - skip filter execution: matched one of the ignored CLIENT_IDS ({})", filterName, clientId);
            return true;
        }

        return false;
    }

}
