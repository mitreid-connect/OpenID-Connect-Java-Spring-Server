package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RegistrationController {

    public static final String PARAM_TARGET = "target";

    public static final String CONTINUE_DIRECT_MAPPING = "/continueDirect";

    @Autowired
    private PerunOidcConfig perunOidcConfig;

    @Autowired
    private WebHtmlClasses htmlClasses;

    @GetMapping(value = CONTINUE_DIRECT_MAPPING, params = { PARAM_TARGET })
    public String showRegistrationForm(HttpServletRequest req, Map<String, Object> model,
                                       @RequestParam(PARAM_TARGET) String target)
    {
        model.put(PARAM_TARGET, target);
        ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);
        if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
            return "lsaai/continue_direct";
        }
        return "continue_direct";
    }

}
