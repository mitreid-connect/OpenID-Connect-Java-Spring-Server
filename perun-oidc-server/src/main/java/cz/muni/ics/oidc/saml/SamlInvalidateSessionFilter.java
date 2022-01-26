package cz.muni.ics.oidc.saml;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.AUTHORIZE_REQ_PATTERN;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.DEVICE_APPROVE_REQ_PATTERN;
import static org.springframework.http.HttpHeaders.REFERER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class SamlInvalidateSessionFilter extends GenericFilterBean {

    private static final RequestMatcher AUTHORIZE_MATCHER = new AntPathRequestMatcher(AUTHORIZE_REQ_PATTERN);
    private static final RequestMatcher AUTHORIZE_ALL_MATCHER = new AntPathRequestMatcher(AUTHORIZE_REQ_PATTERN + "/**");
    private static final RequestMatcher DEVICE_CODE_MATCHER = new AntPathRequestMatcher(DEVICE_APPROVE_REQ_PATTERN);
    private static final RequestMatcher DEVICE_CODE_ALL_MATCHER = new AntPathRequestMatcher(DEVICE_APPROVE_REQ_PATTERN + "/**");
    private static final RequestMatcher MATCHER = new OrRequestMatcher(
            Arrays.asList(AUTHORIZE_MATCHER, AUTHORIZE_ALL_MATCHER, DEVICE_CODE_MATCHER, DEVICE_CODE_ALL_MATCHER));

    private final SecurityContextLogoutHandler contextLogoutHandler;
    private final List<String> internalReferrers = new ArrayList<>();

    public SamlInvalidateSessionFilter(String idpEntityId,
                                       String oidcIssuer,
                                       String proxySpEntityId,
                                       SecurityContextLogoutHandler contextLogoutHandler,
                                       String[] internalReferrers)
    {
        if (StringUtils.hasText(idpEntityId)) {
            this.internalReferrers.add(idpEntityId);
        }
        if (StringUtils.hasText(oidcIssuer)) {
            this.internalReferrers.add(oidcIssuer);
        }
        if (StringUtils.hasText(proxySpEntityId)) {
            this.internalReferrers.add(proxySpEntityId);
        }
        this.contextLogoutHandler = contextLogoutHandler;
        if (internalReferrers != null && internalReferrers.length > 0) {
            List<String> referrers = Arrays.asList(internalReferrers);
            referrers = referrers.stream().filter(StringUtils::hasText).collect(Collectors.toList());
            if (!referrers.isEmpty()) {
                this.internalReferrers.addAll(referrers);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (MATCHER.matches(req)) {
            String referer = req.getHeader(REFERER);
            if (!isInternalReferer(referer)) {
                log.debug("Got external referer, clear session to reauthenticate");
                contextLogoutHandler.logout(req, res, null);
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isInternalReferer(String referer) {
        if (!StringUtils.hasText(referer)) {
            return false;
        }
        for (String internal : internalReferrers) {
            if (referer.startsWith(internal)) {
                return true;
            }
        }
        return false;
    }

}
