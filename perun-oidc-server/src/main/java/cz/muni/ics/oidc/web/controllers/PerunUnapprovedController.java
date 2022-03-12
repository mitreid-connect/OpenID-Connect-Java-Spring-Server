package cz.muni.ics.oidc.web.controllers;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_CLIENT_ID;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_HEADER;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_MESSAGE;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_REASON;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_TARGET;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public static final String UNAPPROVED_IS_CESNET_ELIGIBLE_MAPPING = "/unapprovedIce";
    public static final String UNAPPROVED_ENSURE_VO_MAPPING = "/unapprovedEnsureVo";
    public static final String UNAPPROVED_AUTHORIZATION = "/unapprovedAuthorization";
    public static final String UNAPPROVED_NOT_IN_TEST_VOS_GROUPS = "/unapprovedNotInTestVosGroups";
    public static final String UNAPPROVED_NOT_IN_PROD_VOS_GROUPS = "/unapprovedNotInProdVosGroups";
    public static final String UNAPPROVED_NOT_IN_MANDATORY_VOS_GROUPS = "/unapprovedNotInMandatoryVosGroups";
    public static final String UNAPPROVED_NOT_LOGGED_IN = "/unapprovedNotLoggedIn";

    public static final String REASON_NOT_SET = "notSet";
    public static final String REASON_EXPIRED = "expired";

    private static final String OUT_HEADER = "outHeader";
    private static final String OUT_MESSAGE = "outMessage";
    private static final String OUT_CONTACT_P = "outContactP";

    private static final String ENSURE_VO_HDR = "403_ensure_vo_hdr";
    private static final String ENSURE_VO_MSG = "403_ensure_vo_msg";

    private static final String AUTHORIZATION_HDR = "403_authorization_hdr";
    private static final String AUTHORIZATION_MSG = "403_authorization_msg";

    private static final String ICE_NOT_SET_HDR = "403_isCesnetEligible_notSet_hdr";
    private static final String ICE_NOT_SET_MSG = "403_isCesnetEligible_notSet_msg";
    private static final String ICE_EXPIRED_HDR = "403_isCesnetEligible_expired_hdr";
    private static final String ICE_EXPIRED_MSG = "403_isCesnetEligible_expired_msg";

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

    @Autowired
    private ClientDetailsEntityService clientService;

    @Autowired
    private PerunOidcConfig perunOidcConfig;

    @Autowired
    private WebHtmlClasses htmlClasses;

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

    @GetMapping(value = UNAPPROVED_IS_CESNET_ELIGIBLE_MAPPING)
    public String showUnapprovedIsCesnetEligible(HttpServletRequest req, Map<String, Object> model,
                                                 @RequestParam(value = PARAM_TARGET) String target,
                                                 @RequestParam(value = PARAM_REASON) String reason) {

        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);

        String header;
        String message;

        if (REASON_EXPIRED.equals(reason)) {
            header = ICE_EXPIRED_HDR;
            message = ICE_EXPIRED_MSG;
        } else if (REASON_NOT_SET.equals(reason)){
            header = ICE_NOT_SET_HDR;
            message = ICE_NOT_SET_MSG;
        } else {
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        }

        model.put(OUT_HEADER, header);
        model.put(OUT_MESSAGE, message);
        model.put(OUT_CONTACT_P, CONTACT_LANG_PROP_KEY);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/unapproved_spec";
        }
        return "unapproved_spec";
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

}
