package org.mitre.openid.connect.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class SignOutHelper {

	// http://stackoverflow.com/a/18957784/1098564
	public void signOutProgrammatically(HttpServletRequest request) {
		new SecurityContextLogoutHandler().logout(request, null, null);
	}
}
