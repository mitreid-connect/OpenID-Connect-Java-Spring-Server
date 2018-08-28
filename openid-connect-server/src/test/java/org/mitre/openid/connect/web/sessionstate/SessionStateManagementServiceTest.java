package org.mitre.openid.connect.web.sessionstate;

import com.nimbusds.jose.util.Base64URL;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;

@WebAppConfiguration
@RunWith(MockitoJUnitRunner.class)
public class SessionStateManagementServiceTest {

	private ConfigurationPropertiesBean configBean = new ConfigurationPropertiesBean();

	private MockHttpSession session;

	private String sessionStateKey;

	@Mock
	private HttpServletRequest request;

	private MockHttpServletResponse response;

	private SessionStateManagementService sessionStateManagementService;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		session = new MockHttpSession();
		response = new MockHttpServletResponse();
		configBean.setIssuer("https://auth.example.org/oidc");
		configBean.setSessionStateEnabled(true);
		sessionStateManagementService = new SessionStateManagementService(configBean);
		Field field = SessionStateManagementService.class.getDeclaredField("SESSION_STATE_ATTRIBUTE");
		field.setAccessible(true);

		sessionStateKey = field.get(sessionStateManagementService).toString();

	}

	@Test
	public void getOrigin() throws Exception {
		Method method = SessionStateManagementService.class.getDeclaredMethod("getOrigin", String.class);
		method.setAccessible(true);

		Assert.assertEquals("https://auth.example.org", method.invoke(sessionStateManagementService,"https://auth.example.org/oidc"));
		Assert.assertEquals("http://auth.example.org", method.invoke(sessionStateManagementService,"http://auth.example.org/oidc"));
		Assert.assertEquals("https://auth.example.org:4711", method.invoke(sessionStateManagementService,"https://auth.example.org:4711/oidc"));
		Assert.assertEquals("invalid", method.invoke(sessionStateManagementService,"invalid"));
	}

	@Test
	public void isConfigured() throws Exception {
		ConfigurationPropertiesBean myConfig = new ConfigurationPropertiesBean();
		SessionStateManagementService myService;

		Field field = SessionStateManagementService.class.getDeclaredField("cookiePath");
		field.setAccessible(true);

		myConfig.setIssuer("https://auth.example.org/oidc");
		myService = new SessionStateManagementService(myConfig);
		Assert.assertEquals("/oidc", field.get(myService));

		myConfig.setIssuer("https://auth.example.org:8080/test/differ/");
		myService = new SessionStateManagementService(myConfig);
		Assert.assertEquals("/test/differ/", field.get(myService));

		myConfig.setIssuer("/bla");
		myService = new SessionStateManagementService(myConfig);
		Assert.assertEquals("/bla", field.get(myService));

		myConfig.setIssuer(":_this_is:_invalid!_");
		myService = new SessionStateManagementService(myConfig);
		Assert.assertEquals("/", field.get(myService));

	}

	@Test
	public void getSessionState() {
		// no session => null
		Mockito.when(request.getSession()).thenReturn(null);
		Assert.assertNull(sessionStateManagementService.getSessionState(request));

		// no attributes in the session => null
		Mockito.when(request.getSession(false)).thenReturn(session);
		Assert.assertNull(sessionStateManagementService.getSessionState(request));

		// should return the correct session state value
		session.setAttribute(sessionStateKey, "12345");
		Assert.assertEquals("12345", sessionStateManagementService.getSessionState(request));
	}

	@Test
	public void isSessionStateChanged() {
		ConfigurationPropertiesBean myConfig = new ConfigurationPropertiesBean();
		myConfig.setSessionStateEnabled(false);
		myConfig.setIssuer(configBean.getIssuer());
		SessionStateManagementService myService = new SessionStateManagementService(myConfig);

		// should always return false if session state is disabled
		Assert.assertFalse(myService.isSessionStateChanged(request));

		// no session attributes, no cookies in request object...
		Mockito.when(request.getSession(false)).thenReturn(session);
		// not change...
		Assert.assertFalse(sessionStateManagementService.isSessionStateChanged(request));

		// Cookie found but no state in session should return true
		Cookie[] cookies = new Cookie[]{new Cookie(configBean.getSessionStateCookieName(), "12345")};
		Mockito.when(request.getCookies()).thenReturn(cookies);
		Assert.assertTrue(sessionStateManagementService.isSessionStateChanged(request));


		// Correct session attribute value should return false
		session.setAttribute(sessionStateKey, "12345");
		Assert.assertFalse(sessionStateManagementService.isSessionStateChanged(request));

		// Wrong session attribute should return true
		session.setAttribute(sessionStateKey, "54321");
		Assert.assertTrue(sessionStateManagementService.isSessionStateChanged(request));

		// missing cookie but session value => true
		Mockito.when(request.getCookies()).thenReturn(null);
		Assert.assertTrue(sessionStateManagementService.isSessionStateChanged(request));

	}

	@Test
	public void processSessionStateCookie() {
		// no session, should result in a session state cookie without value and max age 0
		sessionStateManagementService.writeSessionStateCookie(request, response, null, true);
		Assert.assertNotNull(response.getCookie(configBean.getSessionStateCookieName()));
		Assert.assertEquals(0L, response.getCookie(configBean.getSessionStateCookieName()).getMaxAge());
		Assert.assertEquals("", response.getCookie(configBean.getSessionStateCookieName()).getValue());
		Assert.assertEquals(request.isSecure(), response.getCookie(configBean.getSessionStateCookieName()).getSecure());
		Assert.assertEquals("/oidc", response.getCookie(configBean.getSessionStateCookieName()).getPath());

		// session available, should generate a new session state and set it as
		// cookie value as well as in the session attributes
		// first we set an attribute in the session
		session.setAttribute(sessionStateKey, "12345");
		// reset the response object
		response = new MockHttpServletResponse();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		sessionStateManagementService.writeSessionStateCookie(mockRequest, response, session, true);
		Assert.assertNotNull(response.getCookie(configBean.getSessionStateCookieName()));
		Assert.assertNotEquals(0L, response.getCookie(configBean.getSessionStateCookieName()).getMaxAge());
		Assert.assertNotEquals("", response.getCookie(configBean.getSessionStateCookieName()).getValue());
		Assert.assertEquals(request.isSecure(), response.getCookie(configBean.getSessionStateCookieName()).getSecure());
		Assert.assertEquals("/oidc", response.getCookie(configBean.getSessionStateCookieName()).getPath());
		// the session attribute should be changed
		String state = session.getAttribute(sessionStateKey).toString();
		Assert.assertNotEquals("12345", state);
		Assert.assertEquals(state, response.getCookie(configBean.getSessionStateCookieName()).getValue());

		// call again should not change the value
		sessionStateManagementService.writeSessionStateCookie(mockRequest, response, session, true);
		Assert.assertEquals(state, session.getAttribute(sessionStateKey));
		Assert.assertEquals(state, response.getCookie(configBean.getSessionStateCookieName()).getValue());
	}

	@Test
	public void buildSessionStateParam() throws NoSuchAlgorithmException {
		String clientId = "54321";
		String state = "12345";
		String sessionState = sessionStateManagementService.buildSessionStateParam(state, clientId, configBean.getIssuer());
		Assert.assertNotNull(sessionState);
		Assert.assertTrue(sessionState.contains("."));
		String[] strings = sessionState.split("\\.");
		String sessionStateString = clientId + " " + "https://auth.example.org" + " " + state + " " + strings[1];
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		String hash = Base64URL.encode(digest.digest(sessionStateString.getBytes(StandardCharsets.US_ASCII))).toString();
		Assert.assertEquals(hash, strings[0]);
	}

	// logout just calls writeSessionStateCookie, no test

}
