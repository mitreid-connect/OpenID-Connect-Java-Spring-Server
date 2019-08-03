package org.mitre.openid.connect;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

	@RequestMapping("/error")
	public String handle(HttpServletRequest req) {
		Throwable errorException = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		String message = (String) req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String requestUri = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

		logger.error("request {} failed with {}", requestUri, message);
		logger.error("exception", errorException);

		processError(req);
		return "/error-view";
	}

	private void processError(HttpServletRequest request) {
		if (request.getAttribute("error") != null && request.getAttribute("error") instanceof OAuth2Exception) {
			request.setAttribute("errorCode", ((OAuth2Exception)request.getAttribute("error")).getOAuth2ErrorCode());
			request.setAttribute("message", ((OAuth2Exception)request.getAttribute("error")).getMessage());
		} else if (request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) != null) {
			Throwable t = (Throwable)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
			request.setAttribute("errorCode",  t.getClass().getSimpleName() + " (" + request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) + ")");
			request.setAttribute("message", t.getMessage());
		} else if (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) != null) {
			Integer code = (Integer)request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
			HttpStatus status = HttpStatus.valueOf(code);
			request.setAttribute("errorCode", status.toString() + " " + status.getReasonPhrase());
			request.setAttribute("message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
		} else {
			request.setAttribute("errorCode", "Server error");
			request.setAttribute("message", "See the logs for details");
		}
	}
}
