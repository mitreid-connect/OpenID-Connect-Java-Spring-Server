package cz.muni.ics.oidc.saml;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;

public class PerunWebSSOProfileConsumerImpl extends WebSSOProfileConsumerImpl {

    private boolean enableComparison = false;
    private Set<String> reservedPrefixes;

    public void setEnableComparison(boolean enableComparison) {
        this.enableComparison = enableComparison;
    }

    public void setReservedPrefixes(Set<String> reservedPrefixes) {
        this.reservedPrefixes = reservedPrefixes;
    }

    @Override
    protected void verifyAuthenticationStatement(AuthnStatement auth,
                                                 RequestedAuthnContext requestedAuthnContext,
                                                 SAMLMessageContext context)
        throws AuthenticationException
    {
        // Validate users session is still valid
        if (auth.getSessionNotOnOrAfter() != null && auth.getSessionNotOnOrAfter().isBeforeNow()) {
            throw new CredentialsExpiredException("Authentication session is not valid on or after "
                + auth.getSessionNotOnOrAfter());
        }

        // Verify context
        verifyAuthnContext(requestedAuthnContext, auth.getAuthnContext(), context);
    }

    @Override
    protected void verifyAuthnContext(RequestedAuthnContext requestedAuthnContext,
                                      AuthnContext receivedContext, SAMLMessageContext context)
        throws InsufficientAuthenticationException
    {
        if (!enableComparison) {
            return;
        }
        if (filterOutConditionsMet(requestedAuthnContext)) {
            filterOutPrefixedAcrs(requestedAuthnContext);
        }
        super.verifyAuthnContext(requestedAuthnContext, receivedContext, context);
    }

    private boolean filterOutConditionsMet(RequestedAuthnContext requestedAuthnContext) {
        if (requestedAuthnContext == null) {
            return false;
        } else {
            List<AuthnContextClassRef> requested = requestedAuthnContext.getAuthnContextClassRefs();
            return requested != null
                && !requested.isEmpty()
                && reservedPrefixes != null
                && !reservedPrefixes.isEmpty();
        }
    }

    private void filterOutPrefixedAcrs(RequestedAuthnContext requestedAuthnContext) {
        List<AuthnContextClassRef> requested = requestedAuthnContext.getAuthnContextClassRefs();
        Iterator<AuthnContextClassRef> it = requested.iterator();
        while (it.hasNext()) {
            AuthnContextClassRef accr = it.next();
            if (reservedPrefixes == null || reservedPrefixes.isEmpty()) {
                continue;
            }
            for (String prefix : reservedPrefixes) {
                if (accr.getAuthnContextClassRef().startsWith(prefix)) {
                    it.remove();
                }
            }
        }
    }
}
