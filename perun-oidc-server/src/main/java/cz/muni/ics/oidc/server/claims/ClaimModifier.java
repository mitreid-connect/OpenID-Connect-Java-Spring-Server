package cz.muni.ics.oidc.server.claims;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for all code that needs to modify claim values.
 *
 * @see cz.muni.ics.oidc.server.claims.modifiers for different implementations of claim value modifiers
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public abstract class ClaimModifier {

	private final String claimName;
	private final String modifierName;

	public ClaimModifier(ClaimModifierInitContext ctx) {
		this.claimName = ctx.getClaimName();
		this.modifierName = ctx.getModifierName();
		log.debug("{} - claim modifier initialized", ctx.getClaimName());
	}

	public String getClaimName() {
		return claimName;
	}

	public String getModifierName() {
		return modifierName;
	}

	public String getUnifiedName() {
		return claimName + ':' + modifierName;
	}

	public abstract String modify(String value);

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + '{' +
				"claimName='" + claimName + '\'' +
				", modifierName='" + modifierName + '\'' +
				'}';
	}
}
