package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.oauth2.model.AuthenticationStatement;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Claim source which extracts the AuthN instant value from a SAML AuthN statement.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class SamlAuthnInstantClaimSource extends SamlAuthnStatementExtractorBaseClaimSource {

    public SamlAuthnInstantClaimSource(ClaimSourceInitContext ctx) {
        super(ctx);
        log.debug("{} - initialized", getClaimName());
    }

    @Override
    public Set<String> getAttrIdentifiers() {
        return Collections.emptySet();
    }

    @Override
    public JsonNode produceValue(ClaimSourceProduceContext pctx) {
        JsonNode res = JsonNodeFactory.instance.nullNode();

        if (!hasAuthnStatements(pctx)) {
            return res;
        }

        List<AuthenticationStatement> statements = pctx.getSamlAuthenticationDetails().getAuthnStatements();
        for (AuthenticationStatement s: statements) {
            if (!isValidStatement(s)) {
                continue;
            }
            res = JsonNodeFactory.instance.textNode(s.getAuthnInstant());
            break;
        }
        log.debug("{} - produced value '{}'", getClaimName(), res);
        return res;
    }

    private boolean isValidStatement(AuthenticationStatement s) {
        return s != null && StringUtils.hasText(s.getAuthnInstant());
    }

}
