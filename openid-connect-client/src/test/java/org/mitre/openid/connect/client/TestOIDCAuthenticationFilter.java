package org.mitre.openid.connect.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import static org.mockito.Mockito.mock;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestOIDCAuthenticationFilter {

	private OIDCAuthenticationFilter filter = new OIDCAuthenticationFilter();

	@Test
	public void attemptAuthentication_error() throws Exception {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("error")).thenReturn("Error");
		Mockito.when(request.getParameter("error_description")).thenReturn("Description");
		Mockito.when(request.getParameter("error_uri")).thenReturn("http://example.com");

		try {
			filter.attemptAuthentication(request, mock(HttpServletResponse.class));

			fail("AuthorizationEndpointException expected.");
		}
		catch (AuthorizationEndpointException exception) {
			assertThat(exception.getMessage(),
					is("Error from Authorization Endpoint: Error Description http://example.com"));

			assertThat(exception.getError(), is("Error"));
			assertThat(exception.getErrorDescription(), is("Description"));
			assertThat(exception.getErrorURI(), is("http://example.com"));

			assertThat(exception, is(instanceOf(AuthenticationServiceException.class)));
		}
	}
}
