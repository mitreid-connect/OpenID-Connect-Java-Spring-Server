/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.service.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.provider.OAuth2Request;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultOIDCTokenService {
    private static final String CLIENT_ID = "client";
    private static final String KEY_ID = "key";

    private final ConfigurationPropertiesBean configBean = new ConfigurationPropertiesBean();
    private final ClientDetailsEntity client = new ClientDetailsEntity();
    private final OAuth2AccessTokenEntity accessToken = new OAuth2AccessTokenEntity();
    private final OAuth2Request request = new OAuth2Request(CLIENT_ID) { };

    @Mock
    private JWTSigningAndValidationService jwtService;

    @Before
    public void prepare() {
        configBean.setIssuer("https://auth.example.org/");

        client.setClientId(CLIENT_ID);
        Mockito.when(jwtService.getDefaultSigningAlgorithm()).thenReturn(JWSAlgorithm.RS256);
        Mockito.when(jwtService.getDefaultSignerKeyId()).thenReturn(KEY_ID);
    }

    @Test
    public void invokesCustomClaimsHook() throws java.text.ParseException {
        DefaultOIDCTokenService s = new DefaultOIDCTokenService() {
            @Override
            protected void addCustomIdTokenClaims(JWTClaimsSet.Builder idClaims, ClientDetailsEntity client, OAuth2Request request,
                String sub, OAuth2AccessTokenEntity accessToken) {
                idClaims.claim("test", "foo");
            }
        };
        configure(s);

        JWT token = s.createIdToken(client, request, new Date(), "sub", accessToken);
        Assert.assertEquals("foo", token.getJWTClaimsSet().getClaim("test"));
    }


    private void configure(DefaultOIDCTokenService s) {
        s.setConfigBean(configBean);
        s.setJwtService(jwtService);
    }
}
