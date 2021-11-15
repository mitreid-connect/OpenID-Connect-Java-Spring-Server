package cz.muni.ics.oidc.web.controllers;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_ACCEPTED;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_TARGET;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *  Controller for IS TEST SP pages.
 *
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
@Controller
@Slf4j
public class IsTestSpController {

    public static final String MAPPING = "/testRpWarning";
    public static final String IS_TEST_SP_APPROVED_SESS = "isTestSpApprovedSession";
    private static final String TARGET = "target";
    private static final String ACTION = "action";

    private final Localization localization;
    private final WebHtmlClasses htmlClasses;
    private final PerunOidcConfig perunOidcConfig;

    @Autowired
    public IsTestSpController(Localization localization, WebHtmlClasses htmlClasses, PerunOidcConfig perunOidcConfig) {
        this.localization = localization;
        this.htmlClasses = htmlClasses;
        this.perunOidcConfig = perunOidcConfig;
    }

    @GetMapping(value = MAPPING, params = PARAM_TARGET)
    public String isTestSpWarning(HttpServletRequest req,
                                  Map<String, Object> model,
                                  @RequestParam(PARAM_TARGET) String returnUrl)
    {
        log.debug("Display warning page for isTestSp");
        model.put(TARGET, returnUrl);
        model.put(ACTION, req.getRequestURL().toString());
        ControllerUtils.setPageOptions(model, req, localization, htmlClasses, perunOidcConfig);
        return "isTestSpWarning";
    }

    @GetMapping(value = MAPPING, params = {PARAM_TARGET, PARAM_ACCEPTED})
    public String warningApproved(HttpServletRequest request,
                                  @RequestParam(PARAM_TARGET) String target)
    {
        log.debug("Warning approved, set session attribute and redirect to {}", target);
        HttpSession sess = request.getSession();
        if (sess != null) {
            sess.setAttribute(IS_TEST_SP_APPROVED_SESS, true);
        }
        return "redirect:" + target;
    }

}
