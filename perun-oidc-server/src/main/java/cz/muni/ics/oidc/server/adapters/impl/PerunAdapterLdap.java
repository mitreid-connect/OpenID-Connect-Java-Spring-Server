package cz.muni.ics.oidc.server.adapters.impl;

import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.ASSIGNED_GROUP_ID;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.CN;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.DESCRIPTION;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.EDU_PERSON_PRINCIPAL_NAMES;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.GIVEN_NAME;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.MEMBER_OF;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.O;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.OBJECT_CLASS;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.OU_PEOPLE;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_FACILITY;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_FACILITY_DN;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_FACILITY_ID;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_GROUP;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_GROUP_ID;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_PARENT_GROUP_ID;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_RESOURCE;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_RESOURCE_ID;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_UNIQUE_GROUP_NAME;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_USER;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_USER_ID;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_VO;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.PERUN_VO_ID;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.SN;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.UNIQUE_MEMBER;
import static cz.muni.ics.oidc.server.adapters.impl.PerunAdapterLdapConstants.UUID;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.and;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.equal;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.or;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.oidc.exceptions.InconvertibleValueException;
import cz.muni.ics.oidc.models.AttributeMapping;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.models.Resource;
import cz.muni.ics.oidc.models.Vo;
import cz.muni.ics.oidc.models.enums.PerunAttrValueType;
import cz.muni.ics.oidc.models.enums.PerunEntityType;
import cz.muni.ics.oidc.server.PerunPrincipal;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.adapters.PerunAdapterMethods;
import cz.muni.ics.oidc.server.adapters.PerunAdapterMethodsLdap;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import cz.muni.ics.oidc.server.connectors.PerunConnectorLdap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.search.FilterBuilder;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Connects to Perun using LDAP.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Martin Kuba makub@ics.muni.cz
 */
@Slf4j
public class PerunAdapterLdap extends PerunAdapterWithMappingServices implements PerunAdapterMethods, PerunAdapterMethodsLdap {

	private PerunConnectorLdap connectorLdap;
	private String oidcClientIdAttr;
	private String oidcCheckMembershipAttr;
	private final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

	public void setConnectorLdap(PerunConnectorLdap connectorLdap) {
		this.connectorLdap = connectorLdap;
	}

	public void setOidcClientIdAttr(String oidcClientIdAttr) {
		this.oidcClientIdAttr = oidcClientIdAttr;
	}

	public void setOidcCheckMembershipAttr(String oidcCheckMembershipAttr) {
		this.oidcCheckMembershipAttr = oidcCheckMembershipAttr;
	}

	/**
	 * Fetch user based on his principal (extLogin and extSource) from Perun
	 *
	 * @param perunPrincipal principal of user
	 * @return PerunUser with id of found user
	 */
	@Override
	public PerunUser getPreauthenticatedUserId(PerunPrincipal perunPrincipal) {
		FilterBuilder filter = and(
				equal(OBJECT_CLASS, PERUN_USER), equal(EDU_PERSON_PRINCIPAL_NAMES, perunPrincipal.getExtLogin())
		);
		SearchScope scope = SearchScope.ONELEVEL;
		String[] attributes = new String[]{PERUN_USER_ID, GIVEN_NAME, SN};
		EntryMapper<PerunUser> mapper = e -> {
			if (!checkHasAttributes(e, new String[] { PERUN_USER_ID, SN })) {
				return null;
			}

			long id = Long.parseLong(e.get(PERUN_USER_ID).getString());
			String firstName = (e.get(GIVEN_NAME) != null) ? e.get(GIVEN_NAME).getString() : null;
			String lastName = e.get(SN).getString();
			return new PerunUser(id, firstName, lastName);
		};

		return connectorLdap.searchFirst(OU_PEOPLE, filter, scope, attributes, mapper);
	}

	@Override
	public Facility getFacilityByClientId(String clientId) {
		if (!StringUtils.hasText(clientId)) {
			return null;
		}
		SearchScope scope = SearchScope.ONELEVEL;
		String[] attributes = new String[]{PERUN_FACILITY_ID, DESCRIPTION, CN};
		EntryMapper<Facility> mapper = e -> {
			if (!checkHasAttributes(e, attributes)) {
				return null;
			}

			long id = Long.parseLong(e.get(PERUN_FACILITY_ID).getString());
			String name = e.get(CN).getString();
			String description = e.get(DESCRIPTION).getString();

			return new Facility(id, name, description);
		};

		AttributeMapping mapping = this.getFacilityAttributesMappingService().getMappingByIdentifier(oidcClientIdAttr);

		FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_FACILITY), equal(mapping.getLdapName(), clientId));
		return connectorLdap.searchFirst(null, filter, scope, attributes, mapper);
	}

	@Override
	public boolean isMembershipCheckEnabledOnFacility(Facility facility) {
		boolean res = false;

		PerunAttributeValue attrVal = getFacilityAttributeValue(facility, oidcCheckMembershipAttr);
		if (attrVal != null && !attrVal.isNullValue()) {
			res = attrVal.valueAsBoolean();
		}

		return res;
	}

	@Override
	public boolean canUserAccessBasedOnMembership(Facility facility, Long userId) {
		Set<Long> groupsWithAccessIds = getGroupIdsWithAccessToFacility(facility.getId());
		if (groupsWithAccessIds == null || groupsWithAccessIds.isEmpty()) {
			return false;
		}

		Set<Long> userGroupIds = getGroupIdsWhereUserIsMember(userId, null);
		if (userGroupIds == null || userGroupIds.isEmpty()) {
			return false;
		}

		return !Collections.disjoint(userGroupIds, groupsWithAccessIds);
	}

	@Override
	public boolean isUserInGroup(Long userId, Long groupId) {
		String uniqueMemberValue = PERUN_USER_ID + '=' + userId + ',' + OU_PEOPLE + ',' + connectorLdap.getBaseDN();
		FilterBuilder filter = and(
				equal(OBJECT_CLASS, PERUN_GROUP),
				equal(PERUN_GROUP_ID, String.valueOf(groupId)),
				equal(UNIQUE_MEMBER, uniqueMemberValue)
		);

		EntryMapper<Long> mapper = e -> Long.parseLong(e.get(PERUN_GROUP_ID).getString());

		String[] attributes = new String[] { PERUN_GROUP_ID };

		List<Long> ids = connectorLdap.search(null, filter, SearchScope.SUBTREE, attributes, mapper);
		return ids.stream().filter(groupId::equals).count() == 1L;
	}

	@Override
	public List<Affiliation> getGroupAffiliations(Long userId, String groupAffiliationsAttr) {
		Set<Long> userGroupIds = getGroupIdsWhereUserIsMember(userId, null);
		if (userGroupIds == null || userGroupIds.isEmpty()) {
			return new ArrayList<>();
		}

		FilterBuilder[] groupIdFilters = new FilterBuilder[userGroupIds.size()];
		int i = 0;
		for (Long id: userGroupIds) {
			groupIdFilters[i++] = equal(PERUN_GROUP_ID, String.valueOf(id));
		}

		AttributeMapping affiliationsMapping = getGroupAttributesMappingService().getMappingByIdentifier(groupAffiliationsAttr);

		FilterBuilder filterBuilder = and(equal(OBJECT_CLASS, PERUN_GROUP), or(groupIdFilters));
		String[] attributes = new String[] { affiliationsMapping.getLdapName() };
		EntryMapper<Set<Affiliation>> mapper = e -> {
			Set<Affiliation> affiliations = new HashSet<>();
			if (!checkHasAttributes(e, attributes)) {
				return affiliations;
			}

			Attribute a = e.get(affiliationsMapping.getLdapName());
			long linuxTime = System.currentTimeMillis() / 1000L;
			a.iterator().forEachRemaining(v -> affiliations.add(new Affiliation(null, v.getString(), linuxTime)));

			return affiliations;
		};

		List<Set<Affiliation>> affiliationSets = connectorLdap.search(null, filterBuilder, SearchScope.SUBTREE, attributes, mapper);

		return affiliationSets.stream().flatMap(Set::stream).distinct().collect(Collectors.toList());
	}

	@Override
	public List<String> getGroupsAssignedToResourcesWithUniqueNames(Facility facility) {
		List<String> res = new ArrayList<>();

		Set<Long> groupIds = getGroupIdsWithAccessToFacility(facility.getId());
		if (groupIds == null || groupIds.isEmpty()) {
			return res;
		}

		FilterBuilder[] partialFilters = new FilterBuilder[groupIds.size()];
		int i = 0;
		for (Long id: groupIds) {
			partialFilters[i++] = equal(PERUN_GROUP_ID, String.valueOf(id));
		}

		FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_GROUP), or(partialFilters));
		String[] attributes = new String[] {PERUN_UNIQUE_GROUP_NAME};
		EntryMapper<String> mapper = e -> {
			if (!checkHasAttributes(e, attributes)) {
				return null;
			}

			return e.get(PERUN_UNIQUE_GROUP_NAME).getString();
		};

		List<String> uniqueGroupNames = connectorLdap.search(null, filter, SearchScope.SUBTREE, attributes, mapper);
		uniqueGroupNames = uniqueGroupNames.stream().filter(Objects::nonNull).collect(Collectors.toList());
		return uniqueGroupNames;
	}

	@Override
	public Vo getVoByShortName(String shortName) {
		FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_VO), equal(O, shortName));
		String[] attributes = new String[] { PERUN_VO_ID, O, DESCRIPTION };
		EntryMapper<Vo> mapper = e -> {
			if (!checkHasAttributes(e, attributes)) {
				return null;
			}

			Long id = Long.valueOf(e.get(PERUN_VO_ID).getString());
			String shortNameVo = e.get(O).getString();
			String name = e.get(DESCRIPTION).getString();

			return new Vo(id, name, shortNameVo);
		};

		return connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
	}

	@Override
	public Map<String, PerunAttributeValue> getUserAttributeValues(PerunUser user, Collection<String> attrsToFetch) {
		return this.getUserAttributeValues(user.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getUserAttributeValues(Long userId, Collection<String> attrsToFetch) {
		String dnPrefix = PERUN_USER_ID + '=' + userId + ',' + OU_PEOPLE;
		return getAttributeValues(dnPrefix, attrsToFetch, PerunEntityType.USER);
	}

	@Override
	public PerunAttributeValue getUserAttributeValue(PerunUser user, String attrToFetch) {
		return this.getUserAttributeValue(user.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getUserAttributeValue(Long userId, String attrToFetch) {
		Map<String, PerunAttributeValue> map = this.getUserAttributeValues(
				userId, Collections.singletonList(attrToFetch));
		return map.getOrDefault(attrToFetch, null);
	}

	@Override
	public Map<String, PerunAttributeValue> getFacilityAttributeValues(Facility facility, Collection<String> attrsToFetch) {
		return this.getFacilityAttributeValues(facility.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getFacilityAttributeValues(Long facilityId, Collection<String> attrsToFetch) {
		String dnPrefix = PERUN_FACILITY_ID + '=' + facilityId;
		return getAttributeValues(dnPrefix, attrsToFetch, PerunEntityType.FACILITY);
	}

	@Override
	public PerunAttributeValue getFacilityAttributeValue(Facility facility, String attrToFetch) {
		return this.getFacilityAttributeValue(facility.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getFacilityAttributeValue(Long facilityId, String attrToFetch) {
		Map<String, PerunAttributeValue> map = this.getFacilityAttributeValues(
				facilityId, Collections.singletonList(attrToFetch));
		return map.getOrDefault(attrToFetch, null);
	}

	@Override
	public Map<String, PerunAttributeValue> getVoAttributeValues(Vo vo, Collection<String> attrsToFetch) {
		return this.getVoAttributeValues(vo.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getVoAttributeValues(Long voId, Collection<String> attrsToFetch) {
		String dnPrefix = PERUN_VO_ID + '=' + voId;
		return getAttributeValues(dnPrefix, attrsToFetch, PerunEntityType.VO);
	}

	@Override
	public PerunAttributeValue getVoAttributeValue(Vo vo, String attrToFetch) {
		return this.getVoAttributeValue(vo.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getVoAttributeValue(Long voId, String attrToFetch) {
		Map<String, PerunAttributeValue> map = this.getVoAttributeValues(
				voId, Collections.singletonList(attrToFetch));
		return map.getOrDefault(attrToFetch, null);
	}

	@Override
	public Map<String, PerunAttributeValue> getGroupAttributeValues(Group group, Collection<String> attrsToFetch) {
		return this.getGroupAttributeValues(group.getVoId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getGroupAttributeValues(Long groupId, Collection<String> attrsToFetch) {
		String dnPrefix = PERUN_GROUP_ID + '=' + groupId;
		return getAttributeValues(dnPrefix, attrsToFetch, PerunEntityType.GROUP);
	}

	@Override
	public PerunAttributeValue getGroupAttributeValue(Group group, String attrToFetch) {
		return this.getGroupAttributeValue(group.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getGroupAttributeValue(Long groupId, String attrToFetch) {
		Map<String, PerunAttributeValue> map = this.getGroupAttributeValues(
				groupId, Collections.singletonList(attrToFetch));
		return map.getOrDefault(attrToFetch, null);
	}

	@Override
	public Map<String, PerunAttributeValue> getResourceAttributeValues(Resource resource, Collection<String> attrsToFetch) {
		return this.getResourceAttributeValues(resource.getVoId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getResourceAttributeValues(Long resourceId, Collection<String> attrsToFetch) {
		String dnPrefix = PERUN_RESOURCE_ID + '=' + resourceId;
		return getAttributeValues(dnPrefix, attrsToFetch, PerunEntityType.RESOURCE);
	}

	@Override
	public PerunAttributeValue getResourceAttributeValue(Resource resource, String attrToFetch) {
		return this.getResourceAttributeValue(resource.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getResourceAttributeValue(Long resourceId, String attrToFetch) {
		Map<String, PerunAttributeValue> map = this.getResourceAttributeValues(
				resourceId, Collections.singletonList(attrToFetch));
		return map.getOrDefault(attrToFetch, null);
	}

	@Override
	public Set<String> getCapabilities(Facility facility, Set<String> groupNames,
									   String facilityCapabilitiesAttrName,
									   String resourceCapabilitiesAttrName)
	{
		if (facility == null) {
			return new HashSet<>();
		} else if (groupNames == null || groupNames.isEmpty()) {
			return new HashSet<>();
		}

		Set<Long> groupIdsFromGNames = getGroupsByUniqueGroupNames(groupNames).stream()
				.map(Group::getId).collect(Collectors.toSet());

		FilterBuilder[] parts = new FilterBuilder[groupIdsFromGNames.size()];
		int i = 0;
		for (Long gid : groupIdsFromGNames) {
			parts[i] = equal(ASSIGNED_GROUP_ID, String.valueOf(gid));
			i++;
		}
		return getCapabilities(facility, facilityCapabilitiesAttrName, resourceCapabilitiesAttrName, parts);
	}

	@Override
	public Set<String> getCapabilities(Facility facility, Map<Long, String> idToGnameMap,
									   String facilityCapabilitiesAttrName, String resourceCapabilitiesAttrName)
	{
		if (facility == null) {
			return new HashSet<>();
		} else if (idToGnameMap == null || idToGnameMap.isEmpty()) {
			return new HashSet<>();
		}

		FilterBuilder[] parts = new FilterBuilder[idToGnameMap.size()];
		int i = 0;
		for (Long gid : idToGnameMap.keySet()) {
			parts[i] = equal(ASSIGNED_GROUP_ID, String.valueOf(gid));
			i++;
		}
		return getCapabilities(facility, facilityCapabilitiesAttrName, resourceCapabilitiesAttrName, parts);
	}

	@Override
	public Set<Group> getGroupsWhereUserIsActiveWithUniqueNames(Long facilityId, Long userId) {
		Set<Long> userGroups = this.getGroupIdsWhereUserIsMember(userId, null);
		Set<Long> facilityGroups = this.getGroupIdsWithAccessToFacility(facilityId);
		Set<Long> groupIds = userGroups.stream()
				.filter(facilityGroups::contains)
				.collect(Collectors.toSet());
		log.debug("Intersection of userGroups and facilityGroups: {}", groupIds);
		Set<Group> groups = new HashSet<>();

		if (groupIds.isEmpty()) {
			return groups;
		}

		List<Group> resGroups = getGroups(groupIds, PERUN_GROUP_ID);
		groups = new HashSet<>(resGroups);

		return groups;
	}

	@Override
	public Set<Long> getUserGroupsIds(Long userId, Long voId) {
		return getGroupIdsWhereUserIsMember(userId, voId);
	}

	@Override
	public boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> mandatoryVos, Set<Long> mandatoryGroups,
											   Set<Long> envVos, Set<Long> envGroups) {
		final Set<Long> foundGroupIds = new HashSet<>();
		final Set<Long> foundVoIds = new HashSet<>();
		String dnPrefix = getDnPrefixForUserId(userId);
		String[] attributes = new String[] { MEMBER_OF };
		EntryMapper<Void> mapper = e -> {
			if (checkHasAttributes(e, attributes)) {
				Attribute a = e.get(MEMBER_OF);
				a.iterator().forEachRemaining(id -> {
					String fullVal = id.getString();
					String[] parts = fullVal.split(",", 3);

					String groupId = parts[0];
					groupId = groupId.replace(PERUN_GROUP_ID + '=', "");
					foundGroupIds.add(Long.parseLong(groupId));
					String voIdStr = parts[1];
					voIdStr = voIdStr.replace(PERUN_VO_ID + '=', "");
					foundVoIds.add(Long.parseLong(voIdStr));
				});
			}
			return null;
		};
		connectorLdap.lookup(dnPrefix, attributes, mapper);

		return PerunAdapter.decideAccess(foundVoIds, foundGroupIds, mandatoryVos, mandatoryGroups, envVos, envGroups);
	}

	@Override
	public boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> vos, Set<Long> groups) {
		final Set<Long> foundGroupIds = new HashSet<>();
		final Set<Long> foundVoIds = new HashSet<>();
		String dnPrefix = getDnPrefixForUserId(userId);
		String[] attributes = new String[] { MEMBER_OF };
		EntryMapper<Void> mapper = e -> {
			if (checkHasAttributes(e, attributes)) {
				Attribute a = e.get(MEMBER_OF);
				a.iterator().forEachRemaining(id -> {
					String fullVal = id.getString();
					String[] parts = fullVal.split(",", 3);

					String groupId = parts[0];
					groupId = groupId.replace(PERUN_GROUP_ID + '=', "");
					foundGroupIds.add(Long.parseLong(groupId));
					String voIdStr = parts[1];
					voIdStr = voIdStr.replace(PERUN_VO_ID + '=', "");
					foundVoIds.add(Long.parseLong(voIdStr));
				});
			}
			return null;
		};
		connectorLdap.lookup(dnPrefix, attributes, mapper);

		return PerunAdapter.decideAccess(foundVoIds, foundGroupIds, vos, groups);
	}

	@Override
	public boolean isUserInVo(Long userId, String voShortName) {
		if (userId == null) {
			throw new IllegalArgumentException("No userId");
		} else if (!StringUtils.hasText(voShortName)) {
			throw new IllegalArgumentException("No voShortName");
		}

		String uniqueMember = getDnPrefixForUserId(userId) + ',' + this.connectorLdap.getBaseDN();
		FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_VO), equal(UNIQUE_MEMBER, uniqueMember), equal(O, voShortName));
		String[] attributes = new String[] { PERUN_VO_ID, O, DESCRIPTION };
		EntryMapper<Vo> mapper = e -> {
			if (!checkHasAttributes(e, attributes)) {
				return null;
			}

			Long id = Long.valueOf(e.get(PERUN_VO_ID).getString());
			String shortNameVo = e.get(O).getString();
			String name = e.get(DESCRIPTION).getString();

			return new Vo(id, name, shortNameVo);
		};

		Vo vo = connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
		return vo != null;
	}

	private List<Group> getGroups(Collection<?> objects, String objectAttribute) {
		List<Group> result;
		if (objects == null || objects.size() <= 0) {
			result = new ArrayList<>();
		} else {
			FilterBuilder filter;
			if (objects.size() == 1) {
				Object first = objects.toArray()[0];
				filter = and(equal(OBJECT_CLASS, PERUN_GROUP), equal(objectAttribute, String.valueOf(first)));
			} else {
				FilterBuilder[] partialFilters = new FilterBuilder[objects.size()];
				int i = 0;
				for (Object obj: objects) {
					partialFilters[i++] = equal(objectAttribute, String.valueOf(obj));
				}
				filter = and(equal(OBJECT_CLASS, PERUN_GROUP), or(partialFilters));
			}

			String[] attributes = new String[]{PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME,
					PERUN_VO_ID, PERUN_PARENT_GROUP_ID, UUID};

			EntryMapper<Group> mapper = e -> {
				if (!checkHasAttributes(e, new String[]{
						PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, UUID }))
				{
					return null;
				}

				Long id = Long.valueOf(e.get(PERUN_GROUP_ID).getString());
				String name = e.get(CN).getString();
				String description = e.get(DESCRIPTION).getString();
				String uniqueName = e.get(PERUN_UNIQUE_GROUP_NAME).getString();
				Long voId = Long.valueOf(e.get(PERUN_VO_ID).getString());
				Long parentGroupId = null;
				if (e.get(PERUN_PARENT_GROUP_ID) != null) {
					parentGroupId = Long.valueOf(e.get(PERUN_PARENT_GROUP_ID).getString());
				}
				String uuid = e.get(UUID).getString();

				return new Group(id, parentGroupId, name, description, uniqueName,uuid, voId);
			};

			result = connectorLdap.search(null, filter, SearchScope.SUBTREE, attributes, mapper);
			result = result.stream().filter(Objects::nonNull).collect(Collectors.toList());
		}

		return result;
	}

	private Set<Long> getGroupIdsWhereUserIsMember(Long userId, Long voId) {
		String dnPrefix = getDnPrefixForUserId(userId);
		String[] attributes = new String[] { MEMBER_OF };
		EntryMapper<Set<Long>> mapper = e -> {
			Set<Long> ids = new HashSet<>();
			if (checkHasAttributes(e, attributes)) {
				Attribute a = e.get(MEMBER_OF);
				a.iterator().forEachRemaining(id -> {
					String fullVal = id.getString();
					String[] parts = fullVal.split(",", 3);

					String groupId = parts[0];
					groupId = groupId.replace(PERUN_GROUP_ID + '=', "");

					String voIdStr = parts[1];
					voIdStr = voIdStr.replace(PERUN_VO_ID + '=', "");

					if (voId == null || voId.equals(Long.parseLong(voIdStr))) {
						ids.add(Long.parseLong(groupId));
					}
				});
			}

			return ids;
		};

		return connectorLdap.lookup(dnPrefix, attributes, mapper);
	}

	private String getDnPrefixForUserId(Long userId) {
		return PERUN_USER_ID + '=' + userId + ',' + OU_PEOPLE;
	}

	private Set<Long> getGroupIdsWithAccessToFacility(Long facilityId) {
		FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_RESOURCE), equal(PERUN_FACILITY_ID, String.valueOf(facilityId)));
		String[] attributes = new String[] { ASSIGNED_GROUP_ID };
		EntryMapper<Set<Long>> mapper = e -> {
			Set<Long> ids = new HashSet<>();
			if (checkHasAttributes(e, attributes)) {
				Attribute a = e.get(ASSIGNED_GROUP_ID);
				if (a != null) {
					a.iterator().forEachRemaining(id -> ids.add(Long.valueOf(id.getString())));
				}
			}

			return ids;
		};

		List<Set<Long>> assignedGroupIdsAll = connectorLdap.search(null, filter, SearchScope.SUBTREE, attributes, mapper);
		return assignedGroupIdsAll.stream()
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	private Map<String, PerunAttributeValue> getAttributeValues(String dnPrefix, Collection<String> attrsToFetch,
																PerunEntityType entity) {
		Set<AttributeMapping> mappings = this.getMappingsForAttrNames(entity, attrsToFetch);
		String[] attributes = this.getAttributesFromMappings(mappings);

		Map<String, PerunAttributeValue> res = new HashMap<>();
		if (attributes.length != 0) {
			EntryMapper<Map<String, PerunAttributeValue>> mapper = attrValueMapper(mappings);
			res = this.connectorLdap.lookup(dnPrefix, attributes, mapper);
		}

		return res;
	}

	private List<Group> getGroupsByUniqueGroupNames(Set<String> groupNames) {
		List<Group> groups = getGroups(groupNames, PERUN_UNIQUE_GROUP_NAME);
		groups = groups.stream().filter(Objects::nonNull).collect(Collectors.toList());

		return groups;
	}

	private boolean checkHasAttributes(Entry e, String[] attributes) {
		if (e == null) {
			return false;
		} else if (attributes == null) {
			return true;
		}

		for (String attr: attributes) {
			if (e.get(attr) == null) {
				return false;
			}
		}

		return true;
	}

	private EntryMapper<Map<String, PerunAttributeValue>> attrValueMapper(Set<AttributeMapping> attrMappings) {
		return entry -> {
			Map<String, PerunAttributeValue> resultMap = new LinkedHashMap<>();
			Map<String, Attribute> attrNamesMap = new HashMap<>();

			for (Attribute attr : entry.getAttributes()) {
				if (attr.isHumanReadable()) {
					attrNamesMap.put(attr.getId(), attr);
				}
			}

			for (AttributeMapping mapping: attrMappings) {
				if (mapping.getLdapName() == null || mapping.getLdapName().isEmpty()) {
					continue;
				}
				String ldapAttrName = mapping.getLdapName();
				// the library always converts name of attribute to lowercase, therefore we need to convert it as well
				Attribute attribute = attrNamesMap.getOrDefault(ldapAttrName.toLowerCase(), null);
				PerunAttributeValue value = parseValue(attribute, mapping);
				resultMap.put(mapping.getIdentifier(), value);
			}

			return resultMap;
		};
	}

	private PerunAttributeValue parseValue(Attribute attr, AttributeMapping mapping) {
		PerunAttrValueType type = mapping.getAttrType();
		boolean isNull = (attr == null || attr.get() == null || attr.get().isNull());
		switch (type) {
			case STRING:
				return new PerunAttributeValue(mapping.getIdentifier(), PerunAttributeValue.STRING_TYPE,
						isNull ? jsonNodeFactory.nullNode() : jsonNodeFactory.textNode(attr.get().getString()));
			case INTEGER:
				return new PerunAttributeValue(mapping.getIdentifier(), PerunAttributeValue.INTEGER_TYPE,
						isNull ? jsonNodeFactory.nullNode() : jsonNodeFactory.numberNode(Long.parseLong(attr.get().getString())));
			case BOOLEAN:
				return new PerunAttributeValue(mapping.getIdentifier(), PerunAttributeValue.BOOLEAN_TYPE,
						isNull ? jsonNodeFactory.booleanNode(false) : jsonNodeFactory.booleanNode(Boolean.parseBoolean(attr.get().getString())));
			case ARRAY:
				return new PerunAttributeValue(mapping.getIdentifier(), PerunAttributeValue.ARRAY_TYPE,
						isNull ? jsonNodeFactory.arrayNode() : getArrNode(attr));
			case MAP_JSON:
				return new PerunAttributeValue(mapping.getIdentifier(), PerunAttributeValue.MAP_TYPE,
						isNull ? jsonNodeFactory.objectNode() : getMapNodeJson(attr));
			case MAP_KEY_VALUE:
				return new PerunAttributeValue(mapping.getIdentifier(), PerunAttributeValue.MAP_TYPE,
						isNull ? jsonNodeFactory.objectNode() : getMapNodeSeparator(attr, mapping.getSeparator()));
			default:
				throw new IllegalArgumentException("unrecognized type");
		}

	}

	private ObjectNode getMapNodeSeparator(Attribute attr, String separator) {
		ObjectNode objectNode = jsonNodeFactory.objectNode();
		for (Value value : attr) {
			if (value.getString() != null) {
				String[] parts = value.getString().split(separator, 2);
				objectNode.put(parts[0], parts[1]);
			}
		}
		return objectNode;
	}

	private ObjectNode getMapNodeJson(Attribute attr) {
		String jsonStr = attr.get().getString();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readValue(jsonStr, ObjectNode.class);
		} catch (IOException e) {
			throw new InconvertibleValueException("Could not parse value");
		}
	}

	private ArrayNode getArrNode(Attribute attr) {
		ArrayNode arrayNode = jsonNodeFactory.arrayNode(attr.size());
		for (Value value : attr) {
			arrayNode.add(value.getString());
		}
		return arrayNode;
	}

	private boolean isNumber(String s) {
		try {
			Long.parseLong(s);
			return true;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

	private Set<AttributeMapping> getMappingsForAttrNames(PerunEntityType entity, Collection<String> attrsToFetch) {
		Set<AttributeMapping> mappings;
		switch (entity) {
			case USER:
				mappings = this.getUserAttributesMappingService()
						.getMappingsByIdentifiers(attrsToFetch);
				break;
			case FACILITY:
				mappings = this.getFacilityAttributesMappingService()
						.getMappingsByIdentifiers(attrsToFetch);
				break;
			case VO:
				mappings = this.getVoAttributesMappingService()
						.getMappingsByIdentifiers(attrsToFetch);
				break;
			case GROUP:
				mappings = this.getGroupAttributesMappingService()
						.getMappingsByIdentifiers(attrsToFetch);
				break;
			case RESOURCE:
				mappings = this.getResourceAttributesMappingService()
						.getMappingsByIdentifiers(attrsToFetch);
				break;
			default:
				log.error("Unknown ENTITY {}", entity);
				mappings = new HashSet<>();
				break;
		}

		return mappings;
	}

	private String[] getAttributesFromMappings(Set<AttributeMapping> mappings) {
		return mappings.stream()
				.map(AttributeMapping::getLdapName)
				.distinct()
				.collect(Collectors.toList())
				.toArray(new String[] {});
	}

	private Set<String> getFacilityCapabilities(Facility facility, String capabilitiesAttrName) {
		Set<String> result = new HashSet<>();
		PerunAttributeValue attrVal = getFacilityAttributeValue(facility, capabilitiesAttrName);
		if (attrVal != null && !attrVal.isNullValue() && attrVal.valueAsList() != null) {
			result = new HashSet<>(attrVal.valueAsList());
		}

		return result;
	}

	private Set<String> getCapabilities(Facility facility, String facilityCapabilitiesAttrName,
										String resourceCapabilitiesAttrName, FilterBuilder[] parts) {
		String facilityDN = PERUN_FACILITY_ID + '=' + facility.getId() + ',' + connectorLdap.getBaseDN();
		FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_RESOURCE), equal(PERUN_FACILITY_DN, facilityDN), or(parts));
		AttributeMapping capabilitiesMapping = getResourceAttributesMappingService().getMappingByIdentifier(resourceCapabilitiesAttrName);
		String[] attributes = new String[] { capabilitiesMapping.getLdapName(), ASSIGNED_GROUP_ID };
		EntryMapper<Set<String>> mapper = e -> {
			Set<String> capabilities = new HashSet<>();
			if (!checkHasAttributes(e, attributes)) {
				return new HashSet<>();
			}

			Attribute capabilitiesAttr = e.get(capabilitiesMapping.getLdapName());
			if (capabilitiesAttr != null) {
				capabilitiesAttr.iterator().forEachRemaining(v -> capabilities.add(v.getString()));
			}

			return capabilities;
		};
		List<Set<String>> resourceCaps = connectorLdap.search(null, filter, SearchScope.SUBTREE, attributes, mapper);
		Set<String> capabilities = new HashSet<>();
		boolean includeFacilityCapabilities = false;
		if (resourceCaps != null && !resourceCaps.isEmpty()) {
			// if the mapper returns at least one entry, user is a member of some group assigned to the facility
			includeFacilityCapabilities = true;
			resourceCaps.stream()
					.filter(Objects::nonNull)
					.forEach(capabilities::addAll);
		}

		if (facilityCapabilitiesAttrName != null && includeFacilityCapabilities ) {
			Set<String> facilityCapabilities = this.getFacilityCapabilities(facility, facilityCapabilitiesAttrName);
			capabilities.addAll(facilityCapabilities);
		}

		return capabilities;
	}

}
