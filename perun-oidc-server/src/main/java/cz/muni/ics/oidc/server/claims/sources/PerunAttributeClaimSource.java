package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Source for claim which get value of attribute from Perun.
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.attribute</b> - name of the attribute in Perun</li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
@Slf4j
public class PerunAttributeClaimSource extends ClaimSource {

	private static final String ATTRIBUTE = "attribute";

	private final String attributeName;

	public PerunAttributeClaimSource(ClaimSourceInitContext ctx) {
		super(ctx);
		this.attributeName = ClaimUtils.fillStringPropertyOrNoVal(ATTRIBUTE, ctx);
		if (!ClaimUtils.isPropSet(this.attributeName)) {
			throw new IllegalArgumentException("Missing mandatory configuration option - " + ATTRIBUTE);
		}
		log.debug("{} - attributeName: '{}'", getClaimName(), attributeName);
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		return Collections.singleton(attributeName);
	}

	@Override
	public JsonNode produceValue(ClaimSourceProduceContext pctx) {
		JsonNode value = NullNode.getInstance();
		if (ClaimUtils.isPropSetAndHasAttribute(attributeName, pctx)) {
			value = pctx.getAttrValues().get(attributeName).valueAsJson();
		}

		log.debug("{} - produced value for user({}): '{}'", getClaimName(), pctx.getPerunUserId(), value);
		return value;
	}

	@Override
	public String toString() {
		return "Perun attribute " + attributeName;
	}

}
