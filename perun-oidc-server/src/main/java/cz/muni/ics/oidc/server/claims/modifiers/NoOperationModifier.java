package cz.muni.ics.oidc.server.claims.modifiers;

import cz.muni.ics.oidc.server.claims.ClaimModifier;
import cz.muni.ics.oidc.server.claims.ClaimModifierInitContext;

/**
 * No operation modifier. Class just for consistent working with modifiers to avoid checking for null references.
 *
 * No further configuration.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class NoOperationModifier extends ClaimModifier {

	public NoOperationModifier(ClaimModifierInitContext ctx) {
		super(ctx);
	}

	@Override
	public String modify(String value) {
		return value;
	}

	@Override
	public String toString() {
		return getUnifiedName() + " - No operation modifier";
	}

}
