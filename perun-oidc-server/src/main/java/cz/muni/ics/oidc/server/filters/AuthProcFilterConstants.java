package cz.muni.ics.oidc.server.filters;

import java.util.Map;

/**
 * Class containing common constants used by Perun request filters.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public interface AuthProcFilterConstants {

    String AUTHORIZE_REQ_PATTERN = "/auth/authorize";
    String DEVICE_APPROVE_REQ_PATTERN = "/auth/device/authorize";

    String PARAM_CLIENT_ID = "client_id";
    String PARAM_SCOPE = "scope";
    String PARAM_MESSAGE = "message";
    String PARAM_HEADER = "header";
    String PARAM_TARGET = "target";
    String PARAM_FORCE_AUTHN = "forceAuthn";
    String PARAM_PROMPT = "prompt";
    String PARAM_REASON = "reason";
    String PARAM_ACCEPTED = "accepted";
    String PARAM_ACR_VALUES = "acr_values";
    String PARAM_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
    String PARAM_STATE = "state";
    String CLIENT_ID_PREFIX = "urn:cesnet:proxyidp:client_id:";
    String AARC_IDP_HINT = "aarc_idp_hint";

    String IDP_ENTITY_ID_PREFIX = "urn:cesnet:proxyidp:idpentityid:";
    String FILTER_PREFIX = "urn:cesnet:proxyidp:filter:";
    String EFILTER_PREFIX = "urn:cesnet:proxyidp:efilter:";

    String SAML_EPUID = "urn:oid:1.3.6.1.4.1.5923.1.1.1.13";
    String SAML_EPPN = "urn:oid:1.3.6.1.4.1.5923.1.1.1.6";
    String SAML_EPTID = "urn:oid:1.3.6.1.4.1.5923.1.1.1.10";
    String SAML_UID = "urn:oid:0.9.2342.19200300.100.1.1";
    String SAML_UNIQUE_IDENTIFIER = "urn:oid:0.9.2342.19200300.100.1.44";
    String SAML_PERUN_USERID_IDENTIFIER = "urn:cesnet:proxyidp:attribute:perunUserId";

    String REFEDS_MFA = "https://refeds.org/profile/mfa";
    String PROMPT_LOGIN = "login";
    String PROMPT_SELECT_ACCOUNT = "select_account";

    Map<String, String> SAML_IDS = Map.of(
        "eppn", SAML_EPPN,
        "epuid", SAML_EPUID,
        "eptid", SAML_EPTID,
        "uid", SAML_UID,
        "uniqueIdentifier", SAML_UNIQUE_IDENTIFIER,
        "perunUserId", SAML_PERUN_USERID_IDENTIFIER
    );

}
