package org.mitre.openid.connect.web;

import org.mitre.openid.connect.session.SessionStateManagementService;
import org.mitre.openid.connect.view.JsonEntityView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller("sessionStateManagementController")
public class SessionStateManagementController {

	public static final String URL = "sessionState";
	public static final String FRAME_URL = URL + ".html";
	public static final String VALIDATION_URL = URL + "/validate";

	private SessionStateManagementService sessionStateManagementService;

	@Autowired
	public SessionStateManagementController(SessionStateManagementService sessionStateManagementService) {
		this.sessionStateManagementService = sessionStateManagementService;
	}

	@GetMapping(value = "/" + FRAME_URL)
	public String showCheckSession(ModelMap m) {
		return URL;
	}

	@GetMapping(value = "/" + VALIDATION_URL, produces = MediaType.APPLICATION_JSON_VALUE)
	public String doCheckSession(
		HttpSession session,
		HttpServletRequest request,
		ModelMap m) {
		try {
			if (sessionStateManagementService.isSessionStateChanged(request, session)) {
				m.put(JsonEntityView.ENTITY, "changed");
			} else {
				m.put(JsonEntityView.ENTITY, "unchanged");
			}
		} catch (Exception e) {
			m.put(JsonEntityView.ENTITY, "error");
		}

		return JsonEntityView.VIEWNAME;
	}

}
