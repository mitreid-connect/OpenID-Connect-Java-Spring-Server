/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package cz.muni.ics.openid.connect.token;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.service.OIDCTokenService;
import cz.muni.ics.openid.connect.service.UserInfoService;
import java.text.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

@RunWith(MockitoJUnitRunner.class)
public class TestConnectTokenEnhancer {

	private static final String CLIENT_ID = "client";
	private static final String KEY_ID = "key";

	private ConfigurationPropertiesBean configBean = new ConfigurationPropertiesBean();

	@Mock
	private JWTSigningAndValidationService jwtService;

	@Mock
	private ClientDetailsEntityService clientService;

	@Mock
	private UserInfoService userInfoService;

	@Mock
	private OIDCTokenService connectTokenService;

	@Mock
	private OAuth2Authentication authentication;

	private OAuth2Request request = new OAuth2Request(CLIENT_ID) { };

	@InjectMocks
	private ConnectTokenEnhancer enhancer = new ConnectTokenEnhancer();

	@Before
	public void prepare() {
		configBean.setIssuer("https://auth.example.org/");
		enhancer.setConfigBean(configBean);

		ClientDetailsEntity client = new ClientDetailsEntity();
		client.setClientId(CLIENT_ID);
		Mockito.when(clientService.loadClientByClientId(Mockito.anyString())).thenReturn(client);
		Mockito.when(authentication.getOAuth2Request()).thenReturn(request);
		Mockito.when(jwtService.getDefaultSigningAlgorithm()).thenReturn(JWSAlgorithm.RS256);
		Mockito.when(jwtService.getDefaultSignerKeyId()).thenReturn(KEY_ID);
	}

	@Test
	public void invokesCustomClaimsHook() throws ParseException {
		configure(enhancer = new ConnectTokenEnhancer() {
				@Override
				protected void addCustomAccessTokenClaims(Builder builder, OAuth2AccessTokenEntity token,
				    OAuth2Authentication authentication) {
					builder.claim("test", "foo");
				}
			});

		OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();

		OAuth2AccessTokenEntity enhanced = (OAuth2AccessTokenEntity) enhancer.enhance(token, authentication);
		Assert.assertEquals("foo", enhanced.getJwt().getJWTClaimsSet().getClaim("test"));
	}

	private void configure(ConnectTokenEnhancer e) {
		e.setConfigBean(configBean);
		e.setJwtService(jwtService);
		e.setClientService(clientService);
	}
}
