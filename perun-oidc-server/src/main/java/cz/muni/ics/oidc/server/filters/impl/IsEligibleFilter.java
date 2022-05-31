package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.exceptions.ConfigurationException;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.filters.AuthProcFilter;
import cz.muni.ics.oidc.server.filters.AuthProcFilterCommonVars;
import cz.muni.ics.oidc.server.filters.AuthProcFilterConstants;
import cz.muni.ics.oidc.server.filters.AuthProcFilterInitContext;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedController;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.util.StringUtils;

/**
 * This filter verifies that user attribute isCesnetEligible is not older than given time frame.
 * In case the value is older, denies access to the service and forces user to use verified identity.
 * Otherwise, user can to access the service.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].samlAttribute</b> - mapping to isCesnetEligible attribute</li>
 *     <li><b>filter.[name].triggerScope</b> - scope that has to be requested to apply this filter</li>
 *     <li><b>filter.[name].validityPeriodMonths</b> - specify in months, how long the value can be old, if no value
 *         or invalid value has been provided, defaults to 12 months</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class IsEligibleFilter extends AuthProcFilter {

    public static final String APPLIED = "APPLIED_" + IsEligibleFilter.class.getSimpleName();

    public final static String DEFAULT_HEADER_TRANSLATION_KEY = "403_is_eligible_default_header_text";
    public final static String DEFAULT_TEXT_TRANSLATION_KEY = "403_is_eligible_default_text";
    public final static String DEFAULT_BUTTON_TRANSLATION_KEY = "403_is_eligible_default_button_text";
    public final static String DEFAULT_CONTACT_TRANSLATION_KEY = "403_is_eligible_default_contact_text";

    public static final String HEADER_TRANSLATION = "header_translation";
    public static final String TEXT_TRANSLATION = "text_translation";
    public static final String BUTTON_TRANSLATION = "button_translation";
    public static final String CONTACT_TRANSLATION = "contact_translation";

    /* CONFIGURATION PROPERTIES */
    private static final String SAML_ATTRIBUTE = "samlAttribute";
    private static final String TRIGGER_SCOPE = "triggerScope";
    private static final String VALIDITY_PERIOD = "validityPeriodMonths";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String OLD_VALUE_HEADER_TRANSLATION_KEY = "old_value_header_translation_key";
    private static final String OLD_VALUE_TEXT_TRANSLATION_KEY = "old_value_text_translation_key";
    private static final String OLD_VALUE_BUTTON_TRANSLATION_KEY = "old_value_button_translation_key";
    private static final String OLD_VALUE_CONTACT_TRANSLATION_KEY = "old_value_contact_translation_key";

    private static final String NO_VALUE_HEADER_TRANSLATION_KEY = "no_value_header_translation_key";
    private static final String NO_VALUE_TEXT_TRANSLATION_KEY = "no_value_text_translation_key";
    private static final String NO_VALUE_BUTTON_TRANSLATION_KEY = "no_value_button_translation_key";
    private static final String NO_VALUE_CONTACT_TRANSLATION_KEY = "no_value_contact_translation_key";

    /* END OF CONFIGURATION PROPERTIES */

    private final String eligibleLastSeenSAMLAttributeName;
    private final String triggerScope;
    private final int validityPeriod;

    private final String oldValueHeaderTranslationKey;
    private final String oldValueTextTranslationKey;
    private final String oldValueButtonTranslationKey;
    private final String oldValueContactTranslationKey;

    private final String noValueHeaderTranslationKey;
    private final String noValueTextTranslationKey;
    private final String noValueButtonTranslationKey;
    private final String noValueContactTranslationKey;

    private final PerunOidcConfig config;
    private final String filterName;

    public IsEligibleFilter(AuthProcFilterInitContext ctx) throws ConfigurationException {
        super(ctx);
        this.filterName = ctx.getFilterName();

        BeanUtil beanUtil = ctx.getBeanUtil();
        this.config = beanUtil.getBean(PerunOidcConfig.class);
        this.eligibleLastSeenSAMLAttributeName = FiltersUtils.fillStringMandatoryProperty(SAML_ATTRIBUTE, filterName, ctx);
        this.triggerScope = FiltersUtils.fillStringMandatoryProperty(TRIGGER_SCOPE, filterName, ctx);
        int validityPeriodParam = 12;
        if (ctx.hasProperty(VALIDITY_PERIOD)) {
            try {
                validityPeriodParam = Integer.parseInt(ctx.getProperty(VALIDITY_PERIOD));
            } catch (NumberFormatException ignored) {
                //no problem, we have default value
            }
        }

        this.oldValueHeaderTranslationKey = FiltersUtils.fillStringProperty(
                OLD_VALUE_HEADER_TRANSLATION_KEY, ctx, DEFAULT_HEADER_TRANSLATION_KEY);
        this.oldValueTextTranslationKey = FiltersUtils.fillStringProperty(
                OLD_VALUE_TEXT_TRANSLATION_KEY, ctx, DEFAULT_TEXT_TRANSLATION_KEY);
        this.oldValueButtonTranslationKey = FiltersUtils.fillStringProperty(
                OLD_VALUE_BUTTON_TRANSLATION_KEY, ctx, DEFAULT_BUTTON_TRANSLATION_KEY);
        this.oldValueContactTranslationKey = FiltersUtils.fillStringProperty(
                OLD_VALUE_CONTACT_TRANSLATION_KEY, ctx, DEFAULT_CONTACT_TRANSLATION_KEY);

        this.noValueHeaderTranslationKey = FiltersUtils.fillStringProperty(
                NO_VALUE_HEADER_TRANSLATION_KEY, ctx, DEFAULT_HEADER_TRANSLATION_KEY);
        this.noValueTextTranslationKey = FiltersUtils.fillStringProperty(
                NO_VALUE_TEXT_TRANSLATION_KEY, ctx, DEFAULT_TEXT_TRANSLATION_KEY);
        this.noValueButtonTranslationKey = FiltersUtils.fillStringProperty(
                NO_VALUE_BUTTON_TRANSLATION_KEY, ctx, DEFAULT_BUTTON_TRANSLATION_KEY);
        this.noValueContactTranslationKey = FiltersUtils.fillStringProperty(
                NO_VALUE_CONTACT_TRANSLATION_KEY, ctx, DEFAULT_CONTACT_TRANSLATION_KEY);

        this.validityPeriod = validityPeriodParam;

    }

    @Override
    protected String getSessionAppliedParamName() {
        return APPLIED + filterName;
    }

    @Override
    protected boolean process(HttpServletRequest req, HttpServletResponse res, AuthProcFilterCommonVars params) {
       if (!FiltersUtils.isScopePresent(req.getParameter(AuthProcFilterConstants.PARAM_SCOPE), triggerScope)) {
            log.debug("{} - skip execution: scope '{}' is not present in request", filterName, triggerScope);
            return true;
        }

        SAMLCredential samlCredential = FiltersUtils.getSamlCredential(req);
        if (samlCredential == null) {
            log.debug("{} - skip execution: no SAML credential to fetch attribute from is available", filterName);
            return true;
        }

        String eligibleLastSeenTimestamp = samlCredential.getAttributeAsString(eligibleLastSeenSAMLAttributeName);

        String headerKey = noValueHeaderTranslationKey;
        String textKey = noValueTextTranslationKey;
        String buttonKey = noValueButtonTranslationKey;
        String contactKey = noValueContactTranslationKey;

        if (StringUtils.hasText(eligibleLastSeenTimestamp)) {
            LocalDateTime timeStamp;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
                timeStamp = LocalDateTime.parse(eligibleLastSeenTimestamp, formatter);
            } catch (DateTimeParseException e) {
                log.warn("{} - could not parse timestamp from attribute '{}' with value '{}'",
                        filterName, eligibleLastSeenSAMLAttributeName, eligibleLastSeenTimestamp);
                log.debug("{} - skip execution - have no timestamp to compare to", filterName);
                log.trace("{} - details:", filterName, e);
                return true;
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.minusMonths(validityPeriod).isBefore(timeStamp)) {
                log.debug("{} - attribute '{}' value is valid", filterName, eligibleLastSeenSAMLAttributeName);
                return true;
            } else {
                headerKey = oldValueHeaderTranslationKey;
                textKey = oldValueTextTranslationKey;
                buttonKey = oldValueButtonTranslationKey;
                contactKey = oldValueContactTranslationKey;
            }
        }

        HttpSession sess = req.getSession(true);
        sess.setAttribute(HEADER_TRANSLATION, headerKey);
        sess.setAttribute(TEXT_TRANSLATION, textKey);
        sess.setAttribute(BUTTON_TRANSLATION, buttonKey);
        sess.setAttribute(CONTACT_TRANSLATION, contactKey);

        log.debug("{} - attribute '{}' value is invalid, stop user at this point", filterName, eligibleLastSeenTimestamp);
        this.redirect(req, res);
        return false;
    }

    private void redirect(HttpServletRequest req, HttpServletResponse res) {
        Map<String, String> params = new HashMap<>();

        String targetURL = FiltersUtils.buildRequestURL(req,
                Collections.singletonMap(AuthProcFilterConstants.PARAM_PROMPT, "login"));
        params.put(AuthProcFilterConstants.PARAM_TARGET, targetURL);

        String redirectUrl = ControllerUtils.createRedirectUrl(config.getConfigBean().getIssuer(),
                PerunUnapprovedController.UNAPPROVED_IS_ELIGIBLE_MAPPING, params);
        log.debug("{} - redirecting user to unapproved: URL '{}'", filterName, redirectUrl);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

}
