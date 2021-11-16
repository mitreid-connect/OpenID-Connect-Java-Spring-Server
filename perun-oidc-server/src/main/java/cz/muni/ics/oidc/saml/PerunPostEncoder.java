package cz.muni.ics.oidc.saml;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class PerunPostEncoder extends HTTPPostEncoder {

    private static final Logger log = LoggerFactory.getLogger(PerunPostEncoder.class);

    public PerunPostEncoder(VelocityEngine engine, String templateId) {
        super(engine, templateId);
    }

    @Override
    protected void doEncode(MessageContext messageContext) throws MessageEncodingException {
        super.doEncode(messageContext);
    }

    @Override
    protected void populateVelocityContext(VelocityContext velocityContext, SAMLMessageContext messageContext, String endpointURL) throws MessageEncodingException {
        super.populateVelocityContext(velocityContext, messageContext, endpointURL);
        if (messageContext instanceof PerunSAMLMessageContext) {
            PerunSAMLMessageContext mcxt = (PerunSAMLMessageContext) messageContext;
            if (StringUtils.hasText(mcxt.getAarcIdpHint())) {
                velocityContext.put("aarc_idp_hint", mcxt.getAarcIdpHint());
            }
        }
    }
}
