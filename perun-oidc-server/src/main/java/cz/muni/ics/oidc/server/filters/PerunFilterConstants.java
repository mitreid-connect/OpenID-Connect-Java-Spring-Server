package cz.muni.ics.oidc.server.filters;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing common constants used by Perun request filters.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class PerunFilterConstants {

    public static final String AUTHORIZE_REQ_PATTERN = "/authorize";
    public static final String SHIB_IDENTITY_PROVIDER = "Shib-Identity-Provider";
    public static final String SHIB_AUTHN_CONTEXT_CLASS = "Shib-AuthnContext-Class";
    public static final String SHIB_AUTHN_CONTEXT_METHOD = "Shib-Authentication-Method";

    public static final String PARAM_CLIENT_ID = "client_id";
    public static final String PARAM_SCOPE = "scope";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_HEADER = "header";
    public static final String PARAM_TARGET = "target";
    public static final String PARAM_FORCE_AUTHN = "forceAuthn";
    public static final String PARAM_PROMPT = "prompt";
    public static final String PARAM_REASON = "reason";
    public static final String PARAM_ACCEPTED = "accepted";
    public static final String PARAM_ACR_VALUES = "acr_values";
    public static final String PARAM_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
    public static final String PARAM_STATE = "state";
    public static final String CLIENT_ID_PREFIX = "urn:cesnet:proxyidp:client_id:";
    public static final String AARC_IDP_HINT = "aarc_idp_hint";

    public static final String IDP_ENTITY_ID_PREFIX = "urn:cesnet:proxyidp:idpentityid:";
    public static final String FILTER_PREFIX = "urn:cesnet:proxyidp:filter:";
    public static final String EFILTER_PREFIX = "urn:cesnet:proxyidp:efilter:";

    public static final String SAML_EPUID = "urn:oid:1.3.6.1.4.1.5923.1.1.1.13";
    public static final String SAML_EPPN = "urn:oid:1.3.6.1.4.1.5923.1.1.1.6";
    public static final String SAML_EPTID = "urn:oid:1.3.6.1.4.1.5923.1.1.1.10";
    public static final String SAML_UID = "urn:oid:0.9.2342.19200300.100.1.1";
    public static final String SAML_UNIQUE_IDENTIFIER = "urn:oid:0.9.2342.19200300.100.1.44";

    public static final String REFEDS_MFA = "https://refeds.org/profile/mfa";
    public static final String PROMPT_LOGIN = "login";
    public static final String PROMPT_SELECT_ACCOUNT = "select_account";

    public static final Map<String, String> SAML_IDS = new HashMap<>();
    static {
        SAML_IDS.put("eppn", SAML_EPPN);
        SAML_IDS.put("epuid", SAML_EPUID);
        SAML_IDS.put("eptid", SAML_EPTID);
        SAML_IDS.put("uid", SAML_UID);
        SAML_IDS.put("uniqueIdentifier", SAML_UNIQUE_IDENTIFIER);
    }

}
