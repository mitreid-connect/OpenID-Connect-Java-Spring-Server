package cz.muni.ics.oauth2.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AuthenticatingAuthority;
import org.opensaml.saml2.core.AuthnStatement;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SamlAuthenticationDetails {

    public static final String LOCAL_ENTITY_ID = "localEntityId";
    public static final String REMOTE_ENTITY_ID = "remoteEntityId";
    public static final String ATTRIBUTES = "attributes";
    public static final String AUTHN_STATEMENTS = "authnStatements";
    public static final String AUTHN_INSTANT = "authnInstant";
    public static final String AUTHN_CONTEXT_CLASS_REF = "authnContextClassRef";
    public static final String AUTHENTICATING_AUTHORITIES = "authenticatingAuthorities";

    private String remoteEntityID;
    private String localEntityID;
    private Map<String, String[]> attributes;
    private List<AuthenticationStatement> authnStatements;

    public SamlAuthenticationDetails(SAMLCredential samlCredential) {
        this.remoteEntityID = samlCredential.getRemoteEntityID();
        this.localEntityID = samlCredential.getLocalEntityID();
        this.attributes = processAttributes(samlCredential);
        this.authnStatements = processAuthnStatement(samlCredential);
    }

    private List<AuthenticationStatement> processAuthnStatement(SAMLCredential samlCredential) {
        List<AuthenticationStatement> authenticationStatements = new ArrayList<>();
        List<AuthnStatement> samlAuthnStatements = samlCredential.getAuthenticationAssertion().getAuthnStatements();
        if (samlAuthnStatements != null) {
            for (AuthnStatement as : samlAuthnStatements) {
                if (as == null || as.getAuthnContext() == null) {
                    continue;
                }
                List<String> authenticatingAuthorities = new ArrayList<>();
                List<AuthenticatingAuthority> authnAuthorities = as.getAuthnContext()
                        .getAuthenticatingAuthorities();
                if (authnAuthorities != null) {
                    for (AuthenticatingAuthority aa : authnAuthorities) {
                        if (aa != null && StringUtils.hasText(aa.getURI())) {
                            authenticatingAuthorities.add(aa.getURI());
                        }
                    }
                }

                String authnContextClassRef = null;
                if (as.getAuthnContext().getAuthnContextClassRef() != null &&
                        StringUtils.hasText(as.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()))
                {
                    authnContextClassRef = as.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
                }
                String authnInstant = as.getAuthnInstant() != null ? as.getAuthnInstant().toString() : "";
                authenticationStatements.add(new AuthenticationStatement(authenticatingAuthorities, authnContextClassRef, authnInstant));
            }
        }
        return authenticationStatements;
    }

    private Map<String, String[]> processAttributes(SAMLCredential samlCredential) {
        Map<String, String[]> attributes = new HashMap<>();
        List<Attribute> samlAttributes = samlCredential.getAttributes();
        if (samlAttributes != null) {
            for (Attribute a: samlAttributes) {
                if (a == null) {
                    continue;
                }
                String name = a.getName();
                String[] val = samlCredential.getAttributeAsStringArray(name);
                attributes.put(name, val);
            }
        }
        return attributes;
    }

    public static SamlAuthenticationDetails deserialize(String strJson) {
        if (!StringUtils.hasText(strJson)) {
            return null;
        }
        JsonObject json = (JsonObject) JsonParser.parseString(strJson);
        SamlAuthenticationDetails details = new SamlAuthenticationDetails();
        details.setRemoteEntityID(getStringOrNull(json.get(REMOTE_ENTITY_ID)));
        details.setLocalEntityID(getStringOrNull(json.get(LOCAL_ENTITY_ID)));

        Map<String, String[]> attributes = new HashMap<>();
        JsonObject attrs = json.getAsJsonObject(ATTRIBUTES);
        for (Map.Entry<String, JsonElement> e: attrs.entrySet()) {
            JsonArray elements =  e.getValue().getAsJsonArray();
            String[] val = new String[elements.size()];
            int i = 0;
            for (JsonElement element: elements) {
                val[i++] = getStringOrNull(element);
            }
            attributes.put(e.getKey(), val);
        }
        details.setAttributes(attributes);

        List<AuthenticationStatement> authnStatements = new ArrayList<>();
        JsonArray authStmts = json.getAsJsonArray(AUTHN_STATEMENTS);
        for (JsonElement e: authStmts) {
            JsonObject obj = e.getAsJsonObject();
            JsonArray authoritiesArr = obj.getAsJsonArray(AUTHENTICATING_AUTHORITIES);
            List<String> authorities = new ArrayList<>();
            for (JsonElement authority: authoritiesArr) {
                authorities.add(authority.getAsString());
            }
            String authnContextClassRef = getStringOrNull(obj.get(AUTHN_CONTEXT_CLASS_REF));
            String authnInstant = getStringOrNull(obj.get(AUTHN_INSTANT));
            authnStatements.add(new AuthenticationStatement(authorities, authnContextClassRef, authnInstant));
        }
        details.setAuthnStatements(authnStatements);
        return details;
    }

    public static String serialize(SamlAuthenticationDetails o) {
        if (o == null) {
            return null;
        }
        JsonObject object = new JsonObject();
        addStringOrNull(object, LOCAL_ENTITY_ID, o.getLocalEntityID());
        addStringOrNull(object, REMOTE_ENTITY_ID, o.getRemoteEntityID());
        JsonObject attrs = new JsonObject();
        for (Map.Entry<String, String[]> e: o.getAttributes().entrySet()) {
            JsonArray val = new JsonArray();
            for (String v: e.getValue()) {
                if (v == null) {
                    continue;
                }
                val.add(v);
            }
            attrs.add(e.getKey(), val);
        }
        object.add(ATTRIBUTES, attrs);
        JsonArray authnStatements = new JsonArray();

        for (AuthenticationStatement as: o.getAuthnStatements()) {
            JsonObject asJson = new JsonObject();
            addStringOrNull(asJson, AUTHN_CONTEXT_CLASS_REF, as.getAuthnContextClassRef());
            addStringOrNull(asJson, AUTHN_INSTANT, as.getAuthnInstant());
            JsonArray authorities = new JsonArray();
            for (String authAuthority: as.getAuthenticatingAuthorities()) {
                if (authAuthority == null) {
                    continue;
                }
                authorities.add(authAuthority);
            }
            asJson.add(AUTHENTICATING_AUTHORITIES, authorities);
            authnStatements.add(asJson);
        }
        object.add(AUTHN_STATEMENTS, authnStatements);
        return object.toString();
    }

    private static void addStringOrNull(JsonObject target, String key, String value) {
        if (target == null || !StringUtils.hasText(key)) {
            return;
        }
        if (value == null || "null".equalsIgnoreCase(value)) {
            target.add(key, new JsonNull());
        } else {
            target.addProperty(key, value);
        }
    }

    private static String getStringOrNull(JsonElement jsonElement) {
        if (jsonElement != null && jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsString();
        } else {
            return null;
        }
    }
}
