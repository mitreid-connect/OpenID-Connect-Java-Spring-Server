package cz.muni.ics.oidc.models;

import com.google.common.base.Strings;
import cz.muni.ics.oidc.models.enums.PerunAttrValueType;
import java.util.Objects;

/**
 * Attribute mapping model. Provides mapping of attribute with an internal name to names specific for interfaces
 * (i.e. LDAP, RPC, ...)
 *
 * Configuration (replace [attrName] with the actual name of the attribute):
 * <ul>
 *     <li><b>[attrName].mapping.ldap</b> - name of attribute in LDAP</li>
 *     <li><b>[attrName].mapping.rpc</b> - name of attribute in LDAP</li>
 *     <li><b>[attrName].mapping.type</b> - [STRING|INTEGER|BOOLEAN|ARRAY|MAP_JSON|MAP_KEY_VALUE]
 *     - type of attribute value, defaults to STRING</li>
 *     <li><b>[attrName].mapping.separator</b> - separator of keys ands values if type equals to MAP_KEY_VALUE, defaults to '='</li>
 * </ul>
 * @see cz.muni.ics.oidc.server.AttributeMappingsService for attrName configurations
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class AttributeMapping {

	private String identifier;
	private String rpcName;
	private String ldapName;
	private PerunAttrValueType attrType;
	private String separator;

	public AttributeMapping() {
	}

	public AttributeMapping(String identifier, String rpcName, String ldapName, String type) {
		super();
		this.setIdentifier(identifier);
		this.setRpcName(rpcName);
		this.setLdapName(ldapName);
		this.setAttrType(type);
		this.setSeparator("");
	}

	public AttributeMapping(String identifier, String rpcName, String ldapName, String type, String separator) {
		super();
		this.setIdentifier(identifier);
		this.setRpcName(rpcName);
		this.setLdapName(ldapName);
		this.setAttrType(type);
		this.setSeparator(separator);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		if (Strings.isNullOrEmpty(identifier)) {
			throw new IllegalArgumentException("identifier cannot be null nor empty");
		}

		this.identifier = identifier;
	}

	public String getRpcName() {
		return rpcName;
	}

	public void setRpcName(String rpcName) {
		if (Strings.isNullOrEmpty(rpcName)) {
			throw new IllegalArgumentException("rpcName cannot be null nor empty");
		}

		this.rpcName = rpcName;
	}

	public String getLdapName() {
		return ldapName;
	}

	public void setLdapName(String ldapName) {
		this.ldapName = ldapName;
	}

	public PerunAttrValueType getAttrType() {
		return attrType;
	}

	public void setAttrType(String typeStr) {
		PerunAttrValueType type = PerunAttrValueType.parse(typeStr);
		this.setAttrType(type);
	}

	public void setAttrType(PerunAttrValueType attrType) {
		if (attrType == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		this.attrType = attrType;
	}

	public String getSeparator() {
		return this.separator;
	}

	public void setSeparator(String separator) {
		if (separator == null || separator.trim().isEmpty()) {
			separator = "=";
		}
		this.separator = separator;
	}

	@Override
	public String toString() {
		String str = "AttributeMapping{" +
				"identifier='" + identifier + '\'' +
				", rpcName='" + rpcName + '\'' +
				", ldapName='" + ldapName + '\'' +
				", attrType='" + attrType;
		if (PerunAttrValueType.MAP_KEY_VALUE.equals(attrType)) {
			str += "', separator='" + separator;
		}
		str += "'}";

		return str;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AttributeMapping that = (AttributeMapping) o;
		return Objects.equals(identifier, that.identifier) &&
				Objects.equals(rpcName, that.rpcName) &&
				Objects.equals(ldapName, that.ldapName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, rpcName, ldapName);
	}

}
