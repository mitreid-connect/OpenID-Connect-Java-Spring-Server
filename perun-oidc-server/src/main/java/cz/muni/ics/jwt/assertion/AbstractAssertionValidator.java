package cz.muni.ics.jwt.assertion;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

@Slf4j
public abstract class AbstractAssertionValidator implements AssertionValidator {

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
			log.debug("Invalid assertion claims");
			return null;
		}

		return claims.getIssuer();
	}
}
