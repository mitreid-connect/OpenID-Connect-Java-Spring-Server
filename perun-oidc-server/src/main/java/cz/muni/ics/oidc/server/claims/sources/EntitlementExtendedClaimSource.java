package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.external.com.google.gdata.util.common.base.PercentEscaper;

@Slf4j
public class EntitlementExtendedClaimSource extends EntitlementSource {

    private static final String GROUP = "group";
    private static final String GROUP_ATTRIBUTES = "groupAttributes";
    private static final String DISPLAY_NAME = "displayName";

    private static final PercentEscaper ESCAPER = new PercentEscaper("-_.!~*'()", false);

    public EntitlementExtendedClaimSource(ClaimSourceInitContext ctx) {
        super(ctx);
        log.debug("{} - initialized", getClaimName());
    }

    @Override
    public Set<String> getAttrIdentifiers() {
        return super.getAttrIdentifiers();
    }

    @Override
    public JsonNode produceValue(ClaimSourceProduceContext pctx) {
        Long userId = pctx.getPerunUserId();
        Set<String> entitlements = produceEntitlementsExtended(pctx.getFacility(),
                userId, pctx.getPerunAdapter());
        JsonNode result = convertResultStringsToJsonArray(entitlements);
        log.debug("{} - produced value for user({}): '{}'", getClaimName(), userId, result);
        return result;
    }

    private Set<String> produceEntitlementsExtended(Facility facility, Long userId, PerunAdapter perunAdapter) {
        Set<Group> userGroups = getUserGroupsOnFacility(facility, userId, perunAdapter);
        Map<Long, String> groupIdToNameMap = super.getGroupIdToNameMap(userGroups, false);
        Set<String> entitlements = new TreeSet<>();
        this.fillUuidEntitlements(userGroups, entitlements);
        fillForwardedEntitlements(perunAdapter, userId, entitlements);
        fillCapabilities(facility, perunAdapter, groupIdToNameMap,entitlements);
        log.trace("{} - UUID entitlements added", getClaimName());
        return entitlements;
    }

    private void fillUuidEntitlements(Set<Group> userGroups, Set<String> entitlements) {
        for (Group group : userGroups) {
            String displayName = group.getUniqueGroupName();
            if (StringUtils.hasText(displayName) && MEMBERS.equals(group.getName())) {
                displayName = displayName.replace(':' + MEMBERS, "");
            }
            String entitlement = wrapGroupEntitlementToAARC(group.getUuid());
            log.trace("{} - added UUID entitlement: '{}'", getClaimName(), entitlement);
            entitlements.add(entitlement);
            String entitlementWithAttributes = wrapGroupEntitlementToAARCWithAttributes(group.getUuid(), displayName);
            log.trace("{} - added UUID entitlement with displayName: '{}'", getClaimName(), entitlementWithAttributes);
            entitlements.add(entitlementWithAttributes);
        }
    }

    private String wrapGroupEntitlementToAARC(String uuid) {
        return addPrefixAndSuffix(GROUP + ':' + uuid);
    }

    private String wrapGroupEntitlementToAARCWithAttributes(String uuid, String displayName) {
        return addPrefixAndSuffix(GROUP_ATTRIBUTES + ':' + uuid + "?=" + DISPLAY_NAME + '=' +
                ESCAPER.escape(displayName));
    }

}
