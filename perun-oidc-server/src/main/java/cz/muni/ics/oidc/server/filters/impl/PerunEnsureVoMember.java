package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oidc.PerunConstants;
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
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedController;
import cz.muni.ics.oidc.web.controllers.RegistrationController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.util.StringUtils;

/**
 * This filter forwards user to a warning page if the service is in test environment.
 * Otherwise, user can to access the service.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * @see cz.muni.ics.oidc.server.filters.AuthProcFilter (basic configuration options)
 * <ul>
 *     <li><b>filter.[name].triggerAttr</b> - mapping to attribute which contains flag if this is enabled for facility</li>
 *     <li><b>filter.[name].voDefsAttr</b> - mapping to attribute which contains VO(s) to check</li>
 *     <li><b>filter.[name].loginURL</b> - mapping to the attribute containing service login URL</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunEnsureVoMember extends AuthProcFilter {

    private static final String TRIGGER_ATTR = "triggerAttr";
    private static final String VO_DEFS_ATTR = "voDefsAttr";
    private static final String LOGIN_URL_ATTR = "loginURL";

    private final String triggerAttr;
    private final String voDefsAttr;
    private final String loginUrlAttr;
    private final PerunAdapter perunAdapter;
    private final PerunOidcConfig perunOidcConfig;

    public PerunEnsureVoMember(AuthProcFilterInitContext ctx) throws ConfigurationException {
        super(ctx);
        this.perunOidcConfig = ctx.getPerunOidcConfigBean();
        this.perunAdapter = ctx.getPerunAdapterBean();

        this.triggerAttr = FiltersUtils.fillStringMandatoryProperty(TRIGGER_ATTR, ctx);
        this.voDefsAttr = FiltersUtils.fillStringMandatoryProperty(VO_DEFS_ATTR, ctx);
        this.loginUrlAttr = FiltersUtils.fillStringPropertyOrDefaultVal(LOGIN_URL_ATTR, ctx, null);
    }

    @Override
    protected boolean process(HttpServletRequest req, HttpServletResponse res, AuthProcFilterCommonVars params) {
        Facility facility = params.getFacility();
        if (facility == null || facility.getId() == null) {
            log.debug("{} - skip execution: no facility provided", getFilterName());
            return true;
        }

        List<String> attrsToFetch = Arrays.asList(voDefsAttr, triggerAttr, loginUrlAttr);
        Map<String, PerunAttributeValue> attrs = perunAdapter.getFacilityAttributeValues(facility, attrsToFetch);

        if (attrs == null) {
            log.debug("{} - skip filter execution: could not fetch attributes '{}' for facility '{}'",
                    getFilterName(), attrsToFetch, facility);
            return true;
        }

        PerunAttributeValue triggerAttrValue = attrs.getOrDefault(triggerAttr, null);
        if (triggerAttrValue == null || !triggerAttrValue.valueAsBoolean()) {
            log.debug("{} - skip execution: attribute '{}' is null or false, which disables the filter",
                    getFilterName(), triggerAttr);
            return true;
        }

        PerunAttributeValue voDefsAttrValue = getVoDefsAttrValue(attrs.getOrDefault(voDefsAttr, null));
        if (voDefsAttrValue == null) {
            log.debug("{} - skip execution: attribute '{}' has null or no value", getFilterName(), voDefsAttr);
            return true;
        }
        String voShortName = voDefsAttrValue.valueAsString();

        boolean canAccess = perunAdapter.isUserInVo(params.getUser().getId(), voShortName);

        if (canAccess) {
            log.debug("{} - user allowed to continue", getFilterName());
            return true;
        } else {
            redirect(res, getLoginUrl(facility.getId()), voShortName);
            return false;
        }
    }

    private void redirect(HttpServletResponse response, PerunAttributeValue loginUrlAttr, String voShortName) {
        String loginUrl = null;
        if (loginUrlAttr != null && StringUtils.hasText(loginUrlAttr.valueAsString())) {
            loginUrl = loginUrlAttr.valueAsString();
        }
        if (StringUtils.hasText(voShortName) && perunAdapter.getAdapterRpc().hasApplicationForm(voShortName)) {
            redirectDirectly(response, loginUrl, voShortName);
        } else {
            redirectUnapproved(response);
        }
    }

    private PerunAttributeValue getLoginUrl(Long facilityId) {
        if (loginUrlAttr != null) {
            return perunAdapter.getFacilityAttributeValue(facilityId, loginUrlAttr);
        }
        return null;
    }

    private PerunAttributeValue getVoDefsAttrValue(PerunAttributeValue attrValue) {
        if (attrValue == null) {
            return null;
        } else if (attrValue.valueAsJson().isArray() && attrValue.valueAsJson().size() < 1) {
            return null;
        }
        return attrValue;
    }

    @Override
    public String toString() {
        return "PerunEnsureVoMember{" +
                "voDefsAttr='" + voDefsAttr + '\'' +
                ", loginUrlAttr='" + loginUrlAttr + '\'' +
                '}';
    }

    private void redirectDirectly(HttpServletResponse res, String loginUrl, String voShortName) {
        String registrarUrl = perunOidcConfig.getRegistrarUrl();
        Map<String, String> params = new HashMap<>();
        params.put(PerunConstants.REGISTRAR_PARAM_VO, voShortName);
        if (StringUtils.hasText(loginUrl)) {
            params.put(PerunConstants.REGISTRAR_TARGET_NEW, loginUrl);
            params.put(PerunConstants.REGISTRAR_TARGET_EXISTING, loginUrl);
            params.put(PerunConstants.REGISTRAR_TARGET_EXTENDED, loginUrl);
        }
        String target = ControllerUtils.createUrl(registrarUrl, params);

        String url = ControllerUtils.constructRequestUrl(perunOidcConfig, RegistrationController.CONTINUE_DIRECT_MAPPING);
        params.clear();
        params.put(RegistrationController.PARAM_TARGET, target);

        String redirectUrl = ControllerUtils.createUrl(url, params);
        log.debug("{} - redirecting user to '{}'", getFilterName(), redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

    private void redirectUnapproved(HttpServletResponse res) {
        String redirectUrl = ControllerUtils.constructRequestUrl(perunOidcConfig,
                PerunUnapprovedController.UNAPPROVED_ENSURE_VO_MAPPING);

        log.debug("{} - redirecting user to '{}'", getFilterName(), redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

}
