package cz.muni.ics.oidc.server.bbmri;

import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.BY_PEER;
import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.BY_SELF;
import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.BY_SO;
import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.BY_SYSTEM;
import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.TYPE_ACCEPTED_TERMS_AND_POLICIES;
import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.TYPE_AFFILIATION_AND_ROLE;
import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.TYPE_LINKED_IDENTITIES;
import static cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportVisa.TYPE_RESEARCHER_STATUS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import cz.muni.ics.oidc.models.PerunAttribute;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.connectors.Affiliation;
import cz.muni.ics.oidc.server.ga4gh.Ga4ghClaimRepository;
import cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportAndVisaClaimSource;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Class producing GA4GH Passport claim. The claim is specified in
 * https://bit.ly/ga4gh-passport-v1
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.config_file</b> - full path to the configuration file for this claim. See
 *     configuration templates for such a file.</li> (Passed to Ga4ghPassportAndVisaClaimSource.class)
 *     <li><b>custom.claim.[claimName].source.bonaFideStatus.attr</b> - mapping for bonaFideStatus Attribute</li>
 *     <li><b>custom.claim.[claimName].source.groupAffiliations.attr</b> - mapping for groupAffiliations Attribute</li>
 *     <li><b>custom.claim.[claimName].source.termsAndPoliciesGroupId</b> - ID of group in which the membership represents acceptance of terms and policies</li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class BbmriGa4ghClaimSource extends Ga4ghPassportAndVisaClaimSource {

	private static final String BONA_FIDE_URL = "https://doi.org/10.1038/s41431-018-0219-y";
	private final static String BBMRI_ERIC_ORG_URL = "https://www.bbmri-eric.eu/";
	private static final String BBMRI_ID = "bbmri_id";

	private final String bonaFideStatusAttr;
	private final String groupAffiliationsAttr;
	private final Long termsAndPoliciesGroupId;

	public BbmriGa4ghClaimSource(ClaimSourceInitContext ctx) throws URISyntaxException {
		super(ctx, "BBMRI-ERIC");
		log.debug("initializing");
		//remember context
		bonaFideStatusAttr = ctx.getProperty("bonaFideStatus.attr", null);
		groupAffiliationsAttr = ctx.getProperty("groupAffiliations.attr", null);
		//TODO: update group ID
		termsAndPoliciesGroupId = ctx.getLongProperty("termsAndPoliciesGroupId", 10432L);
	}

	@Override
	public Set<String> getAttrIdentifiers() {
		Set<String> set = new HashSet<>();
		if (bonaFideStatusAttr != null) {
			set.add(bonaFideStatusAttr);
		}
		if (groupAffiliationsAttr != null) {
			set.add(groupAffiliationsAttr);
		}
		return set;
	}

	@Override
	protected String getDefaultConfigFilePath() {
		return "/etc/mitreid/bbmri/ga4gh_config.yml";
	}

	@Override
	protected void addAffiliationAndRoles(long now, ClaimSourceProduceContext pctx, ArrayNode passport, List<Affiliation> affiliations) {
		//by=system for users with affiliation asserted by their IdP (set in UserExtSource attribute "affiliation")
		for (Affiliation affiliation : affiliations) {
			//expires 1 year after the last login from the IdP asserting the affiliation
			long expires = Instant.ofEpochSecond(affiliation.getAsserted()).atZone(ZoneId.systemDefault()).plusYears(1L).toEpochSecond();
			if (expires < now) continue;
			JsonNode visa = createPassportVisa(TYPE_AFFILIATION_AND_ROLE, pctx, affiliation.getValue(), affiliation.getSource(), BY_SYSTEM, affiliation.getAsserted(), expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

	@Override
	protected void addAcceptedTermsAndPolicies(long now, ClaimSourceProduceContext pctx, ArrayNode passport) {
		//by=self for members of the group 10432 "Bona Fide Researchers"
		boolean userInGroup = pctx.getPerunAdapter().isUserInGroup(pctx.getPerunUserId(), termsAndPoliciesGroupId);
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
			JsonNode visa = createPassportVisa(TYPE_ACCEPTED_TERMS_AND_POLICIES, pctx, BONA_FIDE_URL, BBMRI_ERIC_ORG_URL, BY_SELF, asserted, expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

	@Override
	protected void addResearcherStatuses(long now, ClaimSourceProduceContext pctx, ArrayNode passport, List<Affiliation> affiliations) {
		//by=peer for users with attribute elixirBonaFideStatusREMS
		PerunAttribute bbmriBonaFideStatus = pctx.getPerunAdapter()
				.getAdapterRpc()
				.getUserAttribute(pctx.getPerunUserId(), bonaFideStatusAttr);

		String valueCreatedAt = null;
		if (bbmriBonaFideStatus != null) {
			valueCreatedAt = bbmriBonaFideStatus.getValueCreatedAt();
		}

		if (valueCreatedAt != null) {
			long asserted = Timestamp.valueOf(valueCreatedAt).getTime() / 1000L;
			long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
			if (expires > now) {
				JsonNode visa = createPassportVisa(TYPE_RESEARCHER_STATUS, pctx, BONA_FIDE_URL, BBMRI_ERIC_ORG_URL, BY_PEER, asserted, expires, null);
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
				JsonNode visa = createPassportVisa(TYPE_RESEARCHER_STATUS, pctx, BONA_FIDE_URL, affiliation.getSource(), BY_SYSTEM, affiliation.getAsserted(), expires, null);
				if (visa != null) {
					passport.add(visa);
				}
			}
		}
		//by=so for users with faculty affiliation asserted by membership in a group with groupAffiliations attribute
		for (Affiliation affiliation : pctx.getPerunAdapter().getGroupAffiliations(pctx.getPerunUserId(), groupAffiliationsAttr)) {
			if (affiliation.getValue().startsWith("faculty@")) {
				long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
				JsonNode visa = createPassportVisa(TYPE_RESEARCHER_STATUS, pctx, BONA_FIDE_URL, BBMRI_ERIC_ORG_URL, BY_SO, affiliation.getAsserted(), expires, null);
				if (visa != null) {
					passport.add(visa);
				}
			}
		}
	}

	@Override
	protected void addControlledAccessGrants(long now, ClaimSourceProduceContext pctx, ArrayNode passport) {
		Set<String> linkedIdentities = new HashSet<>();
		//call Resource Entitlement Management System
		for (Ga4ghClaimRepository repo: CLAIM_REPOSITORIES) {
			callPermissionsJwtAPI(repo, Collections.singletonMap(BBMRI_ID, pctx.getSub()), passport, linkedIdentities);
		}
		if (!linkedIdentities.isEmpty()) {
			for (String linkedIdentity : linkedIdentities) {
				JsonNode visa = createPassportVisa(TYPE_LINKED_IDENTITIES, pctx, linkedIdentity, BBMRI_ERIC_ORG_URL, BY_SYSTEM, now, now + 3600L * 24 * 365, null);
				if (visa != null) {
					passport.add(visa);
				}
			}
		}
	}

}
