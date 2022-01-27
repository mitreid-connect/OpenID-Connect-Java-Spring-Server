package cz.muni.ics.oidc.saml;

import java.io.IOException;
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
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class SamlInvalidateSessionFilter extends GenericFilterBean {

    private static final RequestMatcher MATCHER = new OrRequestMatcher(
            new AntPathRequestMatcher("/authorize"),
            new AntPathRequestMatcher("/device")
    );

    private final SecurityContextLogoutHandler contextLogoutHandler;

    public SamlInvalidateSessionFilter(SecurityContextLogoutHandler contextLogoutHandler) {
        this.contextLogoutHandler = contextLogoutHandler;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (MATCHER.matches(req)) {
            log.debug("Invalidate session to enable SAML IdP re-authentication");
            contextLogoutHandler.logout(req, res, null);
        }
        chain.doFilter(req, res);
    }

}
