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
package cz.muni.ics.jose.keystore;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;

/**
 * @author jricher
 */
public class JWKSetKeyStore {

	private JWKSet jwkSet;
	private Resource location;

	public JWKSetKeyStore() { }

	public JWKSetKeyStore(JWKSet jwkSet) {
		this.setJwkSet(jwkSet);
		initializeJwkSet();
	}

	public JWKSet getJwkSet() {
		return jwkSet;
	}

	public void setJwkSet(JWKSet jwkSet) {
		if (jwkSet == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}

		this.jwkSet = jwkSet;
		initializeJwkSet();
	}

	public Resource getLocation() {
		return location;
	}

	public void setLocation(Resource location) {
		this.location = location;
		initializeJwkSet();
	}

	public List<JWK> getKeys() {
		if (jwkSet == null) {
			initializeJwkSet();
		}

		return jwkSet.getKeys();
	}

	private void initializeJwkSet() {
		if (jwkSet != null) {
			return;
		} else if (location == null) {
			return;
		}

		if (location.exists() && location.isReadable()) {
			try (BufferedReader br = new BufferedReader(
				new InputStreamReader(location.getInputStream(), StandardCharsets.UTF_8))
			) {
				String s = br.lines().collect(Collectors.joining());
				jwkSet = JWKSet.parse(s);
			} catch (IOException e) {
				throw new IllegalArgumentException("Key Set resource could not be read: " + location);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Key Set resource could not be parsed: " + location);                    }
		} else {
			throw new IllegalArgumentException("Key Set resource could not be read: " + location);
		}
	}

}
