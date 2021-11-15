package cz.muni.ics.openid.connect.assertion;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.jwt.signer.service.impl.ClientKeyCacheService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity.AuthMethod;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;

@RunWith(MockitoJUnitRunner.class)
public class TestJWTBearerAuthenticationProvider {

	private static final String CLIENT_ID = "client";
	private static final String SUBJECT = "subject";

	@Mock
	private ClientKeyCacheService validators;
	@Mock
	private ClientDetailsEntityService clientService;
	@Mock
	private ConfigurationPropertiesBean config;

	@InjectMocks
	private JWTBearerAuthenticationProvider jwtBearerAuthenticationProvider;

	@Mock
	private JWTBearerAssertionAuthenticationToken token;
	@Mock
	private ClientDetailsEntity client;
	@Mock
	private JWTSigningAndValidationService validator;

	private final GrantedAuthority authority1 = new SimpleGrantedAuthority("1");
	private final GrantedAuthority authority2 = new SimpleGrantedAuthority("2");
	private final GrantedAuthority authority3 = new SimpleGrantedAuthority("3");

	@Before
	public void setup() {
		when(clientService.loadClientByClientId(CLIENT_ID)).thenReturn(client);

		when(token.getName()).thenReturn(CLIENT_ID);

		when(client.getClientId()).thenReturn(CLIENT_ID);
		when(client.getTokenEndpointAuthMethod()).thenReturn(AuthMethod.NONE);
		when(client.getAuthorities()).thenReturn(ImmutableSet.of(authority1, authority2, authority3));

		when(validators.getValidator(client, JWSAlgorithm.RS256)).thenReturn(validator);
		when(validator.validateSignature(any(SignedJWT.class))).thenReturn(true);

		when(config.getIssuer()).thenReturn("http://issuer.com/");
	}

	@Test
	public void should_not_support_UsernamePasswordAuthenticationToken() {
		assertThat(jwtBearerAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class), is(false));
	}

	@Test
	public void should_support_JWTBearerAssertionAuthenticationToken() {
		assertThat(jwtBearerAuthenticationProvider.supports(JWTBearerAssertionAuthenticationToken.class), is(true));
	}

	@Test
	public void should_throw_UsernameNotFoundException_when_clientService_throws_InvalidClientException() {
		when(clientService.loadClientByClientId(CLIENT_ID)).thenThrow(new InvalidClientException("invalid client"));

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(UsernameNotFoundException.class));
		assertThat(thrown.getMessage(), is("Could not find client: " + CLIENT_ID));
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_PlainJWT() {
		mockPlainJWTAuthAttempt();

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), is("Unsupported JWT type: " + PlainJWT.class.getName()));
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_EncryptedJWT() {
		mockEncryptedJWTAuthAttempt();

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), is("Unsupported JWT type: " + EncryptedJWT.class.getName()));
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_SignedJWT_when_signing_algorithms_do_not_match() {
		when(client.getTokenEndpointAuthSigningAlg()).thenReturn(JWSAlgorithm.RS256);
		SignedJWT signedJWT = createSignedJWT(JWSAlgorithm.ES384);
		when(token.getJwt()).thenReturn(signedJWT);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), is("Client's registered token endpoint signing algorithm (RS256) does not match token's actual algorithm (ES384)"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_SignedJWT_when_unsupported_authentication_method_for_SignedJWT() {
		List<AuthMethod> unsupportedAuthMethods =
			Arrays.asList(null, AuthMethod.NONE, AuthMethod.SECRET_BASIC, AuthMethod.SECRET_POST);

		for (AuthMethod unsupportedAuthMethod : unsupportedAuthMethods) {
			SignedJWT signedJWT = createSignedJWT();
			when(token.getJwt()).thenReturn(signedJWT);
			when(client.getTokenEndpointAuthMethod()).thenReturn(unsupportedAuthMethod);

			Throwable thrown = authenticateAndReturnThrownException();

			assertThat(thrown, instanceOf(AuthenticationServiceException.class));
			assertThat(thrown.getMessage(), is("Client does not support this authentication method."));
		}
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_SignedJWT_when_invalid_algorithm_for_PRIVATE_KEY_auth_method() {
		List<JWSAlgorithm> invalidAlgorithms = Arrays.asList(JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512);

		for (JWSAlgorithm algorithm : invalidAlgorithms) {
			SignedJWT signedJWT = createSignedJWT(algorithm);
			when(token.getJwt()).thenReturn(signedJWT);
			when(client.getTokenEndpointAuthMethod()).thenReturn(AuthMethod.PRIVATE_KEY);
			when(client.getTokenEndpointAuthSigningAlg()).thenReturn(algorithm);

			Throwable thrown = authenticateAndReturnThrownException();

			assertThat(thrown, instanceOf(AuthenticationServiceException.class));
			assertThat(thrown.getMessage(), startsWith("Unable to create signature validator for method"));
		}
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_SignedJWT_when_invalid_algorithm_for_SECRET_JWT_auth_method() {
		List<JWSAlgorithm> invalidAlgorithms = Arrays.asList(
			JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512,
			JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512,
			JWSAlgorithm.PS256, JWSAlgorithm.PS384, JWSAlgorithm.PS512);

		for (JWSAlgorithm algorithm : invalidAlgorithms) {
			SignedJWT signedJWT = createSignedJWT(algorithm);
			when(token.getJwt()).thenReturn(signedJWT);
			when(client.getTokenEndpointAuthMethod()).thenReturn(AuthMethod.SECRET_JWT);
			when(client.getTokenEndpointAuthSigningAlg()).thenReturn(algorithm);

			Throwable thrown = authenticateAndReturnThrownException();

			assertThat(thrown, instanceOf(AuthenticationServiceException.class));
			assertThat(thrown.getMessage(), startsWith("Unable to create signature validator for method"));
		}
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_SignedJWT_when_in_heart_mode_and_auth_method_is_not_PRIVATE_KEY() {
		SignedJWT signedJWT = createSignedJWT(JWSAlgorithm.HS256);
		when(token.getJwt()).thenReturn(signedJWT);
		when(client.getTokenEndpointAuthSigningAlg()).thenReturn(JWSAlgorithm.HS256);
		when(config.isHeartMode()).thenReturn(true);
		when(client.getTokenEndpointAuthMethod()).thenReturn(AuthMethod.SECRET_JWT);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), is("[HEART mode] Invalid authentication method"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_SignedJWT_when_null_validator() {
		mockSignedJWTAuthAttempt();
		when(validators.getValidator(any(ClientDetailsEntity.class), any(JWSAlgorithm.class))).thenReturn(null);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), startsWith("Unable to create signature validator for client"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_for_SignedJWT_when_invalid_signature() {
		SignedJWT signedJWT = mockSignedJWTAuthAttempt();
		when(validator.validateSignature(signedJWT)).thenReturn(false);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), is("Signature did not validate for presented JWT authentication."));
	}

	@Test
	public void should_throw_AuthenticationServiceException_when_null_issuer() {
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer(null).build();
		mockSignedJWTAuthAttempt(jwtClaimsSet);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), is("Assertion Token Issuer is null"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_when_not_matching_issuer() {
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer("not matching").build();
		mockSignedJWTAuthAttempt(jwtClaimsSet);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), startsWith("Issuers do not match"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_when_null_expiration_time() {
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer(CLIENT_ID).expirationTime(null).build();
		mockSignedJWTAuthAttempt(jwtClaimsSet);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), is("Assertion Token does not have required expiration claim"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_when_expired_jwt() {
		Date expiredDate = new Date(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(500));
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer(CLIENT_ID).expirationTime(expiredDate).build();
		mockSignedJWTAuthAttempt(jwtClaimsSet);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), startsWith("Assertion Token is expired"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_when_jwt_valid_in_future() {
		Date futureDate = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(500));
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer(CLIENT_ID).expirationTime(futureDate).notBeforeTime(futureDate).build();
		mockSignedJWTAuthAttempt(jwtClaimsSet);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), startsWith("Assertion Token not valid until"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_when_jwt_issued_in_future() {
		Date futureDate = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(500));
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer(CLIENT_ID).expirationTime(futureDate).issueTime(futureDate).build();
		mockSignedJWTAuthAttempt(jwtClaimsSet);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), startsWith("Assertion Token was issued in the future"));
	}

	@Test
	public void should_throw_AuthenticationServiceException_when_unmatching_audience() {
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer(CLIENT_ID).expirationTime(new Date()).audience("invalid").build();
		mockSignedJWTAuthAttempt(jwtClaimsSet);

		Throwable thrown = authenticateAndReturnThrownException();

		assertThat(thrown, instanceOf(AuthenticationServiceException.class));
		assertThat(thrown.getMessage(), startsWith("Audience does not match"));
	}

	@Test
	public void should_return_valid_token_when_audience_contains_token_endpoint() {
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
			.issuer(CLIENT_ID)
			.subject(SUBJECT)
			.expirationTime(new Date())
			.audience(ImmutableList.of("http://issuer.com/token", "invalid"))
			.build();
		JWT jwt = mockSignedJWTAuthAttempt(jwtClaimsSet);

		Authentication authentication = jwtBearerAuthenticationProvider.authenticate(token);

		assertThat(authentication, instanceOf(JWTBearerAssertionAuthenticationToken.class));

		JWTBearerAssertionAuthenticationToken token = (JWTBearerAssertionAuthenticationToken) authentication;
		assertThat(token.getName(), is(SUBJECT));
		assertThat(token.getJwt(), is(jwt));
		assertThat(token.getAuthorities(), hasItems(authority1, authority2, authority3));
		assertThat(token.getAuthorities().size(), is(4));
	}

	@Test
	public void should_return_valid_token_when_issuer_does_not_end_with_slash_and_audience_contains_token_endpoint() {
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
			.issuer(CLIENT_ID)
			.subject(SUBJECT)
			.expirationTime(new Date())
			.audience(ImmutableList.of("http://issuer.com/token"))
			.build();
		JWT jwt = mockSignedJWTAuthAttempt(jwtClaimsSet);
		when(config.getIssuer()).thenReturn("http://issuer.com/");

		Authentication authentication = jwtBearerAuthenticationProvider.authenticate(token);

		assertThat(authentication, instanceOf(JWTBearerAssertionAuthenticationToken.class));

		JWTBearerAssertionAuthenticationToken token = (JWTBearerAssertionAuthenticationToken) authentication;
		assertThat(token.getName(), is(SUBJECT));
		assertThat(token.getJwt(), is(jwt));
		assertThat(token.getAuthorities(), hasItems(authority1, authority2, authority3));
		assertThat(token.getAuthorities().size(), is(4));
	}

	private void mockPlainJWTAuthAttempt() {
		PlainJWT plainJWT = new PlainJWT(createJwtClaimsSet());
		when(token.getJwt()).thenReturn(plainJWT);
	}

	private void mockEncryptedJWTAuthAttempt() {
		JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.A128GCMKW, EncryptionMethod.A256GCM).build();
		EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, createJwtClaimsSet());
		when(token.getJwt()).thenReturn(encryptedJWT);
	}

	private SignedJWT mockSignedJWTAuthAttempt() {
		return mockSignedJWTAuthAttempt(createJwtClaimsSet());
	}

	private SignedJWT mockSignedJWTAuthAttempt(JWTClaimsSet jwtClaimsSet) {
		SignedJWT signedJWT = createSignedJWT(JWSAlgorithm.RS256, jwtClaimsSet);
		when(token.getJwt()).thenReturn(signedJWT);
		when(client.getTokenEndpointAuthMethod()).thenReturn(AuthMethod.PRIVATE_KEY);
		when(client.getTokenEndpointAuthSigningAlg()).thenReturn(JWSAlgorithm.RS256);
		return signedJWT;
	}

	private Throwable authenticateAndReturnThrownException() {
		try {
			jwtBearerAuthenticationProvider.authenticate(token);
		} catch (Throwable throwable) {
			return throwable;
		}
		throw new AssertionError("No exception thrown when expected");
	}

	private SignedJWT createSignedJWT() {
		return createSignedJWT(JWSAlgorithm.RS256);
	}

	private SignedJWT createSignedJWT(JWSAlgorithm jwsAlgorithm) {
		JWSHeader jwsHeader = new JWSHeader.Builder(jwsAlgorithm).build();
		JWTClaimsSet claims = createJwtClaimsSet();

		return new SignedJWT(jwsHeader, claims);
	}

	private SignedJWT createSignedJWT(JWSAlgorithm jwsAlgorithm, JWTClaimsSet jwtClaimsSet) {
		JWSHeader jwsHeader = new JWSHeader.Builder(jwsAlgorithm).build();

		return new SignedJWT(jwsHeader, jwtClaimsSet);
	}

	private JWTClaimsSet createJwtClaimsSet() {
		return new JWTClaimsSet.Builder()
			.issuer(CLIENT_ID)
			.expirationTime(new Date())
			.audience("http://issuer.com/")
			.build();
	}

}
