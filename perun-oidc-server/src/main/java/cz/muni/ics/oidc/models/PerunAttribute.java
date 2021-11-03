package cz.muni.ics.oidc.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

/**
 * Perun Attribute model
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
public class PerunAttribute extends PerunAttributeValueAwareModel {

    private static final String BEAN_NAME = "Attribute";

    private Long id;
    private String friendlyName;
    private String namespace;
    private String description;
    private String displayName;
    private boolean writable;
    private boolean unique;
    private String entity;
    private String baseFriendlyName;
    private String friendlyNameParameter;
    private String valueCreatedAt;
    private String valueModifiedAt;

    public PerunAttribute(Long id, String friendlyName, String namespace, String description, String type,
                          String displayName, boolean writable, boolean unique, String entity, String baseFriendlyName,
                          String friendlyNameParameter, JsonNode value, String valueCreatedAt, String valueModifiedAt)
    {
        super(type, value);
        this.setId(id);
        this.setFriendlyName(friendlyName);
        this.setNamespace(namespace);
        this.setDescription(description);
        this.setType(type);
        this.setDisplayName(displayName);
        this.setWritable(writable);
        this.setUnique(unique);
        this.setEntity(entity);
        this.setBaseFriendlyName(baseFriendlyName);
        this.setFriendlyNameParameter(friendlyNameParameter);
        this.setValue(type, value);
        this.setValueCreatedAt(valueCreatedAt);
        this.setValueModifiedAt(valueModifiedAt);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        if (!StringUtils.hasText(friendlyName)) {
            throw new IllegalArgumentException("friendlyName cannot be empty");
        }

        this.friendlyName = friendlyName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        if (!StringUtils.hasText(namespace)) {
            throw new IllegalArgumentException("namespace cannot be empty");
        }

        this.namespace = namespace;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (!StringUtils.hasText(displayName)) {
            throw new IllegalArgumentException("displayName cannot be empty");
        }

        this.displayName = displayName;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        if (!StringUtils.hasText(entity)) {
            throw new IllegalArgumentException("entity cannot be empty");
        }

        this.entity = entity;
    }

    public String getBaseFriendlyName() {
        return baseFriendlyName;
    }

    public void setBaseFriendlyName(String baseFriendlyName) {
        this.baseFriendlyName = baseFriendlyName;
    }

    public String getFriendlyNameParameter() {
        return friendlyNameParameter;
    }

    public void setFriendlyNameParameter(String friendlyNameParameter) {
        this.friendlyNameParameter = friendlyNameParameter;
    }

    public String getValueCreatedAt() {
        return valueCreatedAt;
    }

    public void setValueCreatedAt(String valueCreatedAt) {
        this.valueCreatedAt = valueCreatedAt;
    }

    public String getValueModifiedAt() {
        return valueModifiedAt;
    }

    public void setValueModifiedAt(String valueModifiedAt) {
        this.valueModifiedAt = valueModifiedAt;
    }

    @JsonIgnore
    public String getUrn() {
        return this.namespace + ':' + this.friendlyName;
    }

    public ObjectNode toJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        node.put("id", id);
        node.put("friendlyName", friendlyName);
        node.put("namespace", namespace);
        node.put("type", super.getType());
        node.put("displayName", displayName);
        node.put("writable", writable);
        node.put("unique", unique);
        node.put("entity", entity);
        node.put("beanName", BEAN_NAME);
        node.put("baseFriendlyName", baseFriendlyName);
        node.put("friendlyName", friendlyName);
        node.put("friendlyNameParameter", friendlyNameParameter);
        node.set("value", this.valueAsJson());

        return node;
    }

    public PerunAttributeValue toPerunAttributeValue() {
        return new PerunAttributeValue(this.getUrn(), super.getType(), this.valueAsJson());
    }

}
