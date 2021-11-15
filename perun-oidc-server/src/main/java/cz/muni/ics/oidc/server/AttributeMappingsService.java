package cz.muni.ics.oidc.server;


import cz.muni.ics.oidc.models.AttributeMapping;
import cz.muni.ics.oidc.models.enums.PerunAttrValueType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Service providing methods to use AttributeMapping objects when fetching attributes.
 *
 * Names for the attribute are configured in configuration file in the following way:
 * (replace [entity] with one of user|vo|facility|resource|group)
 * <ul>
 *     <li><b>[entity].attribute_names.customList</b> - comma separated list of names for attributes</li>
 * </ul>
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class AttributeMappingsService {

	private static final String LDAP_NAME = ".mapping.ldap";
	private static final String RPC_NAME = ".mapping.rpc";
	private static final String TYPE = ".type";
	private static final String SEPARATOR = ".separator";

	private final Map<String, AttributeMapping> attributeMap;

	public AttributeMappingsService(String[] attrIdentifiersFixed, String[] attrIdentifiersCustom,
									Properties attrMappingsProperties) {
		attributeMap = new HashMap<>();

		if (attrIdentifiersFixed != null) {
			this.initAttrMappings(attrIdentifiersFixed, attrMappingsProperties);
		}

		if (attrIdentifiersCustom != null) {
			this.initAttrMappings(attrIdentifiersCustom, attrMappingsProperties);
		}
	}

	/**
	 * Get AttributeMapping based on the given internal identifier of attribute.
	 * @param identifier String identifier of the attribute.
	 * @return AttributeMapping. If invalid identifier is passed (null or unknown) an exception is thrown.
	 */
	public AttributeMapping getMappingByIdentifier(String identifier) {
		if (identifier == null) {
			throw new IllegalArgumentException("Identifier cannot be null");
		} else if (!attributeMap.containsKey(identifier)) {
			return null;
		}

		return attributeMap.get(identifier);
	}

	/**
	 * Get Set of AttributeMapping based on the given internal identifiers of attributes.
	 * @param identifiers Collection of Strings
	 * @return Set of AttributeMapping. If invalid identifier is passed inside the collection, this identifier is ignored.
	 */
	public Set<AttributeMapping> getMappingsByIdentifiers(Collection<String> identifiers) {
		Set<AttributeMapping> mappings = new HashSet<>();
		if (identifiers != null) {
			for (String identifier : identifiers) {
				try {
					mappings.add(getMappingByIdentifier(identifier));
				} catch (IllegalArgumentException e) {
					log.warn("Caught {} when getting mappings, check your configuration for identifier {}",
							e.getClass(), identifier, e);
				}
			}
		}

		return mappings.stream().filter(Objects::nonNull).collect(Collectors.toSet());
	}

	private void initAttrMappings(String[] attributeIdentifiers, Properties attrProperties) {
		if (attributeIdentifiers == null || attributeIdentifiers.length <= 0) {
			return;
		}

		for (String identifier : attributeIdentifiers) {
			if (identifier == null || identifier.isEmpty()) {
				continue;
			}
			AttributeMapping am = initAttrMapping(identifier, attrProperties);
			log.debug("Initialized attributeMapping: {}", am);
			attributeMap.put(am.getIdentifier(), am);
		}
	}

	private AttributeMapping initAttrMapping(String attrIdentifier, Properties attrProperties) {
		String rpcIdentifier = attrProperties.getProperty(attrIdentifier + RPC_NAME);
		String ldapIdentifier = attrProperties.getProperty(attrIdentifier + LDAP_NAME);
		if (ldapIdentifier != null && ldapIdentifier.trim().isEmpty()) {
			ldapIdentifier = null;
		}

		String type = attrProperties.getProperty(attrIdentifier + TYPE);
		String separator = "";
		if (PerunAttrValueType.MAP_KEY_VALUE.equals(PerunAttrValueType.parse(type))) {
			separator = attrProperties.getProperty(attrIdentifier + SEPARATOR);
		}

		return new AttributeMapping(attrIdentifier, rpcIdentifier, ldapIdentifier, type, separator);
	}

}

