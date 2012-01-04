package org.mitre.openid.connect.web;

import org.mitre.openid.connect.model.UserInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/userinfo")
public class UserInfoEndpoint {

	@RequestMapping("/")
	public ModelAndView getInfo(@RequestParam("access_token") String accessToken, @RequestParam("schema") String schema, ModelAndView mav) {
		
		UserInfo userInfo = new UserInfo();
		
		//populate with info
		
		//If returning JSON
		return new ModelAndView("jsonUserInfoView", "userInfo", userInfo);
		
		//TODO: If returning JWT?
	}
	
}
