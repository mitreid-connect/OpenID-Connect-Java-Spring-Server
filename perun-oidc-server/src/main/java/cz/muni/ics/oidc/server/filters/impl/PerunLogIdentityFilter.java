package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.saml.SamlProperties;
import cz.muni.ics.oidc.server.filters.AuthProcFilter;
import cz.muni.ics.oidc.server.filters.AuthProcFilterParams;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.saml.SAMLCredential;

/**
 * This filter logs information about the user who has logged in INFO level in the format:
 * 'User ID: {}, User identifier: {}, User name: {}, service ID: {}, service name: {}'.
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunLogIdentityFilter extends AuthProcFilter {

    public static final String APPLIED = "APPLIED_" + PerunLogIdentityFilter.class.getSimpleName();

    private final String userIdentifierAttr;

    public PerunLogIdentityFilter(AuthProcFilterParams params) {
        super(params);
        userIdentifierAttr = params.getBeanUtil().getBean(SamlProperties.class).getUserIdentifierAttribute();
    }

    @Override
    protected String getSessionAppliedParamName() {
        return APPLIED;
    }

    @Override
    protected boolean process(HttpServletRequest req, HttpServletResponse res, FilterParams params) {
        PerunUser user = params.getUser();
        ClientDetailsEntity client = params.getClient();
        SAMLCredential samlCredential = FiltersUtils.getSamlCredential(req);

        Long id = -1L;
        String name = "_empty";
        String identifier = "_empty";
        String clientName = "_empty";
        String clientId = "_empty";
        if (user != null) {
            name = user.getFirstName() + ' ' + user.getLastName();
            id = user.getId();
        }
        if (client != null) {
            clientName = client.getClientName();
            clientId = client.getClientId();
        }
        if (samlCredential != null) {
            identifier = FiltersUtils.getExtLogin(samlCredential, userIdentifierAttr);
        }

        log.info("User ID: {}, User identifier: {}, User name: {}, service ID: {}, service name: {}",
                id, identifier, name, clientId, clientName);
        return true;
    }

}
