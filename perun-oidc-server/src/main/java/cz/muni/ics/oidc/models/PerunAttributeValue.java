package cz.muni.ics.oidc.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

/**
 * Model representing value of attribute from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class PerunAttributeValue extends PerunAttributeValueAwareModel {

    private String attrName;

    public PerunAttributeValue(String attrName, String type, JsonNode value) {
        super(type, value);
        this.setAttrName(attrName);
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerunAttributeValue that = (PerunAttributeValue) o;
        return super.equals(that) && Objects.equals(attrName, that.attrName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), valueAsJson(), attrName);
    }

    @Override
    public String toString() {
        return "PerunAttributeValue{" +
                "attrName='" + attrName + '\'' +
                '}';
    }

}
