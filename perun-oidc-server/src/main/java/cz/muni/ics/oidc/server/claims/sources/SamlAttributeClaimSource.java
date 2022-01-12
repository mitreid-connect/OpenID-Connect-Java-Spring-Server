package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import java.util.Collections;
import java.util.Set;

public class SamlAttributeClaimSource extends ClaimSource {

    private static final String ATTRIBUTE = "attribute";
    private static final String MULTI_VALUE = "isMultiValue";
    private static final String NO_VALUE_AS_NULL = "noValueAsNull";
    private static final String SEPARATOR = "separator";

    private final String attributeName;
    private final String separator;
    private final boolean multiValue;
    private final boolean noValueAsNull;

    public SamlAttributeClaimSource(ClaimSourceInitContext ctx) {
        super(ctx);
        this.attributeName = ClaimUtils.fillStringMandatoryProperty(ATTRIBUTE, ctx, getClaimName());
        this.multiValue = ClaimUtils.fillBooleanPropertyOrDefaultVal(MULTI_VALUE, ctx, true);
        this.noValueAsNull = ClaimUtils.fillBooleanPropertyOrDefaultVal(NO_VALUE_AS_NULL, ctx, false);
        this.separator = ClaimUtils.fillStringPropertyOrDefaultVal(SEPARATOR, ctx, ";");
    }

    @Override
    public Set<String> getAttrIdentifiers() {
        return Collections.emptySet();
    }

    @Override
    public JsonNode produceValue(ClaimSourceProduceContext pctx) {
        SamlAuthenticationDetails details = pctx.getSamlAuthenticationDetails();
        if (details == null || details.getAttributes() == null || details.getAttributes().isEmpty()) {
            return JsonNodeFactory.instance.nullNode();
        }
        String[] attrValue = details.getAttributes().getOrDefault(attributeName, null);
        if (multiValue) {
            if (attrValue == null || attrValue.length == 0) {
                return !noValueAsNull ? JsonNodeFactory.instance.arrayNode() : JsonNodeFactory.instance.nullNode();
            } else {
                ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
                for (String val: attrValue) {
                    arrayNode.add(val);
                }
                return arrayNode;
            }
        } else {
            if (attrValue == null || attrValue.length == 0) {
                return JsonNodeFactory.instance.nullNode();
            } else {
                StringBuilder finalStr = new StringBuilder(separator);
                for (String s: attrValue) {
                    finalStr.append(s);
                }
                return JsonNodeFactory.instance.textNode(finalStr.toString());
            }
        }
    }
}
