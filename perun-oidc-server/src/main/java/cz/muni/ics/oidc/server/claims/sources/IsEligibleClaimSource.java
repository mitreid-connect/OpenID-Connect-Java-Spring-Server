package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * This source checks if the timestamp is within defined months subtracted from the current timestamp.
 * If so, returns TRUE, FALSE otherwise.
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.attribute</b> - attribute containing the eligibility last seen timestamp passed via SAML authentication</li>
 *     <li><b>custom.claim.[claimName].source.validityPeriodMonths</b> - amount of months that we subtract from the current timestamp and compare it with the eligibility timestamp. If not provided, default 12 months is used</li>
 * </ul>
 *
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class IsEligibleClaimSource extends ClaimSource {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int DEFAULT_VALIDITY_PERIOD = 12; // 12 months

    private static final String SOURCE_ATTR_NAME = "attribute";
    private static final String VALIDITY_PERIOD_MONTHS = "validityPeriodMonths";

    private final String sourceAttr;
    private final int validityPeriodMonths;

    public IsEligibleClaimSource(ClaimSourceInitContext ctx) {
        super(ctx);

        this.sourceAttr = ClaimUtils.fillStringMandatoryProperty(SOURCE_ATTR_NAME, ctx, getClaimName());
        this.validityPeriodMonths = ClaimUtils.fillIntegerPropertyOrDefaultVal(VALIDITY_PERIOD_MONTHS, ctx, DEFAULT_VALIDITY_PERIOD);
        log.debug("{} - SAML attribute: '{}', validity period in months: '{}'", getClaimName(), sourceAttr, validityPeriodMonths);
    }

    @Override
    public Set<String> getAttrIdentifiers() {
        return Collections.singleton(sourceAttr);
    }

    @Override
    public JsonNode produceValue(ClaimSourceProduceContext pctx) {
        SamlAuthenticationDetails details = pctx.getSamlAuthenticationDetails();
        if (details == null || details.getAttributes() == null || details.getAttributes().isEmpty()) {
            log.warn("{} - no attribute set to get the source attribute from, returning FALSE", getClaimName());
            return JsonNodeFactory.instance.booleanNode(false);
        }
        String[] attrValue = details.getAttributes().getOrDefault(sourceAttr, new String[] {});
        if (attrValue == null || attrValue.length == 0) {
            log.warn("{} - no attribute to construct value from, returning FALSE", getClaimName());
            return JsonNodeFactory.instance.booleanNode(false);
        } else {
            if (attrValue.length > 1)  {
                log.warn("{} - configured source attribute '{}' has more than one value, will use just the first one", getClaimName(), sourceAttr);
            }
            String timestamp = attrValue[0];
            return JsonNodeFactory.instance.booleanNode(isEligible(timestamp));
        }
    }

    private boolean isEligible(String eligibleLastSeenValue) {
        if (!StringUtils.hasText(eligibleLastSeenValue)) {
            return false;
        }
        LocalDate eligibleLastSeenTimestamp;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
            eligibleLastSeenTimestamp = LocalDate.parse(eligibleLastSeenValue, formatter);
        } catch (DateTimeParseException e) {
            log.warn("{} - could not parse value '{}' as timestamp in format '{}'. Returning NOT ELIGIBLE.",
                    getClaimName(), eligibleLastSeenValue, TIMESTAMP_FORMAT);
            return false;
        }

        LocalDate now = LocalDateTime.now().toLocalDate();
        boolean isValid = !eligibleLastSeenTimestamp.isBefore(now.minusMonths(validityPeriodMonths));
        log.debug("{} - timestamp '{}' is time {} the defined period of '{} months'",
                    getClaimName(), eligibleLastSeenTimestamp, isValid ? "within" : "out of", DEFAULT_VALIDITY_PERIOD);
        return isValid;
    }

}
