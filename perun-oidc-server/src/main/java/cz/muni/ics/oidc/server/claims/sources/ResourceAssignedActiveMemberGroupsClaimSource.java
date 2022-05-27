package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Source produces values on attribute value of groups assigned to the facility resource.
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.groupAttribute</b> - group attribute name containing values for claim</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
@Getter
public class ResourceAssignedActiveMemberGroupsClaimSource extends ClaimSource {

	protected static final String GROUP_ATTRIBUTE = "groupAttribute";

	private final String groupAttribute;

	public ResourceAssignedActiveMemberGroupsClaimSource(ClaimSourceInitContext ctx) {
		super(ctx);
		this.groupAttribute = ClaimUtils.fillStringMandatoryProperty(GROUP_ATTRIBUTE, ctx, ctx.getClaimName());
		log.debug("{} - groupAttribute: '{}'", getClaimName(), groupAttribute);
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		return Set.of(groupAttribute);
	}

	@Override
	public JsonNode produceValue(ClaimSourceProduceContext pctx) {
		Long userId = pctx.getPerunUserId();

		Set<String> value = produceSetValue(pctx);

		JsonNode result = ClaimUtils.convertResultStringsToJsonArray(value);
		log.debug("{} - produced value for user({}): '{}'", getClaimName(), userId, result);
		return result;
	}

	protected Set<String> produceSetValue(ClaimSourceProduceContext pctx) {
		Long userId = pctx.getPerunUserId();
		Facility facility = pctx.getFacility();
		PerunAdapter perunAdapter = pctx.getPerunAdapter();
		Set<Group> userGroups = ClaimUtils.getUserGroupsOnFacility(facility, userId, perunAdapter, getClaimName());
		return getValuesFromAttribute(userGroups, perunAdapter);
	}

	protected Set<String> getValuesFromAttribute(Set<Group> userGroups, PerunAdapter perunAdapter) {
		Set<String> policies = new HashSet<>();
		for (Group g: userGroups) {
			PerunAttributeValue policiesAttrValue = perunAdapter.getAdapterFallback()
					.getGroupAttributeValue(g, groupAttribute);
			if (policiesAttrValue != null && !policiesAttrValue.isNullValue() && policiesAttrValue.valueAsList() != null) {
				policies.addAll(policiesAttrValue.valueAsList());
			}
		}

		return policies;
	}

}

