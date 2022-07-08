package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.exceptions.ConfigurationException;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.saml.SamlProperties;
import cz.muni.ics.oidc.server.filters.AuthProcFilter;
import cz.muni.ics.oidc.server.filters.AuthProcFilterInitContext;
import cz.muni.ics.oidc.server.filters.AuthProcFilterCommonVars;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.saml.SAMLCredential;

/**
 * This filter logs information about the user who has logged in INFO level in the format
 * {} - user_id '{}', user_identifier '{}', user_name '{}', service_identifier '{}', service_name: '{}'.
 * @see cz.muni.ics.oidc.server.filters.AuthProcFilter (basic configuration options)
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunLogIdentityFilter extends AuthProcFilter {

    private final String userIdentifierAttr;

    public PerunLogIdentityFilter(AuthProcFilterInitContext params) throws ConfigurationException {
        super(params);
        userIdentifierAttr = params.getBeanUtil().getBean(SamlProperties.class).getUserIdentifierAttribute();
    }

    @Override
    protected boolean process(HttpServletRequest req, HttpServletResponse res, AuthProcFilterCommonVars params) {
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

        log.info("{} - user_id '{}', user_identifier '{}', user_name '{}', service_identifier '{}', service_name: '{}'",
                getFilterName(), id, identifier, name, clientId, clientName);
        return true;
    }

}
