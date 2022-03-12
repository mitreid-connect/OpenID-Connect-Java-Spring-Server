package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.SAMLStatusException;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class LoginController {

	public static final String MAPPING_SUCCESS = "/login_success";
	public static final String MAPPING_FAILURE = "/login_failure";

	public static final String KEY_ERROR_MSG = "error_msg";
	public static final String ATTR_EXCEPTION = "exception_in_auth";

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
		if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
			return "lsaai/login_success";
		}
		return "login_success";
	}

	@RequestMapping(value = MAPPING_FAILURE)
	public String loginFailure(HttpServletRequest req, Map<String, Object> model) {
		Throwable object = (Throwable) req.getAttribute(ATTR_EXCEPTION);
		if (object != null) {
			Throwable t = object;
			SAMLStatusException exc = null;
			while (t != null) {
				if (t instanceof SAMLStatusException) {
					exc = (SAMLStatusException) t;
					break;
				}
				t = t.getCause();
			}
			if (exc != null) {
				String code = exc.getStatusCode();
				if (StatusCode.NO_AUTHN_CONTEXT_URI.equalsIgnoreCase(code)) {
					model.put(KEY_ERROR_MSG, "login_failure.no_authn_context.msg");
				}
			}
		}

		ControllerUtils.setPageOptions(model, req, htmlClasses, perunOidcConfig);
		if (perunOidcConfig.getTheme().equalsIgnoreCase("lsaai")) {
			return "lsaai/login_failure";
		}
		return "login_failure";
	}

}
