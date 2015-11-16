package org.mitre.openid.connect.web;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RunWith(MockitoJUnitRunner.class)
public class EndSessionEndpointTest {
	
	private static final String REDIRECT = "http://foo.com";
	private static final String JWT = "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI0N0ZBNTMyRi1BNUI2LUUzMTEtOEUxQy0wMDFGMjlEREZGMjgiLCJhdWQiOiJwZV90ZXN0Iiwia2lkIjoicnNhMSIsImlzcyI6Imh0dHBzOlwvXC9vcHJjLmJiZC5jYVwvIiwiZXhwIjoxNDQ3MjY5ODgzLCJpYXQiOjE0NDcyNjkyODMsImp0aSI6IjUyNmJiZWI2LTFhZGQtNGFhNi05NGUyLWE4NzM2YjQwYjBlMiJ9.NQpjQ2w7WwCtpWq6SkOaD6NUghWpjFv7e0LmZ6NSlTR3WzJTFUBDWqb5Q9Mf7nxTvacJrJZDS_Z66qIeky_0x9y0gE5GzNwWUgdomQ4QFiuYjrJQkMp8Nz-1xcMBAc8AgJfx5TDWJPTT8LZpWjHSYHiotVXIx9EHSLIBnt3hMQ0j4rOHZSPnAeA7c4yJz4V1kGyCfpQVoCBWaVnIuvFE9UnpunHgMaVpb_w7sW4yYBJ4e6n-WPCUrjNyWuqEiEOhqtblQcp6HJwIkrxaRb6riu7C2UT05oJVkgNAKosLvjIz176MCVOgw2WPu7HP4qrmvpnQH_xAz6rjkPC3f7Fj1w";
	@Mock Model model;
	@Mock AbstractAuthenticationToken auth;
	@Mock RedirectAttributes redirectAttributes;
	@Mock HttpServletRequest request;
	@Mock OAuth2TokenEntityService tokenService;
	@Mock EndSessionValidator endSessionValidator;
	@Mock SignOutHelper signOutHelper;
	private EndSessionEndpoint fixture;
	
	@Before
	public void before() {
		fixture = new EndSessionEndpoint();
		fixture.tokenService = tokenService;
		fixture.endSessionValidator = endSessionValidator;
		fixture.signOutHelper = signOutHelper;
	}

	@Test
	public void shouldReturnLogoutViewIfInvalid() throws Exception {
		
		mockValid(false);
		
		String view = fixture.getEndSession(JWT, REDIRECT, auth, model, redirectAttributes, request);
		
		assertEquals("redirect:/logout", view);
	}

	private void mockValid(boolean isValid) {
		Mockito.when(endSessionValidator.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.any(AbstractAuthenticationToken.class))).thenReturn(isValid);
	}

	@Test
	public void shouldReturnPostLogoutRedirectUriIfValid() throws Exception {
		
		mockValid(true);
		
		String view = fixture.getEndSession(JWT, REDIRECT, auth, model, redirectAttributes, request);
		
		assertEquals("redirect:" + REDIRECT, view);
	}
	
	@Test
	public void shouldDeleteTokenIfValid() throws Exception {
		
		mockValid(true);
		
		fixture.getEndSession(JWT, REDIRECT, auth, model, redirectAttributes, request);
		
		Mockito.verify(tokenService).revokeAccessToken(Mockito.any(OAuth2AccessTokenEntity.class));
	}
	
	@Test
	public void shouldSignOutProgrammaticallyIfValid() throws Exception {

		mockValid(true);
		
		fixture.getEndSession(JWT, REDIRECT, auth, model, redirectAttributes, request);
		
		Mockito.verify(signOutHelper).signOutProgrammatically(Mockito.any(HttpServletRequest.class));
	}
	
}
