package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Source for claim which releases the defined value.
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.valueSeparator</b> - @NULL or actual separator, if set to smth. else than null,
 *     value is considered as an array</li>
 *     <li><b>custom.claim.[claimName].source.value</b> - list of values separated by specified separator, in case
 *     of string separator should be set to @NULL and full value will be released as string</li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
@Slf4j
public class StaticValueClaimSource extends ClaimSource {

	private static final String NO_SEPARATOR = "@NULL";
	private static final String VALUE_SEPARATOR = "valueSeparator";
	private static final String VALUE = "value";

	private final String valueSeparator;
	private String[] valueArr;
	private final String valueStr;

	public StaticValueClaimSource(ClaimSourceInitContext ctx) {
		super(ctx);
		this.valueSeparator = ctx.getProperty(VALUE_SEPARATOR, NO_SEPARATOR);
		this.valueStr = ctx.getProperty(VALUE, null);
		this.valueArr = null;
		if (valueStr != null) {
			valueArr = valueStr.split(valueSeparator);
		}
		log.debug("{} - valueSeparator: '{}', valueStr: '{}', valueArr: '{}'", getClaimName(),
				valueSeparator, valueStr, valueArr);
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		return Collections.emptySet();
	}

	@Override
	public JsonNode produceValue(ClaimSourceProduceContext pctx) {
		JsonNode value = NullNode.getInstance();
		if (!NO_SEPARATOR.equals(valueSeparator) && valueArr != null) {
			ArrayNode arrJson = JsonNodeFactory.instance.arrayNode();
			for (String v: valueArr) {
				if (StringUtils.hasText(v)) {
					arrJson.add(v);
				}
			}
			if (arrJson.size() > 0) {
				value = arrJson;
			}
		} else if (StringUtils.hasText(valueStr)) {
			value = JsonNodeFactory.instance.textNode(valueStr);
		}

		log.debug("{} - produced value for user({}): '{}'", getClaimName(), pctx.getPerunUserId(), value);
		return value;
	}

	@Override
	public String toString() {
		return "Fixed value " + (valueArr != null ? Arrays.toString(valueArr) : valueStr);
	}

}
