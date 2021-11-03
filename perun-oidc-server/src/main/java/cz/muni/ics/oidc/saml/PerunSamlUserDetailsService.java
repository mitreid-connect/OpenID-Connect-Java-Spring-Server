package cz.muni.ics.oidc.saml;

import cz.muni.ics.oidc.server.PerunPrincipal;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

public class PerunSamlUserDetailsService implements SAMLUserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(PerunSamlUserDetailsService.class);

    private final PerunAdapter perunAdapter;

    @Autowired
    public PerunSamlUserDetailsService(PerunAdapter perunAdapter) {
        this.perunAdapter = perunAdapter;
    }

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        log.debug("Loading user for SAML credential");
        PerunPrincipal p = FiltersUtils.getPerunPrincipal(credential);
        log.debug("Fetching user from perun ({})", p);
        return perunAdapter.getPreauthenticatedUserId(p);
    }

}
