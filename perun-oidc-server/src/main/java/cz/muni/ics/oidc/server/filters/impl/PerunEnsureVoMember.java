package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.AuthProcFilter;
import cz.muni.ics.oidc.server.filters.AuthProcFilterParams;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedController;
import cz.muni.ics.oidc.web.controllers.RegistrationController;
import java.util.Arrays;
import java.util.HashMap;
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
 * <ul>
 *     <li><b>filter.[name].triggerAttr</b> - mapping to attribute which contains flag if this is enabled for facility</li>
 *     <li><b>filter.[name].voDefsAttr</b> - mapping to attribute which contains VO(s) to check</li>
 *     <li><b>filter.[name].loginURL</b> - mapping to the attribute containing service login URL</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunEnsureVoMember extends AuthProcFilter {

    public static final String APPLIED = "APPLIED_" + PerunEnsureVoMember.class.getSimpleName();

    private static final String TRIGGER_ATTR = "triggerAttr";
    private static final String VO_DEFS_ATTR = "voDefsAttr";
    private static final String LOGIN_URL_ATTR = "loginURL";

    private final String triggerAttr;
    private final String voDefsAttr;
    private final String loginUrlAttr;
    private final PerunAdapter perunAdapter;
    private final String filterName;
    private final PerunOidcConfig perunOidcConfig;

    public PerunEnsureVoMember(AuthProcFilterParams params) {
        super(params);
        BeanUtil beanUtil = params.getBeanUtil();

        this.perunOidcConfig = beanUtil.getBean(PerunOidcConfig.class);
        this.perunAdapter = beanUtil.getBean(PerunAdapter.class);
        this.filterName = params.getFilterName();

        this.triggerAttr = FiltersUtils.fillStringMandatoryProperty(TRIGGER_ATTR, filterName, params);
        this.voDefsAttr = FiltersUtils.fillStringMandatoryProperty(VO_DEFS_ATTR, filterName, params);

        this.loginUrlAttr = params.getProperty(LOGIN_URL_ATTR);
        log.debug("{} - initialized filter: {}", filterName, this);
    }

    @Override
    protected String getSessionAppliedParamName() {
        return APPLIED;
    }

    @Override
    protected boolean process(HttpServletRequest req, HttpServletResponse res, FilterParams params) {
        Facility facility = params.getFacility();
        if (facility == null || facility.getId() == null) {
            log.debug("{} - skip execution: no facility provided", filterName);
            return true;
        }

        Map<String, PerunAttributeValue> attrs = perunAdapter.getFacilityAttributeValues(facility,
                Arrays.asList(voDefsAttr, triggerAttr, loginUrlAttr));

        PerunAttributeValue triggerAttrValue = attrs.getOrDefault(triggerAttr, null);
        if (triggerAttrValue == null || !triggerAttrValue.valueAsBoolean()) {
            log.debug("{} - skip execution: attribute '{}' is null or false, which disables the filter",
                    filterName, triggerAttr);
            return true;
        }

        PerunAttributeValue voDefsAttrValue = getVoDefsAttrValue(attrs.getOrDefault(voDefsAttr, null));
        if (voDefsAttrValue == null) {
            log.debug("{} - skip execution: attribute '{}' has null or no value", filterName, voDefsAttr);
            return true;
        }
        String voShortName = voDefsAttrValue.valueAsString();

        boolean canAccess = perunAdapter.isUserInVo(params.getUser().getId(), voShortName);

        if (canAccess) {
            log.debug("{} - user allowed to continue", filterName);
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
        params.put("vo", voShortName);
        if (StringUtils.hasText(loginUrl)) {
            params.put("targetnew", loginUrl);
            params.put("targetexisting", loginUrl);
        }
        String target = ControllerUtils.createUrl(registrarUrl, params);

        String url = ControllerUtils.constructRequestUrl(perunOidcConfig, RegistrationController.CONTINUE_DIRECT_MAPPING);
        params.clear();
        params.put(RegistrationController.PARAM_TARGET, target);

        String redirectUrl = ControllerUtils.createUrl(url, params);
        log.debug("{} - redirecting user to '{}'", filterName, redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

    private void redirectUnapproved(HttpServletResponse res) {
        String redirectUrl = ControllerUtils.constructRequestUrl(perunOidcConfig,
                PerunUnapprovedController.UNAPPROVED_ENSURE_VO_MAPPING);

        log.debug("{} - redirecting user to '{}'", filterName, redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

}
