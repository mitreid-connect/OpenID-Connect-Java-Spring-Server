package cz.muni.ics.oidc.server.claims;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Keeps definition of a custom user claim.
 *
 * Configuration declaring custom claims:
 * <ul>
 *     <li><b>custom.claims</b> - coma separated list of names of the claims</li>
 * </ul>
 *
 * Configuration for claim(replace [claimName] with name of the claim): *
 * <ul>
 *     <li><b>custom.claim.[claimName].claim</b> - name of the claim</li>
 *     <li><b>custom.claim.[claimName].scope</b> - scope that needs to be granted to include the claim</li>
 *     <li><b>custom.claim.[claimName].source.class</b> instance of a class implementing {@link ClaimSource}</li>
 *     <li><b>custom.claim.[claimName].modifier.class</b> instance of a class implementing {@link ClaimModifier}</li>
 * </ul>
 *
 *
 * @see ClaimSource
 * @see ClaimModifier
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class PerunCustomClaimDefinition {

	private final String scope;
	private final String claim;
	private final ClaimSource claimSource;
	private final List<ClaimModifier> claimModifiers = new ArrayList<>();

	public PerunCustomClaimDefinition(String scope,
									  String claim,
									  ClaimSource claimSource,
									  List<ClaimModifier> claimModifiers) {
		this.scope = scope;
		this.claim = claim;
		this.claimSource = claimSource;
		this.claimModifiers.addAll(claimModifiers);
		log.debug("initialized scope '{}' with claim '{}', claimSource '{}' and modifiers '{}", scope, claim,
				(claimSource != null ? claimSource.getClass().getSimpleName() : "none"),
				(!claimModifiers.isEmpty() ? claimModifiers.stream()
						.map(cm -> cm.getClass().getSimpleName())
						.collect(Collectors.joining(",")) : "none")
		);
	}

	public String getScope() {
		return scope;
	}

	public String getClaim() {
		return claim;
	}

	public ClaimSource getClaimSource() {
		return claimSource;
	}

	public List<ClaimModifier> getClaimModifiers() {
		return claimModifiers;
	}

}
