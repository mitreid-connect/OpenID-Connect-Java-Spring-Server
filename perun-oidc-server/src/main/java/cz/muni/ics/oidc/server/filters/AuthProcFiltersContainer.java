package cz.muni.ics.oidc.server.filters;

import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.AUTHORIZE_REQ_PATTERN;
import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.DEVICE_APPROVE_REQ_PATTERN;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.saml.SamlProperties;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Wrapper filter for the AuthProcFilters in the security chain. Takes care of providing most basic parameters
 * and calls the custom AuthProcFilter chain.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class AuthProcFiltersContainer extends GenericFilterBean {

    private static final RequestMatcher AUTHORIZE_MATCHER = new AntPathRequestMatcher(AUTHORIZE_REQ_PATTERN);
    private static final RequestMatcher AUTHORIZE_ALL_MATCHER = new AntPathRequestMatcher(AUTHORIZE_REQ_PATTERN + "/**");
    private static final RequestMatcher DEVICE_CODE_MATCHER = new AntPathRequestMatcher(DEVICE_APPROVE_REQ_PATTERN);
    private static final RequestMatcher DEVICE_CODE_ALL_MATCHER = new AntPathRequestMatcher(DEVICE_APPROVE_REQ_PATTERN + "/**");
    private static final RequestMatcher MATCHER = new OrRequestMatcher(
            Arrays.asList(AUTHORIZE_MATCHER, AUTHORIZE_ALL_MATCHER, DEVICE_CODE_MATCHER, DEVICE_CODE_ALL_MATCHER));

    private final Properties properties;
    private final BeanUtil beanUtil;
    private final OAuth2RequestFactory authRequestFactory;
    private final ClientDetailsEntityService clientDetailsEntityService;
    private final PerunAdapter perunAdapter;
    private final SamlProperties samlProperties;

    private List<AuthProcFilter> filters;

    @Autowired
    public AuthProcFiltersContainer(@Qualifier("coreProperties")Properties properties,
                                    BeanUtil beanUtil,
                                    OAuth2RequestFactory authRequestFactory,
                                    ClientDetailsEntityService clientDetailsEntityService,
                                    PerunAdapter perunAdapter,
                                    SamlProperties samlProperties)
    {
        this.properties = properties;
        this.beanUtil = beanUtil;
        this.authRequestFactory = authRequestFactory;
        this.clientDetailsEntityService = clientDetailsEntityService;
        this.perunAdapter = perunAdapter;
        this.samlProperties = samlProperties;
    }

    @PostConstruct
    public void postConstruct() {
        this.filters = AuthProcFiltersInitializer.initialize(properties, beanUtil);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        if (!MATCHER.matches(req)) {
            log.debug("AuthProc filters have been skipped, did not match authorization nor device req URL");
        } else {
            if (filters != null && !filters.isEmpty()) {
                ClientDetailsEntity client = FiltersUtils.extractClientFromRequest(req, authRequestFactory,
                        clientDetailsEntityService);
                Facility facility = null;
                if (client != null && StringUtils.hasText(client.getClientId())) {
                    try {
                        facility = perunAdapter.getFacilityByClientId(client.getClientId());
                    } catch (Exception e) {
                        log.warn("{} - could not fetch facility for client_id '{}'",
                                AuthProcFiltersContainer.class.getSimpleName(), client.getClientId(), e);
                    }
                }
                PerunUser user = FiltersUtils.getPerunUser(req, perunAdapter, samlProperties);
                AuthProcFilterCommonVars params = new AuthProcFilterCommonVars(client, facility, user);
                for (AuthProcFilter filter : filters) {
                    if (!filter.doFilter(req, res, params)) {
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(req, res);
    }

}
