package cz.muni.ics.oidc.server.ga4gh;

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
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Class producing GA4GH Passport claim. The claim is specified in
 * https://bit.ly/ga4gh-passport-v1
 *
 * Configuration (replace [claimName] with the name of the claim):
 * <ul>
 *     <li><b>custom.claim.[claimName].source.config_file</b> - full path to the configuration file for this claim. See
 *     configuration templates for such a file.</li> (Passed to Ga4ghPassportAndVisaClaimSource.class)
 *     <li><b>custom.claim.[claimName].source.bonaFideStatus.attr</b> - mapping for bonaFideStatus Attribute</li>
 *     <li><b>custom.claim.[claimName].source.bonaFideStatusREMS.attr</b> - mapping for bonaFideStatus Attribute</li>
 *     <li><b>custom.claim.[claimName].source.groupAffiliations.attr</b> - mapping for groupAffiliations Attribute</li>
 *     <li><b>custom.claim.[claimName].source.termsAndPoliciesGroupId</b> - ID of group in which the membership represents acceptance of terms and policies</li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class ElixirGa4ghClaimSource extends Ga4ghPassportAndVisaClaimSource {

	private static final String BONA_FIDE_URL = "https://doi.org/10.1038/s41431-018-0219-y";
	private static final String ELIXIR_ORG_URL = "https://elixir-europe.org/";
	private static final String ELIXIR_ID = "elixir_id";
	private static final String FACULTY_AT = "faculty@";

	private final String bonaFideStatusAttr;
	private final String bonaFideStatusREMSAttr;
	private final String groupAffiliationsAttr;
	private final Long termsAndPoliciesGroupId;

	public ElixirGa4ghClaimSource(ClaimSourceInitContext ctx) throws URISyntaxException {
		super(ctx, "ELIXIR");
		bonaFideStatusAttr = ctx.getProperty("bonaFideStatus.attr", null);
		bonaFideStatusREMSAttr = ctx.getProperty("bonaFideStatusREMS.attr", null);
		groupAffiliationsAttr = ctx.getProperty("groupAffiliations.attr", null);
		termsAndPoliciesGroupId = ctx.getLongProperty("termsAndPoliciesGroupId", 10432L);
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
	protected String getDefaultConfigFilePath() {
		return "/etc/mitreid/elixir/ga4gh_config.yml";
	}

	@Override
	protected void addAffiliationAndRoles(long now,
										  ClaimSourceProduceContext pctx,
										  ArrayNode passport,
										  List<Affiliation> affiliations)
	{
		//by=system for users with affiliation asserted by their IdP (set in UserExtSource attribute "affiliation")
		if (affiliations == null) {
			return;
		}
		for (Affiliation affiliation: affiliations) {
			//expires 1 year after the last login from the IdP asserting the affiliation
			long expires = Ga4ghUtils.getOneYearExpires(affiliation.getAsserted());
			if (expires < now) {
				continue;
			}
			JsonNode visa = createPassportVisa(TYPE_AFFILIATION_AND_ROLE, pctx, affiliation.getValue(),
					affiliation.getSource(), BY_SYSTEM, affiliation.getAsserted(), expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

	@Override
	protected void addAcceptedTermsAndPolicies(long now, ClaimSourceProduceContext pctx, ArrayNode passport) {
		//by=self for members of the group "Bona Fide Researchers"
		boolean userInGroup = pctx.getPerunAdapter().isUserInGroup(pctx.getPerunUserId(), termsAndPoliciesGroupId);
		if (!userInGroup) {
			return;
		}
		long asserted = now;
		if (bonaFideStatusAttr != null) {
			PerunAttribute bonaFideStatus = pctx.getPerunAdapter()
					.getAdapterRpc()
					.getUserAttribute(pctx.getPerunUserId(), bonaFideStatusAttr);
			if (bonaFideStatus != null && bonaFideStatus.getValueCreatedAt() != null) {
				asserted = Timestamp.valueOf(bonaFideStatus.getValueCreatedAt()).getTime() / 1000L;
			}
		}
		long expires = Ga4ghUtils.getExpires(asserted, 100L);
		if (expires < now) {
			return;
		}
		JsonNode visa = createPassportVisa(TYPE_ACCEPTED_TERMS_AND_POLICIES, pctx,
				BONA_FIDE_URL, ELIXIR_ORG_URL, BY_SELF, asserted, expires, null);
		if (visa != null) {
			passport.add(visa);
		}
	}

	@Override
	protected void addResearcherStatuses(long now,
										 ClaimSourceProduceContext pctx,
										 ArrayNode passport,
										 List<Affiliation> affiliations)
	{
		addResearcherStatusFromBonaFideAttribute(pctx, now, passport);
		addResearcherStatusFromAffiliation(pctx, affiliations, now, passport);
		addResearcherStatusGroupAffiliations(pctx, now, passport);
	}

	@Override
	protected void addControlledAccessGrants(long now, ClaimSourceProduceContext pctx, ArrayNode passport) {
		if (CLAIM_REPOSITORIES.isEmpty()) {
			return;
		}
		Set<String> linkedIdentities = new HashSet<>();
		for (Ga4ghClaimRepository repo: CLAIM_REPOSITORIES) {
			callPermissionsJwtAPI(repo, Collections.singletonMap(ELIXIR_ID, pctx.getSub()), passport, linkedIdentities);
		}
		if (linkedIdentities.isEmpty()) {
			return;
		}
		for (String linkedIdentity : linkedIdentities) {
			long expires = Ga4ghUtils.getOneYearExpires(now);
			JsonNode visa = createPassportVisa(TYPE_LINKED_IDENTITIES, pctx, linkedIdentity,
					ELIXIR_ORG_URL, BY_SYSTEM, now, expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

	private void addResearcherStatusFromBonaFideAttribute(ClaimSourceProduceContext pctx,
														  long now,
														  ArrayNode passport)
	{
		//by=peer for users with attribute elixirBonaFideStatusREMS
		String valueCreatedAt = null;
		PerunAttribute elixirBonaFideStatusREMS = pctx.getPerunAdapter()
				.getAdapterRpc()
				.getUserAttribute(pctx.getPerunUserId(), bonaFideStatusREMSAttr);
		if (elixirBonaFideStatusREMS != null) {
			valueCreatedAt = elixirBonaFideStatusREMS.getValueCreatedAt();
		}
		if (valueCreatedAt == null) {
			return;
		}
		long asserted = Timestamp.valueOf(valueCreatedAt).getTime() / 1000L;
		long expires = Ga4ghUtils.getOneYearExpires(asserted);
		if (expires < now) {
			return;
		}
		JsonNode visa = createPassportVisa(TYPE_RESEARCHER_STATUS, pctx, BONA_FIDE_URL,
				ELIXIR_ORG_URL, BY_PEER, asserted, expires, null);
		if (visa != null) {
			passport.add(visa);
		}
	}

	private void addResearcherStatusFromAffiliation(ClaimSourceProduceContext pctx,
													List<Affiliation> affiliations,
													long now,
													ArrayNode passport)
	{
		//by=system for users with faculty affiliation asserted by their IdP (set in UserExtSource attribute "affiliation")
		if (affiliations == null) {
			return;
		}
		for (Affiliation affiliation: affiliations) {
			if (!StringUtils.startsWithIgnoreCase(affiliation.getValue(), FACULTY_AT)) {
				continue;
			}
			long expires = Ga4ghUtils.getOneYearExpires(affiliation.getAsserted());
			if (expires < now) {
				continue;
			}
			JsonNode visa = createPassportVisa(TYPE_RESEARCHER_STATUS, pctx, BONA_FIDE_URL,
					affiliation.getSource(), BY_SYSTEM, affiliation.getAsserted(), expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

	private void addResearcherStatusGroupAffiliations(ClaimSourceProduceContext pctx, long now, ArrayNode passport) {
		//by=so for users with faculty affiliation asserted by membership in a group with groupAffiliations attribute
		List<Affiliation> groupAffiliations = pctx.getPerunAdapter()
				.getGroupAffiliations(pctx.getPerunUserId(), groupAffiliationsAttr);
		if (groupAffiliations == null) {
			return;
		}
		for (Affiliation affiliation: groupAffiliations) {
			if (!StringUtils.startsWithIgnoreCase(affiliation.getValue(), FACULTY_AT)) {
				continue;
			}
			long expires = Ga4ghUtils.getOneYearExpires(now);
			JsonNode visa = createPassportVisa(TYPE_RESEARCHER_STATUS, pctx, BONA_FIDE_URL,
					ELIXIR_ORG_URL, BY_SO, affiliation.getAsserted(), expires, null);
			if (visa != null) {
				passport.add(visa);
			}
		}
	}

}
