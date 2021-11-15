package cz.muni.ics.oidc.server.filters.impl;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_FORCE_AUTHN;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_REASON;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_SCOPE;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_TARGET;
import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.REASON_EXPIRED;
import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.REASON_NOT_SET;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.PerunFilterConstants;
import cz.muni.ics.oidc.server.filters.PerunRequestFilter;
import cz.muni.ics.oidc.server.filters.PerunRequestFilterParams;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedController;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;

/**
 * This filter verifies that user attribute isCesnetEligible is not older than given time frame.
 * In case the value is older, denies access to the service and forces user to use verified identity.
 * Otherwise, user can to access the service.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].isCesnetEligibleAttr</b> - mapping to isCesnetEligible attribute</li>
 *     <li><b>filter.[name].validityPeriod</b> - specify in months, how long the value can be old, if no value
 *         or invalid value has been provided, defaults to 12 months</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunIsCesnetEligibleFilter extends PerunRequestFilter {

    /* CONFIGURATION PROPERTIES */
    private static final String IS_CESNET_ELIGIBLE_ATTR_NAME = "isCesnetEligibleAttr";
    private static final String IS_CESNET_ELIGIBLE_SCOPE = "isCesnetEligibleScope";
    private static final String VALIDITY_PERIOD = "validityPeriod";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final String isCesnetEligibleAttrName;
    private final String triggerScope;
    private final int validityPeriod;
    /* END OF CONFIGURATION PROPERTIES */

    private final PerunAdapter perunAdapter;
    private final String filterName;

    public PerunIsCesnetEligibleFilter(PerunRequestFilterParams params) {
        super(params);
        BeanUtil beanUtil = params.getBeanUtil();
        this.perunAdapter = beanUtil.getBean(PerunAdapter.class);
        this.isCesnetEligibleAttrName = params.getProperty(IS_CESNET_ELIGIBLE_ATTR_NAME);
        this.triggerScope = params.getProperty(IS_CESNET_ELIGIBLE_SCOPE);
        int validityPeriodParam = 12;
        if (params.hasProperty(VALIDITY_PERIOD)) {
            try {
                validityPeriodParam = Integer.parseInt(params.getProperty(VALIDITY_PERIOD));
            } catch (NumberFormatException ignored) {
                //no problem, we have default value
            }
        }

        this.validityPeriod = validityPeriodParam;
        this.filterName = params.getFilterName();
    }

    @Override
    protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!FiltersUtils.isScopePresent(request.getParameter(PARAM_SCOPE), triggerScope)) {
            log.debug("{} - skip execution: scope '{}' is not present in request", filterName, triggerScope);
            return true;
        }

        PerunUser user = params.getUser();
        if (user == null || user.getId() == null) {
            log.debug("{} - skip execution: no user provider", filterName);
            return true;
        }

        String reason = REASON_NOT_SET;
        PerunAttributeValue attrValue = perunAdapter.getUserAttributeValue(user.getId(), isCesnetEligibleAttrName);
        if (attrValue != null) {
            LocalDateTime timeStamp;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
                timeStamp = LocalDateTime.parse(attrValue.valueAsString(), formatter);
            } catch (DateTimeParseException e) {
                log.warn("{} - could not parse timestamp from attribute '{}' value: '{}'",
                        filterName, isCesnetEligibleAttrName, attrValue.valueAsString());
                log.debug("{} - skip execution: no timestamp to compare to", filterName);
                log.trace("{} - details:", filterName, e);
                return true;
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.minusMonths(validityPeriod).isBefore(timeStamp)) {
                log.debug("{} - attribute '{}' value is valid", filterName, isCesnetEligibleAttrName);
                return true;
            } else {
                reason = REASON_EXPIRED;
            }
        }

        log.debug("{} - attribute '{}' value is invalid, stop user at this point", filterName, attrValue);
        this.redirect(request, response, reason);
        return false;
    }

    private void redirect(HttpServletRequest req, HttpServletResponse res, String reason) {
        Map<String, String> params = new HashMap<>();

        String targetURL = FiltersUtils.buildRequestURL(req, Collections.singletonMap(PARAM_FORCE_AUTHN, "true"));
        params.put(PARAM_TARGET, targetURL);
        params.put(PARAM_REASON, reason);

        String redirectUrl = ControllerUtils.createRedirectUrl(req, PerunFilterConstants.AUTHORIZE_REQ_PATTERN,
                PerunUnapprovedController.UNAPPROVED_IS_CESNET_ELIGIBLE_MAPPING, params);
        log.debug("{} - redirecting user to unapproved: URL '{}'", filterName, redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

}
