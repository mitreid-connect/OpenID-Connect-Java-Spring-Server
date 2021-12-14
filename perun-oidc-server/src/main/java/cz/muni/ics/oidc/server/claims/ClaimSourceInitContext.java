package cz.muni.ics.oidc.server.claims;

import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Context for initializing ClaimValueSources.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
@Getter
public class ClaimSourceInitContext extends ClaimInitContext {

	private final PerunOidcConfig perunOidcConfig;
	private final JWTSigningAndValidationService jwtService;

	public ClaimSourceInitContext(PerunOidcConfig perunOidcConfig,
								  JWTSigningAndValidationService jwtService,
								  String propertyPrefix,
								  Properties properties,
								  String claimName)
	{
		super(propertyPrefix, properties, claimName);

		this.perunOidcConfig = perunOidcConfig;
		this.jwtService = jwtService;

		log.debug("{} - context: property prefix for modifier configured to '{}'", claimName, propertyPrefix);
	}

}
