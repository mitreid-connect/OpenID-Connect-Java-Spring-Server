package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Claim source which takes value from two attributes from Perun.
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.attribute1</b> - name of the first attribute in Perun</li>
 *     <li><b>custom.claim.[claimName].source.attribute2</b> - name of the second attribute in Perun</li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
@Slf4j
public class TwoArrayAttributesClaimSource extends ClaimSource {

	public static final String ATTRIBUTE_1 = "attribute1";
	public static final String ATTRIBUTE_2 = "attribute2";

	private final String attribute1Name;
	private final String attribute2Name;

	public TwoArrayAttributesClaimSource(ClaimSourceInitContext ctx) {
		super(ctx);
		this.attribute1Name = ClaimUtils.fillStringPropertyOrNoVal(ATTRIBUTE_1, ctx);
		if (!ClaimUtils.isPropSet(this.attribute1Name)) {
			throw new IllegalArgumentException(getClaimName() + " - missing mandatory configuration option: " +
					ATTRIBUTE_1);
		}
		this.attribute2Name = ClaimUtils.fillStringPropertyOrNoVal(ATTRIBUTE_2, ctx);
		if (!ClaimUtils.isPropSet(this.attribute2Name)) {
			throw new IllegalArgumentException(getClaimName() + " - missing mandatory configuration option: " +
					ATTRIBUTE_2);
		}
		log.debug("{} - attribute1Name: '{}', attribute2Name: '{}'", getClaimName(), attribute1Name, attribute2Name);
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		return new HashSet<>(Arrays.asList(attribute1Name, attribute1Name));
	}

	@Override
	public JsonNode produceValue(ClaimSourceProduceContext pctx) {
		JsonNode j1 = new ArrayNode(JsonNodeFactory.instance);
		if (ClaimUtils.isPropSetAndHasAttribute(attribute1Name, pctx)) {
			j1 = pctx.getAttrValues().get(attribute1Name).valueAsJson();
		}
		log.trace("{} - found values for '{}': {}", getClaimName(), attribute1Name, j1);

		JsonNode j2 = new ArrayNode(JsonNodeFactory.instance);
		if (ClaimUtils.isPropSetAndHasAttribute(attribute2Name, pctx)) {
			j2 = pctx.getAttrValues().get(attribute2Name).valueAsJson();
		}
		log.trace("{} - found values for '{}': {}", getClaimName(), attribute2Name, j2);

		JsonNode result;
		if (j1 == null || j1.isNull() || !j1.isArray()) {
			result = j2;
		}  else if (j2 == null || j2.isNull() || !j2.isArray()) {
			result = j1;
		} else {
			ArrayNode a1 = (ArrayNode) j1;
			ArrayNode a2 = (ArrayNode) j2;
			ArrayNode arr = a1.arrayNode(a1.size() + a2.size());
			arr.addAll(a1);
			arr.addAll(a2);
			result = arr;
		}
		log.debug("{} - produced value for user({}): '{}'", getClaimName(), pctx.getPerunUserId(), result);
		return result;
	}

}
