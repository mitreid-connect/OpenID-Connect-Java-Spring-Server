package cz.muni.ics.oidc.server.filters;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.saml.SamlProperties;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * This filter calls other Perun filters saved in the PerunFiltersContext
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class CallPerunFiltersFilter extends GenericFilterBean {

    @Autowired
    private Properties coreProperties;

    @Autowired
    private BeanUtil beanUtil;

    @Autowired
    private OAuth2RequestFactory authRequestFactory;

    @Autowired
    private ClientDetailsEntityService clientDetailsEntityService;

    @Autowired
    private PerunAdapter perunAdapter;

    @Autowired
    private SamlProperties samlProperties;

    private PerunFiltersContext perunFiltersContext;

    @PostConstruct
    public void postConstruct() {
        this.perunFiltersContext = new PerunFiltersContext(coreProperties, beanUtil);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        List<PerunRequestFilter> filters = perunFiltersContext.getFilters();
        if (filters != null && !filters.isEmpty()) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            ClientDetailsEntity client = FiltersUtils.extractClientFromRequest(request, authRequestFactory,
                    clientDetailsEntityService);
            Facility facility = null;
            if (client != null && StringUtils.hasText(client.getClientId())) {
                try {
                    facility = perunAdapter.getFacilityByClientId(client.getClientId());
                } catch (Exception e) {
                    log.warn("{} - could not fetch facility for client_id '{}'",
                            CallPerunFiltersFilter.class.getSimpleName(), client.getClientId(), e);
                }
            }
            PerunUser user = FiltersUtils.getPerunUser(request, perunAdapter, samlProperties.getUserIdentifierAttribute());
            FilterParams params = new FilterParams(client, facility, user);
            for (PerunRequestFilter filter : filters) {
                if (!filter.doFilter(servletRequest, servletResponse, params)) {
                    return;
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
