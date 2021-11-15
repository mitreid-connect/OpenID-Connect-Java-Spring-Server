package cz.muni.ics.oidc.saml;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.CLIENT_ID_PREFIX;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.EFILTER_PREFIX;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.FILTER_PREFIX;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.IDP_ENTITY_ID_PREFIX;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_CLIENT_ID;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_PROMPT;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.REFEDS_MFA;

import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.FacilityAttrsConfig;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.filters.PerunFilterConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.util.StringUtils;

@Slf4j
public class PerunSamlEntryPoint extends SAMLEntryPoint {

    private final PerunAdapter perunAdapter;
    private final PerunOidcConfig config;
    private final FacilityAttrsConfig facilityAttrsConfig;
    private final SamlProperties samlProperties;

    @Autowired
    public PerunSamlEntryPoint(PerunAdapter perunAdapter,
                               PerunOidcConfig config,
                               FacilityAttrsConfig facilityAttrsConfig,
                               SamlProperties samlProperties)
    {
        this.perunAdapter = perunAdapter;
        this.config = config;
        this.facilityAttrsConfig = facilityAttrsConfig;
        this.samlProperties = samlProperties;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        try {
            SAMLMessageContext context = contextProvider.getLocalAndPeerEntity(request, response);

            if (isECP(context)) {
                initializeECP(request, context, e);
            } else if (isDiscovery(context)) {
                initializeDiscovery(context);
            } else {
                initializeSSO(request, context, e);
            }
        } catch (SAMLException | MetadataProviderException | MessageEncodingException e1) {
            log.debug("Error initializing entry point", e1);
            throw new ServletException(e1);
        }
    }

    protected WebSSOProfileOptions getExtendedOptions(HttpServletRequest request,
                                                      SAMLMessageContext context,
                                                      AuthenticationException exception)
            throws MetadataProviderException
    {
        WebSSOProfileOptions options = super.getProfileOptions(context, exception);
        addExtraParams(request, options);
        return options;
    }


    // copy from super class to call our getProfileOptions
    protected void initializeECP(HttpServletRequest request, SAMLMessageContext context,
                                 AuthenticationException e)
            throws MetadataProviderException, SAMLException, MessageEncodingException
    {
        WebSSOProfileOptions options = getExtendedOptions(request, context, e);
        log.debug("Processing SSO using ECP profile");
        webSSOprofileECP.sendAuthenticationRequest(context, options);
        samlLogger.log(SAMLConstants.AUTH_N_REQUEST, SAMLConstants.SUCCESS, context);
    }

    // copy from super class to call our getProfileOptions
    protected void initializeSSO(HttpServletRequest request, SAMLMessageContext context,
                                 AuthenticationException e)
            throws MetadataProviderException, SAMLException, MessageEncodingException
    {
        WebSSOProfileOptions options = getExtendedOptions(request, context, e);
        AssertionConsumerService consumerService = SAMLUtil.getConsumerService(
                (SPSSODescriptor) context.getLocalEntityRoleMetadata(), options.getAssertionConsumerIndex());

        // HoK WebSSO
        if (SAMLConstants.SAML2_HOK_WEBSSO_PROFILE_URI.equals(consumerService.getBinding())) {
            if (webSSOprofileHoK == null) {
                log.warn("WebSSO HoK profile was specified to be used, but profile is not " +
                    "configured in the EntryPoint, HoK will be skipped");
            } else {
                log.debug("Processing SSO using WebSSO HolderOfKey profile");
                webSSOprofileHoK.sendAuthenticationRequest(context, options);
                samlLogger.log(SAMLConstants.AUTH_N_REQUEST, SAMLConstants.SUCCESS, context);
                return;
            }
        }

        // Ordinary WebSSO
        log.debug("Processing SSO using WebSSO profile");
        webSSOprofile.sendAuthenticationRequest(context, options);
        samlLogger.log(SAMLConstants.AUTH_N_REQUEST, SAMLConstants.SUCCESS, context);
    }

    private void addExtraParams(HttpServletRequest request, WebSSOProfileOptions options) {
        log.debug("Transforming OIDC params to SAML");
        processAcrValues(request, options);
        processForceAuthn(request, options);
        processPrompt(request, options);
    }

    private void processForceAuthn(HttpServletRequest request, WebSSOProfileOptions options) {
        if (PerunSamlUtils.needsReAuthByForceAuthn(request)) {
            log.debug("Transformed forceAuthn parameter to SAML forceAuthn=true");
            options.setForceAuthN(true);
        }
    }

    private void processPrompt(HttpServletRequest request, WebSSOProfileOptions options) {
        if (PerunSamlUtils.needsReAuthByPrompt(request)) {
            log.debug("Transformed prompt parameter ({}) to SAML forceAuthn=true",
                request.getParameter(PARAM_PROMPT));
            options.setForceAuthN(true);
        }
    }

    private void processAcrValues(HttpServletRequest request, WebSSOProfileOptions options) {
        String acrValues = request.getParameter(PerunFilterConstants.PARAM_ACR_VALUES);
        log.debug("Processing acr_values parameter: {}", acrValues);
        List<String> acrs = convertAcrValuesToList(acrValues);

        if (!hasAcrForcingIdp(acrs)) {
            String clientId = request.getParameter(PerunFilterConstants.PARAM_CLIENT_ID);
            String idpFilter = extractIdpFilterForRp(clientId);
            if (idpFilter != null) {
                log.debug("Added IdP filter as SAML AuthnContextClassRef ({})", idpFilter);
                acrs.add(idpFilter);
            }
        }

        if (PerunSamlUtils.needsReAuthByMfa(request)) {
            log.debug("ACRs include {}, added forceAuthn to proxy request", REFEDS_MFA);
            options.setForceAuthN(true);
        }

        if (StringUtils.hasText(request.getParameter(PARAM_CLIENT_ID)) && config.isAddClientIdToAcrs()) {
            String clientIdAcr = CLIENT_ID_PREFIX + request.getParameter(PARAM_CLIENT_ID);
            log.debug("Adding client_id ACR ({}) to list of AuthnContextClassRefs for purposes" +
                    " of displaying service name on the wayf", clientIdAcr);
            acrs.add(clientIdAcr);
        }

        if (acrs.size() > 0) {


            options.setAuthnContexts(acrs);
            log.debug("Transformed acr_values ({}) to SAML AuthnContextClassRef ({})",
                acrValues, options.getAuthnContexts());
        }
    }

    private void processAcrs(List<String> acrs) {
        if (acrs == null || acrs.isEmpty()) {
            return;
        }

        String[] reservedAcrsPrefixes = samlProperties.getAcrReservedPrefixes();
        Set<String> reservedPrefixes = (reservedAcrsPrefixes != null) ?
                new HashSet<>(Arrays.asList(reservedAcrsPrefixes)) : new HashSet<>();
        if (reservedPrefixes.isEmpty()) {
            return;
        }

        boolean hasNonReserved = false;
        for (String prefix: reservedPrefixes) {
            for (String acr: acrs) {
                if (!acr.startsWith(prefix)) {
                    log.debug("ACR with non reserved prefix found: {}", acr);
                    hasNonReserved = true;
                    break;
                }
            }
            if (hasNonReserved) {
                break;
            }
        }

        if (!hasNonReserved) {
            List<String> toBeAdded = new LinkedList<>(Arrays.asList(samlProperties.getAcrsToBeAdded()));
            log.debug("NO ACR with non reserved prefix found, adding following: {}", toBeAdded);
            acrs.addAll(toBeAdded);
        }
    }

    private List<String> convertAcrValuesToList(String acrValues) {
        List<String> acrs = new LinkedList<>();
        if (StringUtils.hasText(acrValues)) {
            String[] parts = acrValues.split(" ");
            if (parts.length > 0) {
                for (String acr: parts) {
                    if (StringUtils.hasText(acr)) {
                        acrs.add(acr);
                    }
                }
            }
        }
        return acrs;
    }

    private boolean hasAcrForcingIdp(List<String> acrs) {
        boolean hasIdpEntityId = acrs != null
            && !acrs.isEmpty()
            && acrs.stream().anyMatch(
            acr -> StringUtils.hasText(acr) && acr.startsWith(IDP_ENTITY_ID_PREFIX)
        );

        if (hasIdpEntityId) {
            log.debug("Request contains ACR to force specific IdP, no configured IdP filter will be used");
        }
        return hasIdpEntityId;
    }

    private String extractIdpFilterForRp(String clientId) {
        if (!config.isAskPerunForIdpFiltersEnabled()) {
            return null;
        }
        Facility facility = null;
        if (clientId != null) {
            facility = perunAdapter.getFacilityByClientId(clientId);
        }
        Map<String, PerunAttributeValue> filterAttributes = new HashMap<>();
        if (facility != null) {
            filterAttributes = this.getFacilityFilterAttributes(facility);
        }

        String idpFilterAcr = null;
        String idpFilter = fetchRpIdpFilter(filterAttributes, facilityAttrsConfig.getWayfFilterAttr());
        if (StringUtils.hasText(idpFilter)) {
            idpFilterAcr = FILTER_PREFIX + idpFilter;
        } else {
            String idpEFilter = fetchRpIdpFilter(filterAttributes, facilityAttrsConfig.getWayfEFilterAttr());
            if (StringUtils.hasText(idpEFilter)) {
                idpFilterAcr = EFILTER_PREFIX + idpEFilter;
            }
        }
        return idpFilterAcr;
    }

    private Map<String, PerunAttributeValue> getFacilityFilterAttributes(Facility facility) {
        if (facility != null && facility.getId() != null) {
            List<String> attrsToFetch = new ArrayList<>();
            attrsToFetch.add(facilityAttrsConfig.getWayfEFilterAttr());
            attrsToFetch.add(facilityAttrsConfig.getWayfFilterAttr());
            return perunAdapter.getFacilityAttributeValues(facility, attrsToFetch);
        }
        return new HashMap<>();
    }

    private String fetchRpIdpFilter(Map<String, PerunAttributeValue> filterAttributes, String attrName) {
        String result = null;
        if (filterAttributes.get(attrName) != null) {
            PerunAttributeValue filterAttribute = filterAttributes.get(attrName);
            if (filterAttribute != null && filterAttribute.valueAsString() != null) {
                result = filterAttribute.valueAsString();
            }
        }
        return result;
    }

}
