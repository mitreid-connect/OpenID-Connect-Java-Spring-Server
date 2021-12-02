package cz.muni.ics.oidc.server.ga4gh;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Ga4ghPassportVisa {

    public static final String GA4GH_VISA_V1 = "ga4gh_visa_v1";

    public static final String TYPE_AFFILIATION_AND_ROLE = "AffiliationAndRole";
    public static final String TYPE_ACCEPTED_TERMS_AND_POLICIES = "AcceptedTermsAndPolicies";
    public static final String TYPE_RESEARCHER_STATUS = "ResearcherStatus";
    public static final String TYPE_LINKED_IDENTITIES = "LinkedIdentities";

    public static final String BY_SYSTEM = "system";
    public static final String BY_SO = "so";
    public static final String BY_PEER = "peer";
    public static final String BY_SELF = "self";

    public static final String SUB = "sub";
    public static final String EXP = "exp";
    public static final String ISS = "iss";
    public static final String TYPE = "type";
    public static final String ASSERTED = "asserted";
    public static final String VALUE = "value";
    public static final String SOURCE = "source";
    public static final String BY = "by";
    public static final String CONDITION = "condition";

    private boolean verified = false;
    private String linkedIdentity;
    private String sub;
    private String iss;
    private String type;
    private String value;

    @ToString.Exclude
    private String signer;

    @ToString.Exclude
    private String jwt;

    @ToString.Exclude
    private String prettyPayload;

    public Ga4ghPassportVisa(String jwt) {
        this.jwt = jwt;
    }

    public String getPrettyString() {
        return prettyPayload + ", signed by " + signer;
    }

}
