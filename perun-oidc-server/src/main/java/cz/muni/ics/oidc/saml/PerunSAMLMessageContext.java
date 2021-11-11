package cz.muni.ics.oidc.saml;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.ws.message.handler.HandlerChainResolver;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.OutTransport;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.storage.SAMLMessageStorage;

import javax.net.ssl.HostnameVerifier;
import javax.xml.namespace.QName;

public class PerunSAMLMessageContext extends SAMLMessageContext {

    private String aarcIdpHint;
    private final SAMLMessageContext originalContext;

    public PerunSAMLMessageContext(SAMLMessageContext originalContext) {
        this.originalContext = originalContext;
    }

    public String getAarcIdpHint() {
        return aarcIdpHint;
    }

    public void setAarcIdpHint(String aarcIdpHint) {
        this.aarcIdpHint = aarcIdpHint;
    }

    @Override
    public ExtendedMetadata getLocalExtendedMetadata() {
        return originalContext.getLocalExtendedMetadata();
    }

    @Override
    public void setLocalExtendedMetadata(ExtendedMetadata localExtendedMetadata) {
        originalContext.setLocalExtendedMetadata(localExtendedMetadata);
    }

    @Override
    public ExtendedMetadata getPeerExtendedMetadata() {
        return originalContext.getPeerExtendedMetadata();
    }

    @Override
    public void setPeerExtendedMetadata(ExtendedMetadata peerExtendedMetadata) {
        originalContext.setPeerExtendedMetadata(peerExtendedMetadata);
    }

    @Override
    public Decrypter getLocalDecrypter() {
        return originalContext.getLocalDecrypter();
    }

    @Override
    public void setLocalDecrypter(Decrypter localDecrypter) {
        originalContext.setLocalDecrypter(localDecrypter);
    }

    @Override
    public SignatureTrustEngine getLocalTrustEngine() {
        return originalContext.getLocalTrustEngine();
    }

    @Override
    public void setLocalTrustEngine(SignatureTrustEngine localTrustEngine) {
        originalContext.setLocalTrustEngine(localTrustEngine);
    }

    @Override
    public Credential getLocalSigningCredential() {
        return originalContext.getLocalSigningCredential();
    }

    @Override
    public void setLocalSigningCredential(Credential localSigningCredential) {
        originalContext.setLocalSigningCredential(localSigningCredential);
    }

    @Override
    public TrustEngine<X509Credential> getLocalSSLTrustEngine() {
        return originalContext.getLocalSSLTrustEngine();
    }

    @Override
    public void setLocalSSLTrustEngine(TrustEngine<X509Credential> localSSLTrustEngine) {
        originalContext.setLocalSSLTrustEngine(localSSLTrustEngine);
    }

    @Override
    public X509Credential getLocalSSLCredential() {
        return originalContext.getLocalSSLCredential();
    }

    @Override
    public void setLocalSSLCredential(X509Credential localSSLCredential) {
        originalContext.setLocalSSLCredential(localSSLCredential);
    }

    @Override
    public HostnameVerifier getLocalSSLHostnameVerifier() {
        return originalContext.getLocalSSLHostnameVerifier();
    }

    @Override
    public void setGetLocalSSLHostnameVerifier(HostnameVerifier verifier) {
        originalContext.setGetLocalSSLHostnameVerifier(verifier);
    }

    @Override
    public X509Credential getPeerSSLCredential() {
        return originalContext.getPeerSSLCredential();
    }

    @Override
    public void setPeerSSLCredential(X509Credential peerSSLCredential) {
        originalContext.setPeerSSLCredential(peerSSLCredential);
    }

    @Override
    public String getInboundSAMLBinding() {
        return originalContext.getInboundSAMLBinding();
    }

    @Override
    public void setInboundSAMLBinding(String inboundSAMLBinding) {
        originalContext.setInboundSAMLBinding(inboundSAMLBinding);
    }

    @Override
    public Endpoint getLocalEntityEndpoint() {
        return originalContext.getLocalEntityEndpoint();
    }

    @Override
    public void setLocalEntityEndpoint(Endpoint localEntityEndpoint) {
        originalContext.setLocalEntityEndpoint(localEntityEndpoint);
    }

    @Override
    public boolean isPeerUserSelected() {
        return originalContext.isPeerUserSelected();
    }

    @Override
    public void setPeerUserSelected(boolean peerUserSelected) {
        originalContext.setPeerUserSelected(peerUserSelected);
    }

    @Override
    public SAMLMessageStorage getMessageStorage() {
        return originalContext.getMessageStorage();
    }

    @Override
    public void setMessageStorage(SAMLMessageStorage messageStorage) {
        originalContext.setMessageStorage(messageStorage);
    }

    @Override
    public SAMLObject getInboundSAMLMessage() {
        return originalContext.getInboundSAMLMessage();
    }

    @Override
    public String getInboundSAMLMessageId() {
        return originalContext.getInboundSAMLMessageId();
    }

    @Override
    public DateTime getInboundSAMLMessageIssueInstant() {
        return originalContext.getInboundSAMLMessageIssueInstant();
    }

    @Override
    public String getInboundSAMLProtocol() {
        return originalContext.getInboundSAMLProtocol();
    }

    @Override
    public String getLocalEntityId() {
        return originalContext.getLocalEntityId();
    }

    @Override
    public EntityDescriptor getLocalEntityMetadata() {
        return originalContext.getLocalEntityMetadata();
    }

    @Override
    public QName getLocalEntityRole() {
        return originalContext.getLocalEntityRole();
    }

    @Override
    public RoleDescriptor getLocalEntityRoleMetadata() {
        return originalContext.getLocalEntityRoleMetadata();
    }

    @Override
    public MetadataProvider getMetadataProvider() {
        return originalContext.getMetadataProvider();
    }

    @Override
    public Credential getOuboundSAMLMessageSigningCredential() {
        return originalContext.getOuboundSAMLMessageSigningCredential();
    }

    @Override
    public SAMLObject getOutboundSAMLMessage() {
        return originalContext.getOutboundSAMLMessage();
    }

    @Override
    public String getOutboundSAMLMessageId() {
        return originalContext.getOutboundSAMLMessageId();
    }

    @Override
    public DateTime getOutboundSAMLMessageIssueInstant() {
        return originalContext.getOutboundSAMLMessageIssueInstant();
    }

    @Override
    public String getOutboundSAMLProtocol() {
        return originalContext.getOutboundSAMLProtocol();
    }

    @Override
    public Endpoint getPeerEntityEndpoint() {
        return originalContext.getPeerEntityEndpoint();
    }

    @Override
    public String getPeerEntityId() {
        return originalContext.getPeerEntityId();
    }

    @Override
    public EntityDescriptor getPeerEntityMetadata() {
        return originalContext.getPeerEntityMetadata();
    }

    @Override
    public QName getPeerEntityRole() {
        return originalContext.getPeerEntityRole();
    }

    @Override
    public RoleDescriptor getPeerEntityRoleMetadata() {
        return originalContext.getPeerEntityRoleMetadata();
    }

    @Override
    public String getRelayState() {
        return originalContext.getRelayState();
    }

    @Override
    public SAMLObject getSubjectNameIdentifier() {
        return originalContext.getSubjectNameIdentifier();
    }

    @Override
    public boolean isInboundSAMLMessageAuthenticated() {
        return originalContext.isInboundSAMLMessageAuthenticated();
    }

    @Override
    public void setInboundSAMLMessage(SAMLObject message) {
        originalContext.setInboundSAMLMessage(message);
    }

    @Override
    public void setInboundSAMLMessageAuthenticated(boolean isAuthenticated) {
        originalContext.setInboundSAMLMessageAuthenticated(isAuthenticated);
    }

    @Override
    public void setInboundSAMLMessageId(String id) {
        originalContext.setInboundSAMLMessageId(id);
    }

    @Override
    public void setInboundSAMLMessageIssueInstant(DateTime instant) {
        originalContext.setInboundSAMLMessageIssueInstant(instant);
    }

    @Override
    public void setInboundSAMLProtocol(String protocol) {
        originalContext.setInboundSAMLProtocol(protocol);
    }

    @Override
    public void setLocalEntityId(String id) {
        originalContext.setLocalEntityId(id);
    }

    @Override
    public void setLocalEntityMetadata(EntityDescriptor metadata) {
        originalContext.setLocalEntityMetadata(metadata);
    }

    @Override
    public void setLocalEntityRole(QName role) {
        originalContext.setLocalEntityRole(role);
    }

    @Override
    public void setLocalEntityRoleMetadata(RoleDescriptor role) {
        originalContext.setLocalEntityRoleMetadata(role);
    }

    @Override
    public void setMetadataProvider(MetadataProvider provider) {
        originalContext.setMetadataProvider(provider);
    }

    @Override
    public void setOutboundSAMLMessage(SAMLObject message) {
        originalContext.setOutboundSAMLMessage(message);
    }

    @Override
    public void setOutboundSAMLMessageId(String id) {
        originalContext.setOutboundSAMLMessageId(id);
    }

    @Override
    public void setOutboundSAMLMessageIssueInstant(DateTime instant) {
        originalContext.setOutboundSAMLMessageIssueInstant(instant);
    }

    @Override
    public void setOutboundSAMLMessageSigningCredential(Credential credential) {
        originalContext.setOutboundSAMLMessageSigningCredential(credential);
    }

    @Override
    public void setOutboundSAMLProtocol(String protocol) {
        originalContext.setOutboundSAMLProtocol(protocol);
    }

    @Override
    public void setPeerEntityEndpoint(Endpoint endpoint) {
        originalContext.setPeerEntityEndpoint(endpoint);
    }

    @Override
    public void setPeerEntityId(String id) {
        originalContext.setPeerEntityId(id);
    }

    @Override
    public void setPeerEntityMetadata(EntityDescriptor metadata) {
        originalContext.setPeerEntityMetadata(metadata);
    }

    @Override
    public void setPeerEntityRole(QName role) {
        originalContext.setPeerEntityRole(role);
    }

    @Override
    public void setPeerEntityRoleMetadata(RoleDescriptor role) {
        originalContext.setPeerEntityRoleMetadata(role);
    }

    @Override
    public void setRelayState(String state) {
        originalContext.setRelayState(state);
    }

    @Override
    public void setSubjectNameIdentifier(SAMLObject identifier) {
        originalContext.setSubjectNameIdentifier(identifier);
    }

    @Override
    public byte[] getOutboundMessageArtifactType() {
        return originalContext.getOutboundMessageArtifactType();
    }

    @Override
    public void setOutboundMessageArtifactType(byte[] type) {
        originalContext.setOutboundMessageArtifactType(type);
    }

    @Override
    public boolean isIssuerAuthenticated() {
        return originalContext.isIssuerAuthenticated();
    }

    @Override
    public String getCommunicationProfileId() {
        return originalContext.getCommunicationProfileId();
    }

    @Override
    public XMLObject getInboundMessage() {
        return originalContext.getInboundMessage();
    }

    @Override
    public String getInboundMessageIssuer() {
        return originalContext.getInboundMessageIssuer();
    }

    @Override
    public InTransport getInboundMessageTransport() {
        return originalContext.getInboundMessageTransport();
    }

    @Override
    public XMLObject getOutboundMessage() {
        return originalContext.getOutboundMessage();
    }

    @Override
    public String getOutboundMessageIssuer() {
        return originalContext.getOutboundMessageIssuer();
    }

    @Override
    public OutTransport getOutboundMessageTransport() {
        return originalContext.getOutboundMessageTransport();
    }

    @Override
    public SecurityPolicyResolver getSecurityPolicyResolver() {
        return originalContext.getSecurityPolicyResolver();
    }

    @Override
    public void setCommunicationProfileId(String id) {
        originalContext.setCommunicationProfileId(id);
    }

    @Override
    public void setInboundMessage(XMLObject message) {
        originalContext.setInboundMessage(message);
    }

    @Override
    public void setInboundMessageIssuer(String issuer) {
        originalContext.setInboundMessageIssuer(issuer);
    }

    @Override
    public void setInboundMessageTransport(InTransport transport) {
        originalContext.setInboundMessageTransport(transport);
    }

    @Override
    public void setOutboundMessage(XMLObject message) {
        originalContext.setOutboundMessage(message);
    }

    @Override
    public void setOutboundMessageIssuer(String issuer) {
        originalContext.setOutboundMessageIssuer(issuer);
    }

    @Override
    public void setOutboundMessageTransport(OutTransport transport) {
        originalContext.setOutboundMessageTransport(transport);
    }

    @Override
    public void setSecurityPolicyResolver(SecurityPolicyResolver resolver) {
        originalContext.setSecurityPolicyResolver(resolver);
    }

    @Override
    public HandlerChainResolver getPreSecurityInboundHandlerChainResolver() {
        return originalContext.getPreSecurityInboundHandlerChainResolver();
    }

    @Override
    public HandlerChainResolver getPostSecurityInboundHandlerChainResolver() {
        return originalContext.getPostSecurityInboundHandlerChainResolver();
    }

    @Override
    public HandlerChainResolver getOutboundHandlerChainResolver() {
        return originalContext.getOutboundHandlerChainResolver();
    }

    @Override
    public void setPreSecurityInboundHandlerChainResolver(HandlerChainResolver newHandlerChainResolver) {
        originalContext.setPreSecurityInboundHandlerChainResolver(newHandlerChainResolver);
    }

    @Override
    public void setPostSecurityInboundHandlerChainResolver(HandlerChainResolver newHandlerChainResolver) {
        originalContext.setPostSecurityInboundHandlerChainResolver(newHandlerChainResolver);
    }

    @Override
    public void setOutboundHandlerChainResolver(HandlerChainResolver newHandlerChainResolver) {
        originalContext.setOutboundHandlerChainResolver(newHandlerChainResolver);
    }
}
