package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Source produces MINIO policies based on groups assigned to the facility resource and username.
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.groupAttribute</b> - group attribute name containing values for claim</li>
 *     <li><b>custom.claim.[claimName].source.usernamePolicyAttribute</b> - attribute mapping for username to add it as another policy</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class MinIOPoliciesClaimSource extends ResourceAssignedActiveMemberGroupsClaimSource {

	protected static final String USERNAME_POLICY_ATTRIBUTE = "usernamePolicyAttribute";
	private final String usernamePolicyAttribute;

	public MinIOPoliciesClaimSource(ClaimSourceInitContext ctx) {
		super(ctx);
		this.usernamePolicyAttribute = ClaimUtils.fillStringMandatoryProperty(
				USERNAME_POLICY_ATTRIBUTE, ctx, ctx.getClaimName());
		log.debug("{} - groupPoliciesAttribute: '{}', usernamePolicyAttribute: '{}'",
				getClaimName(), super.getGroupAttribute(), usernamePolicyAttribute);
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		Set<String> set = new HashSet<>(super.getAttrIdentifiers());
		set.add(usernamePolicyAttribute);
		return set;
	}

	@Override
	public JsonNode produceValue(ClaimSourceProduceContext pctx) {
		Long userId = pctx.getPerunUserId();
		Set<String> policies = producePolicies(pctx);
		JsonNode result = ClaimUtils.convertResultStringsToJsonArray(policies);
		log.debug("{} - produced value for user({}): '{}'", getClaimName(), userId, result);
		return result;
	}

	protected Set<String> producePolicies(ClaimSourceProduceContext pctx)
	{
		Set<String> policies = super.produceSetValue(pctx);
		String userNamePolicy = getUsername(pctx.getPerunUserId(), pctx.getPerunAdapter());
		if (userNamePolicy != null) {
			policies.add(userNamePolicy);
		}

		return policies;
	}

	private String getUsername(Long userId, PerunAdapter perunAdapter) {
		PerunAttributeValue usernameAttrValue = perunAdapter.getAdapterFallback().getUserAttributeValue(userId, usernamePolicyAttribute);
		if (usernameAttrValue != null && !usernameAttrValue.isNullValue() && StringUtils.hasText(usernameAttrValue.valueAsString())) {
			return usernameAttrValue.valueAsString();
		}
		return null;
	}

}
