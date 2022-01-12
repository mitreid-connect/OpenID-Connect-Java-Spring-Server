package cz.muni.ics.oidc.server.userInfo;

import static cz.muni.ics.oidc.server.PerunScopeClaimTranslationService.ADDRESS;
import static cz.muni.ics.oidc.server.PerunScopeClaimTranslationService.EMAIL;
import static cz.muni.ics.oidc.server.PerunScopeClaimTranslationService.OPENID;
import static cz.muni.ics.oidc.server.PerunScopeClaimTranslationService.PHONE;
import static cz.muni.ics.oidc.server.PerunScopeClaimTranslationService.PROFILE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.cache.CacheLoader;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunAttributeValueAwareModel;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.claims.ClaimModifier;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.PerunCustomClaimDefinition;
import cz.muni.ics.oidc.server.userInfo.mappings.AddressMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.EmailMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.OpenidMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.PhoneMappings;
import cz.muni.ics.oidc.server.userInfo.mappings.ProfileMappings;
import cz.muni.ics.openid.connect.model.Address;
import cz.muni.ics.openid.connect.model.DefaultAddress;
import cz.muni.ics.openid.connect.model.UserInfo;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Setter
@Slf4j
@Builder
public class PerunUserInfoCacheLoader extends CacheLoader<UserInfoCacheKey, UserInfo> {

    private OpenidMappings openidMappings;
    private ProfileMappings profileMappings;
    private EmailMappings emailMappings;
    private AddressMappings addressMappings;
    private PhoneMappings phoneMappings;
    private PerunAdapter perunAdapter;
    private List<PerunCustomClaimDefinition> customClaims;
    private boolean fillAttributes;
    private List<ClaimModifier> subModifiers;

    @Override
    public UserInfo load(UserInfoCacheKey key) {
        log.debug("load({}) ... populating cache for the key", key);
        PerunUserInfo ui = new PerunUserInfo();
        long perunUserId = key.getUserId();
        Set<String> attributes = constructAttributes(key.getScopes());
        Map<String, PerunAttributeValue> userAttributeValues = fetchUserAttributes(perunUserId, attributes);

        ClaimSourceProduceContext.ClaimSourceProduceContextBuilder builder = ClaimSourceProduceContext.builder()
                .perunUserId(perunUserId)
                .sub(ui.getSub())
                .attrValues(userAttributeValues)
                .scopes(key.getScopes())
                .client(key.getClient())
                .perunAdapter(perunAdapter)
                .samlAuthenticationDetails(key.getAuthenticationDetails());
        if (key.getClient() != null) {
            builder = builder.facility(perunAdapter.getFacilityByClientId(key.getClient().getClientId()));
        }
        ClaimSourceProduceContext pctx = builder.build();

        processStandardScopes(pctx, ui);
        processCustomScopes(pctx, ui);
        return ui;
    }

    private Map<String, PerunAttributeValue> fetchUserAttributes(long perunUserId, Set<String> attributes) {
        Map<String, PerunAttributeValue> userAttributeValues  =
                perunAdapter.getUserAttributeValues(perunUserId, attributes);

        if (shouldFillAttrs(userAttributeValues)) {
            List<String> attrNames = userAttributeValues.entrySet()
                    .stream()
                    .filter(entry -> (null == entry.getValue() || entry.getValue().isNullValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            Map<String, PerunAttributeValue> missingAttrs = perunAdapter.getAdapterFallback()
                    .getUserAttributeValues(perunUserId, attrNames);
            userAttributeValues.putAll(missingAttrs);
        }
        return userAttributeValues;
    }

    private Set<String> constructAttributes(Set<String> requestedScopes) {
        Set<String> attributes = new HashSet<>();
        if (requestedScopes != null && !requestedScopes.isEmpty()) {
            if (requestedScopes.contains(OPENID)) {
                attributes.addAll(openidMappings.getAttrNames());
            }
            if (requestedScopes.contains(PROFILE)) {
                attributes.addAll(profileMappings.getAttrNames());
            }
            if (requestedScopes.contains(EMAIL)) {
                attributes.addAll(emailMappings.getAttrNames());
            }
            if (requestedScopes.contains(ADDRESS)) {
                attributes.addAll(addressMappings.getAttrNames());
            }
            if (requestedScopes.contains(PHONE)) {
                attributes.addAll(phoneMappings.getAttrNames());
            }

            for (PerunCustomClaimDefinition pccd : customClaims) {
                if (requestedScopes.contains(pccd.getScope())) {
                    attributes.addAll(pccd.getClaimSource().getAttrIdentifiers());
                }
            }
        }
        return attributes;
    }

    private void processCustomScopes(ClaimSourceProduceContext pctx, PerunUserInfo ui) {
        log.debug("processing custom claims");
        for (PerunCustomClaimDefinition claimDef : customClaims) {
            if (isScopeRequested(claimDef.getScope(), pctx.getScopes())) {
                processCustomScope(claimDef, pctx, ui);
            }
        }
        log.debug("UserInfo created");
    }

    private void processCustomScope(PerunCustomClaimDefinition claimDef, ClaimSourceProduceContext pctx, PerunUserInfo ui) {
        log.debug("producing value for custom claim {}", claimDef.getClaim());
        JsonNode claimInJson = claimDef.getClaimSource().produceValue(pctx);
        log.debug("produced value {}={}", claimDef.getClaim(), claimInJson);
        if (claimInJson == null || claimInJson.isNull()) {
            log.debug("claim {} is null", claimDef.getClaim());
            return;
        } else if (claimInJson.isTextual() && !StringUtils.hasText(claimInJson.asText())) {
            log.debug("claim {} is a string and it is empty or null", claimDef.getClaim());
            return;
        } else if ((claimInJson.isArray() || claimInJson.isObject()) && claimInJson.size() == 0) {
            log.debug("claim {} is an object or array and it is empty or null", claimDef.getClaim());
            return;
        }
        List<ClaimModifier> claimModifiers = claimDef.getClaimModifiers();
        if (claimModifiers != null && !claimModifiers.isEmpty()) {
            claimInJson = modifyClaims(claimModifiers, claimInJson);
        }
        ui.getCustomClaims().put(claimDef.getClaim(), claimInJson);
    }

    private boolean isScopeRequested(String scope, Set<String> scopes) {
        return scopes != null && scopes.contains(scope);
    }

    private void processStandardScopes(ClaimSourceProduceContext ctx, PerunUserInfo ui) {
        Set<String> scopes = ctx.getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            if (scopes.contains(OPENID)) {
                processOpenid(ctx.getAttrValues(), ctx.getPerunUserId(), ui);
            }
            if (scopes.contains(PROFILE)) {
                processProfile(ctx.getAttrValues(), ui);
            }
            if (scopes.contains(EMAIL)) {
                processEmail(ctx.getAttrValues(), ui);
            }
            if (scopes.contains(ADDRESS)) {
                processAddress(ctx.getAttrValues(), ui);
            }
            if (scopes.contains(PHONE)) {
                processPhone(ctx.getAttrValues(), ui);
            }
        }
    }

    private void processOpenid(Map<String, PerunAttributeValue> userAttributeValues, long perunUserId,
                               PerunUserInfo ui) {
        JsonNode subJson = extractJsonValue(openidMappings.getSub(), userAttributeValues);
        if (subJson != null && !subJson.isNull() && StringUtils.hasText(subJson.asText())) {
            if (subModifiers != null) {
                subJson = modifyClaims(subModifiers, subJson);
                if (subJson.asText() == null || !StringUtils.hasText(subJson.asText())) {
                    throw new RuntimeException("Sub has no value after modification for username " + perunUserId);
                }
            }
            ui.setSub(subJson.asText());
        }
        ui.setId(perunUserId);
    }

    private void processProfile(Map<String, PerunAttributeValue> userAttributeValues, PerunUserInfo ui) {
        ui.setPreferredUsername(extractStringValue(profileMappings.getPreferredUsername(), userAttributeValues));
        ui.setGivenName(extractStringValue(profileMappings.getGivenName(), userAttributeValues));
        ui.setFamilyName(extractStringValue(profileMappings.getFamilyName(), userAttributeValues));
        ui.setMiddleName(extractStringValue(profileMappings.getMiddleName(), userAttributeValues));
        ui.setName(extractStringValue(profileMappings.getName(), userAttributeValues));
        ui.setNickname(extractStringValue(profileMappings.getNickname(), userAttributeValues));
        ui.setProfile(extractStringValue(profileMappings.getProfile(), userAttributeValues));
        ui.setPicture(extractStringValue(profileMappings.getPicture(), userAttributeValues));
        ui.setWebsite(extractStringValue(profileMappings.getWebsite(), userAttributeValues));
        ui.setZoneinfo(extractStringValue(profileMappings.getZoneinfo(), userAttributeValues));
        ui.setGender(extractStringValue(profileMappings.getGender(), userAttributeValues));
        ui.setBirthdate(extractStringValue(profileMappings.getBirthdate(), userAttributeValues));
        ui.setLocale(extractStringValue(profileMappings.getLocale(), userAttributeValues));
        ui.setUpdatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

    private void processEmail(Map<String, PerunAttributeValue> userAttributeValues, PerunUserInfo ui) {
        ui.setEmail(extractStringValue(emailMappings.getEmail(), userAttributeValues));
        ui.setEmailVerified(Boolean.parseBoolean(
                extractStringValue(emailMappings.getEmailVerified(), userAttributeValues)));
    }

    private void processAddress(Map<String, PerunAttributeValue> userAttributeValues, PerunUserInfo ui) {
        Address address = null;
        if (isAddressAvailable(userAttributeValues)) {
            address = new DefaultAddress();
            address.setFormatted(extractStringValue(addressMappings.getFormatted(), userAttributeValues));
            address.setStreetAddress(extractStringValue(addressMappings.getStreetAddress(), userAttributeValues));
            address.setLocality(extractStringValue(addressMappings.getLocality(), userAttributeValues));
            address.setPostalCode(extractStringValue(addressMappings.getPostalCode(), userAttributeValues));
            address.setCountry(extractStringValue(addressMappings.getCountry(), userAttributeValues));
        }
        ui.setAddress(address);
    }

    private void processPhone(Map<String, PerunAttributeValue> userAttributeValues, PerunUserInfo ui) {
        ui.setPhoneNumber(extractStringValue(phoneMappings.getPhoneNumber(), userAttributeValues));
        ui.setPhoneNumberVerified(Boolean.parseBoolean(
                extractStringValue(phoneMappings.getPhoneNumber(), userAttributeValues)));
    }

    private boolean isAddressAvailable(Map<String, PerunAttributeValue> userAttributeValues) {
        return hasNonNullValue(addressMappings.getFormatted(), userAttributeValues)
                || hasNonNullValue(addressMappings.getStreetAddress(), userAttributeValues)
                || hasNonNullValue(addressMappings.getLocality(), userAttributeValues)
                || hasNonNullValue(addressMappings.getPostalCode(), userAttributeValues)
                || hasNonNullValue(addressMappings.getCountry(), userAttributeValues);
    }

    private boolean hasNonNullValue(String mapping, Map<String, PerunAttributeValue> valueMap) {
        if (mapping == null) {
            return false;
        }
        PerunAttributeValue v = valueMap.getOrDefault(mapping, null);
        return v != null && !v.isNullValue();
    }

    private JsonNode extractJsonValue(String mapping, Map<String, PerunAttributeValue> valueMap) {
        PerunAttributeValue v = extractValue(mapping, valueMap);
        if (v != null) {
            return v.valueAsJson();
        }
        return JsonNodeFactory.instance.nullNode();
    }

    private String extractStringValue(String mapping, Map<String, PerunAttributeValue> valueMap) {
        PerunAttributeValue v = extractValue(mapping, valueMap);
        if (v != null) {
            return v.valueAsString();
        }
        return null;
    }

    private PerunAttributeValue extractValue(String mapping, Map<String, PerunAttributeValue> valueMap) {
        if (!StringUtils.hasText(mapping)) {
            return null;
        }
        return valueMap.getOrDefault(mapping, null);
    }

    private JsonNode modifyClaims(List<ClaimModifier> claimModifiers, JsonNode value) {
        for (ClaimModifier modifier: claimModifiers) {
            value = modifyClaim(modifier, value);
        }
        return value;
    }

    private JsonNode modifyClaim(ClaimModifier modifier, JsonNode orig) {
        JsonNode claimInJson = orig.deepCopy();
        if (claimInJson.isTextual()) {
            return TextNode.valueOf(modifier.modify(claimInJson.asText()));
        } else if (claimInJson.isArray()) {
            ArrayNode arrayNode = (ArrayNode) claimInJson;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode item = arrayNode.get(i);
                if (item.isTextual()) {
                    String original = item.asText();
                    String modified = modifier.modify(original);
                    arrayNode.set(i, TextNode.valueOf(modified));
                }
            }
            return arrayNode;
        } else {
            log.warn("Original value is neither string nor array of strings - cannot modify values");
            return orig;
        }
    }

    private boolean shouldFillAttrs(Map<String, PerunAttributeValue> userAttributeValues) {
        if (fillAttributes) {
            if (userAttributeValues.isEmpty()) {
                return true;
            } else if (userAttributeValues.containsValue(null)) {
                return true;
            } else {
                return !userAttributeValues.values().stream()
                        .filter(PerunAttributeValueAwareModel::isNullValue)
                        .collect(Collectors.toSet())
                        .isEmpty();
            }
        }
        return false;
    }

}
