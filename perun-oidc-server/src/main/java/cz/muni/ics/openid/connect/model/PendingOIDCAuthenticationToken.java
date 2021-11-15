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

package cz.muni.ics.openid.connect.model;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import cz.muni.ics.openid.connect.config.ServerConfiguration;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * AuthenticationToken for use as a data shuttle from the filter to the auth provider.
 *
 * @author jricher
 */
public class PendingOIDCAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 22100073066377804L;

	private final ImmutableMap<String, String> principal;
	private final String accessTokenValue; // string representation of the access token
	private final String refreshTokenValue; // string representation of the refresh token
	private transient JWT idToken; // this needs a custom serializer
	private final String issuer; // issuer URL (parsed from the id token)
	private final String sub; // user id (parsed from the id token)

	private final transient ServerConfiguration serverConfiguration; // server configuration used to fulfill this token, don't serialize it

	public PendingOIDCAuthenticationToken (String subject, String issuer,
			ServerConfiguration serverConfiguration,
			JWT idToken, String accessTokenValue, String refreshTokenValue)
	{
		super(new ArrayList<GrantedAuthority>(0));

		this.principal = ImmutableMap.of("sub", subject, "iss", issuer);
		this.sub = subject;
		this.issuer = issuer;
		this.idToken = idToken;
		this.accessTokenValue = accessTokenValue;
		this.refreshTokenValue = refreshTokenValue;

		this.serverConfiguration = serverConfiguration;

		setAuthenticated(false);
	}

	@Override
	public Object getCredentials() {
		return accessTokenValue;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

	public String getSub() {
		return sub;
	}

	public JWT getIdToken() {
		return idToken;
	}

	public String getAccessTokenValue() {
		return accessTokenValue;
	}

	public String getRefreshTokenValue() {
		return refreshTokenValue;
	}

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public String getIssuer() {
		return issuer;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (idToken == null) {
			out.writeObject(null);
		} else {
			out.writeObject(idToken.serialize());
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, ParseException {
		in.defaultReadObject();
		Object o = in.readObject();
		if (o != null) {
			idToken = JWTParser.parse((String)o);
		}
	}

}
