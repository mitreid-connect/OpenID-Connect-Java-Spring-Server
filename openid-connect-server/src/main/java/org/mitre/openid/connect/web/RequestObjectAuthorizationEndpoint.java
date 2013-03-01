package org.mitre.openid.connect.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	protected final Log logger = LogFactory.getLog(getClass());

	@RequestMapping(value = "/authorize", params = "request")
	public String authorizeRequestObject(@RequestParam("request") String jwtString, @RequestParam(value = "response_type", required = false) String responseType, HttpServletRequest request) {

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
		        // TODO Auto-generated catch block
		        e.printStackTrace();
	        } catch (URISyntaxException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
		}
		
		return "forward:/oauth/authorize?" + query;
		
	}
	
}
