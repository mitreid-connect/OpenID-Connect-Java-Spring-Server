package cz.muni.ics.oidc.server.claims;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;
import org.springframework.util.StringUtils;

public class ClaimUtils {

    public static final String NO_VALUE = null;

    public static boolean isPropSetAndHasNonNullAttribute(String propertyName, ClaimSourceProduceContext ctx) {
        return isPropSetAndHasAttribute(propertyName, ctx)
                && ctx.getAttrValues().get(propertyName) != null;
    }

    public static boolean isPropSetAndHasAttribute(String propertyName, ClaimSourceProduceContext ctx) {
        return isPropSet(propertyName) && ctx.getAttrValues().containsKey(propertyName);
    }

    public static boolean isPropSet(String propertyName) {
        return StringUtils.hasText(propertyName);
    }

    public static String fillStringMandatoryProperty(String suffix, ClaimInitContext ctx, String claimName) {
        String filled = fillStringPropertyOrDefaultVal(ctx.getProperty(suffix, NO_VALUE), NO_VALUE);

        if (filled == null) {
            throw new IllegalArgumentException(claimName + " - missing mandatory configuration option: " + suffix);
        }

        return filled;
    }

    public static String fillStringPropertyOrDefaultVal(String suffix, ClaimInitContext ctx, String defaultVal) {
        return fillStringPropertyOrDefaultVal(ctx.getProperty(suffix, NO_VALUE), defaultVal);
    }

    private static String fillStringPropertyOrDefaultVal(String prop, String defaultVal) {
        if (StringUtils.hasText(prop)) {
            return prop;
        } else {
            return defaultVal;
        }
    }

    public static boolean fillBooleanPropertyOrDefaultVal(String suffix, ClaimSourceInitContext ctx, boolean defaultVal) {
        return fillBooleanPropertyOrDefaultVal(ctx.getProperty(suffix, NO_VALUE), defaultVal);
    }

    private static boolean fillBooleanPropertyOrDefaultVal(String prop, boolean defaultVal) {
        if (StringUtils.hasText(prop)) {
            return Boolean.parseBoolean(prop);
        } else {
            return defaultVal;
        }
    }

    public static ArrayNode listToArrayNode(List<String> list) {
        ArrayNode res = JsonNodeFactory.instance.arrayNode();
        if (list != null && !list.isEmpty()) {
            for (String s : list) {
                if (StringUtils.hasText(s)) {
                    res.add(s);
                }
            }
        }
        return res;
    }
}
