package cz.muni.ics.oidc.server.adapters.impl;

import static cz.muni.ics.oidc.models.PerunAttributeValue.STRING_TYPE;
import static cz.muni.ics.oidc.models.enums.MemberStatus.VALID;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.ATTRIBUTES_MANAGER;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.FACILITIES_MANAGER;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.GROUPS_MANAGER;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.MEMBERS_MANAGER;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.REGISTRAR_MANAGER;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.RESOURCES_MANAGER;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.USERS_MANAGER;
import static cz.muni.ics.oidc.server.connectors.PerunConnectorRpc.VOS_MANAGER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import cz.muni.ics.oidc.models.AttributeMapping;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.Member;
import cz.muni.ics.oidc.models.Model;
import cz.muni.ics.oidc.models.PerunAttribute;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.models.Resource;
import cz.muni.ics.oidc.models.UserExtSource;
import cz.muni.ics.oidc.models.Vo;
import cz.muni.ics.oidc.models.enums.MemberStatus;
import cz.muni.ics.oidc.models.enums.PerunEntityType;
import cz.muni.ics.oidc.models.mappers.RpcMapper;
import cz.muni.ics.oidc.server.PerunPrincipal;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.adapters.PerunAdapterMethods;
import cz.muni.ics.oidc.server.adapters.PerunAdapterMethodsRpc;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import cz.muni.ics.oidc.server.connectors.PerunConnectorRpc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Interface for fetching data from Perun via RPC
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @author Dominik František Bučík bucik@ics.muni.cz
 * @author Peter Jancus jancus@ics.muni.cz
 */
@Slf4j
public class PerunAdapterRpc extends PerunAdapterWithMappingServices implements PerunAdapterMethods, PerunAdapterMethodsRpc {

	private PerunConnectorRpc connectorRpc;

	private String oidcClientIdAttr;
	private String oidcCheckMembershipAttr;
	private String orgUrlAttr;
	private String affiliationsAttr;

	public void setConnectorRpc(PerunConnectorRpc connectorRpc) {
		this.connectorRpc = connectorRpc;
	}

	public void setOidcClientIdAttr(String oidcClientIdAttr) {
		this.oidcClientIdAttr = oidcClientIdAttr;
	}

	public void setOidcCheckMembershipAttr(String oidcCheckMembershipAttr) {
		this.oidcCheckMembershipAttr = oidcCheckMembershipAttr;
	}

	public void setOrgUrlAttr(String orgUrlAttr) {
		this.orgUrlAttr = orgUrlAttr;
	}

	public void setAffiliationsAttr(String affiliationsAttr) {
		this.affiliationsAttr = affiliationsAttr;
	}

	@Override
	public PerunUser getPreauthenticatedUserId(PerunPrincipal perunPrincipal) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("extLogin", perunPrincipal.getExtLogin());
		map.put("extSourceName", perunPrincipal.getExtSourceName());

		JsonNode response = connectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map);
		return RpcMapper.mapPerunUser(response);
	}

	@Override
	public Facility getFacilityByClientId(String clientId) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		} else if (!StringUtils.hasText(clientId)) {
			return null;
		}

		AttributeMapping mapping = this.getFacilityAttributesMappingService().getMappingByIdentifier(oidcClientIdAttr);

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("attributeName", mapping.getRpcName());
		map.put("attributeValue", clientId);
		JsonNode jsonNode = connectorRpc.post(FACILITIES_MANAGER, "getFacilitiesByAttribute", map);

		return (jsonNode.size() > 0) ? RpcMapper.mapFacility(jsonNode.get(0)) : null;
	}

	@Override
	public boolean isMembershipCheckEnabledOnFacility(Facility facility) {
		if (!this.connectorRpc.isEnabled()) {
			return false;
		}

		AttributeMapping mapping = this.getFacilityAttributesMappingService().getMappingByIdentifier(oidcCheckMembershipAttr);

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("facility", facility.getId());
		map.put("attributeName", mapping.getRpcName());
		JsonNode res = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", map);

		return res.get("value").asBoolean(false);
	}

	@Override
	public boolean canUserAccessBasedOnMembership(Facility facility, Long userId) {
		if (!this.connectorRpc.isEnabled()) {
			return true;
		}

		List<Group> activeGroups = getGroupsWhereUserIsActive(facility, userId);
		return !activeGroups.isEmpty();
	}

	@Override
	public Map<Vo, List<Group>> getGroupsForRegistration(Facility facility, Long userId, List<String> voShortNames) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		List<Vo> vos = getVosByShortNames(voShortNames);
		Map<Long, Vo> vosMap = convertVoListToMap(vos);
		List<Member> userMembers = getMembersByUser(userId);
		userMembers = new ArrayList<>(new HashSet<>(userMembers));

		//Filter out vos where member is other than valid or expired. These vos cannot be used for registration
		Map<Long, MemberStatus> memberVoStatuses = convertMembersListToStatusesMap(userMembers);
		Map<Long, Vo> vosForRegistration = new HashMap<>();
		for (Map.Entry<Long, Vo> entry : vosMap.entrySet()) {
			if (memberVoStatuses.containsKey(entry.getKey())) {
				MemberStatus status = memberVoStatuses.get(entry.getKey());
				if (VALID.equals(status) || MemberStatus.EXPIRED.equals(status)) {
					vosForRegistration.put(entry.getKey(), entry.getValue());
				}
			} else {
				vosForRegistration.put(entry.getKey(), entry.getValue());
			}
		}

		// filter groups only if their VO is in the allowed VOs and if they have registration form
		List<Group> allowedGroups = getAllowedGroups(facility);
		List<Group> groupsForRegistration = allowedGroups.stream()
				.filter(group -> vosForRegistration.containsKey(group.getVoId()) && hasApplicationForm(group))
				.collect(Collectors.toList());

		// create map for processing
		Map<Vo, List<Group>> result = new HashMap<>();
		for (Group group : groupsForRegistration) {
			Vo vo = vosMap.get(group.getVoId());
			if (!result.containsKey(vo)) {
				result.put(vo, new ArrayList<>());
			}
			List<Group> list = result.get(vo);
			list.add(group);
		}

		return result;
	}

	@Override
	public boolean groupWhereCanRegisterExists(Facility facility) {
		if (!this.connectorRpc.isEnabled()) {
			return false;
		}

		List<Group> allowedGroups = getAllowedGroups(facility);

		if (!allowedGroups.isEmpty()) {
			for (Group group : allowedGroups) {
				if (hasApplicationForm(group)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isUserInGroup(Long userId, Long groupId) {
		if (!this.connectorRpc.isEnabled()) {
			return false;
		}

		Map<String, Object> groupParams = new LinkedHashMap<>();
		groupParams.put("id", groupId);
		JsonNode groupResponse = connectorRpc.post(GROUPS_MANAGER, "getGroupById", groupParams);
		Group group = RpcMapper.mapGroup(groupResponse);

		Map<String, Object> memberParams = new LinkedHashMap<>();
		memberParams.put("vo", group.getVoId());
		memberParams.put("user", userId);
		JsonNode memberResponse = connectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", memberParams);
		Member member = RpcMapper.mapMember(memberResponse);

		Map<String, Object> isGroupMemberParams = new LinkedHashMap<>();
		isGroupMemberParams.put("group", groupId);
		isGroupMemberParams.put("member", member.getId());
		JsonNode res = connectorRpc.post(GROUPS_MANAGER, "isGroupMember", isGroupMemberParams);

		return res.asBoolean(false);
	}

	@Override
	public boolean setUserAttribute(Long userId, PerunAttribute attribute) {
		if (!this.connectorRpc.isEnabled()) {
			return true;
		}

		JsonNode attributeJson = attribute.toJson();

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("user", userId);
		map.put("attribute", attributeJson);

		JsonNode response = connectorRpc.post(ATTRIBUTES_MANAGER, "setAttribute", map);
		return (response == null || response.isNull() || response instanceof NullNode);
	}

	@Override
	public List<Affiliation> getUserExtSourcesAffiliations(Long userId) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		List<UserExtSource> userExtSources = getUserExtSources(userId);
		List<Affiliation> affiliations = new ArrayList<>();

		AttributeMapping affMapping = new AttributeMapping("affMapping", affiliationsAttr, "", PerunAttributeValue.ARRAY_TYPE);
		AttributeMapping orgUrlMapping = new AttributeMapping("orgUrl", orgUrlAttr, "", STRING_TYPE);
		Set<AttributeMapping> attributeMappings = new HashSet<>(Arrays.asList(affMapping, orgUrlMapping));

		for (UserExtSource ues : userExtSources) {
			if ("cz.metacentrum.perun.core.impl.ExtSourceIdp".equals(ues.getExtSource().getType())) {
				Map<String, PerunAttributeValue> uesAttrValues = getUserExtSourceAttributeValues(ues.getId(), attributeMappings);

				long asserted = ues.getLastAccess().getTime() / 1000L;

				String orgUrl = uesAttrValues.get(orgUrlMapping.getIdentifier()).valueAsString();
				String affs = uesAttrValues.get(affMapping.getIdentifier()).valueAsString();
				if (affs != null) {
					for (String aff : affs.split(";")) {
						String source = ( (orgUrl != null) ? orgUrl : ues.getExtSource().getName() );
						Affiliation affiliation = new Affiliation(source, aff, asserted);
						log.debug("found {} from IdP {} with orgURL {} asserted at {}", aff, ues.getExtSource().getName(),
								orgUrl, asserted);
						affiliations.add(affiliation);
					}
				}
			}
		}

		return affiliations;
	}

	@Override
	public List<Affiliation> getGroupAffiliations(Long userId, String groupAffiliationsAttr) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		List<Affiliation> affiliations = new ArrayList<>();

		List<Member> userMembers = getMembersByUser(userId);
		for (Member member : userMembers) {
			if (VALID.equals(member.getStatus())) {
				List<Group> memberGroups = getMemberGroups(member.getId());
				for (Group group : memberGroups) {
					PerunAttributeValue attrValue = this.getGroupAttributeValue(group, groupAffiliationsAttr);
					if (attrValue != null && attrValue.valueAsString() != null) {
						long linuxTime = System.currentTimeMillis() / 1000L;
						for (String value : attrValue.valueAsList()) {
							Affiliation affiliation = new Affiliation(null, value, linuxTime);
							log.debug("found {} on group {}", value, group.getName());
							affiliations.add(affiliation);
						}
					}
				}
			}
		}

		return affiliations;
	}

	@Override
	public List<String> getGroupsAssignedToResourcesWithUniqueNames(Facility facility) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		List<Resource> resources = getAssignedResources(facility);
		List<String> result = new ArrayList<>();

		String voShortName = "urn:perun:group:attribute-def:virt:voShortName";

		for (Resource res : resources) {
			List<Group> groups = getRichGroupsAssignedToResourceWithAttributesByNames(res, Collections.singletonList(voShortName));

			for (Group group : groups) {
				if (group.getAttributeByUrnName(voShortName) != null &&
						group.getAttributeByUrnName(voShortName).hasNonNull("value")) {
					String value = group.getAttributeByUrnName(voShortName).get("value").textValue();
					group.setUniqueGroupName(value + ":" + group.getName());
					result.add(group.getUniqueGroupName());
				}
			}
		}

		return result;
	}

	@Override
	public Map<String, PerunAttribute> getEntitylessAttributes(String attributeName) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		Map<String, Object> attrNameMap = new LinkedHashMap<>();
		attrNameMap.put("attrName", attributeName);
		JsonNode entitylessAttributesJson = connectorRpc.post(ATTRIBUTES_MANAGER, "getEntitylessAttributes", attrNameMap);

		Long attributeDefinitionId = RpcMapper.mapAttribute(entitylessAttributesJson.get(0)).getId();

		Map<String, Object> attributeDefinitionIdMap = new LinkedHashMap<>();
		attributeDefinitionIdMap.put("attributeDefinition", attributeDefinitionId);
		JsonNode entitylessKeysJson = connectorRpc.post(ATTRIBUTES_MANAGER, "getEntitylessKeys", attributeDefinitionIdMap);

		Map<String, PerunAttribute> result = new LinkedHashMap<>();

		for(int i = 0; i < entitylessKeysJson.size(); i++) {
			result.put(entitylessKeysJson.get(i).asText(), RpcMapper.mapAttribute(entitylessAttributesJson.get(i)));
		}

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	@Override
	public Vo getVoByShortName(String shortName) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("shortName", shortName);

		JsonNode jsonNode = connectorRpc.post(VOS_MANAGER, "getVoByShortName", params);
		return RpcMapper.mapVo(jsonNode);
	}

	@Override
	public Map<String, PerunAttributeValue> getUserAttributeValues(PerunUser user, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getUserAttributeValues(user.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getUserAttributeValues(Long userId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		Map<String, PerunAttribute> userAttributes = this.getUserAttributes(userId, attrsToFetch);
		return extractValues(userAttributes);
	}

	@Override
	public PerunAttributeValue getUserAttributeValue(PerunUser user, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getUserAttributeValue(user.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getUserAttributeValue(Long userId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getUserAttribute(userId, attrToFetch).toPerunAttributeValue();
	}

	@Override
	public Map<String, PerunAttributeValue> getFacilityAttributeValues(Facility facility, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getFacilityAttributeValues(facility.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getFacilityAttributeValues(Long facilityId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		Map<String, PerunAttribute> facilityAttributes = this.getFacilityAttributes(facilityId, attrsToFetch);
		return extractValues(facilityAttributes);
	}

	@Override
	public PerunAttributeValue getFacilityAttributeValue(Facility facility, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getFacilityAttributeValue(facility.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getFacilityAttributeValue(Long facilityId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getFacilityAttribute(facilityId, attrToFetch).toPerunAttributeValue();
	}

	@Override
	public Map<String, PerunAttributeValue> getVoAttributeValues(Vo vo, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getVoAttributeValues(vo.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getVoAttributeValues(Long voId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		Map<String, PerunAttribute> voAttributes = this.getVoAttributes(voId, attrsToFetch);
		return extractValues(voAttributes);
	}

	@Override
	public PerunAttributeValue getVoAttributeValue(Vo vo, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getVoAttributeValue(vo.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getVoAttributeValue(Long voId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getVoAttribute(voId, attrToFetch).toPerunAttributeValue();
	}

	@Override
	public Map<String, PerunAttributeValue> getGroupAttributeValues(Group group, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getGroupAttributeValues(group.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getGroupAttributeValues(Long groupId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		Map<String, PerunAttribute> groupAttributes = this.getGroupAttributes(groupId, attrsToFetch);
		return extractValues(groupAttributes);
	}

	@Override
	public PerunAttributeValue getGroupAttributeValue(Group group, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getGroupAttributeValue(group.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getGroupAttributeValue(Long groupId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getGroupAttribute(groupId, attrToFetch).toPerunAttributeValue();
	}

	@Override
	public Map<String, PerunAttributeValue> getResourceAttributeValues(Resource resource, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getResourceAttributeValues(resource.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttributeValue> getResourceAttributeValues(Long resourceId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		Map<String, PerunAttribute> resourceAttributes = this.getResourceAttributes(resourceId, attrsToFetch);
		return extractValues(resourceAttributes);
	}

	@Override
	public PerunAttributeValue getResourceAttributeValue(Resource resource, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getResourceAttributeValue(resource.getId(), attrToFetch);
	}

	@Override
	public PerunAttributeValue getResourceAttributeValue(Long resourceId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getResourceAttribute(resourceId, attrToFetch).toPerunAttributeValue();
	}

	@Override
	public Map<String, PerunAttribute> getFacilityAttributes(Facility facility, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getFacilityAttributes(facility.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return getAttributes(PerunEntityType.FACILITY, facilityId, attrsToFetch);
	}

	@Override
	public PerunAttribute getFacilityAttribute(Facility facility, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getFacilityAttribute(facility.getId(), attrToFetch);
	}

	@Override
	public PerunAttribute getFacilityAttribute(Long facilityId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return getAttribute(PerunEntityType.FACILITY, facilityId, attrToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getGroupAttributes(Group group, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getGroupAttributes(group.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getGroupAttributes(Long groupId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return getAttributes(PerunEntityType.GROUP, groupId, attrsToFetch);
	}

	@Override
	public PerunAttribute getGroupAttribute(Group group, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getGroupAttribute(group.getId(), attrToFetch);
	}

	@Override
	public PerunAttribute getGroupAttribute(Long groupId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return getAttribute(PerunEntityType.GROUP, groupId, attrToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getUserAttributes(PerunUser user, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getUserAttributes(user.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getUserAttributes(Long userId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return getAttributes(PerunEntityType.USER, userId, attrsToFetch);
	}

	@Override
	public PerunAttribute getUserAttribute(PerunUser user, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getUserAttribute(user.getId(), attrToFetch);
	}

	@Override
	public PerunAttribute getUserAttribute(Long userId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return getAttribute(PerunEntityType.USER, userId, attrToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getVoAttributes(Vo vo, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getVoAttributes(vo.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getVoAttributes(Long voId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return getAttributes(PerunEntityType.VO, voId, attrsToFetch);
	}

	@Override
	public PerunAttribute getVoAttribute(Vo vo, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getVoAttribute(vo.getId(), attrToFetch);
	}

	@Override
	public PerunAttribute getVoAttribute(Long voId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return getAttribute(PerunEntityType.VO, voId, attrToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getResourceAttributes(Resource resource, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return this.getResourceAttributes(resource.getId(), attrsToFetch);
	}

	@Override
	public Map<String, PerunAttribute> getResourceAttributes(Long resourceId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		return getAttributes(PerunEntityType.RESOURCE, resourceId, attrsToFetch);
	}

	@Override
	public PerunAttribute getResourceAttribute(Resource resource, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return this.getResourceAttribute(resource.getId(), attrToFetch);
	}

	@Override
	public PerunAttribute getResourceAttribute(Long resourceId, String attrToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		return getAttribute(PerunEntityType.RESOURCE, resourceId, attrToFetch);
	}

	@Override
	public Set<String> getCapabilities(Facility facility, Set<String> groupNames,
									   String facilityCapabilitiesAttrName,
									   String resourceCapabilitiesAttrName)
	{
		if (!this.connectorRpc.isEnabled()) {
			return new HashSet<>();
		}

		if (facility == null) {
			return new HashSet<>();
		}

		Set<String> capabilities = new HashSet<>();
		Set<String> resourceGroupNames = new HashSet<>();

		if (null != resourceCapabilitiesAttrName) {
			List<Resource> resources = this.getAssignedRichResources(facility);
			for (Resource resource : resources) {
				PerunAttributeValue attrValue = this.getResourceAttributeValue(resource.getId(), resourceCapabilitiesAttrName);

				List<String> resourceCapabilities = attrValue.valueAsList();
				if (resourceCapabilities == null || resourceCapabilities.size() == 0) {
					continue;
				}
				List<Group> groups = this.getAssignedGroups(resource.getId());
				for (Group group : groups) {
					resourceGroupNames.add(group.getName());
					String groupName = group.getName();
					if (resource.getVo() != null) {
						groupName = resource.getVo().getShortName() + ':' + groupName;
					}
					group.setUniqueGroupName(groupName);

					if (groupNames.contains(groupName)) {
						log.trace("Group [{}] found in users groups, add capabilities [{}]", groupName, resourceCapabilities);
						capabilities.addAll(resourceCapabilities);
					} else {
						log.trace("Group [{}] not found in users groups, continue to the next one", groupName);
					}
				}
			}
		}

		if (null != facilityCapabilitiesAttrName && !Collections.disjoint(groupNames, resourceGroupNames)) {
			Set<String> facilityCapabilities = this.getFacilityCapabilities(facility, facilityCapabilitiesAttrName);
			capabilities.addAll(facilityCapabilities);
		}

		return capabilities;
	}

	@Override
	public Set<String> getCapabilities(Facility facility, Map<Long, String> idToGnameMap,
									   String facilityCapabilitiesAttrName, String resourceCapabilitiesAttrName)
	{
		if (!this.connectorRpc.isEnabled()) {
			return new HashSet<>();
		}

		if (facility == null) {
			return new HashSet<>();
		}

		return this.getCapabilities(facility, new HashSet<>(idToGnameMap.values()), facilityCapabilitiesAttrName,
				resourceCapabilitiesAttrName);
	}

	@Override
	public Set<Group> getGroupsWhereUserIsActiveWithUniqueNames(Long facilityId, Long userId) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashSet<>();
		}

		Set<Group> groups = this.getGroupsWhereUserIsActive(facilityId, userId);

		Map<Long, String> voIdToShortNameMap = new HashMap<>();
		groups.forEach(g -> {
			if (!voIdToShortNameMap.containsKey(g.getVoId())) {
				Vo vo = this.getVoById(g.getVoId());
				if (vo != null) {
					voIdToShortNameMap.put(vo.getId(), vo.getShortName());
				}
			}
			g.setUniqueGroupName(voIdToShortNameMap.get(g.getVoId()) + ':' + g.getName());
		});

		return groups;
	}

	@Override
	public Set<Long> getUserGroupsIds(Long userId, Long voId) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashSet<>();
		}

		Member member = getMemberByUser(userId, voId);
		Set<Long> groups = new HashSet<>();
		if (member != null) {
			groups = getMemberGroups(member.getId()).stream().map(Group::getId).collect(Collectors.toSet());
		}

		return groups;
	}

	@Override
	public boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> mandatoryVos, Set<Long> mandatoryGroups,
											   Set<Long> envVos, Set<Long> envGroups) {
		List<Member> members = getMembersByUser(userId);
		Set<Long> foundVoIds = new HashSet<>();
		Set<Long> foundGroupIds = new HashSet<>();
		boolean skipGroups = mandatoryGroups.isEmpty() && envGroups.isEmpty();
		for (Member m: members) {
			if (MemberStatus.VALID.equals(m.getStatus())) {
				foundVoIds.add(m.getVoId());
			}
			if (!skipGroups) {
				foundGroupIds.addAll(getMemberGroups(m.getId()).stream().map(Model::getId).collect(Collectors.toList()));
			}
		}

		return PerunAdapter.decideAccess(foundVoIds, foundGroupIds, mandatoryVos, mandatoryGroups, envVos, envGroups);
	}

	@Override
	public boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> vos, Set<Long> groups) {
		List<Member> members = getMembersByUser(userId);
		Set<Long> foundVoIds = new HashSet<>();
		Set<Long> foundGroupIds = new HashSet<>();
		boolean skipGroups = groups.isEmpty();

		for (Member m: members) {
			if (MemberStatus.VALID.equals(m.getStatus())) {
				foundVoIds.add(m.getVoId());
			}
			if (!skipGroups) {
				foundGroupIds.addAll(getMemberGroups(m.getId()).stream().map(Model::getId).collect(Collectors.toList()));
			}
		}

		return PerunAdapter.decideAccess(foundVoIds, foundGroupIds, vos, groups);
	}

	@Override
	public boolean isUserInVo(Long userId, String voShortName) {
		if (userId == null) {
			throw new IllegalArgumentException("No userId");
		} else if (!StringUtils.hasText(voShortName)) {
			throw new IllegalArgumentException("No voShortName");
		}

		Vo vo = getVoByShortName(voShortName);
		if (vo == null || vo.getId() == null) {
			log.debug("isUserInVo - No VO found, returning false");
			return false;
		}
		try {
			Member member = getMemberByUser(userId, vo.getId());
			if (member == null) {
				log.debug("isUserInVo - No member found, returning false");
				return false;
			}
			return VALID.equals(member.getStatus());
		} catch (Exception e) {
			log.debug("isUserInVo - caught exception, probably user is not a member");
			log.trace("{}", e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean hasApplicationForm(String voShortName) {
		if (!this.connectorRpc.isEnabled()) {
			return false;
		}

		Vo vo = getVoByShortName(voShortName);
		if (vo == null || vo.getId() == null) {
			return false;
		}
		return hasApplicationForm(vo.getId());
	}

	private Member getMemberByUser(Long userId, Long voId) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("user", userId);
		params.put("vo", voId);
		JsonNode jsonNode = connectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", params);

		return RpcMapper.mapMember(jsonNode);
	}

	private Set<Group> getGroupsWhereUserIsActive(Long facilityId, Long userId) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashSet<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("facility", facilityId);
		map.put("user", userId);
		JsonNode res = connectorRpc.post(USERS_MANAGER, "getGroupsWhereUserIsActive", map);

		return new HashSet<>(RpcMapper.mapGroups(res));
	}

	private Vo getVoById(Long voId) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", voId);

		JsonNode res = connectorRpc.post(VOS_MANAGER, "getVoById", map);
		return RpcMapper.mapVo(res);
	}

	private List<Group> getAssignedGroups(Long resourceId) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("resource", resourceId);

		JsonNode response = connectorRpc.post(RESOURCES_MANAGER, "getAssignedGroups", params);

		return RpcMapper.mapGroups(response);
	}

	private Map<String, PerunAttributeValue> extractValues(Map<String, PerunAttribute> attributeMap) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}
		
		Map<String, PerunAttributeValue> resultMap = new LinkedHashMap<>();
		for (Map.Entry<String, PerunAttribute> attrPair: attributeMap.entrySet()) {
			String attrName = attrPair.getKey();
			PerunAttribute attr = attrPair.getValue();
			if (attr != null) {
				resultMap.put(attrName, attr.toPerunAttributeValue());
			}
		}

		return resultMap;
	}

	private Map<String, PerunAttribute> getAttributes(PerunEntityType entity, Long entityId, Collection<String> attrsToFetch) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		} else if (attrsToFetch == null || attrsToFetch.isEmpty()) {
			return new HashMap<>();
		}

		Set<AttributeMapping> mappings;
		switch (entity) {
			case USER: mappings = this.getUserAttributesMappingService()
					.getMappingsByIdentifiers(attrsToFetch);
				break;
			case FACILITY: mappings = this.getFacilityAttributesMappingService()
					.getMappingsByIdentifiers(attrsToFetch);
				break;
			case VO: mappings = this.getVoAttributesMappingService()
					.getMappingsByIdentifiers(attrsToFetch);
				break;
			case GROUP: mappings = this.getGroupAttributesMappingService()
					.getMappingsByIdentifiers(attrsToFetch);
				break;
			case RESOURCE: mappings = this.getResourceAttributesMappingService()
					.getMappingsByIdentifiers(attrsToFetch);
				break;
			default: mappings  = new HashSet<>();
				break;
		}

		List<String> rpcNames = mappings.stream().map(AttributeMapping::getRpcName).collect(Collectors.toList());

		Map<String, Object> map = new LinkedHashMap<>();
		map.put(entity.toString().toLowerCase(), entityId);
		map.put("attrNames", rpcNames);

		JsonNode res = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", map);
		return RpcMapper.mapAttributes(res, mappings);
	}

	private List<Group> getMemberGroups(Long memberId) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("member", memberId);

		JsonNode response = connectorRpc.post(GROUPS_MANAGER, "getMemberGroups", map);
		return RpcMapper.mapGroups(response);
	}

	private List<Member> getMembersByUser(Long userId) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("user", userId);
		JsonNode jsonNode = connectorRpc.post(MEMBERS_MANAGER, "getMembersByUser", params);

		return RpcMapper.mapMembers(jsonNode);
	}

	private List<Vo> getVosByShortNames(List<String> voShortNames) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		List<Vo> vos = new ArrayList<>();
		for (String shortName : voShortNames) {
			Vo vo = getVoByShortName(shortName);
			vos.add(vo);
		}

		return vos;
	}

	private Map<Long, MemberStatus> convertMembersListToStatusesMap(List<Member> userMembers) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}
		
		Map<Long, MemberStatus> res = new HashMap<>();
		for (Member m : userMembers) {
			res.put(m.getVoId(), m.getStatus());
		}

		return res;
	}

	private Map<String, PerunAttributeValue> getUserExtSourceAttributeValues(Long uesId, Set<AttributeMapping> attrMappings) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("userExtSource", uesId);
		map.put("attrNames", attrMappings.stream().map(AttributeMapping::getRpcName).collect(Collectors.toList()));

		JsonNode response = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", map);
		Map<String, PerunAttribute> attributeMap = RpcMapper.mapAttributes(response, attrMappings);
		return extractValues(attributeMap);
	}

	private List<Group> getAllowedGroups(Facility facility) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("facility", facility.getId());
		JsonNode jsonNode = connectorRpc.post(FACILITIES_MANAGER, "getAllowedGroups", map);
		List<Group> result = new ArrayList<>();
		for (int i = 0; i < jsonNode.size(); i++) {
			JsonNode groupNode = jsonNode.get(i);
			result.add(RpcMapper.mapGroup(groupNode));
		}

		return result;
	}

	private boolean hasApplicationForm(Group group) {
		if (!this.connectorRpc.isEnabled()) {
			return false;
		}
		
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("group", group.getId());
		try {
			if (group.getName().equalsIgnoreCase("members")) {
				log.debug("hasApplicationForm({}) continues to call regForm for VO {}", group, group.getVoId());
				return hasApplicationForm(group.getVoId());
			} else {
				connectorRpc.post(REGISTRAR_MANAGER, "getApplicationForm", map);
			}
		} catch (Exception e) {
			// when group does not have form exception is thrown. Every error thus is supposed as group without form
			// this method will be used after calling other RPC methods - if RPC is not available other methods should discover it first
			return false;
		}

		return true;
	}

	private boolean hasApplicationForm(Long voId) {
		if (!this.connectorRpc.isEnabled()) {
			return false;
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("vo", voId);
		try {
			connectorRpc.post(REGISTRAR_MANAGER, "getApplicationForm", map);
		} catch (Exception e) {
			// when vo does not have form exception is thrown. Every error thus is supposed as vo without form
			// this method will be used after calling other RPC methods - if RPC is not available other methods should discover it first
			return false;
		}

		return true;
	}

	private List<Group> getGroupsWhereUserIsActive(Facility facility, Long userId) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("facility", facility.getId());
		map.put("user", userId);
		JsonNode jsonNode = connectorRpc.post(USERS_MANAGER, "getGroupsWhereUserIsActive", map);

		return RpcMapper.mapGroups(jsonNode);
	}

	private Map<Long, Vo> convertVoListToMap(List<Vo> vos) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashMap<>();
		}
		
		Map<Long, Vo> map = new HashMap<>();
		for (Vo vo : vos) {
			map.put(vo.getId(), vo);
		}

		return map;
	}

	private List<Resource> getAssignedResources(Facility facility) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("facility", facility.getId());

		JsonNode res = connectorRpc.post(FACILITIES_MANAGER, "getAssignedResources", map);
		return RpcMapper.mapResources(res);
	}

	private List<Resource> getAssignedRichResources(Facility facility) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("facility", facility.getId());

		JsonNode res = connectorRpc.post(FACILITIES_MANAGER, "getAssignedRichResources", map);
		return RpcMapper.mapResources(res);
	}

	private List<Group> getRichGroupsAssignedToResourceWithAttributesByNames(Resource resource, List<String> attrNames) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		Set<AttributeMapping> mappings = this.getGroupAttributesMappingService()
				.getMappingsByIdentifiers(attrNames);
		List<String> rpcNames = mappings.stream().map(AttributeMapping::getRpcName).collect(Collectors.toList());
		map.put("resource", resource.getId());
		map.put("attrNames", rpcNames);

		JsonNode res = connectorRpc.post(GROUPS_MANAGER, "getRichGroupsAssignedToResourceWithAttributesByNames", map);
		List<Group> groups = new ArrayList<>();

		for (int i = 0; i < res.size(); i++) {
			JsonNode jsonNode = res.get(i);
			Group group = RpcMapper.mapGroup(jsonNode);

			JsonNode groupAttrs = jsonNode.get("attributes");
			Map<String, JsonNode> attrsMap = new HashMap<>();

			for (int j = 0; j < groupAttrs.size(); j++) {
				JsonNode attr = groupAttrs.get(j);

				String namespace = attr.get("namespace").textValue();
				String friendlyName = attr.get("friendlyName").textValue();

				attrsMap.put(namespace + ":" + friendlyName, attr);
			}

			group.setAttributes(attrsMap);
			groups.add(group);
		}

		return groups;
	}

	private PerunAttribute getAttribute(PerunEntityType entity, Long entityId, String attributeName) {
		if (!this.connectorRpc.isEnabled()) {
			return null;
		}

		AttributeMapping mapping;
		switch (entity) {
			case USER: mapping = this.getUserAttributesMappingService()
					.getMappingByIdentifier(attributeName);
				break;
			case FACILITY: mapping = this.getFacilityAttributesMappingService()
					.getMappingByIdentifier(attributeName);
				break;
			case VO: mapping = this.getVoAttributesMappingService()
					.getMappingByIdentifier(attributeName);
				break;
			case GROUP: mapping = this.getGroupAttributesMappingService()
					.getMappingByIdentifier(attributeName);
				break;
			case RESOURCE: mapping = this.getResourceAttributesMappingService()
					.getMappingByIdentifier(attributeName);
				break;
			default:
				throw new IllegalArgumentException("Unrecognized entity");
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put(entity.toString().toLowerCase(), entityId);
		map.put("attributeName", mapping.getRpcName());

		JsonNode res = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", map);
		return RpcMapper.mapAttribute(res);
	}

	private List<UserExtSource> getUserExtSources(Long userId) {
		if (!this.connectorRpc.isEnabled()) {
			return new ArrayList<>();
		}

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("user", userId);

		JsonNode response = connectorRpc.post(USERS_MANAGER, "getUserExtSources", map);
		return RpcMapper.mapUserExtSources(response);
	}

	private Set<String> getFacilityCapabilities(Facility facility, String capabilitiesAttrName) {
		if (!this.connectorRpc.isEnabled()) {
			return new HashSet<>();
		}

		Set<String> capabilities = new HashSet<>();
		if (facility != null) {
			PerunAttributeValue attr = getFacilityAttributeValue(facility, capabilitiesAttrName);
			if (attr != null && attr.valueAsList() != null) {
				capabilities = new HashSet<>(attr.valueAsList());
			}
		}

		return capabilities;
	}

}
