package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.oauth2.web.DeviceEndpoint;
import cz.muni.ics.oidc.server.PerunDeviceCodeAcrRepository;
import cz.muni.ics.oidc.server.PerunScopeClaimTranslationService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.filters.PerunFilterConstants;
import cz.muni.ics.oidc.server.userInfo.PerunUserInfo;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import cz.muni.ics.openid.connect.models.DeviceCodeAcr;
import cz.muni.ics.openid.connect.service.UserInfoService;
import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApproveDeviceController {

    public static final String DEVICE = "device";
    public static final String APPROVE_DEVICE = "approveDevice";
    public static final String DEVICE_APPROVED = "deviceApproved";
    public static final String REQUEST_USER_CODE = "requestUserCode";
    public static final String USER_CODE = "user_code";
    public static final String DEVICE_CODE = "device_code";
    public static final String USER_OAUTH_APPROVAL = "user_oauth_approval";
    public static final String URL = "devicecode";
    public static final String VERIFICATION_URI = "verification_uri";
    public static final String VERIFICATION_URI_COMPLETE = "verification_uri_complete";
    public static final String ACR_VALUES = "acr_values";
    public static final String ENTITY = "entity";
    public static final String CLIENT_ID = "client_id";
    public static final String SCOPE = "scope";
    public static final String ACR = "acr";

    private final SystemScopeService scopeService;
    private final DeviceEndpoint deviceEndpoint;
    private final PerunOidcConfig perunOidcConfig;
    private final Localization localization;
    private final WebHtmlClasses htmlClasses;
    private final PerunScopeClaimTranslationService scopeClaimTranslationService;
    private final UserInfoService userInfoService;
    private final PerunDeviceCodeAcrRepository deviceCodeAcrRepository;

    @Autowired
    public ApproveDeviceController(SystemScopeService scopeService,
                                   DeviceEndpoint deviceEndpoint,
                                   PerunOidcConfig perunOidcConfig,
                                   Localization localization,
                                   WebHtmlClasses htmlClasses,
                                   PerunScopeClaimTranslationService scopeClaimTranslationService,
                                   UserInfoService userInfoService,
                                   PerunDeviceCodeAcrRepository perunDeviceCodeAcrRepository)
    {
        this.scopeService = scopeService;
        this.deviceEndpoint = deviceEndpoint;
        this.perunOidcConfig = perunOidcConfig;
        this.localization = localization;
        this.htmlClasses = htmlClasses;
        this.scopeClaimTranslationService = scopeClaimTranslationService;
        this.userInfoService = userInfoService;
        this.deviceCodeAcrRepository = perunDeviceCodeAcrRepository;
    }

    @RequestMapping(
            value = {"/" + URL},
            method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE},
            params = {CLIENT_ID, ACR_VALUES}
    )
    public String requestDeviceCodeMFA(@RequestParam(CLIENT_ID) String clientId, @RequestParam(name = SCOPE, required = false) String scope,
                                       @RequestParam(name = ACR_VALUES) String acrValues, Map<String, String> parameters, ModelMap model)
    {
        String result = deviceEndpoint.requestDeviceCode(clientId, scope, parameters, model);

        Map<String, Object> response = (Map<String, Object>) model.get(ENTITY);
        response.replace(VERIFICATION_URI, response.get(VERIFICATION_URI) + "?" + ACR_VALUES + "=" + acrValues);
        response.replace(VERIFICATION_URI_COMPLETE, response.get(VERIFICATION_URI_COMPLETE) + "&" + ACR_VALUES + "=" + acrValues);
        storeAcrBase((String) response.get(DEVICE_CODE), (String)response.get(USER_CODE));

        return result;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEVICE,
            consumes = {"text/html", "application/xhtml+xml","application/xml;q=0.9","image/webp","*/*;q=0.8"})
    public String requestUserCode(@RequestParam(value = USER_CODE, required = false) String userCode,
                                  @ModelAttribute("authorizationRequest") AuthorizationRequest authRequest,
                                  Principal p,
                                  HttpServletRequest req,
                                  ModelMap model,
                                  HttpSession session)
    {
        String result = deviceEndpoint.requestUserCode(userCode, model, session);
        if (result.equals(REQUEST_USER_CODE) && !perunOidcConfig.getTheme().equalsIgnoreCase("default")) {
            ControllerUtils.setPageOptions(model, req, localization, htmlClasses, perunOidcConfig);
            model.put("page", REQUEST_USER_CODE);
            String shibAuthnContextClass = "";
            if (StringUtils.hasText(req.getParameter(ACR_VALUES))) {
                shibAuthnContextClass = (String) req.getAttribute(PerunFilterConstants.SHIB_AUTHN_CONTEXT_CLASS);
                if (!StringUtils.hasText(shibAuthnContextClass)) {
                    shibAuthnContextClass = (String) req.getAttribute(PerunFilterConstants.SHIB_AUTHN_CONTEXT_METHOD);
                }
                if (!StringUtils.hasText(shibAuthnContextClass)) {
                    shibAuthnContextClass = "";
                }
            }
            model.put(ACR, shibAuthnContextClass);
            return "themedRequestUserCode";
        } else if (result.equals(APPROVE_DEVICE) && !perunOidcConfig.getTheme().equalsIgnoreCase("default")) {
            return themedApproveDevice(model, p, req);
        }
        return result;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(value = "/" + DEVICE + "/verify",
            consumes = {"text/html", "application/xhtml+xml","application/xml;q=0.9","image/webp","*/*;q=0.8"})
    public String readUserCode(@RequestParam(USER_CODE) String userCode,
                               @ModelAttribute("authorizationRequest") AuthorizationRequest authRequest,
                               Principal p,
                               HttpServletRequest req,
                               ModelMap model,
                               HttpSession session)
    {
        String result = deviceEndpoint.readUserCode(userCode, model, session);
        if (result.equals(APPROVE_DEVICE) && !perunOidcConfig.getTheme().equalsIgnoreCase("default")) {
            if (StringUtils.hasText(req.getParameter(ACR))) {
                storeAcr(req.getParameter(ACR), userCode);
            }

            return themedApproveDevice(model, p, req);
        } else if (result.equals(REQUEST_USER_CODE) && !perunOidcConfig.getTheme().equalsIgnoreCase("default")) {
            ControllerUtils.setPageOptions(model, req, localization, htmlClasses, perunOidcConfig);
            model.put("page", REQUEST_USER_CODE);
            return "themedRequestUserCode";
        }

        return result;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(value = "/" + DEVICE + "/approve", params = {USER_CODE, USER_OAUTH_APPROVAL})
    public String approveDevice(@RequestParam(USER_CODE) String userCode,
                                @RequestParam(USER_OAUTH_APPROVAL) Boolean approve,
                                @ModelAttribute(USER_OAUTH_APPROVAL) AuthorizationRequest authRequest,
                                Principal p,
                                HttpServletRequest req,
                                ModelMap model,
                                Authentication auth,
                                HttpSession session)
    {
        String result = deviceEndpoint.approveDevice(userCode, approve, model, auth, session);
        if (result.equals(DEVICE_APPROVED) && !perunOidcConfig.getTheme().equalsIgnoreCase("default")) {
            model.remove("scopes");

            DeviceCode dc = (DeviceCode)session.getAttribute("deviceCode");
            ClientDetailsEntity client = (ClientDetailsEntity) model.get("client");
            PerunUserInfo user = (PerunUserInfo) userInfoService.getByUsernameAndClientId(
                    p.getName(), client.getClientId());

            ControllerUtils.setScopesAndClaims(scopeService, scopeClaimTranslationService, model, dc.getScope(), user);
            ControllerUtils.setPageOptions(model, req, localization, htmlClasses, perunOidcConfig);

            model.put("page", DEVICE_APPROVED);
            return "themedDeviceApproved";
        }

        return result;
    }

    private void storeAcr(String acrValue, String userCode) {
        DeviceCodeAcr acr = deviceCodeAcrRepository.getByUserCode(userCode);
        acr.setShibAuthnContextClass(acrValue);
        long expiresAtEpoch = Instant.now().plusSeconds(600L).toEpochMilli();
        acr.setExpiresAt(expiresAtEpoch);
        deviceCodeAcrRepository.store(acr);
    }

    private String themedApproveDevice(ModelMap model, Principal p, HttpServletRequest req) {
        model.remove("scopes");
        DeviceCode dc = (DeviceCode) model.get("dc");
        ClientDetailsEntity client = (ClientDetailsEntity) model.get("client");
        PerunUserInfo user = (PerunUserInfo) userInfoService.getByUsernameAndClientId(
                p.getName(), client.getClientId());
        ControllerUtils.setScopesAndClaims(scopeService, scopeClaimTranslationService, model, dc.getScope(), user);
        ControllerUtils.setPageOptions(model, req, localization, htmlClasses, perunOidcConfig);

        model.put("page", APPROVE_DEVICE);
        return "themedApproveDevice";
    }

    private void storeAcrBase(String deviceCode, String userCode) {
        DeviceCodeAcr acrBase = new DeviceCodeAcr(deviceCode, userCode);
        acrBase.setExpiresAt(Instant.now().plusSeconds(1800).toEpochMilli());
        deviceCodeAcrRepository.store(acrBase);
    }

}
