package cz.muni.ics.oidc.server.claims;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context for initializing ClaimModifiers.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class ClaimModifierInitContext {

	private final String propertyPrefix;
	private final Properties properties;
	private final String claimName;
	private final String modifierName;

	public ClaimModifierInitContext(String propertyPrefix, Properties properties, String claimName, String modifierName) {
		this.propertyPrefix = propertyPrefix;
		this.properties = properties;
		this.claimName = claimName;
		this.modifierName = modifierName;
		log.debug("{}:{} - context: property prefix for modifier configured to '{}'",
				claimName, modifierName, propertyPrefix);
	}

	public String getClaimName() {
		return claimName;
	}

	public String getModifierName() {
		return modifierName;
	}

	public String getProperty(String suffix, String defaultValue) {
		return properties.getProperty(propertyPrefix + '.' + suffix, defaultValue);
	}

}
