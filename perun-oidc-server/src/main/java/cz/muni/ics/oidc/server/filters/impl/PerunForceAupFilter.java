package cz.muni.ics.oidc.server.filters.impl;

import static cz.muni.ics.oidc.web.controllers.AupController.APPROVED;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.Aup;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttribute;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.saml.SamlProperties;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.PerunRequestFilter;
import cz.muni.ics.oidc.server.filters.PerunRequestFilterParams;
import cz.muni.ics.oidc.web.controllers.AupController;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * AUP filter checks if there are new AUPs which user hasn't accepted yet and forces him to do that.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].orgAupsAttrName</b> - Mapping to Perun entityless attribute containing organization AUPs</li>
 *     <li><b>filter.[name].userAupsAttrName</b> - Mapping to Perun user attribute containing list of AUPS approved by user</li>
 *     <li><b>filter.[name].voAupAttrName</b> - Mapping to Perun VO attribute containing AUP specific for VO</li>
 *     <li><b>filter.[name].facilityRequestedAupsAttrName</b> - Mapping to Perun facility attribute containing list of AUPs requested
 *         by the service. Contains only keys for those AUPs</li>
 *     <li><b>filter.[name].voShortNamesAttrName</b> - Mapping to Perun facility attribute containing list of short names for VOs
 *         that have a resource assigned to the facility</li>
 * </ul>
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunForceAupFilter extends PerunRequestFilter {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /* CONFIGURATION PROPERTIES */
    private static final String ORG_AUPS_ATTR_NAME = "orgAupsAttrName";
    private static final String USER_AUPS_ATTR_NAME = "userAupsAttrName";
    private static final String VO_AUP_ATTR_NAME = "voAupAttrName";
    private static final String FACILITY_REQUESTED_AUPS_ATTR_NAME = "facilityRequestedAupsAttrName";
    private static final String VO_SHORT_NAMES_ATTR_NAME = "voShortNamesAttrName";

    private final String perunOrgAupsAttrName;
    private final String perunUserAupsAttrName;
    private final String perunVoAupAttrName;
    private final String perunFacilityRequestedAupsAttrName;
    private final String perunFacilityVoShortNamesAttrName;
    /* END OF CONFIGURATION PROPERTIES */

    private final ObjectMapper mapper = new ObjectMapper();

    private final PerunAdapter perunAdapter;
    private final PerunOidcConfig perunOidcConfig;
    private final SamlProperties samlProperties;
    private final String filterName;

    public PerunForceAupFilter(PerunRequestFilterParams params) {
        super(params);
        BeanUtil beanUtil = params.getBeanUtil();
        this.perunAdapter = beanUtil.getBean(PerunAdapter.class);
        this.perunOidcConfig = beanUtil.getBean(PerunOidcConfig.class);
        this.samlProperties = beanUtil.getBean(SamlProperties.class);

        this.perunOrgAupsAttrName = params.getProperty(ORG_AUPS_ATTR_NAME);
        this.perunUserAupsAttrName = params.getProperty(USER_AUPS_ATTR_NAME);
        this.perunVoAupAttrName = params.getProperty(VO_AUP_ATTR_NAME);
        this.perunFacilityRequestedAupsAttrName = params.getProperty(FACILITY_REQUESTED_AUPS_ATTR_NAME);
        this.perunFacilityVoShortNamesAttrName = params.getProperty(VO_SHORT_NAMES_ATTR_NAME);
        this.filterName = params.getFilterName();
    }

    @Override
    protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (request.getSession() != null && request.getSession().getAttribute(APPROVED) != null) {
            request.getSession().removeAttribute(APPROVED);
            log.debug("{} - skip filter execution: aups are already approved, check at next access to the service due" +
                    " to a delayed propagation to LDAP", filterName);
            return true;
        }

        PerunUser user = FiltersUtils.getPerunUser(request, perunAdapter, samlProperties.getUserIdentifierAttribute());
        if (user == null || user.getId() == null) {
            log.debug("{} - skip filter execution: no user provider", filterName);
            return true;
        }

        Facility facility = params.getFacility();
        if (facility == null || facility.getId() == null) {
            log.debug("{} - skip filter execution: no facility provider", filterName);
            return true;
        }

        List<String> attrsToFetch = new ArrayList<>(
                Arrays.asList(perunFacilityRequestedAupsAttrName, perunFacilityVoShortNamesAttrName));
        Map<String, PerunAttributeValue> facilityAttributes = perunAdapter.getFacilityAttributeValues(facility, attrsToFetch);

        if (facilityAttributes == null) {
            log.debug("{} - skip filter execution: could not fetch attributes '{}' for facility '{}'",
                    filterName, attrsToFetch, facility);
            return true;
        } else if (!facilityAttributes.containsKey(perunFacilityRequestedAupsAttrName) &&
                !facilityAttributes.containsKey(perunFacilityVoShortNamesAttrName))
        {
            log.debug("{} - skip filter execution: could not fetch required attributes '{}' and '{}' for facility '{}'",
                    filterName, perunFacilityRequestedAupsAttrName, perunFacilityVoShortNamesAttrName, facility);
            return true;
        }

        Map<String, Aup> newAups;

        try {
            newAups = getAupsToApprove(user, facilityAttributes);
        } catch (ParseException | IOException e) {
            log.warn("{} - caught parse exception when processing AUPs to approve", filterName);
            log.trace("{} - details:", filterName, e);
            return true;
        }

        if (!newAups.isEmpty()) {
            log.debug("{} - user has to approve some AUPs", filterName);
            log.trace("{} - AUPS to be approved: '{}'", filterName, newAups);
            String newAupsString = mapper.writeValueAsString(newAups);

            request.getSession().setAttribute(AupController.RETURN_URL, request.getRequestURI()
                    .replace(request.getContextPath(), "") + '?' + request.getQueryString());
            request.getSession().setAttribute(AupController.NEW_AUPS, newAupsString);
            request.getSession().setAttribute(AupController.USER_ATTR, perunUserAupsAttrName);

            log.debug("{} - redirecting user '{}' to AUPs approval page", filterName, user);
            response.sendRedirect(request.getContextPath() + '/' + AupController.URL);
            return false;
        }

        log.debug("{} - no need to approve any AUPs", filterName);
        return true;
    }

    private Map<String, Aup> getAupsToApprove(PerunUser user, Map<String, PerunAttributeValue> facilityAttributes)
            throws ParseException, IOException
    {
        Map<String, Aup> aupsToApprove= new LinkedHashMap<>();

        PerunAttributeValue userAupsAttr = perunAdapter.getUserAttributeValue(user.getId(), perunUserAupsAttrName);
        if (perunOidcConfig.isFillMissingUserAttrs() && (userAupsAttr == null || userAupsAttr.isNullValue())) {
            userAupsAttr = perunAdapter.getAdapterFallback().getUserAttributeValue(user.getId(), perunUserAupsAttrName);
        }
        Map<String, List<Aup>> userAups = convertToMapKeyToListOfAups(userAupsAttr.valueAsMap());

        PerunAttributeValue requestedAupsAttr = facilityAttributes.get(perunFacilityRequestedAupsAttrName);
        PerunAttributeValue facilityVoShortNamesAttr = facilityAttributes.get(perunFacilityVoShortNamesAttrName);

        if (requestedAupsAttr != null && !requestedAupsAttr.isNullValue() && requestedAupsAttr.valueAsList() != null
                && !requestedAupsAttr.valueAsList().isEmpty()) {
            Map<String, Aup> orgAupsToApprove = getOrgAupsToApprove(requestedAupsAttr.valueAsList(), userAups);
            mergeAupMaps(aupsToApprove, orgAupsToApprove);
        }

        if (facilityVoShortNamesAttr != null && !facilityVoShortNamesAttr.isNullValue()
                && facilityVoShortNamesAttr.valueAsList() != null && !facilityVoShortNamesAttr.valueAsList().isEmpty()) {
            Map<String, Aup> voAupsToApprove = getVoAupsToApprove(facilityVoShortNamesAttr.valueAsList(), userAups);
            mergeAupMaps(aupsToApprove, voAupsToApprove);
        }

        return aupsToApprove;
    }

    private void mergeAupMaps(Map<String, Aup> original, Map<String, Aup> updates) {
        for (Map.Entry<String, Aup> pair: updates.entrySet()) {
            if (original.containsKey(pair.getKey())) {
                Aup originalAup = original.get(pair.getKey());
                Aup updateAup = pair.getValue();
                if (updateAup.getDateAsLocalDate().isAfter(originalAup.getDateAsLocalDate())) {
                    original.replace(pair.getKey(), pair.getValue());
                }
            } else {
                original.put(pair.getKey(), pair.getValue());
            }
        }
    }

    private Map<String, Aup> getVoAupsToApprove(List<String> facilityVoShortNames, Map<String, List<Aup>> userAups)
            throws IOException, ParseException {
        Map<String, Aup> aupsToApprove = new LinkedHashMap<>();
        Map<String, List<Aup>> voAups = getVoAups(facilityVoShortNames);

        if (!voAups.isEmpty()) {
            for (Map.Entry<String, List<Aup>> keyToVoAup : voAups.entrySet()) {
                Aup voLatestAup = getLatestAupFromList(keyToVoAup.getValue());
                if (userAups.containsKey(keyToVoAup.getKey())) {
                    Aup userLatestAup = getLatestAupFromList(userAups.get(keyToVoAup.getKey()));
                    if (! (voLatestAup.getDateAsLocalDate().isAfter(userLatestAup.getDateAsLocalDate()))) {
                        continue;
                    }
                }
                log.debug("{} - need to approve AUP with key '{}' ({})", filterName, keyToVoAup.getKey(), voLatestAup);
                aupsToApprove.put(keyToVoAup.getKey(), voLatestAup);
            }
        }

        log.trace("{} - VO AUPs to approve: {}", filterName, aupsToApprove);
        return aupsToApprove;
    }

    private Map<String, Aup> getOrgAupsToApprove(List<String > requestedAups, Map<String, List<Aup>> userAups)
            throws ParseException, IOException
    {
        Map<String, Aup> aupsToApprove = new LinkedHashMap<>();
        Map<String, List<Aup>> orgAups = new HashMap<>();

        Map<String, PerunAttribute> orgAupsAttr = perunAdapter.getAdapterRpc()
                .getEntitylessAttributes(perunOrgAupsAttrName);

        if (orgAupsAttr != null && !orgAupsAttr.isEmpty()) {
            for (Map.Entry<String, PerunAttribute> entry : orgAupsAttr.entrySet()) {
                if (entry.getValue() != null && entry.getValue().valueAsString() != null) {
                    List<Aup> aups = Arrays.asList(mapper.readValue(entry.getValue().valueAsString(), Aup[].class));
                    orgAups.put(entry.getKey(), aups);
                }
            }
        }
        log.debug("{} - Mapped ORG aups: {}", filterName, orgAups);

        if (!orgAups.isEmpty()) {
            for (String requiredOrgAupKey : requestedAups) {
                if (!orgAups.containsKey(requiredOrgAupKey) || orgAups.get(requiredOrgAupKey) == null) {
                    continue;
                }
                Aup orgLatestAup = getLatestAupFromList(orgAups.get(requiredOrgAupKey));
                if (userAups.containsKey(requiredOrgAupKey)) {
                    Aup userLatestAup = getLatestAupFromList(userAups.get(requiredOrgAupKey));
                    if (!(orgLatestAup.getDateAsLocalDate().isAfter(userLatestAup.getDateAsLocalDate()))) {
                        continue;
                    }
                }
                log.debug("{} - need to approve AUP with key '{}' ({})", filterName, requiredOrgAupKey, orgLatestAup);
                aupsToApprove.put(requiredOrgAupKey, orgLatestAup);
            }
        }

        log.debug("{} - ORG AUPs to approve: {}", filterName, aupsToApprove);
        return aupsToApprove;
    }

    private Map<String, List<Aup>> getVoAups(List<String> voShortNames) throws IOException {
        Map<String, List<Aup>> voAups = new HashMap<>();

        if (voShortNames != null && !voShortNames.isEmpty()) {
            for (String voShortName : voShortNames) {
                Long voId = perunAdapter.getVoByShortName(voShortName).getId();

                PerunAttributeValue voAupAttr = perunAdapter.getVoAttributeValue(voId, perunVoAupAttrName);
                if (voAupAttr == null || voAupAttr.valueAsString() == null) {
                    continue;
                }

                if (StringUtils.hasText(voAupAttr.valueAsString())) {
                    List<Aup> aups = Arrays.asList(mapper.readValue(voAupAttr.valueAsString(), Aup[].class));
                    if (!aups.isEmpty()) {
                        voAups.put(voShortName, aups);
                    }
                }
            }
        }

        return voAups;
    }

    private Map<String, List<Aup>> convertToMapKeyToListOfAups(Map<String, String> keyToListOfAupsString)
            throws IOException
    {
        Map<String, List<Aup>> resultMap = new HashMap<>();
        if (keyToListOfAupsString != null && !keyToListOfAupsString.isEmpty()) {
            for (Map.Entry<String, String> entry : keyToListOfAupsString.entrySet()) {
                List<Aup> aups = Arrays.asList(mapper.readValue(entry.getValue(), Aup[].class));
                resultMap.put(entry.getKey(), aups);
            }
        }
        return resultMap;
    }

    private Aup getLatestAupFromList(List<Aup> aups) throws ParseException {
        Aup latestAup = aups.get(0);

        for (Aup aup : aups) {
            Date latestAupDate = new SimpleDateFormat(DATE_FORMAT).parse(latestAup.getDate());
            Date aupDate = new SimpleDateFormat(DATE_FORMAT).parse(aup.getDate());
            log.info("latestAupDate({}): {}", latestAup, latestAupDate);
            log.info("aupDate({}): {}", aup, aupDate);
            if (latestAupDate.before(aupDate)) {
                log.info("before");
                latestAup = aup;
            }
        }

        log.info("latestAup: {}", latestAup);
        return latestAup;
    }

}
