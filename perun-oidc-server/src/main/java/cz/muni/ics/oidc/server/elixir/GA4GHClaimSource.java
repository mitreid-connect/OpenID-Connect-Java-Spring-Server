package cz.muni.ics.oidc.server.elixir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oidc.models.PerunAttribute;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import cz.muni.ics.openid.connect.web.JWKSetPublishingEndpoint;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Class producing GA4GH Passport claim. The claim is specified in
 * https://bit.ly/ga4gh-passport-v1
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.config_file</b> - full path to the configuration file for this claim. See
 *     configuration templates for such a file.</li>
 *     <li><b>custom.claim.[claimName].source.bonaFideStatus.attr</b> - mapping for bonaFideStatus Attriute</li>
 *     <li><b>custom.claim.[claimName].source.bonaFideStatusREMS.attr</b> - mapping for bonaFideStatus Attriute</li>
 *     <li><b>custom.claim.[claimName].source.groupAffiliations.attr</b> - mapping for groupAffiliations Attriute</li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class GA4GHClaimSource extends ClaimSource {

	static final String GA4GH_SCOPE = "ga4gh_passport_v1";
	private static final String GA4GH_CLAIM = "ga4gh_passport_v1";

	private static final String BONA_FIDE_URL = "https://doi.org/10.1038/s41431-018-0219-y";
	private static final String ELIXIR_ORG_URL = "https://elixir-europe.org/";
	private static final String ELIXIR_ID = "elixir_id";

	private final JWTSigningAndValidationService jwtService;
	private final URI jku;
	private final String issuer;
	private static final List<ClaimRepository> claimRepositories = new ArrayList<>();
	private static final Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets = new HashMap<>();
	private static final Map<URI, String> signers = new HashMap<>();

	private final String bonaFideStatusAttr;
	private final String bonaFideStatusREMSAttr;
	private final String groupAffiliationsAttr;

	public GA4GHClaimSource(ClaimSourceInitContext ctx) throws URISyntaxException {
		super(ctx);
		log.debug("initializing");
		//remember context
		jwtService = ctx.getJwtService();
		issuer = ctx.getPerunOidcConfig().getConfigBean().getIssuer();
		jku = new URI(issuer + JWKSetPublishingEndpoint.URL);
		// load config file
		parseConfigFile(ctx.getProperty("config_file", "/etc/mitreid/elixir/ga4gh_config.yml"));
		bonaFideStatusAttr = ctx.getProperty("bonaFideStatus.attr", null);
		bonaFideStatusREMSAttr = ctx.getProperty("bonaFideStatusREMS.attr", null);
		groupAffiliationsAttr = ctx.getProperty("groupAffiliations.attr", null);
	}

	static void parseConfigFile(String file) {
		YAMLMapper mapper = new YAMLMapper();
		try {
			JsonNode root = mapper.readValue(new File(file), JsonNode.class);
			// prepare claim repositories
			for (JsonNode repo : root.path("repos")) {
				String name = repo.path("name").asText();
				String actionURL = repo.path("url").asText();
				JsonNode headers = repo.path("headers");
				Map<String, String> headersWithValues = new HashMap<>();
				for (JsonNode header: headers) {
					headersWithValues.put(header.path("header").asText(), header.path("value").asText());
				}
				if (actionURL == null || headersWithValues.isEmpty()) {
					log.error("claim repository " + repo + " not defined with url|auth_header|auth_value ");
					continue;
				}
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.setRequestFactory(
						new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),
								headersWithValues.entrySet()
										.stream()
										.map(e -> new AddHeaderInterceptor(e.getKey(), e.getValue()))
										.collect(Collectors.toList()))
				);
				claimRepositories.add(new ClaimRepository(name, restTemplate, actionURL));
				log.info("GA4GH Claims Repository " + name + " configured at " + actionURL);
			}
			// prepare claim signers
			for (JsonNode signer : root.path("signers")) {
				String name = signer.path("name").asText();
				String jwks = signer.path("jwks").asText();
				try {
					URL jku = new URL(jwks);
					remoteJwkSets.put(jku.toURI(), new RemoteJWKSet<>(jku));
					signers.put(jku.toURI(), name);
					log.info("JWKS Signer " + name + " added with keys " + jwks);
				} catch (MalformedURLException | URISyntaxException e) {
					log.error("cannot add to RemoteJWKSet map: " + name + " " + jwks, e);
				}
			}
		} catch (IOException ex) {
			log.error("cannot read GA4GH config file", ex);
		}
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		Set<String> set = new HashSet<>();
		if (bonaFideStatusAttr != null) {
			set.add(bonaFideStatusAttr);
		}
		if (bonaFideStatusREMSAttr != null) {
			set.add(bonaFideStatusREMSAttr);
		}
		if (groupAffiliationsAttr != null) {
			set.add(groupAffiliationsAttr);
		}
		return set;
	}

	@Override
	public JsonNode produceValue(ClaimSourceProduceContext pctx) {
		if (pctx.getClient() == null) {
			log.debug("client is not set");
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


	private void addAffiliationAndRoles(long now, ClaimSourceProduceContext pctx, ArrayNode passport, List<Affiliation> affiliations) {
		//by=system for users with affiliation asserted by their IdP (set in UserExtSource attribute "affiliation")
		for (Affiliation affiliation : affiliations) {
			//expires 1 year after the last login from the IdP asserting the affiliation
			long expires = Instant.ofEpochSecond(affiliation.getAsserted()).atZone(ZoneId.systemDefault()).plusYears(1L).toEpochSecond();
			if (expires < now) continue;
			JsonNode visa = createPassportVisa("AffiliationAndRole", pctx, affiliation.getValue(), affiliation.getSource(), "system", affiliation.getAsserted(), expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

	private void addAcceptedTermsAndPolicies(long now, ClaimSourceProduceContext pctx, ArrayNode passport) {
		//by=self for members of the group 10432 "Bona Fide Researchers"
		boolean userInGroup = pctx.getPerunAdapter().isUserInGroup(pctx.getPerunUserId(), 10432L);
		if (userInGroup) {
			PerunAttribute bonaFideStatus = pctx.getPerunAdapter()
					.getAdapterRpc()
					.getUserAttribute(pctx.getPerunUserId(), bonaFideStatusAttr);
			String valueCreatedAt = bonaFideStatus.getValueCreatedAt();
			long asserted;
			if (valueCreatedAt != null) {
				asserted = Timestamp.valueOf(valueCreatedAt).getTime() / 1000L;
			} else {
				asserted = System.currentTimeMillis() / 1000L;
			}
			long expires = Instant.ofEpochSecond(asserted).atZone(ZoneId.systemDefault()).plusYears(100L).toEpochSecond();
			if (expires < now) return;
			JsonNode visa = createPassportVisa("AcceptedTermsAndPolicies", pctx, BONA_FIDE_URL, ELIXIR_ORG_URL, "self", asserted, expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

	private void addResearcherStatuses(long now, ClaimSourceProduceContext pctx, ArrayNode passport, List<Affiliation> affiliations) {
		//by=peer for users with attribute elixirBonaFideStatusREMS
		PerunAttribute elixirBonaFideStatusREMS = pctx.getPerunAdapter()
				.getAdapterRpc()
				.getUserAttribute(pctx.getPerunUserId(), bonaFideStatusREMSAttr);

		String valueCreatedAt = null;
		if (elixirBonaFideStatusREMS != null) {
			valueCreatedAt = elixirBonaFideStatusREMS.getValueCreatedAt();
		}

		if (valueCreatedAt != null) {
			long asserted = Timestamp.valueOf(valueCreatedAt).getTime() / 1000L;
			long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
			if (expires > now) {
				JsonNode visa = createPassportVisa("ResearcherStatus", pctx, BONA_FIDE_URL, ELIXIR_ORG_URL, "peer", asserted, expires, null);
				if (visa != null) {
					passport.add(visa);
				}
			}
		}
		//by=system for users with faculty affiliation asserted by their IdP (set in UserExtSource attribute "affiliation")
		for (Affiliation affiliation : affiliations) {
			if (affiliation.getValue().startsWith("faculty@")) {
				long expires = Instant.ofEpochSecond(affiliation.getAsserted()).atZone(ZoneId.systemDefault()).plusYears(1L).toEpochSecond();
				if (expires < now) continue;
				JsonNode visa = createPassportVisa("ResearcherStatus", pctx, BONA_FIDE_URL, affiliation.getSource(), "system", affiliation.getAsserted(), expires, null);
				if (visa != null) {
					passport.add(visa);
				}
			}
		}
		//by=so for users with faculty affiliation asserted by membership in a group with groupAffiliations attribute
		for (Affiliation affiliation : pctx.getPerunAdapter().getGroupAffiliations(pctx.getPerunUserId(), groupAffiliationsAttr)) {
			if (affiliation.getValue().startsWith("faculty@")) {
				long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
				JsonNode visa = createPassportVisa("ResearcherStatus", pctx, BONA_FIDE_URL, ELIXIR_ORG_URL, "so", affiliation.getAsserted(), expires, null);
				if (visa != null) {
					passport.add(visa);
				}
			}
		}
	}

	private static String isoDate(long linuxTime) {
		return DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault()));
	}

	private static String isoDateTime(long linuxTime) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault()));
	}

	private JsonNode createPassportVisa(String type, ClaimSourceProduceContext pctx, String value, String source, String by, long asserted, long expires, JsonNode condition) {
		long now = System.currentTimeMillis() / 1000L;
		if (asserted > now) {
			log.warn("visa asserted in future ! perunUserId {} sub {} type {} value {} source {} by {} asserted {}", pctx.getPerunUserId(), pctx.getSub(), type, value, source, by, Instant.ofEpochSecond(asserted));
			return null;
		}
		if (expires <= now) {
			log.warn("visa already expired ! perunUserId {} sub {} type {} value {} source {} by {} expired {}", pctx.getPerunUserId(), pctx.getSub(), type, value, source, by, Instant.ofEpochSecond(expires));
			return null;
		}

		Map<String, Object> passportVisaObject = new HashMap<>();
		passportVisaObject.put("type", type);
		passportVisaObject.put("asserted", asserted);
		passportVisaObject.put("value", value);
		passportVisaObject.put("source", source);
		passportVisaObject.put("by", by);
		if (condition != null && !condition.isNull() && !condition.isMissingNode()) {
			passportVisaObject.put("condition", condition);
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
				.claim("ga4gh_visa_v1", passportVisaObject)
				.build();
		SignedJWT myToken = new SignedJWT(jwsHeader, jwtClaimsSet);
		jwtService.signJwt(myToken);
		return JsonNodeFactory.instance.textNode(myToken.serialize());
	}

	private void addControlledAccessGrants(long now, ClaimSourceProduceContext pctx, ArrayNode passport) {
		Set<String> linkedIdentities = new HashSet<>();
		//call Resource Entitlement Management System
		for (ClaimRepository repo : claimRepositories) {
			callPermissionsJwtAPI(repo, Collections.singletonMap(ELIXIR_ID, pctx.getSub()), pctx, passport, linkedIdentities);
		}
		if (!linkedIdentities.isEmpty()) {
			for (String linkedIdentity : linkedIdentities) {
				JsonNode visa = createPassportVisa("LinkedIdentities", pctx, linkedIdentity, ELIXIR_ORG_URL, "system", now, now + 3600L * 24 * 365, null);
				if (visa != null) {
					passport.add(visa);
				}
			}
		}
	}

	private void callPermissionsJwtAPI(ClaimRepository repo, Map<String, String> uriVariables, ClaimSourceProduceContext pctx, ArrayNode passport, Set<String> linkedIdentities) {
		JsonNode response = callHttpJsonAPI(repo, uriVariables);
		if (response != null) {
			JsonNode visas = response.path(GA4GH_CLAIM);
			if (visas.isArray()) {
				for (JsonNode visaNode : visas) {
					if (visaNode.isTextual()) {
						PassportVisa visa = parseAndVerifyVisa(visaNode.asText());
						if (visa.isVerified()) {
							log.debug("adding a visa to passport: {}", visa);
							passport.add(passport.textNode(visa.getJwt()));
							linkedIdentities.add(visa.getLinkedIdentity());
						} else {
							log.warn("skipping visa: {}", visa);
						}
					} else {
						log.warn("element of ga4gh_passport_v1 is not a String: {}", visaNode);
					}
				}
			} else {
				log.warn("ga4gh_passport_v1 is not an array in {}", response);
			}
		}
	}


	public static PassportVisa parseAndVerifyVisa(String jwtString) {
		PassportVisa visa = new PassportVisa(jwtString);
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
				log.error("JKU {} is not among trusted key sets", jku);
				return visa;
			}
			List<JWK> keys = remoteJWKSet.get(new JWKSelector(new JWKMatcher.Builder().keyID(signedJWT.getHeader().getKeyID()).build()), null);
			RSASSAVerifier verifier = new RSASSAVerifier(((RSAKey) keys.get(0)).toRSAPublicKey());
			visa.setVerified(signedJWT.verify(verifier));
			if (visa.isVerified()) {
				processPayload(visa, signedJWT.getPayload());
			}
		} catch (Exception ex) {
			log.error("visa " + jwtString + " cannot be parsed and verified", ex);
		}
		return visa;
	}

	static private final ObjectMapper JSON_MAPPER = new ObjectMapper();

	static private void processPayload(PassportVisa visa, Payload payload) throws IOException {
		JsonNode doc = JSON_MAPPER.readValue(payload.toString(), JsonNode.class);
		checkVisaKey(visa, doc, "sub");
		checkVisaKey(visa, doc, "exp");
		checkVisaKey(visa, doc, "iss");
		JsonNode visa_v1 = doc.path("ga4gh_visa_v1");
		checkVisaKey(visa, visa_v1, "type");
		checkVisaKey(visa, visa_v1, "asserted");
		checkVisaKey(visa, visa_v1, "value");
		checkVisaKey(visa, visa_v1, "source");
		checkVisaKey(visa, visa_v1, "by");
		if (!visa.isVerified()) return;
		long exp = doc.get("exp").asLong();
		if (exp < Instant.now().getEpochSecond()) {
			log.warn("visa expired on " + isoDateTime(exp));
			visa.setVerified(false);
			return;
		}
		visa.setLinkedIdentity(URLEncoder.encode(doc.get("sub").asText(), "utf-8") + "," + URLEncoder.encode(doc.get("iss").asText(), "utf-8"));
		visa.setPrettyPayload(
				visa_v1.get("type").asText() + ":  \"" + visa_v1.get("value").asText() + "\" asserted " + isoDate(visa_v1.get("asserted").asLong())
		);
	}

	static private void checkVisaKey(PassportVisa visa, JsonNode jsonNode, String key) {
		if (jsonNode.path(key).isMissingNode()) {
			log.warn(key + " is missing");
			visa.setVerified(false);
		} else {
			switch (key) {
				case "sub":
					visa.setSub(jsonNode.path(key).asText());
					break;
				case "iss":
					visa.setIss(jsonNode.path(key).asText());
					break;
				case "type":
					visa.setType(jsonNode.path(key).asText());
					break;
				case "value":
					visa.setValue(jsonNode.path(key).asText());
					break;
			}
		}
	}

	@SuppressWarnings("Duplicates")
	private static JsonNode callHttpJsonAPI(ClaimRepository repo, Map<String, String> uriVariables) {
		//get permissions data
		try {
			JsonNode result;
			//make the call
			try {
				if (log.isDebugEnabled()) {
					log.debug("calling Permissions API at {}", repo.getRestTemplate().getUriTemplateHandler().expand(repo.getActionURL(), uriVariables));
				}
				result = repo.getRestTemplate().getForObject(repo.getActionURL(), JsonNode.class, uriVariables);
			} catch (HttpClientErrorException ex) {
				MediaType contentType = ex.getResponseHeaders().getContentType();
				String body = ex.getResponseBodyAsString();
				log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + repo.getActionURL() + " Content-Type: " + contentType);
				if (ex.getRawStatusCode() == 404) {
					log.warn("Got status 404 from Permissions endpoint {}, ELIXIR AAI user is not linked to user at Permissions API", repo.getActionURL());
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

	public static class PassportVisa {
		String jwt;
		boolean verified = false;
		String linkedIdentity;
		String signer;
		String prettyPayload;
		private String sub;
		private String iss;
		private String type;
		private String value;

		PassportVisa(String jwt) {
			this.jwt = jwt;
		}

		public String getJwt() {
			return jwt;
		}

		public boolean isVerified() {
			return verified;
		}

		void setVerified(boolean verified) {
			this.verified = verified;
		}

		String getLinkedIdentity() {
			return linkedIdentity;
		}

		void setLinkedIdentity(String linkedIdentity) {
			this.linkedIdentity = linkedIdentity;
		}

		void setSigner(String signer) {
			this.signer = signer;
		}

		void setPrettyPayload(String prettyPayload) {
			this.prettyPayload = prettyPayload;
		}

		public String getPrettyString() {
			return prettyPayload + ", signed by " + signer;
		}

		@Override
		public String toString() {
			return "PassportVisa{" +
//					"jwt='" + jwt + '\'' +
					"  type=" + type +
					", sub=" + sub +
					", iss=" + iss +
					", value=" + value +
					", verified=" + verified +
					", linkedIdentity=" + linkedIdentity +
					'}';
		}

		public void setSub(String sub) {
			this.sub = sub;
		}

		public String getSub() {
			return sub;
		}

		public void setIss(String iss) {
			this.iss = iss;
		}

		public String getIss() {
			return iss;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static class ClaimRepository {
		private String name;
		private RestTemplate restTemplate;
		private String actionURL;

		public ClaimRepository(String name, RestTemplate restTemplate, String actionURL) {
			this.name = name;
			this.restTemplate = restTemplate;
			this.actionURL = actionURL;
		}

		public RestTemplate getRestTemplate() {
			return restTemplate;
		}

		public String getActionURL() {
			return actionURL;
		}

		public String getName() {
			return name;
		}
	}

}
