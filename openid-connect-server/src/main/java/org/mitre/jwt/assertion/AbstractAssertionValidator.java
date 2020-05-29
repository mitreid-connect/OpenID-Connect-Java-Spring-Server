package org.mitre.jwt.assertion;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public abstract class AbstractAssertionValidator implements AssertionValidator {

	private static Logger logger = LoggerFactory.getLogger(AbstractAssertionValidator.class);

	/**
	 * Extract issuer from claims present in JWT assertion.
	 * @param assertion JWT assertion object.
	 * @return Value of issuer from claims (can be null), NULL in case of error when parsing the assertion.
	 */
	protected String extractIssuer(JWT assertion) {
		if (!(assertion instanceof SignedJWT)) {
			return null;
		}

		JWTClaimsSet claims;
		try {
			claims = assertion.getJWTClaimsSet();
		} catch (ParseException e) {
			logger.debug("Invalid assertion claims");
			return null;
		}

		return claims.getIssuer();
	}
}
