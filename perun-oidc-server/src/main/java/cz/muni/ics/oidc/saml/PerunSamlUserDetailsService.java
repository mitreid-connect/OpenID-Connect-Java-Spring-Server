package cz.muni.ics.oidc.saml;

import cz.muni.ics.oidc.server.PerunPrincipal;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

@Slf4j
public class PerunSamlUserDetailsService implements SAMLUserDetailsService {

    private final PerunAdapter perunAdapter;
    private final SamlProperties samlProperties;

    @Autowired
    public PerunSamlUserDetailsService(PerunAdapter perunAdapter, SamlProperties samlProperties) {
        this.perunAdapter = perunAdapter;
        this.samlProperties = samlProperties;
    }

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        log.debug("Loading user for SAML credential");
        PerunPrincipal p = FiltersUtils.getPerunPrincipal(credential, samlProperties.getUserIdentifierAttribute());
        log.debug("Fetching user from perun ({})", p);
        return perunAdapter.getPreauthenticatedUserId(p);
    }

}
