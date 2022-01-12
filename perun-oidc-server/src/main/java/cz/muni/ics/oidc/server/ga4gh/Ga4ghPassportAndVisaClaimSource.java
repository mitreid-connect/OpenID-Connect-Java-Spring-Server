package cz.muni.ics.oidc.server.ga4gh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import cz.muni.ics.openid.connect.web.JWKSetPublishingEndpoint;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Class producing GA4GH Passport claim. The claim is specified in
 * https://bit.ly/ga4gh-passport-v1
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.config_file</b> - full path to the configuration file for this claim. See
 *     configuration templates for such a file.</li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
@Getter
public abstract class Ga4ghPassportAndVisaClaimSource extends ClaimSource {

    public static final String GA4GH_SCOPE = "ga4gh_passport_v1";
    public static final String GA4GH_CLAIM = "ga4gh_passport_v1";

    protected static final List<Ga4ghClaimRepository> CLAIM_REPOSITORIES = new ArrayList<>();
    protected static final Map<URI, RemoteJWKSet<SecurityContext>> REMOTE_JWK_SETS = new HashMap<>();
    protected static final Map<URI, String> SIGNERS = new HashMap<>();
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    private final JWTSigningAndValidationService jwtService;
    private final URI jku;
    private final String issuer;

    public Ga4ghPassportAndVisaClaimSource(ClaimSourceInitContext ctx, String implType) throws URISyntaxException {
        super(ctx);
        log.debug("Initializing GA4GH Passports and Visa Claim Source - (version by {})", implType);
        //remember context
        jwtService = ctx.getJwtService();
        issuer = ctx.getPerunOidcConfig().getConfigBean().getIssuer();
        jku = new URI(issuer + JWKSetPublishingEndpoint.URL);
        // load config file
        String configFile = ctx.getProperty("config_file", getDefaultConfigFilePath());
        Ga4ghUtils.parseConfigFile(configFile, CLAIM_REPOSITORIES, REMOTE_JWK_SETS, SIGNERS);
    }

    @Override
    public JsonNode produceValue(ClaimSourceProduceContext pctx) {
        if (pctx.getClient() == null) {
            log.debug("Client is not set");
            return JsonNodeFactory.instance.textNode("Global Alliance For Genomic Health structured claim");
        }
        if (!pctx.getClient().getScope().contains(GA4GH_SCOPE)) {
            log.debug("Client '{}' does not have scope ga4gh", pctx.getClient().getClientName());
            return null;
        }

        List<Affiliation> affiliations = pctx.getPerunAdapter()
                .getAdapterRpc()
                .getUserExtSourcesAffiliations(pctx.getPerunUserId());

        ArrayNode ga4gh_passport_v1 = JsonNodeFactory.instance.arrayNode();
        long now = Instant.now().getEpochSecond();
        addAffiliationAndRoles(now, pctx, ga4gh_passport_v1, affiliations);
        addAcceptedTermsAndPolicies(now, pctx, ga4gh_passport_v1);
        addResearcherStatuses(now, pctx, ga4gh_passport_v1, affiliations);
        addControlledAccessGrants(now, pctx, ga4gh_passport_v1);
        return ga4gh_passport_v1;
    }

    public static Ga4ghPassportVisa parseAndVerifyVisa(String subValue) {
        return Ga4ghUtils.parseAndVerifyVisa(subValue, SIGNERS, REMOTE_JWK_SETS, MAPPER);
    }

    protected abstract String getDefaultConfigFilePath();

    protected abstract void addAffiliationAndRoles(long now, ClaimSourceProduceContext pctx,
                                                   ArrayNode passport, List<Affiliation> affiliations);

    protected abstract void addAcceptedTermsAndPolicies(long now, ClaimSourceProduceContext pctx, ArrayNode passport);

    protected abstract void addResearcherStatuses(long now, ClaimSourceProduceContext pctx,
                                        ArrayNode passport, List<Affiliation> affiliations);

    protected abstract void addControlledAccessGrants(long now, ClaimSourceProduceContext pctx, ArrayNode passport);

    protected JsonNode createPassportVisa(String type, ClaimSourceProduceContext pctx, String value, String source,
                                          String by, long asserted, long expires, JsonNode condition)
    {
        long now = System.currentTimeMillis() / 1000L;
        if (asserted > now) {
            log.warn("Visa asserted in future, it will be ignored!");
            log.debug("Visa information: perunUserId={}, sub={}, type={}, value={}, source={}, by={}, asserted={}",
                    pctx.getPerunUserId(), pctx.getSub(), type, value, source, by, Instant.ofEpochSecond(asserted));
            return null;
        }
        if (expires <= now) {
            log.warn("Visa is expired, it will be ignored!");
            log.debug("Visa information: perunUserId={}, sub={}, type={}, value={}, source={}, by={}, expired={}",
                    pctx.getPerunUserId(), pctx.getSub(), type, value, source, by, Instant.ofEpochSecond(expires));
            return null;
        }

        Map<String, Object> passportVisaObject = new HashMap<>();
        passportVisaObject.put(Ga4ghPassportVisa.TYPE, type);
        passportVisaObject.put(Ga4ghPassportVisa.ASSERTED, asserted);
        passportVisaObject.put(Ga4ghPassportVisa.VALUE, value);
        passportVisaObject.put(Ga4ghPassportVisa.SOURCE, source);
        passportVisaObject.put(Ga4ghPassportVisa.BY, by);
        if (condition != null && !condition.isNull() && !condition.isMissingNode()) {
            passportVisaObject.put(Ga4ghPassportVisa.CONDITION, condition);
        }
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(jwtService.getDefaultSigningAlgorithm().getName()))
                .keyID(jwtService.getDefaultSignerKeyId())
                .type(JOSEObjectType.JWT)
                .jwkURL(jku)
                .build();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(new Date(expires * 1000L))
                .subject(pctx.getSub())
                .jwtID(UUID.randomUUID().toString())
                .claim(Ga4ghPassportVisa.GA4GH_VISA_V1, passportVisaObject)
                .build();
        SignedJWT myToken = new SignedJWT(jwsHeader, jwtClaimsSet);
        jwtService.signJwt(myToken);
        return JsonNodeFactory.instance.textNode(myToken.serialize());
    }

    protected void callPermissionsJwtAPI(Ga4ghClaimRepository repo,
                                         Map<String, String> uriVariables,
                                         ArrayNode passport,
                                         Set<String> linkedIdentities)
    {
        log.debug("GA4GH: {}", uriVariables);
        JsonNode response = callHttpJsonAPI(repo, uriVariables);
        if (response != null) {
            JsonNode visas = response.path(GA4GH_CLAIM);
            if (visas.isArray()) {
                for (JsonNode visaNode : visas) {
                    if (visaNode.isTextual()) {
                        Ga4ghPassportVisa visa = Ga4ghUtils.parseAndVerifyVisa(visaNode.asText(), SIGNERS, REMOTE_JWK_SETS, MAPPER);
                        if (visa.isVerified()) {
                            log.debug("Adding a visa to passport: {}", visa);
                            passport.add(passport.textNode(visa.getJwt()));
                            linkedIdentities.add(visa.getLinkedIdentity());
                        } else {
                            log.warn("Skipping visa: {}", visa);
                        }
                    } else {
                        log.warn("Element of {} is not a String: {}", GA4GH_CLAIM, visaNode);
                    }
                }
            } else {
                log.warn("{} is not an array in {}", GA4GH_CLAIM, response);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private static JsonNode callHttpJsonAPI(Ga4ghClaimRepository repo, Map<String, String> uriVariables) {
        //get permissions data
        try {
            JsonNode result;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Calling Permissions API at {}", repo.getRestTemplate().getUriTemplateHandler().expand(repo.getActionURL(), uriVariables));
                }
                result = repo.getRestTemplate().getForObject(repo.getActionURL(), JsonNode.class, uriVariables);
            } catch (HttpClientErrorException ex) {
                MediaType contentType = ex.getResponseHeaders().getContentType();
                String body = ex.getResponseBodyAsString();
                log.error("HTTP ERROR: {}, URL: {}, Content-Type: {}", ex.getRawStatusCode(),
                        repo.getActionURL(), contentType);
                if (ex.getRawStatusCode() == 404) {
                    log.warn("Got status 404 from Permissions endpoint {}, ELIXIR AAI user is not linked to user at Permissions API",
                            repo.getActionURL());
                    return null;
                }
                if ("json".equals(contentType.getSubtype())) {
                    try {
                        log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
                    } catch (IOException e) {
                        log.error("cannot parse error message from JSON", e);
                    }
                } else {
                    log.error("cannot make REST call, exception: {} message: {}", ex.getClass().getName(), ex.getMessage());
                }
                return null;
            }
            log.debug("Permissions API response: {}", result);
            return result;
        } catch (Exception ex) {
            log.error("Cannot get dataset permissions", ex);
        }
        return null;
    }

}
