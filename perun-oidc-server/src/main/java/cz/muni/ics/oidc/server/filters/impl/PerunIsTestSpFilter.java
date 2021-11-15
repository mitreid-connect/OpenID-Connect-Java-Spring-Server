package cz.muni.ics.oidc.server.filters.impl;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_TARGET;
import static cz.muni.ics.oidc.web.controllers.IsTestSpController.IS_TEST_SP_APPROVED_SESS;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.PerunFilterConstants;
import cz.muni.ics.oidc.server.filters.PerunRequestFilter;
import cz.muni.ics.oidc.server.filters.PerunRequestFilterParams;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.controllers.IsTestSpController;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;

/**
 * This filter forwards user to a warning page if the service is in test environment.
 * Otherwise, user can to access the service.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].isTestSpAttr</b> - mapping to isCesnetEligible attribute</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <500348@mail.muni.cz>
 */
@Slf4j
public class PerunIsTestSpFilter extends PerunRequestFilter {

    private static final String IS_TEST_SP_ATTR_NAME = "isTestSpAttr";

    private final String isTestSpAttrName;
    private final PerunAdapter perunAdapter;
    private final String filterName;

    public PerunIsTestSpFilter(PerunRequestFilterParams params) {
        super(params);
        BeanUtil beanUtil = params.getBeanUtil();
        this.perunAdapter = beanUtil.getBean(PerunAdapter.class);
        this.isTestSpAttrName = params.getProperty(IS_TEST_SP_ATTR_NAME);
        this.filterName = params.getFilterName();
    }

    @Override
    protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        Facility facility = params.getFacility();
        if (facility == null || facility.getId() == null) {
            log.debug("{} - skip execution: no facility provided", filterName);
            return true;
        } else if (testSpWarningApproved(request)){
            log.debug("{} - skip execution: warning already approved", filterName);
            return true;
        }

        PerunAttributeValue attrValue = perunAdapter.getFacilityAttributeValue(facility.getId(), isTestSpAttrName);
        if (attrValue == null) {
            log.debug("{} - skip execution: attribute {} has null value", filterName, isTestSpAttrName);
            return true;
        } else if (attrValue.valueAsBoolean()) {
            log.debug("{} - redirecting user to test SP warning page", filterName);
            this.redirect(request, response);
            return false;
        }
        log.debug("{} - service is not testing, let user access it", filterName);
        return true;
    }

    private boolean testSpWarningApproved(HttpServletRequest req) {
        if (req.getSession() == null) {
            return false;
        }
        boolean approved = false;
        if (req.getSession().getAttribute(IS_TEST_SP_APPROVED_SESS) != null) {
            approved = (Boolean) req.getSession().getAttribute(IS_TEST_SP_APPROVED_SESS);
            req.getSession().removeAttribute(IS_TEST_SP_APPROVED_SESS);
        }
        return approved;
    }

    private void redirect(HttpServletRequest req, HttpServletResponse res) {
        String targetURL = FiltersUtils.buildRequestURL(req);

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TARGET, targetURL);
        String redirectUrl = ControllerUtils.createRedirectUrl(req, PerunFilterConstants.AUTHORIZE_REQ_PATTERN,
                IsTestSpController.MAPPING, params);
        log.debug("{} - redirecting user to testSP warning page: {}", filterName, redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

}
