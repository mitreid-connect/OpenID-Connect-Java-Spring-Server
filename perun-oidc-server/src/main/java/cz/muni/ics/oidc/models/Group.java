package cz.muni.ics.oidc.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Group object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
public class Group extends Model {

	private Long parentGroupId;
	private String name;
	private String description;
	private String uniqueGroupName; // voShortName + ":" + group name
	private String uuid;
	private Long voId;

	private Map<String, JsonNode> attributes = new LinkedHashMap<>();

	public Group() {
	}

	public Group(Long id, Long parentGroupId, String name, String description, String uniqueGroupName, String uuid, Long voId) {
		super(id);
		this.setParentGroupId(parentGroupId);
		this.setName(name);
		this.setDescription(description);
		this.setUniqueGroupName(uniqueGroupName);
		this.setUuid(uuid);
		this.setVoId(voId);
	}

	public Long getParentGroupId() {
		return parentGroupId;
	}

	public void setParentGroupId(Long parentGroupId) {
		this.parentGroupId = parentGroupId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (Strings.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("name cannot be null nor empty");
		}

		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (null == description) {
			throw new IllegalArgumentException("description cannot be null");
		}

		this.description = description;
	}

	public void setUniqueGroupName(String uniqueGroupName) {
		this.uniqueGroupName = uniqueGroupName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getVoId() {
		return voId;
	}

	public void setVoId(Long voId) {
		if (voId == null) {
			throw new IllegalArgumentException("voId cannot be null");
		}

		this.voId = voId;
	}

	public Map<String, JsonNode> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, JsonNode> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets identifier voShortName:group.name usable for groupNames in AARC format.
	 */
	public String getUniqueGroupName() {
		return uniqueGroupName;
	}

	/**
	 * Gets attribute by urn name
	 *
	 * @param attributeName urn name of attribute
	 * @return attribute
	 */
	public JsonNode getAttributeByUrnName(String attributeName) {
		if (attributes == null || !attributes.containsKey(attributeName)) {
			return null;
		}

		return attributes.get(attributeName);
	}

	/**
	 * Gets attribute by friendly name
	 *
	 * @param attributeName attribute name
	 * @param attributeUrnPrefix urn prefix of attribute
	 * @return attribute
	 */
	public JsonNode getAttributeByFriendlyName(String attributeName, String attributeUrnPrefix) {
		String key = attributeUrnPrefix + ":" + attributeName;

		if (attributes == null || !attributes.containsKey(key)) {
			return null;
		}

		return attributes.get(key);
	}

	@Override
	public String toString() {
		return "Group{" +
				"id=" + getId() +
				", name='" + name + '\'' +
				", uniqueGroupName='" + uniqueGroupName + '\'' +
				", description='" + description + '\'' +
				", parentGroupId=" + parentGroupId +
				", voId=" + voId +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Group group = (Group) o;
		return Objects.equals(parentGroupId, group.parentGroupId) &&
				Objects.equals(name, group.name) &&
				Objects.equals(description, group.description) &&
				Objects.equals(uniqueGroupName, group.uniqueGroupName) &&
				Objects.equals(voId, group.voId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), parentGroupId, name, description, uniqueGroupName, voId);
	}
}
