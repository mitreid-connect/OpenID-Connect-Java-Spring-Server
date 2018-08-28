package org.mitre.openid.connect.web.sessionstate;

import com.nimbusds.jose.util.Base64URL;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@WebAppConfiguration
@RunWith(MockitoJUnitRunner.class)

public class SessionStateAuthorizationInterceptorTest {

	private ConfigurationPropertiesBean configBean = new ConfigurationPropertiesBean();

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private MockHttpSession session;

	private SessionStateManagementService sessionStateManagementService;

	// Test URLs

	private static String successCodeUrl = "http://localhost/callback?code=12345&state=_mystate_";

	private static String successTokenUrl = "http://localhost/callback#access_token=12345&state=_mystate_";


	@Before
	public void setUp() throws Exception {
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();
		session = new MockHttpSession();
		request.setSession(session);
		configBean.setIssuer("https://auth.example.org/oidc");
		configBean.setSessionStateEnabled(true);
		sessionStateManagementService = new SessionStateManagementService(configBean);
	}

	@Test
	public void postHandle() throws Exception{

		// Init
		SessionStateAuthorizationInterceptor stateAuthorizationInterceptor = new SessionStateAuthorizationInterceptor(sessionStateManagementService);
		ModelAndView authorizationView = new ModelAndView();
		RedirectView redirectView = new RedirectView();

		String clientId = "54321";
		String sessionState = "12345";

		// Call without view should not throw an exception and not change the view
		try {
			stateAuthorizationInterceptor.postHandle(request, response, null, authorizationView);
			assertNull(authorizationView.getView());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		// Errors should not be handled as well
		String errorUrl = "http://localhost/callback?error=myerror";
		redirectView.setUrl(errorUrl);
		authorizationView.setView(redirectView);
		checkUnchanged(stateAuthorizationInterceptor, authorizationView, redirectView);

		// Code should be handled, but no session & session state also does nothing
		redirectView.setUrl(successCodeUrl);
		authorizationView.setView(redirectView);
		checkUnchanged(stateAuthorizationInterceptor, authorizationView, redirectView);

		// now set a session state
		session.setAttribute(SessionStateManagementService.SESSION_STATE_ATTRIBUTE, sessionState);
		// and the request param
		request.setParameter(OAuth2Utils.CLIENT_ID, clientId);

		// now the session state should be set for code ...
		checkSuccess(stateAuthorizationInterceptor, authorizationView, redirectView, clientId, sessionState);

		// ... and token requests
		redirectView.setUrl(successTokenUrl);
		checkSuccess(stateAuthorizationInterceptor, authorizationView, redirectView, clientId, sessionState);

	}

	private void checkSuccess(SessionStateAuthorizationInterceptor stateAuthorizationInterceptor, ModelAndView authorizationView, RedirectView redirectView, String clientId, String sessionState) {

		Pattern pattern = Pattern.compile("[?&]session_state=([a-zA-Z0-9_-]+)\\.([a-zA-Z0-9_-]+)[&]?");

		try {
			stateAuthorizationInterceptor.postHandle(request, response, null, authorizationView);
			assertNotNull(authorizationView.getView());
			assertSame(redirectView, authorizationView.getView());
			String newRedirectUrl = ((RedirectView)authorizationView.getView()).getUrl();
			assertNotEquals(successCodeUrl, newRedirectUrl);
			assertNotEquals(successTokenUrl, newRedirectUrl);
			Matcher matcher = pattern.matcher(newRedirectUrl);
			assertTrue(matcher.find());
			String state = matcher.group(1);
			String salt = matcher.group(2);
			assertNotNull(state);
			String sessionStateString = clientId + " " + "http://localhost" + " " + sessionState + " " + salt;
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String hash = Base64URL.encode(digest.digest(sessionStateString.getBytes(StandardCharsets.US_ASCII))).toString();
			Assert.assertEquals(hash, state);

		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private void checkUnchanged(SessionStateAuthorizationInterceptor stateAuthorizationInterceptor, ModelAndView authorizationView, RedirectView redirectView) {
		try {
			stateAuthorizationInterceptor.postHandle(request, response, null, authorizationView);
			assertNotNull(authorizationView.getView());
			assertSame(redirectView, authorizationView.getView());
			assertEquals(redirectView.getUrl(), ((RedirectView)authorizationView.getView()).getUrl());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
