package cz.muni.ics.oidc.saml;

import static cz.muni.ics.openid.connect.web.AuthenticationTimeStamper.AUTH_TIMESTAMP;

import cz.muni.ics.oidc.server.PerunOIDCTokenService;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.springframework.security.core.Authentication;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Slf4j
public class PerunSamlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException
    {
        Date authTimestamp = new Date();
        request.getSession().setAttribute(AUTH_TIMESTAMP, authTimestamp);
        if (authentication instanceof ExpiringUsernameAuthenticationToken) {
            ExpiringUsernameAuthenticationToken token = (ExpiringUsernameAuthenticationToken) authentication;
            Object details = token.getDetails();
            if (details instanceof WebAuthenticationDetails) {
                WebAuthenticationDetails webDetails = (WebAuthenticationDetails) details;
                log.info("successful authentication, remote IP address {}", webDetails.getRemoteAddress());
            }
            setAcrToSession(request, token);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void setAcrToSession(HttpServletRequest request, Authentication token) {
        String acrs = ((SAMLCredential) token.getCredentials()).getAuthenticationAssertion()
                .getAuthnStatements().stream()
                .map(AuthnStatement::getAuthnContext)
                .map(AuthnContext::getAuthnContextClassRef)
                .map(AuthnContextClassRef::getAuthnContextClassRef)
                .collect(Collectors.joining());
        request.getSession(true).setAttribute(PerunOIDCTokenService.SESSION_PARAM_ACR, acrs);;
    }

}
