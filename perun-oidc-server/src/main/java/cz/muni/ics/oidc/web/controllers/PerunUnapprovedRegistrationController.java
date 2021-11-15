package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.Vo;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.FacilityAttrsConfig;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the unapproved page which offers registration.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Controller
@Slf4j
public class PerunUnapprovedRegistrationController {

    public static final String REGISTRATION_FORM_MAPPING = "/regForm";
    public static final String REGISTRATION_FORM_SUBMIT_MAPPING = "/regForm/submit";
    public static final String REGISTRATION_CONTINUE_MAPPING = "/regForm/continue";

    @Autowired
    private ClientDetailsEntityService clientService;

    @Autowired
    private PerunAdapter perunAdapter;

    @Autowired
    private FacilityAttrsConfig facilityAttrsConfig;

    @Autowired
    private PerunOidcConfig perunOidcConfig;

    @Autowired
    private Localization localization;

    @Autowired
    private WebHtmlClasses htmlClasses;

    @GetMapping(value = REGISTRATION_FORM_MAPPING)
    public String showRegistrationForm(Map<String, Object> model, ServletRequest req, ServletResponse res,
                                       @RequestParam("client_id") String clientId,
                                       @RequestParam("facility_id") Long facilityId,
                                       @RequestParam("user_id") Long userId) {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        ClientDetailsEntity client;

        try {
            client = clientService.loadClientByClientId(clientId);
        } catch (OAuth2Exception e) {
            log.error("confirmAccess: OAuth2Exception was thrown when attempting to load client", e);
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        } catch (IllegalArgumentException e) {
            log.error("confirmAccess: IllegalArgumentException was thrown when attempting to load client", e);
            model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
            return HttpCodeView.VIEWNAME;
        }

        if (client == null) {
            log.error("confirmAccess: could not find client {}", clientId);
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        }

        Facility facility = perunAdapter.getFacilityByClientId(clientId);
        Map<String, PerunAttributeValue> facilityAttributes = perunAdapter.getFacilityAttributeValues(facility,
                facilityAttrsConfig.getMembershipAttrNames());
        List<String> voShortNames = facilityAttributes.get(facilityAttrsConfig.getVoShortNamesAttr()).valueAsList();
        Map<Vo, List<Group>> groupsForRegistration = perunAdapter.getAdapterRpc()
                .getGroupsForRegistration(facility, userId, voShortNames);
        log.debug("groupsForReg: {}", groupsForRegistration);

        if (groupsForRegistration.isEmpty()) {
            String redirectUrl = ControllerUtils.createRedirectUrl(request, REGISTRATION_FORM_MAPPING,
                    PerunUnapprovedController.UNAPPROVED_MAPPING, Collections.singletonMap("client_id", clientId));
            response.reset();
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", redirectUrl);
            return null;
        } else if (groupsForRegistration.keySet().size() == 1) {
            for (Map.Entry<Vo, List<Group>> entry: groupsForRegistration.entrySet()) {
                // no other way how to extract the first item (as it is the only)
                List<Group> groupList = groupsForRegistration.get(entry.getKey());
                if (groupList.size() == 1) {
                    Group group = groupList.get(0);
                    String redirectUrl = createRegistrarUrl(entry.getKey(), group.getName());
                    response.reset();
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", redirectUrl);
                    return null;
                }
            }
        }

        ControllerUtils.setPageOptions(model, request, localization, htmlClasses, perunOidcConfig);
        model.put("client", client);
        model.put("facilityId", facilityId);
        model.put("action", buildActionUrl(request));
        model.put("groupsForRegistration", groupsForRegistration);
        model.put("page", "regForm");

        return "registrationForm";
    }

    @GetMapping(value = REGISTRATION_FORM_SUBMIT_MAPPING)
    public void processRegistrationForm(@RequestParam("selectedGroup") String groupName,
                                        @RequestParam("selectedVo") String voName,
                                        ServletResponse res) throws IOException {
        HttpServletResponse request = (HttpServletResponse) res;

        groupName = groupName.split(":", 2)[1];

        String redirectUrl = createRegistrarUrl(voName, groupName);

        request.sendRedirect(redirectUrl);
    }

    @GetMapping(value = REGISTRATION_CONTINUE_MAPPING)
    public String showContinuePage(Map<String, Object> model, ServletRequest req,
                                   @RequestParam("client_id") String clientId,
                                   @RequestParam("facility_id") Long facilityId,
                                   @RequestParam("user_id") Long userId) {
        HttpServletRequest request = (HttpServletRequest) req;

        model.put("page", "regContinue");
        model.put("client_id", clientId);
        model.put("facility_id", facilityId);
        model.put("user_id", userId);
        model.put("action", request.getRequestURL().toString()
                .replace(REGISTRATION_CONTINUE_MAPPING, REGISTRATION_FORM_MAPPING));
        ControllerUtils.setPageOptions(model, request, localization, htmlClasses, perunOidcConfig);

        return "registrationFormContinue";
    }

    private String createRegistrarUrl(Vo vo, String groupName) {
        return createRegistrarUrl(vo.getShortName(), groupName);
    }

    private String createRegistrarUrl(String vohortName, String groupName) {
        String redirectUrl = perunOidcConfig.getRegistrarUrl().concat("?vo=").concat(vohortName);
        if (groupName != null && !groupName.isEmpty() && !groupName.equalsIgnoreCase("members")) {
            redirectUrl = redirectUrl.concat("&group=").concat(groupName);
        }

        return redirectUrl;
    }

    private String buildActionUrl(HttpServletRequest request) {
        int startIndex = request.getRequestURL().lastIndexOf(REGISTRATION_FORM_MAPPING);
        int length = request.getRequestURL().length();
        return request.getRequestURL().delete(startIndex, length)
                .append(REGISTRATION_FORM_SUBMIT_MAPPING).toString();
    }

}
