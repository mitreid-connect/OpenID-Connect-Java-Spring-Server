package cz.muni.ics.oidc.server.adapters;

import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.models.Resource;
import cz.muni.ics.oidc.models.Vo;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for getting data from Perun interfaces.
 * Used for fetching necessary data about users, services etc.
 *
 * @author Martin Kuba makub@ics.muni.czc
 * @author Dominik František Bučík bucik@ics.muni.cz
 * @author Peter Jancus jancus@ics.muni.cz
 */
public interface PerunAdapterMethods {

	/**
	 * Fetch user based on his principal (extLogin and extSource) from Perun
	 *
	 * @return PerunUser with id of found user
	 */
	PerunUser getPreauthenticatedUserId(String extLogin, String extSourceName);

	/**
	 * Fetch user attribute values
	 *
	 * @param user User for whom the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getUserAttributeValues(PerunUser user, Collection<String> attrsToFetch);

	/**
	 * Fetch user attribute values
	 *
	 * @param userId Id of the user for whom the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getUserAttributeValues(Long userId, Collection<String> attrsToFetch);

	/**
	 * Fetch user attribute value
	 *
	 * @param user User for whom the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getUserAttributeValue(PerunUser user, String attrToFetch);

	/**
	 * Fetch user attribute value
	 *
	 * @param userId Id of user for whom the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getUserAttributeValue(Long userId, String attrToFetch);

	/**
	 * Fetch facility registered in Perun associated with the given OIDC client_id value.
	 *
	 * @param clientId value for the OIDCClientID attribute
	 * @return Found facility or null
	 */
	Facility getFacilityByClientId(String clientId);

	/**
	 * Decide if facility has requested to check membership of user in associated groups before access
	 *
	 * @param facility Facility object
	 * @return TRUE if check should be done, FALSE otherwise
	 */
	boolean isMembershipCheckEnabledOnFacility(Facility facility);

	/**
	 * Perform check if user can access service based on his/her membership
	 * in groups assigned to facility resources
	 *
	 * @param facility Facility object
	 * @param userId ID of user
	 * @return TRUE if user can access, FALSE otherwise
	 */
	boolean canUserAccessBasedOnMembership(Facility facility, Long userId);

	/**
	 * Fetch facility attribute values
	 *
	 * @param facility Facility for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getFacilityAttributeValues(Facility facility, Collection<String> attrsToFetch);

	/**
	 * Fetch facility attribute values
	 *
	 * @param facilityId Id of facility for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getFacilityAttributeValues(Long facilityId, Collection<String> attrsToFetch);

	/**
	 * Fetch facility attribute value
	 *
	 * @param facility Facility for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getFacilityAttributeValue(Facility facility, String attrToFetch);

	/**
	 * Fetch facility attribute value
	 *
	 * @param facilityId Id of facility for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getFacilityAttributeValue(Long facilityId, String attrToFetch);

	/**
	 * Check if user is member of the group
	 * @param userId ID of user
	 * @param groupId ID of group
	 * @return TRUE if the user is member of the group, FALSE otherwise
	 */
	boolean isUserInGroup(Long userId, Long groupId);

	/**
	 * For the given user, get all string values of the groupAffiliation attribute of groups of the user
	 *
	 * @param userId id of user
	 * @param groupAffiliationsAttr name of attribute containing group affiliations
	 * @return List of values of the affiliation attribute (filled or empty)
	 */
	List<Affiliation> getGroupAffiliations(Long userId, String groupAffiliationsAttr);

	/**
	 * For the given facility, get all allowed groups
	 *
	 * @param facility facility
	 * @return List of unique names of the groups (filled or empty)
	 */
	List<String> getGroupsAssignedToResourcesWithUniqueNames(Facility facility);

	/**
	 * Get groups where user is active (also in VO in which group exists) and are assigned to the resources of facility.
	 * Fill the uniqueGroupName for groups as well.
	 * @param facilityId Id of Facility
	 * @param userId Id of User
	 * @return Set of groups (filled or empty)
	 */
	Set<Group> getGroupsWhereUserIsActiveWithUniqueNames(Long facilityId, Long userId);

	/**
	 * Fetch VO attribute values
	 *
	 * @param vo VO for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getVoAttributeValues(Vo vo, Collection<String> attrsToFetch);

	/**
	 * Fetch VO attribute values
	 *
	 * @param voId Id of VO for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getVoAttributeValues(Long voId, Collection<String> attrsToFetch);

	/**
	 * Fetch VO attribute value
	 *
	 * @param vo Vo for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getVoAttributeValue(Vo vo, String attrToFetch);

	/**
	 * Fetch VO attribute value
	 *
	 * @param voId Id of vo for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getVoAttributeValue(Long voId, String attrToFetch);

	/**
	 * Get vo with the given short name
	 *
	 * @param shortName short name of VO
	 * @return Found VO or null
	 */
	Vo getVoByShortName(String shortName);

	/**
	 * Fetch group attribute values
	 *
	 * @param group Group for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getGroupAttributeValues(Group group, Collection<String> attrsToFetch);

	/**
	 * Fetch group attribute values
	 *
	 * @param groupId Id of group for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getGroupAttributeValues(Long groupId, Collection<String> attrsToFetch);

	/**
	 * Fetch group attribute value
	 *
	 * @param group Group for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getGroupAttributeValue(Group group, String attrToFetch);

	/**
	 * Fetch group attribute value
	 *
	 * @param groupId Id of group for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getGroupAttributeValue(Long groupId, String attrToFetch);

	/**
	 * Gets the capabilities
	 * @param facility Facility representing client
	 * @param groupNames Names of groups the user is member of.
	 * @param facilityCapabilitiesAttrName String name of attribute containing facility capabilities. Pass null for ignore.
	 * @param resourceCapabilitiesAttrName String name of attribute containing resource capabilities. Pass null for ignore.
	 * @return set of capabilities assigned on resources
	 */
	Set<String> getCapabilities(Facility facility, Set<String> groupNames, String facilityCapabilitiesAttrName,
								String resourceCapabilitiesAttrName);

	/**
	 * Gets the capabilities
	 * @param facility Facility representing client
	 * @param idToGnameMap Map of ID to name of the groups user is member of.
	 * @param facilityCapabilitiesAttrName String name of attribute containing facility capabilities. Pass null for ignore.
	 * @param resourceCapabilitiesAttrName String name of attribute containing resource capabilities. Pass null for ignore.
	 * @return set of capabilities assigned on resources
	 */
	Set<String> getCapabilities(Facility facility, Map<Long, String> idToGnameMap, String facilityCapabilitiesAttrName,
								String resourceCapabilitiesAttrName);

	/**
	 * Fetch resource attribute values
	 *
	 * @param resource Resource for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getResourceAttributeValues(Resource resource, Collection<String> attrsToFetch);

	/**
	 * Fetch resource attribute values
	 *
	 * @param resourceId Id of resource for which the attribute values are being fetch
	 * @param attrsToFetch List of Strings representing attribute values to fetch
	 * @return Map of attrName:PerunAttributeValue (filled or empty)
	 */
	Map<String, PerunAttributeValue> getResourceAttributeValues(Long resourceId, Collection<String> attrsToFetch);

	/**
	 * Fetch resource attribute value
	 *
	 * @param resource Resource for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getResourceAttributeValue(Resource resource, String attrToFetch);

	/**
	 * Fetch resource attribute value
	 *
	 * @param resourceId Id of resource for which the attribute value is being fetch
	 * @param attrToFetch String representing attribute value to fetch
	 * @return PerunAttributeValue or null if not found
	 */
	PerunAttributeValue getResourceAttributeValue(Long resourceId, String attrToFetch);

	/**
	 * Fetch group IDs where user is member based on userID and voID
	 * @param userId id of user
	 * @param voId id of vo
	 * @return List of groups IDs (filled or empty)
	 */
	Set<Long> getUserGroupsIds(Long userId, Long voId);

	/**
	 * Check if user is valid member of given VOs (identified by IDs)
	 * @param userId ID of user in Perun
	 * @param mandatoryVos Set of IDs identifying the VOs
	 * @param mandatoryGroups Set of IDs identifying the Groups
	 * @param envVos Set of IDs identifying the VOs
	 * @param envGroups Set of IDs identifying the Groups
	 * @return returns TRUE if:
	 * 	User is member of at least one specified mandatory VO, and
	 * 	User is member of at least one specified mandatory GROUP, and
	 * 	User is member of at least one specified env VO, and
	 * 	User is member of at least one specified env GROUP.
	 * 	Returns FALSE otherwise.
	 */
	boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> mandatoryVos, Set<Long> mandatoryGroups,
										Set<Long> envVos, Set<Long> envGroups);

	/**
	 * Check if user is valid member of given VOs (identified by IDs)
	 * @param userId ID of user in Perun
	 * @param vos Set of IDs identifying the VOs
	 * @param groups Set of IDs identifying the Groups
	 * @return returns TRUE if:
	 * 	User is member of at least one specified mandatory VO, and
	 * 	User is member of at least one specified mandatory GROUP, and
	 * 	Returns FALSE otherwise.
	 */
	boolean isValidMemberInGroupsAndVos(Long userId, Set<Long> vos, Set<Long> groups);

	boolean isUserInVo(Long userId, String voShortName);

	PerunUser getPerunUser(Long userId);

}
