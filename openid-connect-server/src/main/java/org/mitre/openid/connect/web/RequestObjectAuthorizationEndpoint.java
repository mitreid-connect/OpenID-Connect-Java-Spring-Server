package org.mitre.openid.connect.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;


/**
 * This @Controller is a hack to get around SECOAUTH's AuthorizationEndpoint requiring the response_type parameter to be passed in.
 * 
 * @author jricher
 *
 */
@Controller("requestObjectAuthorzationEndpoint")
//@Component
public class RequestObjectAuthorizationEndpoint {

	private static Logger logger = LoggerFactory.getLogger(RequestObjectAuthorizationEndpoint.class);

	@RequestMapping(value = "/authorize", params = "request")
	public String authorizeRequestObject(@RequestParam("request") String jwtString, @RequestParam(value = "response_type", required = false) String responseType, HttpServletRequest request, ModelAndView mav) {

		String query = request.getQueryString();

		if (responseType == null) {
			try {
				JWT requestObject = JWTParser.parse(jwtString);
				responseType = (String)requestObject.getJWTClaimsSet().getClaim("response_type");

				URI uri = new URIBuilder(Strings.nullToEmpty(request.getServletPath()) + Strings.nullToEmpty(request.getPathInfo()) + "?" + query)
				.addParameter("response_type", responseType)
				.build();

				query = uri.getRawQuery();//uri.toString();

			} catch (ParseException e) {
				logger.error("ParseException while attempting to authorize request object: " + e.getStackTrace().toString());
				mav.addObject("code", HttpStatus.BAD_REQUEST);
				return "httpCodeView";

			} catch (URISyntaxException e) {
				logger.error("URISyntaxError while attempting to authorize request object: " + e.getStackTrace().toString());
				mav.addObject("code", HttpStatus.BAD_REQUEST);
				return "httpCodeView";
			}
		}

		return "forward:/oauth/authorize?" + query;

	}

}
