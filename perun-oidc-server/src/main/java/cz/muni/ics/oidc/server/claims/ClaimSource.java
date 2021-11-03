package cz.muni.ics.oidc.server.claims;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for code that can produce claim values.
 *
 * @see cz.muni.ics.oidc.server.claims.sources for different implementations of claim sources
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
public abstract class ClaimSource {

	private static final Logger log = LoggerFactory.getLogger(ClaimSource.class);

	private final String claimName;

	public ClaimSource(ClaimSourceInitContext ctx) {
		this.claimName = ctx.getClaimName();
		log.debug("{} - claim source initialized", getClaimName());
	}

	public String getClaimName() {
		return claimName;
	}

	public abstract Set<String> getAttrIdentifiers();

	public abstract JsonNode produceValue(ClaimSourceProduceContext pctx);

	@Override
	public String toString() {
		return this.getClass().getName();
	}

}
