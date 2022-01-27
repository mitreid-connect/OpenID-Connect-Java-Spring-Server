package cz.muni.ics.oidc.server.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

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
public abstract class AuthProcFilter {

    private static final String DELIMITER = ",";
    private static final String CLIENT_IDS = "clientIds";
    private static final String SUBS = "subs";

    private final String filterName;
    private Set<String> clientIds = new HashSet<>();
    private Set<String> subs = new HashSet<>();

    public AuthProcFilter(AuthProcFilterParams params) {
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

    protected abstract String getSessionAppliedParamName();

    /**
     * In this method is done whole logic of filer
     *
     * @param request request
     * @param response response
     * @return boolean if filter was successfully done
     * @throws IOException this exception could be thrown because of failed or interrupted I/O operation
     */
    protected abstract boolean process(HttpServletRequest request, HttpServletResponse response, FilterParams params)
            throws IOException;

    public boolean doFilter(HttpServletRequest req, HttpServletResponse res, FilterParams params) throws IOException {
        if (!skip(req)) {
            log.trace("{} - executing filter", filterName);
            return process(req, res, params);
        } else {
            return true;
        }
    }

    private boolean skip(HttpServletRequest request) {
        if (hasBeenApplied(request.getSession(true))) {
            return true;
        }
        log.debug("{} - marking filter as applied", filterName);
        request.getSession(true).setAttribute(getSessionAppliedParamName(), true);
        return skipForSub(request.getUserPrincipal())
                || skipForClientId(request.getParameter(PerunFilterConstants.PARAM_CLIENT_ID));
    }

    private boolean hasBeenApplied(HttpSession sess) {
        String sessionParamName = getSessionAppliedParamName();
        if (sess.getAttribute(sessionParamName) != null) {
            log.debug("{} - skip filter execution: filter has been already applied", filterName);
            return true;
        }
        return false;
    }

    private boolean skipForSub(Principal p) {
        String sub = (p != null) ? p.getName() : null;
        if (sub != null && subs.contains(sub)) {
            log.debug("{} - skip filter execution: matched one of the ignored SUBS ({})", filterName, sub);
            return true;
        }
        return false;
    }

    private boolean skipForClientId(String clientId) {
        if (clientId != null && clientIds.contains(clientId)){
            log.debug("{} - skip filter execution: matched one of the ignored CLIENT_IDS ({})", filterName, clientId);
            return true;
        }
        return false;
    }

}
