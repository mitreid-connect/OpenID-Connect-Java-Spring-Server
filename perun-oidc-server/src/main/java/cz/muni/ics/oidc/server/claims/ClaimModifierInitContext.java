package cz.muni.ics.oidc.server.claims;

import java.util.Properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Context for initializing ClaimModifiers.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
@Getter
public class ClaimModifierInitContext extends ClaimInitContext {

	private final String modifierName;

	public ClaimModifierInitContext(String propertyPrefix, Properties properties, String claimName, String modifierName) {
		super(propertyPrefix, properties, claimName);

		this.modifierName = modifierName;

		log.debug("{}:{} - context: property prefix for modifier configured to '{}'",
				claimName, modifierName, propertyPrefix);
	}

}
