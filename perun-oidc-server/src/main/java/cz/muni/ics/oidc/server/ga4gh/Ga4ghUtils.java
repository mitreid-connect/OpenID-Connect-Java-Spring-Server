package cz.muni.ics.oidc.server.ga4gh;

import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportAndVisaClaimSource.GA4GH_CLAIM;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.oidc.server.AddHeaderInterceptor;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class Ga4ghUtils {

    public static final String CONF_KEY_REPOS = "repos";
    public static final String CONF_KEY_NAME = "name";
    public static final String CONF_KEY_URL = "url";
    public static final String CONF_KEY_HEADERS = "headers";
    public static final String CONF_KEY_HEADER = "header";
    public static final String CONF_KEY_VALUE = "value";
    public static final String CONF_KEY_SIGNERS = "signers";
    public static final String CONF_KEY_JWKS = "jwks";

    public static void parseConfigFile(String file,
                                       List<Ga4ghClaimRepository> claimRepositories,
                                       Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets,
                                       Map<URI, String> signers)
    {
        YAMLMapper mapper = new YAMLMapper();
        try {
            JsonNode root = mapper.readValue(new File(file), JsonNode.class);
            // prepare claim repositories
            for (JsonNode repo : root.path(CONF_KEY_REPOS)) {
                initializeRepo(repo, claimRepositories);
            }
            // prepare claim signers
            for (JsonNode signer : root.path(CONF_KEY_SIGNERS)) {
                initializeSigner(signer, signers, remoteJwkSets);
            }
        } catch (IOException ex) {
            log.error("cannot read GA4GH config file", ex);
        }
    }

    public static Ga4ghPassportVisa parseAndVerifyVisa(String jwtString,
                                                       Map<URI, String> signers,
                                                       Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets,
                                                       ObjectMapper mapper)
    {
        Ga4ghPassportVisa visa = new Ga4ghPassportVisa(jwtString);
        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(jwtString);
            URI jku = signedJWT.getHeader().getJWKURL();
            if (jku == null) {
                log.error("JKU is missing in JWT header");
                return visa;
            }
            visa.setSigner(signers.get(jku));
            RemoteJWKSet<SecurityContext> remoteJWKSet = remoteJwkSets.get(jku);
            if (remoteJWKSet == null) {
                log.error("JKU '{}' is not among trusted key sets", jku);
                return visa;
            }
            List<JWK> keys = remoteJWKSet.get(new JWKSelector(
                    new JWKMatcher.Builder().keyID(signedJWT.getHeader().getKeyID()).build()), null);
            RSASSAVerifier verifier = new RSASSAVerifier(((RSAKey) keys.get(0)).toRSAPublicKey());
            visa.setVerified(signedJWT.verify(verifier));
            if (visa.isVerified()) {
                Ga4ghUtils.processPayload(mapper, visa, signedJWT.getPayload());
            }
        } catch (Exception ex) {
            log.error("Visa '{}' cannot be parsed and verified", jwtString, ex);
        }
        return visa;
    }

    public static void processPayload(ObjectMapper mapper, Ga4ghPassportVisa visa, Payload payload)
            throws IOException
    {
        JsonNode doc = mapper.readValue(payload.toString(), JsonNode.class);
        checkVisaKey(visa, doc, Ga4ghPassportVisa.SUB);
        checkVisaKey(visa, doc, Ga4ghPassportVisa.EXP);
        checkVisaKey(visa, doc, Ga4ghPassportVisa.ISS);
        JsonNode visa_v1 = doc.path(Ga4ghPassportVisa.GA4GH_VISA_V1);
        if (visa_v1.isMissingNode() || visa_v1.isNull() || visa_v1.isEmpty()) {
            log.warn("Nothing available in '{}', considering visa as not verified", Ga4ghPassportVisa.GA4GH_VISA_V1);
            visa.setVerified(false);
            return;
        }
        checkVisaKey(visa, visa_v1, Ga4ghPassportVisa.TYPE);
        checkVisaKey(visa, visa_v1, Ga4ghPassportVisa.ASSERTED);
        checkVisaKey(visa, visa_v1, Ga4ghPassportVisa.VALUE);
        checkVisaKey(visa, visa_v1, Ga4ghPassportVisa.SOURCE);
        checkVisaKey(visa, visa_v1, Ga4ghPassportVisa.BY);
        if (!visa.isVerified()) {
            return;
        }
        long exp = doc.get(Ga4ghPassportVisa.EXP).asLong();
        if (exp < Instant.now().getEpochSecond()) {
            log.warn("visa expired on {}", isoDateTime(exp));
            visa.setVerified(false);
            return;
        }
        visa.setLinkedIdentity(URLEncoder.encode(doc.get(Ga4ghPassportVisa.SUB).asText(), "utf-8") +
                ',' + URLEncoder.encode(doc.get(Ga4ghPassportVisa.ISS).asText(), "utf-8"));
        visa.setPrettyPayload(
                visa_v1.get(Ga4ghPassportVisa.TYPE).asText() + ": '"
                        + visa_v1.get(Ga4ghPassportVisa.VALUE).asText() + "' asserted at '"
                        + isoDate(visa_v1.get(Ga4ghPassportVisa.ASSERTED).asLong()) + '\''
        );
    }

    public static long getOneYearExpires(long asserted) {
        return getExpires(asserted, 1L);
    }

    public static long getExpires(long asserted, long addYears) {
        return Instant.ofEpochSecond(asserted).atZone(ZoneId.systemDefault()).plusYears(addYears).toEpochSecond();
    }

    private static void initializeSigner(JsonNode signer,
                                         Map<URI, String> signers,
                                         Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets)
    {
        String name = signer.path(CONF_KEY_NAME).asText();
        String jwks = signer.path(CONF_KEY_JWKS).asText();
        try {
            URL jku = new URL(jwks);
            remoteJwkSets.put(jku.toURI(), new RemoteJWKSet<>(jku));
            signers.put(jku.toURI(), name);
            log.info("JWKS Signer '{}' added with keys '{}'", name, jwks);
        } catch (MalformedURLException | URISyntaxException e) {
            log.error("cannot add to RemoteJWKSet map: '{}' -> '{}'", name, jwks, e);
        }
    }

    private static void initializeRepo(JsonNode repo, List<Ga4ghClaimRepository> claimRepositories) {
        String name = repo.path(CONF_KEY_NAME).asText();
        String actionURL = repo.path(CONF_KEY_URL).asText();
        JsonNode headers = repo.path(CONF_KEY_HEADERS);
        Map<String, String> headersWithValues = new HashMap<>();
        for (JsonNode header: headers) {
            headersWithValues.put(header.path(CONF_KEY_HEADER).asText(), header.path(CONF_KEY_VALUE).asText());
        }
        if (actionURL == null || headersWithValues.isEmpty()) {
            log.error("claim repository '{}' not defined with url|auth_header|auth_value", repo);
            return;
        }
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(
                new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),
                        headersWithValues.entrySet()
                                .stream()
                                .map(e -> new AddHeaderInterceptor(e.getKey(), e.getValue()))
                                .collect(Collectors.toList()))
        );
        claimRepositories.add(new Ga4ghClaimRepository(name, actionURL, restTemplate));
        log.info("GA4GH Claims Repository '{}' configured at '{}'", name, actionURL);
    }

    private static void checkVisaKey(Ga4ghPassportVisa visa, JsonNode jsonNode, String key) {
        if (jsonNode.path(key).isMissingNode()) {
            log.warn("Key '{}' is missing in the Visa, therefore cannot be verified", key);
            visa.setVerified(false);
        } else {
            switch (key) {
                case Ga4ghPassportVisa.SUB:
                    visa.setSub(jsonNode.path(key).asText());
                    break;
                case Ga4ghPassportVisa.ISS:
                    visa.setIss(jsonNode.path(key).asText());
                    break;
                case Ga4ghPassportVisa.TYPE:
                    visa.setType(jsonNode.path(key).asText());
                    break;
                case Ga4ghPassportVisa.VALUE:
                    visa.setValue(jsonNode.path(key).asText());
                    break;
            }
        }
    }

    private static String isoDate(long linuxTime) {
        return isoFormat(linuxTime, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static String isoDateTime(long linuxTime) {
        return isoFormat(linuxTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    private static String isoFormat(long linuxTime, DateTimeFormatter formatter) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault());
        return formatter.format(zdt);
    }

}
