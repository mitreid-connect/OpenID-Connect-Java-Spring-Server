package cz.muni.ics.oidc.saml;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class SamlInvalidateSessionFilter extends GenericFilterBean {

    private final AntPathRequestMatcher matcher;

    private final SecurityContextLogoutHandler contextLogoutHandler;
    private final List<String> internalReferrers = new ArrayList<>();

    public SamlInvalidateSessionFilter(String pattern,
                                       String idpEntityId,
                                       String oidcIssuer,
                                       String proxySpEntityId,
                                       SecurityContextLogoutHandler contextLogoutHandler,
                                       String[] internalReferrers)
    {
        this.matcher = new AntPathRequestMatcher(pattern);
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
        if (matcher.matches(req)) {
            String referer = req.getHeader(REFERER);
            if (!isInternalReferer(referer)) {
                log.debug("Got external referer, clear session to reauthenticate");
                contextLogoutHandler.logout(req, res, null);
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isInternalReferer(String referer) {
        if (!StringUtils.hasText(referer)) { // no referer, consider as internal
            return true;
        }
        for (String internal : internalReferrers) {
            if (referer.startsWith(internal)) {
                return true;
            }
        }
        return false;
    }

}
