package org.mitre.openid.connect.web;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * OpenID Connect UserInfo endpoint, as specified in Standard sec 5 and Messages sec 2.4.
 * 
 * @author AANGANES
 *
 */
@Controller
public class UserInfoEndpoint {

	@Autowired
	OAuth2TokenEntityService tokenService;
	
	@Autowired
	UserInfoService userInfoService;
	
	/**
	 * Get information about the user as specified in the accessToken->idToken included in this request
	 * 
	 * @param accessToken	the Access Token associated with this request
	 * @param schema		the data schema to use, default is openid	
	 * @param mav			the ModelAndView object associated with this request
	 * @return				JSON or JWT response containing UserInfo data
	 */
	@RequestMapping(value="/userinfo", method= {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView getInfo(@RequestParam("access_token") String accessToken, @RequestParam("schema") String schema, ModelAndView mav) {
		
		//This will throw the proper error if the token cannot be found
		OAuth2AccessTokenEntity token = tokenService.getAccessToken(accessToken);
		
		if (schema != "openid") {
			//openid is the ONLY defined schema and is a required parameter
			//Will we be defining other schemas?
			//if schema is unrecognized, throw an error?
			
		}
		
		String userId = token.getIdToken().getTokenClaims().getUserId();
		
		UserInfo userInfo = userInfoService.getByUserId(userId);
		
		ClientDetailsEntity client = token.getClient();
		
		//if client wants plain JSON, give it JSON; if it wants a JWT, give it a JWT
		
		//If returning JSON
		return new ModelAndView("jsonUserInfoView", "userInfo", userInfo);
		
		// If returning JWT
		//Jwt jwt = new Jwt(new JwtHeader(), new JwtClaims(userInfo.toJson()), null);
		//sign jwt according to client's userinfo_signed_response_algs parameter
		//mav.addObject(jwt);
		//return mav;
	}
	
}
