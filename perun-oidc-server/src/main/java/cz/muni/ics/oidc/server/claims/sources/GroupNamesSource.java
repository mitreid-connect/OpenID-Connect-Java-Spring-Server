package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Source fetches all unique group names in context of user and facility. If no facility exists for the client, empty
 * list is returned as result.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class GroupNamesSource extends ClaimSource {

	protected static final String MEMBERS = "members";

	public GroupNamesSource(ClaimSourceInitContext ctx) {
		super(ctx);
		log.debug("{} - initialized", getClaimName());
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		return Collections.emptySet();
	}

	@Override
	public JsonNode produceValue(ClaimSourceProduceContext pctx) {
		Map<Long, String> idToNameMap = this.produceGroupNames(pctx);
		JsonNode result = convertResultStringsToJsonArray(new HashSet<>(idToNameMap.values()));
		log.debug("{} - produced value for user({}): '{}'", getClaimName(), pctx.getPerunUserId(), result);
		return result;
	}

	protected Map<Long, String> produceGroupNames(ClaimSourceProduceContext pctx) {
		log.trace("{} - produce group names with trimming 'members' part of the group names", getClaimName());
		Facility facility = pctx.getContextCommonParameters().getClient();
		Set<Group> userGroups = getUserGroupsOnFacility(facility, pctx.getPerunUserId(), pctx.getPerunAdapter());
		return getGroupIdToNameMap(userGroups, true);
	}

	protected Map<Long, String> getGroupIdToNameMap(Set<Group> userGroups, boolean trimMembers) {
		Map<Long, String> idToNameMap = new HashMap<>();
		userGroups.forEach(g -> {
			String uniqueName = g.getUniqueGroupName();
			if (trimMembers && StringUtils.hasText(uniqueName) && MEMBERS.equals(g.getName())) {
				uniqueName = uniqueName.replace(':' + MEMBERS, "");
				g.setUniqueGroupName(uniqueName);
			}

			idToNameMap.put(g.getId(), g.getUniqueGroupName());
		});

		log.trace("{} - group ID to group name map: '{}'", getClaimName(), idToNameMap);
		return idToNameMap;
	}

	protected Set<Group> getUserGroupsOnFacility(Facility facility, Long userId, PerunAdapter perunAdapter) {
		Set<Group> userGroups = new HashSet<>();
		if (facility == null) {
			log.warn("{} - no facility provided when searching for user groups, will return empty set", getClaimName());
		} else {
			userGroups = perunAdapter.getGroupsWhereUserIsActiveWithUniqueNames(facility.getId(), userId);
		}
		log.trace("{} - found user groups: '{}'", getClaimName(), userGroups);
		return userGroups;
	}

	protected JsonNode convertResultStringsToJsonArray(Collection<String> collection) {
		ArrayNode arr = JsonNodeFactory.instance.arrayNode();
		collection.forEach(arr::add);
		return arr;
	}

}
