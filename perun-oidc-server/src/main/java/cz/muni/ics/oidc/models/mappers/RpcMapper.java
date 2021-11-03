package cz.muni.ics.oidc.models.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.oidc.exceptions.MissingFieldException;
import cz.muni.ics.oidc.models.AttributeMapping;
import cz.muni.ics.oidc.models.Aup;
import cz.muni.ics.oidc.models.ExtSource;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.Member;
import cz.muni.ics.oidc.models.PerunAttribute;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.models.Resource;
import cz.muni.ics.oidc.models.UserExtSource;
import cz.muni.ics.oidc.models.Vo;
import cz.muni.ics.oidc.models.enums.MemberStatus;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.util.StringUtils;


/**
 * This class is mapping JsonNodes to object models.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class RpcMapper {

	public static final String ID = "id";
	public static final String UUID = "uuid";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String PARENT_GROUP_ID = "parentGroupId";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String VO_ID = "voId";
	public static final String USER_ID = "userId";
	public static final String STATUS = "status";
	public static final String FACILITY_ID = "facilityId";
	public static final String TYPE = "type";
	public static final String SHORT_NAME = "shortName";
	public static final String LOGIN = "login";
	public static final String EXT_SOURCE = "extSource";
	public static final String LOA = "loa";
	public static final String LAST_ACCESS = "lastAccess";
	public static final String PERSISTENT = "persistent";
	public static final String FRIENDLY_NAME = "friendlyName";
	public static final String NAMESPACE = "namespace";
	public static final String DISPLAY_NAME = "displayName";
	public static final String WRITABLE = "writable";
	public static final String UNIQUE = "unique";
	public static final String ENTITY = "entity";
	public static final String BASE_FRIENDLY_NAME = "baseFriendlyName";
	public static final String FRIENDLY_NAME_PARAMETER = "friendlyNameParameter";
	public static final String VALUE = "value";
	public static final String VALUE_CREATED_AT = "valueCreatedAt";
	public static final String VALUE_MODIFIED_AT = "valueModifiedAt";
	public static final String VERSION = "version";
	public static final String DATE = "date";
	public static final String LINK = "link";
	public static final String TEXT = "text";

	/**
	 * Maps JsonNode to User model.
	 *
	 * @param json User in JSON format from Perun to be mapped.
	 * @return Mapped User object.
	 */
	public static PerunUser mapPerunUser(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		String firstName = getFieldAsString(json, FIRST_NAME);
		String lastName = getRequiredFieldAsString(json, LAST_NAME);

		return new PerunUser(id, firstName, lastName);
	}

	/**
	 * Maps JsonNode to List of USERS.
	 *
	 * @param jsonArray JSON array of users in JSON format from Perun to be mapped.
	 * @return List of users.
	 */
	public static List<PerunUser> mapPerunUsers(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		return IntStream.range(0, jsonArray.size()).
				mapToObj(jsonArray::get).
				map(RpcMapper::mapPerunUser).
				collect(Collectors.toList());
	}

	/**
	 * Maps JsonNode to Group model.
	 *
	 * @param json Group in JSON format from Perun to be mapped.
	 * @return Mapped Group object.
	 */
	public static Group mapGroup(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		Long parentGroupId = getFieldAsLong(json, PARENT_GROUP_ID);
		String name = getRequiredFieldAsString(json, NAME);
		String description = getFieldAsString(json, DESCRIPTION);
		Long voId = getRequiredFieldAsLong(json, VO_ID);
		String uuid = getRequiredFieldAsString(json, UUID);

		return new Group(id, parentGroupId, name, description, null, uuid,  voId);
	}

	/**
	 * Maps JsonNode to List of Groups.
	 *
	 * @param jsonArray JSON array of groups in JSON format from Perun to be mapped.
	 * @return List of groups.
	 */
	public static List<Group> mapGroups(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		List<Group> result = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonNode groupNode = jsonArray.get(i);
			Group mappedGroup = RpcMapper.mapGroup(groupNode);
			result.add(mappedGroup);
		}

		return result;
	}

	/**
	 * Maps JsonNode to Facility model.
	 *
	 * @param json Facility in JSON format from Perun to be mapped.
	 * @return Mapped Facility object.
	 */
	public static Facility mapFacility(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		String name = getRequiredFieldAsString(json, NAME);
		String description = getFieldAsString(json, DESCRIPTION);

		return new Facility(id, name, description);
	}

	/**
	 * Maps JsonNode to List of Facilities.
	 *
	 * @param jsonArray JSON array of facilities in JSON format from Perun to be mapped.
	 * @return List of facilities.
	 */
	public static List<Facility> mapFacilities(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		List<Facility> result = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonNode facilityNode = jsonArray.get(i);
			Facility mappedFacility = RpcMapper.mapFacility(facilityNode);
			result.add(mappedFacility);
		}

		return result;
	}

	/**
	 * Maps JsonNode to Member model.
	 *
	 * @param json Member in JSON format from Perun to be mapped.
	 * @return Mapped Member object.
	 */
	public static Member mapMember(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		Long userId = getRequiredFieldAsLong(json, USER_ID);
		Long voId = getRequiredFieldAsLong(json, VO_ID);
		MemberStatus status = MemberStatus.fromString(getRequiredFieldAsString(json, STATUS));

		return new Member(id, userId, voId, status);
	}

	/**
	 * Maps JsonNode to List of Members.
	 *
	 * @param jsonArray JSON array of members in JSON format from Perun to be mapped.
	 * @return List of members.
	 */
	public static List<Member> mapMembers(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		List<Member> members = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonNode memberNode = jsonArray.get(i);
			Member mappedMember = RpcMapper.mapMember(memberNode);
			members.add(mappedMember);
		}

		return members;
	}

	/**
	 * Maps JsonNode to Resource model.
	 *
	 * @param json Resource in JSON format from Perun to be mapped.
	 * @return Mapped Resource object.
	 */
	public static Resource mapResource(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		Long voId = getRequiredFieldAsLong(json, VO_ID);
		Long facilityId = getRequiredFieldAsLong(json, FACILITY_ID);
		String name = getRequiredFieldAsString(json, NAME);
		String description = getFieldAsString(json, DESCRIPTION);

		Vo vo = null;
		if (json.hasNonNull("vo")) {
			JsonNode voJson = json.get("vo");
			vo = RpcMapper.mapVo(voJson);
		}

		return new Resource(id, voId, facilityId, name, description, vo);
	}

	/**
	 * Maps JsonNode to List of Resources.
	 *
	 * @param jsonArray JSON array of resources in JSON format from Perun to be mapped.
	 * @return List of resources.
	 */
	public static List<Resource> mapResources(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		List<Resource> resources = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonNode resource = jsonArray.get(i);
			Resource mappedResource = RpcMapper.mapResource(resource);
			resources.add(mappedResource);
		}

		return resources;
	}

	/**
	 * Maps JsonNode to ExtSource model.
	 *
	 * @param json ExtSource in JSON format from Perun to be mapped.
	 * @return Mapped ExtSource object.
	 */
	public static ExtSource mapExtSource(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		String name = getRequiredFieldAsString(json, NAME);
		String type = getRequiredFieldAsString(json, TYPE);

		return new ExtSource(id, name, type);
	}

	/**
	 * Maps JsonNode to List of ExtSources.
	 *
	 * @param jsonArray JSON array of extSources in JSON format from Perun to be mapped.
	 * @return List of extSources.
	 */
	public static List<ExtSource> mapExtSources(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		List<ExtSource> extSources = new ArrayList<>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonNode extSource = jsonArray.get(i);
			ExtSource mappedExtSource = RpcMapper.mapExtSource(extSource);
			extSources.add(mappedExtSource);
		}

		return extSources;
	}

	/**
	 * Maps JsonNode to VO model.
	 *
	 * @param json VO in JSON format from Perun to be mapped.
	 * @return Mapped VO object.
	 */
	public static Vo mapVo(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		String name = getRequiredFieldAsString(json, NAME);
		String shortName = getRequiredFieldAsString(json, SHORT_NAME);

		return new Vo(id, name, shortName);
	}

	/**
	 * Maps JsonNode to List of VOs.
	 *
	 * @param jsonArray JSON array of VOs in JSON format from Perun to be mapped.
	 * @return List of VOs.
	 */
	public static List<Vo> mapVos(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		List<Vo> vos = new ArrayList<>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonNode voJson = jsonArray.get(i);
			Vo mappedVo = RpcMapper.mapVo(voJson);
			vos.add(mappedVo);
		}

		return vos;
	}

	/**
	 * Maps JsonNode to UserExtSource model.
	 *
	 * @param json UserExtSource in JSON format from Perun to be mapped.
	 * @return Mapped UserExtSource object.
	 */
	public static UserExtSource mapUserExtSource(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		String login = getRequiredFieldAsString(json, LOGIN);
		ExtSource extSource = RpcMapper.mapExtSource(getRequiredFieldAsJsonNode(json, EXT_SOURCE));
		int loa = getRequiredFieldAsInt(json, LOA);
		boolean persistent = getRequiredFieldAsBoolean(json, PERSISTENT);
		Timestamp lastAccess = Timestamp.valueOf(getRequiredFieldAsString(json, LAST_ACCESS));

		return new UserExtSource(id, extSource, login, loa, persistent, lastAccess);
	}

	/**
	 * Maps JsonNode to List of UserExtSources.
	 *
	 * @param jsonArray JSON array of userExtSources in JSON format from Perun to be mapped.
	 * @return List of userExtSources.
	 */
	public static List<UserExtSource> mapUserExtSources(JsonNode jsonArray) {
		if (jsonArray.isNull()) {
			return new ArrayList<>();
		}

		List<UserExtSource> userExtSources = new ArrayList<>();

		for (int i = 0; i < jsonArray.size(); i++) {
			JsonNode userExtSource = jsonArray.get(i);
			UserExtSource mappedUes = RpcMapper.mapUserExtSource(userExtSource);
			userExtSources.add(mappedUes);
		}

		return userExtSources;
	}

	/**
	 * Maps JsonNode to PerunAttribute model.
	 *
	 * @param json PerunAttribute in JSON format from Perun to be mapped.
	 * @return Mapped PerunAttribute object.
	 */
	public static PerunAttribute mapAttribute(JsonNode json) {
		if (json == null || json.isNull()) {
			return null;
		}

		Long id = getRequiredFieldAsLong(json, ID);
		String friendlyName = getRequiredFieldAsString(json, FRIENDLY_NAME);
		String namespace = getRequiredFieldAsString(json, NAMESPACE);
		String description = getFieldAsString(json, DESCRIPTION);
		String type = getRequiredFieldAsString(json, TYPE);
		String displayName = getRequiredFieldAsString(json, DISPLAY_NAME);
		boolean writable = getRequiredFieldAsBoolean(json, WRITABLE);
		boolean unique = getRequiredFieldAsBoolean(json, UNIQUE);
		String entity = getRequiredFieldAsString(json, ENTITY);
		String baseFriendlyName = getFieldAsString(json, BASE_FRIENDLY_NAME);
		String friendlyNameParameter = getFieldAsString(json, FRIENDLY_NAME_PARAMETER);
		JsonNode value = getFieldAsJsonNode(json, VALUE);
		String valueCreatedAt = getFieldAsString(json, VALUE_CREATED_AT);
		if (!StringUtils.hasText(valueCreatedAt)) {
			valueCreatedAt = null;
		}
		String valueModifiedAt = getFieldAsString(json, VALUE_MODIFIED_AT);
		if (!StringUtils.hasText(valueModifiedAt)) {
			valueModifiedAt = null;
		}

		return new PerunAttribute(id, friendlyName, namespace, description, type, displayName,
				writable, unique, entity, baseFriendlyName, friendlyNameParameter, value, valueCreatedAt, valueModifiedAt);
	}

	/**
	 * Map JsonNode to Map of Perun attributes
	 * @param jsonNode attributes as array in JSON format to be mapped
	 * @return Map of PerunAttributes mapped from JsonNode, where key = URN, value = Attribute
	 */
	public static Map<String, PerunAttribute> mapAttributes(JsonNode jsonNode, Set<AttributeMapping> attrMappings) {
		Map<String, PerunAttribute> res = new HashMap<>();
		Map<String, PerunAttribute> attributesAsMap = new HashMap<>();

		for (int i = 0; i < jsonNode.size(); i++) {
			JsonNode attribute = jsonNode.get(i);
			PerunAttribute mappedAttribute = mapAttribute(attribute);
			attributesAsMap.put(mappedAttribute.getUrn(), mappedAttribute);
		}

		for (AttributeMapping mapping: attrMappings) {
			String attrKey = mapping.getRpcName();
			if (attributesAsMap.containsKey(attrKey)) {
				PerunAttribute attribute = attributesAsMap.get(attrKey);
				res.put(mapping.getIdentifier(), attribute);
			} else {
				res.put(mapping.getIdentifier(), null);
			}
		}

		return res;
	}

	/**
	 * Map JsonNode to list of Aup
	 * @param json Aup in JSON format
	 * @return Mapped Aup
	 */
	public static Aup mapAup(JsonNode json) {
		Aup aup = new Aup();
		aup.setVersion(getFieldAsString(json, VERSION));
		aup.setDate(getFieldAsString(json, DATE));
		aup.setLink(getFieldAsString(json, LINK));
		aup.setText(getFieldAsString(json, TEXT));
		aup.setSignedOn(json.hasNonNull(Aup.SIGNED_ON) ? json.get(Aup.SIGNED_ON).asText() : null);
		return aup;
	}

	private static Long getRequiredFieldAsLong(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			throw new MissingFieldException();
		}
		return json.get(name).asLong();
	}

	private static Long getFieldAsLong(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			return 0L;
		}
		return json.get(name).asLong();
	}

	private static int getRequiredFieldAsInt(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			throw new MissingFieldException();
		}
		return json.get(name).asInt();
	}

	private static int getFieldAsInt(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			return 0;
		}
		return json.get(name).asInt();
	}

	private static boolean getRequiredFieldAsBoolean(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			throw new MissingFieldException();
		}
		return json.get(name).asBoolean();
	}

	private static boolean getFieldAsBoolean(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			return false;
		}
		return json.get(name).asBoolean();
	}

	private static String getRequiredFieldAsString(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			throw new MissingFieldException();
		}
		return json.get(name).asText();
	}

	private static String getFieldAsString(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			return "";
		}
		return json.get(name).asText();
	}

	private static JsonNode getRequiredFieldAsJsonNode(JsonNode json, String name) {
		if (!json.hasNonNull(name)) {
			throw new MissingFieldException();
		}
		return json.get(name);
	}

	private static JsonNode getFieldAsJsonNode(JsonNode json, String name) {
		return json.get(name);
	}

}
