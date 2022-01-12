package cz.muni.ics.oidc.server.claims.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.oauth2.model.AuthenticationStatement;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.util.StringUtils;

public class SamlAuthnStatementClaimSource extends ClaimSource {

    public SamlAuthnStatementClaimSource(ClaimSourceInitContext ctx) {
        super(ctx);
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
        List<AuthenticationStatement> statements = details.getAuthnStatements();
        if (statements == null || statements.isEmpty()) {
            return JsonNodeFactory.instance.arrayNode();
        } else {
            ArrayNode res = JsonNodeFactory.instance.arrayNode();
            for (AuthenticationStatement s: statements) {
                if (s == null) {
                    continue;
                }
                ObjectNode subNode = JsonNodeFactory.instance.objectNode();
                subNode.put("authenticatingAuthorities", transformAuthenticatingAuthorities(s));
                subNode.put("authnContextClassRef", transformAuthnContextClassRef(s));
                res.add(subNode);
            }
            return res;
        }
    }

    private JsonNode transformAuthenticatingAuthorities(AuthenticationStatement s) {
        if (s == null) {
            return JsonNodeFactory.instance.arrayNode();
        }
        return ClaimUtils.listToArrayNode(s.getAuthenticatingAuthorities());
    }

    private JsonNode transformAuthnContextClassRef(AuthenticationStatement s) {
        if (s == null) {
            return JsonNodeFactory.instance.nullNode();
        }
        return StringUtils.hasText(s.getAuthnContextClassRef()) ?
                JsonNodeFactory.instance.textNode(s.getAuthnContextClassRef()) : JsonNodeFactory.instance.nullNode();
    }

}
