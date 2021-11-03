package cz.muni.ics.oidc.server.adapters;

import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.Group;
import cz.muni.ics.oidc.models.PerunAttribute;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.models.Resource;
import cz.muni.ics.oidc.models.Vo;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface with specific methods that only rpc interface can execute
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public interface PerunAdapterMethodsRpc {

	/**
	 * Get list of groups where user can register to gain access to the service
	 *
	 * @param facility facility the user tries to access
	 * @param userId id of user
	 * @return List of groups where user can register or empty list
	 */
	Map<Vo, List<Group>> getGroupsForRegistration(Facility facility, Long userId, List<String> voShortNames);

	/**
	 * Decide if there is a group where user can register
	 *
	 * @param facility facility being accessed
	 * @return true if at least one group with registration form exists
	 */
	boolean groupWhereCanRegisterExists(Facility facility);

	/**
	 * Sets the attribute of the user.
	 * @param userId id of user
	 * @param attribute attribute
	 */
	boolean setUserAttribute(Long userId, PerunAttribute attribute);

	/**
	 * For the given user, gets all string values of the affiliation attribute of all UserExtSources of type ExtSourceIdp
	 * @param userId id of user
	 * @return list of values of attribute affiliation
	 */
	List<Affiliation> getUserExtSourcesAffiliations(Long userId);

	/**
	 * Gets the map of entityless attributes.
	 * @param attributeName full name of attribute
	 * @return map of attributes
	 */
	Map<String, PerunAttribute> getEntitylessAttributes(String attributeName);

	/**
	 * Fetch facility attributes
	 * @param facility Facility for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getFacilityAttributes(Facility facility, Collection<String> attrsToFetch);

	/**
	 * Fetch facility attributes
	 * @param facilityId Id of facility for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getFacilityAttributes(Long facilityId, Collection<String> attrsToFetch);

	/**
	 * Fetch group attributes
	 * @param group Group for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getGroupAttributes(Group group, Collection<String> attrsToFetch);

	/**
	 * Fetch facility attribute value
	 *
	 * @param facility Facility for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getFacilityAttribute(Facility facility, String attrToFetch);

	/**
	 * Fetch facility attribute value
	 *
	 * @param facilityId Id of facility for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getFacilityAttribute(Long facilityId, String attrToFetch);

	/**
	 * Fetch group attributes
	 * @param groupId Id of group for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getGroupAttributes(Long groupId, Collection<String> attrsToFetch);

	/**
	 * Fetch group attribute value
	 *
	 * @param group Group for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getGroupAttribute(Group group, String attrToFetch);

	/**
	 * Fetch group attribute value
	 *
	 * @param groupId Id of group for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getGroupAttribute(Long groupId, String attrToFetch);

	/**
	 * Fetch user attributes
	 * @param user User for whom the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getUserAttributes(PerunUser user, Collection<String> attrsToFetch);

	/**
	 * Fetch user attributes
	 * @param userId Id of user for whom the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getUserAttributes(Long userId, Collection<String> attrsToFetch);

	/**
	 * Fetch user attribute value
	 *
	 * @param user User for whom the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getUserAttribute(PerunUser user, String attrToFetch);

	/**
	 * Fetch user attribute value
	 *
	 * @param userId Id of user for whom the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getUserAttribute(Long userId, String attrToFetch);

	/**
	 * Fetch vo attributes
	 * @param vo VO for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getVoAttributes(Vo vo, Collection<String> attrsToFetch);

	/**
	 * Fetch vo attributes
	 * @param voId Id of VO for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getVoAttributes(Long voId, Collection<String> attrsToFetch);

	/**
	 * Fetch VO attribute
	 *
	 * @param vo Vo for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getVoAttribute(Vo vo, String attrToFetch);

	/**
	 * Fetch VO attribute
	 *
	 * @param voId Id of vo for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getVoAttribute(Long voId, String attrToFetch);

	/**
	 * Fetch resource attributes
	 * @param resource VO for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getResourceAttributes(Resource resource, Collection<String> attrsToFetch);

	/**
	 * Fetch resource attributes
	 * @param resourceId Id of VO for which the attribute values are being fetched.
	 * @param attrsToFetch Collection of String representing attribute values to fetch
	 * @return Map of attrName:PerunAttribute (filled or empty)
	 */
	Map<String, PerunAttribute> getResourceAttributes(Long resourceId, Collection<String> attrsToFetch);

	/**
	 * Fetch VO attribute
	 *
	 * @param resource Resource for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getResourceAttribute(Resource resource, String attrToFetch);

	/**
	 * Fetch VO attribute
	 *
	 * @param resourceId Id of resource for which the attribute is being fetched
	 * @param attrToFetch String representing attribute to fetch
	 * @return PerunAttribute
	 */
	PerunAttribute getResourceAttribute(Long resourceId, String attrToFetch);

	boolean hasApplicationForm(String voShortName);

}
