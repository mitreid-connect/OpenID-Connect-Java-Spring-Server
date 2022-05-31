package cz.muni.ics.oidc.server.filters;

import cz.muni.ics.oidc.exceptions.ConfigurationException;
import cz.muni.ics.oidc.saml.SamlProperties;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract class for Perun AuthProc filters. All filters defined and called in the
 * {@link cz.muni.ics.oidc.server.filters.AuthProcFiltersContainer} instance have to extend this base class.
 *
 * Configuration of filter (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].class</b> - Class the filter instantiates</li>
 *     <li><b>filter.[name].skip_for_users</b> - comma separated list of users for whom the execution of the filter
 *     will be skipped if the users' SUB matches any value in the list</li>
 *     <li><b>filter.[name].skip_for_clients</b> - comma separated list of clients for which the execution of the filter
 *     will be skipped if the CLIENT_ID matches any value in the list</li>
 *     <li><b>filter.[name].execute_for_users</b> - comma separated list of users for whom the filter will be executed
 *     if the users' SUB matches any value in the list</li>
 *     <li><b>filter.[name].execute_for_clients</b> - comma separated list of clients for whom the filter will be executed
 *     if the CLIENT_ID matches any value in the list</li>
 * </ul>
 * <i>NOTE: if none of the SKIP/EXECUTE conditions is specified (or the lists are empty), filter is run for all users
 * and all clients</i>
 *
 * @see cz.muni.ics.oidc.server.filters.impl package for specific filters and their configuration
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
@Getter
public abstract class AuthProcFilter {

    public static final String APPLIED = "APPLIED_";

    private static final String DELIMITER = ",";
    private static final String EXECUTE = "execute";
    private static final String EXECUTE_FOR_CLIENTS = "execute_for_clients";
    private static final String EXECUTE_FOR_USERS = "execute_for_users";
    private static final String SKIP_FOR_CLIENTS = "skip_for_clients";
    private static final String SKIP_FOR_USERS = "skip_for_users";
    private static final String SUBS = "subs";
    private static final String CLIENT_IDS = "clientIds";

    private final String filterName;
    private final Set<String> executeForClients = new HashSet<>();
    private final Set<String> executeForUsers = new HashSet<>();
    private final Set<String> skipForClients = new HashSet<>();
    private final Set<String> skipForUsers = new HashSet<>();

    private final SamlProperties samlProperties;

    public AuthProcFilter(AuthProcFilterInitContext ctx) throws ConfigurationException {
        filterName = ctx.getFilterName();
        this.samlProperties = ctx.getBeanUtil().getBean(SamlProperties.class);
        initializeExecutionRulesLists(ctx);

        if (!Collections.disjoint(executeForClients, skipForClients)) {
            throw new ConfigurationException("Filter '" + filterName + "' is configured to be run and skipped for the same client");
        } else if (!Collections.disjoint(executeForUsers, skipForUsers)) {
            throw new ConfigurationException("Filter '" + filterName + "' is configured to be run and skipped for the same user");
        }

        log.info("{} - filter initialized", filterName);
        if (!skipForUsers.isEmpty()) {
            log.info("{} - skip execution for users with SUB in: '{}'", filterName, skipForUsers);
        }
        if (!skipForClients.isEmpty()) {
            log.info("{} - skip execution for clients with CLIENT_ID in: '{}'", filterName, skipForClients);
        }
        if (!executeForUsers.isEmpty()) {
            log.info("{} - execute for users with SUB in: '{}'", filterName, executeForUsers);
        }
        if (!executeForClients.isEmpty()) {
            log.info("{} - execute for clients with CLIENT_ID in: '{}'", filterName, executeForClients);
        }
    }

    protected String getSessionAppliedParamName() {
        return APPLIED + getClass().getSimpleName() + '_' + getFilterName();
    }

    /**
     * In this method is done whole logic of filer
     *
     * @param request request
     * @param response response
     * @return boolean if filter was successfully done
     * @throws IOException this exception could be thrown because of failed or interrupted I/O operation
     */
    protected abstract boolean process(HttpServletRequest request, HttpServletResponse response, AuthProcFilterCommonVars params)
            throws IOException;

    public boolean doFilter(HttpServletRequest req, HttpServletResponse res, AuthProcFilterCommonVars params) throws IOException {
        if (!skip(req)) {
            log.trace("{} - executing filter", filterName);
            return process(req, res, params);
        } else {
            return true;
        }
    }

    private boolean skip(HttpServletRequest req) {
        if (hasBeenApplied(req.getSession(true))) {
            return true;
        }
        log.debug("{} - marking filter as applied", filterName);
        req.getSession(true).setAttribute(getSessionAppliedParamName(), true);
        String sub = FiltersUtils.getUserIdentifier(req, samlProperties.getUserIdentifierAttribute());
        String clientId = FiltersUtils.getClientId(req);

        boolean explicitExecution = executeForSub(sub) || executeForClientId(clientId);
        boolean explicitSkip = skipForClientId(clientId) || skipForSub(sub);
        return !explicitExecution && explicitSkip;
    }

    private boolean hasBeenApplied(HttpSession sess) {
        String sessionParamName = getSessionAppliedParamName();
        if (sess.getAttribute(sessionParamName) != null) {
            log.debug("{} - skip filter execution: filter has been already applied", filterName);
            return true;
        }
        return false;
    }

    private boolean executeForSub(String sub) {
        return checkRule(sub, executeForUsers, "{} - execute filter: matched one of the explicit SUBS ({})");
    }

    private boolean executeForClientId(String clientId) {
        return checkRule(clientId, executeForClients, "{} - execute filter: matched one of the explicit CLIENT_IDS ({})");
    }

    private boolean skipForSub(String sub) {
        return checkRule(sub, skipForUsers, "{} - skip filter execution: matched one of the ignored SUBS ({})");
    }

    private boolean skipForClientId(String clientId) {
        return checkRule(clientId, skipForClients, "{} - skip filter execution: matched one of the ignored CLIENT_IDS ({})");
    }

    private boolean checkRule(String param, Set<String> ruleSet, String logMsg) {
        if (param != null && ruleSet.contains(param)){
            log.debug(logMsg, filterName, param);
            return true;
        }
        return false;
    }

    private void initializeExecutionRulesLists(AuthProcFilterInitContext ctx) {
        initializeExecutionRuleList(ctx, EXECUTE_FOR_CLIENTS, executeForClients);
        initializeExecutionRuleList(ctx, SKIP_FOR_CLIENTS, skipForClients);
        initializeExecutionRuleList(ctx, CLIENT_IDS, skipForClients);

        initializeExecutionRuleList(ctx, EXECUTE_FOR_USERS, executeForUsers);
        initializeExecutionRuleList(ctx, SKIP_FOR_USERS, skipForUsers);
        initializeExecutionRuleList(ctx, SUBS, skipForUsers);
    }

    private void initializeExecutionRuleList(AuthProcFilterInitContext ctx, String property, Set<String> list) {
        if (ctx.hasProperty(property)) {
            String value = ctx.getProperty(property, "");
            list.addAll(Arrays.asList(value.split(DELIMITER)));
        }
    }

}
