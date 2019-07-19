package org.mitre.openid.connect;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

	@RequestMapping("/errorController")
	public String handle(HttpServletRequest req) {
		Throwable errorException = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		String message = (String) req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String requestUri = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

		logger.error("request {} failed with {}", requestUri, message);
		logger.error("exception", errorException);

		return "/error";
	}
}
