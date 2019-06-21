package org.mitre.openid.connect.web.sessionstate;

import org.mitre.openid.connect.view.JsonEntityView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for Session State Management as defined by OpenID Session Management
 *
 * @author jsinger
 */

@Controller("sessionStateManagementController")
public class SessionStateManagementController {

	// Name of the view for the OP IFrame
	public static final String URL = "sessionState";

	// Name of the endpoint url for the OP IFrame
	public static final String FRAME_URL = URL + ".html";

	// Name of the backend validation endpoint
	public static final String VALIDATION_URL = URL + "/validate";

	// Session state management bean
	private SessionStateManagementService sessionStateManagementService;

	/**
	 * Constructor for SessionStateManagementController
	 * @param sessionStateManagementService The session state management service to use
	 */
	@Autowired
	public SessionStateManagementController(SessionStateManagementService sessionStateManagementService) {
		this.sessionStateManagementService = sessionStateManagementService;
	}

	/**
	 * Endpoint for the OP IFrame html content
	 *
	 * @return The OP IFrame View
	 */
	@GetMapping(value = "/" + FRAME_URL, produces = MediaType.TEXT_HTML_VALUE )
	public ModelAndView showCheckSession() {
		return new ModelAndView(URL);
	}

	/**
	 * Backend session state validation endpoint
	 *
	 * Validates the session state of the sessio state cookie
	 * and the stored session value and returns appropriate response:
	 * * "changed" if the state has changed
	 * * "unchanged" if the state has not changed
	 * * "error" if an exception occurs
	 *
	 * @param request The http request
	 * @param m The ModelMap to use
	 * @return Json view with a string as defined above
	 */
	@GetMapping(value = "/" + VALIDATION_URL, produces = MediaType.APPLICATION_JSON_VALUE)
	public String doCheckSession(
		HttpServletRequest request,
		ModelMap m) {
		try {
			if (sessionStateManagementService.isSessionStateChanged(request)) {
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
