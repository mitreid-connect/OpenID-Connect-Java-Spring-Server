package org.mitre.openid.connect.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;

@RunWith(MockitoJUnitRunner.class)
public class EndSessionValidatorTest {
	
	private static final String USERNAME = "bar@foo.com";
	private static final String REDIRECT = "http://redirect.com";
	private static final String NULL_REDIRECT = null;
	private static final String BAD_REDIRECT = "http://malory.com";
	
	private static final String NULL_JWT = null;
	private static final String BAD_JWT = "ey.foo.bar";
	private static final String GOOD_JWT = "eyJraWQiOiJyc2ExIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI0N0ZBNTMyRi1BNUI2LUUzMTEtOEUxQy0wMDFGMjlEREZGMjgiLCJhdWQiOiJwZV90ZXN0Iiwia2lkIjoicnNhMSIsImlzcyI6Imh0dHBzOlwvXC9vcHJjLmJiZC5jYVwvIiwiZXhwIjoxNDQ3MjY5ODgzLCJpYXQiOjE0NDcyNjkyODMsImp0aSI6IjUyNmJiZWI2LTFhZGQtNGFhNi05NGUyLWE4NzM2YjQwYjBlMiJ9.NQpjQ2w7WwCtpWq6SkOaD6NUghWpjFv7e0LmZ6NSlTR3WzJTFUBDWqb5Q9Mf7nxTvacJrJZDS_Z66qIeky_0x9y0gE5GzNwWUgdomQ4QFiuYjrJQkMp8Nz-1xcMBAc8AgJfx5TDWJPTT8LZpWjHSYHiotVXIx9EHSLIBnt3hMQ0j4rOHZSPnAeA7c4yJz4V1kGyCfpQVoCBWaVnIuvFE9UnpunHgMaVpb_w7sW4yYBJ4e6n-WPCUrjNyWuqEiEOhqtblQcp6HJwIkrxaRb6riu7C2UT05oJVkgNAKosLvjIz176MCVOgw2WPu7HP4qrmvpnQH_xAz6rjkPC3f7Fj1w";
	
	@Mock AbstractAuthenticationToken auth;
	@Mock OAuth2TokenEntityService tokenService;
	
	private EndSessionValidator fixture;
	
	@Before
	public void before() {
		fixture = new EndSessionValidator();
		fixture.tokenService = tokenService;
	}
	
	@Test
	public void shouldNotAcceptNullJwt() throws Exception {
		boolean valid = fixture.isValid(NULL_JWT, REDIRECT, auth);
		
		Assert.assertTrue(!valid);
	}
	@Test
	public void shouldNotAcceptBadJwt() throws Exception {
		boolean valid = fixture.isValid(BAD_JWT, REDIRECT, auth);
		
		Assert.assertTrue(!valid);
	}
	@Test
	public void shouldNotAcceptBlankRedirect() throws Exception {
		boolean valid = fixture.isValid(GOOD_JWT, NULL_REDIRECT, auth);
		
		Assert.assertTrue(!valid);
	}
	@Test
	public void shouldNotAcceptIfTokenLookupFails() throws Exception {
		
		Mockito.when(tokenService.readAccessToken(GOOD_JWT)).thenThrow(new InvalidTokenException(""));
		
		boolean valid = fixture.isValid(GOOD_JWT, REDIRECT, auth);
		
		Assert.assertTrue(!valid);
		
	}
	@Test
	public void shouldNotAcceptIfTokenForWrongUser() throws Exception {
		Mockito.when(auth.getPrincipal()).thenReturn("foo@foo.com");
		
		Mockito.when(tokenService.readAccessToken(GOOD_JWT)).thenReturn(EndSessionTestHelper.createClientDetailsWithPostLogoutRedirectUri(REDIRECT, USERNAME));
		
		boolean valid = fixture.isValid(GOOD_JWT, REDIRECT, auth);
		
		Assert.assertTrue(!valid);
		
	}
	@Test
	public void shouldNotAcceptIfPostLogoutRedirectUriNotFound() throws Exception {
		
		Mockito.when(tokenService.readAccessToken(GOOD_JWT)).thenReturn(EndSessionTestHelper.createClientDetailsWithPostLogoutRedirectUri(REDIRECT, USERNAME));
		
		boolean valid = fixture.isValid(GOOD_JWT, BAD_REDIRECT, auth);
		
		Assert.assertTrue(!valid);
	}
	@Test
	public void shouldAcceptGoodJwtAndRedirect() throws Exception {
		
		Mockito.when(auth.getPrincipal()).thenReturn(USERNAME);
		Mockito.when(tokenService.readAccessToken(GOOD_JWT)).thenReturn(EndSessionTestHelper.createClientDetailsWithPostLogoutRedirectUri(REDIRECT, USERNAME));
		
		boolean valid = fixture.isValid(GOOD_JWT, REDIRECT, auth);
		
		Assert.assertTrue(valid);
	}
	
}
