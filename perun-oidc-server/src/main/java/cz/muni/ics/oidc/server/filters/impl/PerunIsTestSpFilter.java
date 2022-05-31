package cz.muni.ics.oidc.server.filters.impl;

import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.PARAM_TARGET;
import static cz.muni.ics.oidc.web.controllers.IsTestSpController.IS_TEST_SP_APPROVED_SESS;

import cz.muni.ics.oidc.exceptions.ConfigurationException;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.filters.AuthProcFilter;
import cz.muni.ics.oidc.server.filters.AuthProcFilterInitContext;
import cz.muni.ics.oidc.server.filters.AuthProcFilterCommonVars;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.controllers.IsTestSpController;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;

/**
 * This filter forwards user to a warning page if the service is in test environment.
 * Otherwise, user can to access the service.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * @see cz.muni.ics.oidc.server.filters.AuthProcFilter (basic configuration options)
 * <ul>
 *     <li><b>filter.[name].isTestSpAttr</b> - mapping to isTestSp attribute</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <500348@mail.muni.cz>
 */
@Slf4j
public class PerunIsTestSpFilter extends AuthProcFilter {

    private static final String IS_TEST_SP_ATTR_NAME = "isTestSpAttr";

    private final String isTestSpAttrName;
    private final PerunAdapter perunAdapter;
    private final PerunOidcConfig config;

    public PerunIsTestSpFilter(AuthProcFilterInitContext ctx) throws ConfigurationException {
        super(ctx);
        this.perunAdapter = ctx.getPerunAdapterBean();
        this.config = ctx.getPerunOidcConfigBean();
        this.isTestSpAttrName = FiltersUtils.fillStringMandatoryProperty(IS_TEST_SP_ATTR_NAME, ctx);
    }

    @Override
    protected boolean process(HttpServletRequest req, HttpServletResponse res, AuthProcFilterCommonVars params) throws IOException {
        Facility facility = params.getFacility();
        if (facility == null || facility.getId() == null) {
            log.debug("{} - skip execution: no facility provided", getFilterName());
            return true;
        } else if (testSpWarningApproved(req)){
            log.debug("{} - skip execution: warning already approved", getFilterName());
            return true;
        }

        PerunAttributeValue attrValue = perunAdapter.getFacilityAttributeValue(facility.getId(), isTestSpAttrName);
        if (attrValue == null) {
            log.debug("{} - skip execution: attribute {} has null value", getFilterName(), isTestSpAttrName);
            return true;
        } else if (attrValue.valueAsBoolean()) {
            log.debug("{} - redirecting user to test SP warning page", getFilterName());
            this.redirect(req, res);
            return false;
        }
        log.debug("{} - service is not testing, let user access it", getFilterName());
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
        String redirectUrl = ControllerUtils.createRedirectUrl(config.getConfigBean().getIssuer(),
                IsTestSpController.MAPPING, params);
        log.debug("{} - redirecting user to testSP warning page: {}", getFilterName(), redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

}
