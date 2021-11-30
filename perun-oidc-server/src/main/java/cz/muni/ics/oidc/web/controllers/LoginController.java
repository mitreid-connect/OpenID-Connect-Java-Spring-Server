package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class LoginController {

	public static final String MAPPING_SUCCESS = "/login_success";
	public static final String MAPPING_FAILURE = "/login_failure";

	private final WebHtmlClasses htmlClasses;
	private final PerunOidcConfig perunOidcConfig;

	@Autowired
	public LoginController(PerunOidcConfig perunOidcConfig, WebHtmlClasses htmlClasses) {
		this.perunOidcConfig = perunOidcConfig;
		this.htmlClasses = htmlClasses;
	}

	@RequestMapping(value = MAPPING_SUCCESS)
	public String loginSuccess(HttpServletRequest req, Map<String, Object> model) {
		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);
		return "login_success";
	}

	@RequestMapping(value = MAPPING_FAILURE)
	public String loginFailure(HttpServletRequest req, Map<String, Object> model) {
		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);
		return "login_failure";
	}

}
