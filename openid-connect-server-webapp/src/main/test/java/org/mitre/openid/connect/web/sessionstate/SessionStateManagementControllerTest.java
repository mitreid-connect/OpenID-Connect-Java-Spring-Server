package org.mitre.openid.connect.web.sessionstate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.view.JsonEntityView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebAppConfiguration
@ContextConfiguration("file:src/main/webapp/WEB-INF/application-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class SessionStateManagementControllerTest {

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private ConfigurationPropertiesBean config;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders
			.webAppContextSetup(this.wac)
			.apply(springSecurity())
			.build();
	}

	@Test
	public void showCheckSession() throws Exception {
		mockMvc.perform(get("/" + SessionStateManagementController.FRAME_URL))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(SessionStateManagementController.URL))
			;
	}

	@Test
	public void doCheckSession() throws Exception {
		mockMvc.perform(get("/" + SessionStateManagementController.VALIDATION_URL))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(JsonEntityView.VIEWNAME))
			.andExpect(content().string("\"unchanged\""));

		// Login to get a session
		MvcResult mvcResult = mockMvc.perform(formLogin("/login").user("user").password("password"))
			.andDo(print())
			.andExpect(status().is(HttpStatus.FOUND.value()))
			.andExpect(redirectedUrl("/"))
			.andExpect(cookie().exists(config.getSessionStateCookieName()))
			.andReturn();

		HttpSession session = mvcResult.getRequest().getSession();
		Cookie opssCookie = mvcResult.getResponse().getCookie(config.getSessionStateCookieName());

		// Should respond unchanged
		mockMvc.perform(get("/" + SessionStateManagementController.VALIDATION_URL)
			.session((MockHttpSession)session).cookie(opssCookie))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(JsonEntityView.VIEWNAME))
			.andExpect(content().string("\"unchanged\""));

		// No Session -> respond with "changed"
		mockMvc.perform(get("/" + SessionStateManagementController.VALIDATION_URL)
			.cookie(opssCookie))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(JsonEntityView.VIEWNAME))
			.andExpect(content().string("\"changed\""));

		// No Cookie -> respond with changed
		mockMvc.perform(get("/" + SessionStateManagementController.VALIDATION_URL)
			.session((MockHttpSession)session))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(JsonEntityView.VIEWNAME))
			.andExpect(content().string("\"changed\""));

		String tmpVal = opssCookie.getValue();
		// Changed cookie value -> changed
		opssCookie.setValue("_invalid_");
		mockMvc.perform(get("/" + SessionStateManagementController.VALIDATION_URL)
			.session((MockHttpSession)session).cookie(opssCookie))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(JsonEntityView.VIEWNAME))
			.andExpect(content().string("\"changed\""));

		// restore cookie value
		opssCookie.setValue(tmpVal);

		// Should respond unchanged, just to be sure...
		mockMvc.perform(get("/" + SessionStateManagementController.VALIDATION_URL)
			.session((MockHttpSession)session).cookie(opssCookie))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(JsonEntityView.VIEWNAME))
			.andExpect(content().string("\"unchanged\""));

		// Logut to finish the session
		HttpSession session1 = mockMvc.perform(logout())
			.andDo(print())
			.andExpect(status().is(HttpStatus.FOUND.value()))
			.andExpect(redirectedUrl("/"))
			// expect the session state cookie to be deleted
			.andExpect(cookie().exists(config.getSessionStateCookieName()))
			.andExpect(cookie().maxAge(config.getSessionStateCookieName(), 0))
			.andExpect(cookie().value(config.getSessionStateCookieName(), ""))
			.andReturn()
			.getRequest()
			.getSession();

		mockMvc.perform(get("/" + SessionStateManagementController.VALIDATION_URL)
			.session((MockHttpSession)session1).cookie(opssCookie))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name(JsonEntityView.VIEWNAME))
			.andExpect(content().string("\"changed\""));

	}

}
