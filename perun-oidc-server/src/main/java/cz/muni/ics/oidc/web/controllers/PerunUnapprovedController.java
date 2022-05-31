package cz.muni.ics.oidc.web.controllers;

import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.PARAM_CLIENT_ID;
import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.PARAM_HEADER;
import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.PARAM_MESSAGE;
import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.PARAM_TARGET;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.BUTTON_TRANSLATION;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.CONTACT_TRANSLATION;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.DEFAULT_BUTTON_TRANSLATION_KEY;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.DEFAULT_CONTACT_TRANSLATION_KEY;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.DEFAULT_HEADER_TRANSLATION_KEY;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.DEFAULT_TEXT_TRANSLATION_KEY;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.HEADER_TRANSLATION;
import static cz.muni.ics.oidc.server.filters.impl.IsEligibleFilter.TEXT_TRANSLATION;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * Ctonroller for the unapproved page.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Controller
@Slf4j
public class PerunUnapprovedController {

    public static final String UNAPPROVED_MAPPING = "/unapproved";
    public static final String UNAPPROVED_SPECIFIC_MAPPING = "/unapproved_spec";
    public static final String UNAPPROVED_IS_ELIGIBLE_MAPPING = "/unapprovedNotEligible";
    public static final String UNAPPROVED_ENSURE_VO_MAPPING = "/unapprovedEnsureVo";
    public static final String UNAPPROVED_AUTHORIZATION = "/unapprovedAuthorization";
    public static final String UNAPPROVED_NOT_IN_TEST_VOS_GROUPS = "/unapprovedNotInTestVosGroups";
    public static final String UNAPPROVED_NOT_IN_PROD_VOS_GROUPS = "/unapprovedNotInProdVosGroups";
    public static final String UNAPPROVED_NOT_IN_MANDATORY_VOS_GROUPS = "/unapprovedNotInMandatoryVosGroups";
    public static final String UNAPPROVED_NOT_LOGGED_IN = "/unapprovedNotLoggedIn";

    private static final String OUT_HEADER = "outHeader";
    private static final String OUT_MESSAGE = "outMessage";
    private static final String OUT_BUTTON = "outButton";
    private static final String OUT_CONTACT_P = "outContactP";

    private static final String ENSURE_VO_HDR = "403_ensure_vo_hdr";
    private static final String ENSURE_VO_MSG = "403_ensure_vo_msg";

    private static final String AUTHORIZATION_HDR = "403_authorization_hdr";
    private static final String AUTHORIZATION_MSG = "403_authorization_msg";

    private static final String NOT_IN_TEST_VOS_GROUPS_HDR = "403_not_in_test_vos_groups_hdr";
    private static final String NOT_IN_TEST_VOS_GROUPS_MSG = "403_not_in_test_vos_groups_msg";

    private static final String NOT_IN_PROD_VOS_GROUPS_HDR = "403_not_in_prod_vos_groups_hdr";
    private static final String NOT_IN_PROD_VOS_GROUPS_MSG = "403_not_in_prod_vos_groups_msg";

    private static final String NOT_IN_MANDATORY_VOS_GROUPS_HDR = "403_not_in_mandatory_vos_groups_hdr";
    private static final String NOT_IN_MANDATORY_VOS_GROUPS_MSG = "403_not_in_mandatory_vos_groups_msg";

    private static final String NOT_LOGGED_IN_HDR = "403_not_logged_in_hdr";
    private static final String NOT_LOGGED_IN_MSG = "403_not_logged_in_msg";

    private static final String CONTACT_LANG_PROP_KEY = "contact_p";
    private static final String CONTACT_MAIL = "contactMail";
    private static final String HAS_TARGET = "hasTarget";
    private static final String REASON = "reason";

    public static final String TARGET = "target";

    @Autowired
    private ClientDetailsEntityService clientService;

    @Autowired
    private PerunOidcConfig perunOidcConfig;

    @Autowired
    private WebHtmlClasses htmlClasses;

    @Autowired
    private SecurityContextLogoutHandler logoutHandler;

    @GetMapping(value = UNAPPROVED_MAPPING)
    public String showUnapproved(HttpServletRequest req,
                                 Map<String, Object> model,
                                 @RequestParam(PARAM_CLIENT_ID) String clientId)
    {
        ClientDetailsEntity client;

        try {
            client = clientService.loadClientByClientId(clientId);
        } catch (OAuth2Exception e) {
            log.error("showUnapproved: OAuth2Exception was thrown when attempting to load client", e);
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        } catch (IllegalArgumentException e) {
            log.error("showUnapproved: IllegalArgumentException was thrown when attempting to load client", e);
            model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
            return HttpCodeView.VIEWNAME;
        }

        if (client == null) {
            log.error("showUnapproved: could not find client " + clientId);
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        }

        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);
        model.put("client", client);

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved";
        }
        return "unapproved";
    }

    @GetMapping(value = UNAPPROVED_SPECIFIC_MAPPING)
    public String showUnapprovedSpec(HttpServletRequest req, Map<String, Object> model,
                                     @RequestParam(value = PARAM_HEADER, required = false) String header,
                                     @RequestParam(value = PARAM_MESSAGE, required = false) String message)
    {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        model.put(OUT_HEADER, header);
        model.put(OUT_MESSAGE, message);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_spec";
        }
        return "unapproved_spec";
    }

    @GetMapping(value = UNAPPROVED_IS_ELIGIBLE_MAPPING)
    public String showUnapprovedIsEligible(HttpServletRequest req,
                                                 Map<String, Object> model,
                                                 @RequestParam(value = PARAM_TARGET, required = false) String target)
    {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        HttpSession sess = req.getSession();
        String header = loadSessionTranslationKey(sess, HEADER_TRANSLATION, DEFAULT_HEADER_TRANSLATION_KEY);
        String message = loadSessionTranslationKey(sess, TEXT_TRANSLATION, DEFAULT_TEXT_TRANSLATION_KEY);
        String button = loadSessionTranslationKey(sess, BUTTON_TRANSLATION, DEFAULT_BUTTON_TRANSLATION_KEY);
        String contactP = loadSessionTranslationKey(sess, CONTACT_TRANSLATION, DEFAULT_CONTACT_TRANSLATION_KEY);

        model.put(OUT_HEADER, header);
        model.put(OUT_MESSAGE, message);
        model.put(OUT_BUTTON, button);
        model.put(OUT_CONTACT_P, contactP);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());
        model.put(HAS_TARGET, StringUtils.hasText(target));
        req.getSession(true).setAttribute(TARGET, target);

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_is_eligible";
        }
        return "unapproved_is_eligible";
    }

    @PostMapping(value = UNAPPROVED_IS_ELIGIBLE_MAPPING)
    public String showUnapprovedIsEligibleHandle(HttpServletRequest req,
                                                       HttpServletResponse res,
                                                       Map<String, Object> model,
                                                       @SessionAttribute(PARAM_TARGET) String target)
    {
        if (!StringUtils.hasText(target)) {
            return showUnapprovedIsEligible(req, model, null);
        } else {
            logoutHandler.logout(req, res, null);
            return "redirect:" + target;
        }
    }

    @GetMapping(value = UNAPPROVED_ENSURE_VO_MAPPING)
    public String showUnapprovedEnsureVo(HttpServletRequest req, Map<String, Object> model) {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        model.put(OUT_HEADER, ENSURE_VO_HDR);
        model.put(OUT_MESSAGE, ENSURE_VO_MSG);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        return "unapproved_spec";
    }

    @GetMapping(value = UNAPPROVED_AUTHORIZATION)
    public String showUnapprovedAuthorization(HttpServletRequest req, Map<String, Object> model) {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        model.put(OUT_HEADER, AUTHORIZATION_HDR);
        model.put(OUT_MESSAGE, AUTHORIZATION_MSG);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_spec";
        }
        return "unapproved_spec";
    }

    @GetMapping(value = UNAPPROVED_NOT_IN_TEST_VOS_GROUPS)
    public String showUnapprovedNotInTestVosGroups(HttpServletRequest req, Map<String, Object> model) {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        model.put(OUT_HEADER, NOT_IN_TEST_VOS_GROUPS_HDR);
        model.put(OUT_MESSAGE, NOT_IN_TEST_VOS_GROUPS_MSG);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_spec";
        }
        return "unapproved_spec";
    }

    @GetMapping(value = UNAPPROVED_NOT_IN_PROD_VOS_GROUPS)
    public String showUnapprovedNotInProdVosGroups(HttpServletRequest req, Map<String, Object> model) {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        model.put(OUT_HEADER, NOT_IN_PROD_VOS_GROUPS_HDR);
        model.put(OUT_MESSAGE, NOT_IN_PROD_VOS_GROUPS_MSG);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_spec";
        }
        return "unapproved_spec";
    }

    @GetMapping(value = UNAPPROVED_NOT_IN_MANDATORY_VOS_GROUPS)
    public String showUnapprovedNotInMandatoryVosGroups(HttpServletRequest req, Map<String, Object> model) {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        model.put(OUT_HEADER, NOT_IN_MANDATORY_VOS_GROUPS_HDR);
        model.put(OUT_MESSAGE, NOT_IN_MANDATORY_VOS_GROUPS_MSG);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_spec";
        }
        return "unapproved_spec";
    }

    @GetMapping(value = UNAPPROVED_NOT_LOGGED_IN)
    public String showUnapprovedNotLoggedIn(HttpServletRequest req, Map<String, Object> model) {
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        model.put(OUT_HEADER, NOT_LOGGED_IN_HDR);
        model.put(OUT_MESSAGE, NOT_LOGGED_IN_MSG);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_spec";
        }
        return "unapproved_spec";
    }


    private String loadSessionTranslationKey(HttpSession sess, String key, String fallbackValue) {
        if (sess != null && StringUtils.hasText((String) sess.getAttribute(key))) {
            return (String) sess.getAttribute(key);
        }
        return fallbackValue;
    }

}
