package cz.muni.ics.oidc.server.claims;

import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * Context for initializing ClaimValueSources.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class ClaimSourceInitContext {

	private final PerunOidcConfig perunOidcConfig;
	private final JWTSigningAndValidationService jwtService;
	private final String propertyPrefix;
	private final Properties properties;
	private final String claimName;

	public ClaimSourceInitContext(PerunOidcConfig perunOidcConfig,
								  JWTSigningAndValidationService jwtService,
								  String propertyPrefix,
								  Properties properties,
								  String claimName)
	{
		this.perunOidcConfig = perunOidcConfig;
		this.jwtService = jwtService;
		this.propertyPrefix = propertyPrefix;
		this.properties = properties;
		this.claimName = claimName;
		log.debug("{} - context: property prefix for modifier configured to '{}'", claimName, propertyPrefix);
	}

	public String getClaimName() {
		return claimName;
	}

	public String getProperty(String suffix, String defaultValue) {
		return properties.getProperty(propertyPrefix + "." + suffix, defaultValue);
	}

	public JWTSigningAndValidationService getJwtService() {
		return jwtService;
	}

	public PerunOidcConfig getPerunOidcConfig() {
		return perunOidcConfig;
	}

}
