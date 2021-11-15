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

package cz.muni.ics.jwt.assertion.impl;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.jwt.assertion.AbstractAssertionValidator;
import cz.muni.ics.jwt.assertion.AssertionValidator;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.jwt.signer.service.impl.JWKSetCacheService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Checks to see if the assertion has been signed by a particular authority available from a whitelist
 * @author jricher
 */
@Slf4j
public class WhitelistedIssuerAssertionValidator extends AbstractAssertionValidator implements AssertionValidator {

	private Map<String, String> whitelist = new HashMap<>(); //Map of issuer -> JWKSetUri
	private JWKSetCacheService jwkCache;

	public Map<String, String> getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(Map<String, String> whitelist) {
		this.whitelist = whitelist;
	}

	public JWKSetCacheService getJwkCache() {
		return jwkCache;
	}

	public void setJwkCache(JWKSetCacheService jwkCache) {
		this.jwkCache = jwkCache;
	}

	@Override
	public boolean isValid(JWT assertion) {
		String issuer = extractIssuer(assertion);
		if (StringUtils.isEmpty(issuer)) {
			log.debug("No issuer for assertion, rejecting");
			return false;
		} else if (!whitelist.containsKey(issuer)) {
			log.debug("Issuer is not in whitelist, rejecting");
			return false;
		}

		String jwksUri = whitelist.getOrDefault(issuer, null);
		if (jwksUri == null) {
			return false;
		}

		JWTSigningAndValidationService validator = jwkCache.getValidator(jwksUri);

		return validator.validateSignature((SignedJWT) assertion);
	}

}
