package cz.muni.ics.oidc.server.claims.sources;

import cz.muni.ics.oauth2.model.AuthenticationStatement;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for a claim source which extracts value from a SAML AuthN statement.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public abstract class SamlAuthnStatementExtractorBaseClaimSource extends ClaimSource {

    public SamlAuthnStatementExtractorBaseClaimSource(ClaimSourceInitContext ctx) {
        super(ctx);
    }

    protected boolean hasAuthnStatements(ClaimSourceProduceContext pctx) {
        SamlAuthenticationDetails details = pctx.getSamlAuthenticationDetails();

        if (details == null || details.getAttributes() == null || details.getAttributes().isEmpty()) {
            return false;
        }

        List<AuthenticationStatement> statements = details.getAuthnStatements();
        if (statements == null || statements.isEmpty()) {
            return false;
        }
        return true;
    }

}
