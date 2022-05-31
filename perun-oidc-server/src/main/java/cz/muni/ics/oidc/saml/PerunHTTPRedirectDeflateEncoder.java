package cz.muni.ics.oidc.saml;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.util.URLBuilder;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.util.Pair;
import org.springframework.util.StringUtils;

import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.AARC_IDP_HINT;

public class PerunHTTPRedirectDeflateEncoder extends HTTPRedirectDeflateEncoder {

    @Override
    protected String buildRedirectURL(SAMLMessageContext messageContext, String endpointURL, String message)
            throws MessageEncodingException
    {
        String url = super.buildRedirectURL(messageContext, endpointURL, message);
        if (messageContext instanceof PerunSAMLMessageContext) {
            PerunSAMLMessageContext mcxt = (PerunSAMLMessageContext) messageContext;
            if (StringUtils.hasText(mcxt.getAarcIdpHint())) {
                URLBuilder builder = new URLBuilder(url);
                builder.getQueryParams().add(new Pair<>(AARC_IDP_HINT, mcxt.getAarcIdpHint()));
                url = builder.buildURL();
            }
        }
        return url;
    }
}
