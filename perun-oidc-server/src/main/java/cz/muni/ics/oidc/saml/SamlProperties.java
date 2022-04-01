package cz.muni.ics.oidc.saml;

import cz.muni.ics.oidc.exceptions.ConfigurationException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SamlProperties implements InitializingBean {

    public static final String LOOKUP_ORIGINAL_AUTH = "original_auth";
    public static final String LOOKUP_PERUN_USER_ID = "perun_user_id";
    public static final String LOOKUP_STATIC_EXT_SOURCE = "static_ext_source";

    private String entityID;
    private String keystoreLocation;
    private String keystorePassword;
    private String keystoreDefaultKey;
    private String keystoreDefaultKeyPassword;
    private String defaultIdpEntityId;
    private String idpMetadataFile;
    private String idpMetadataUrl;
    private String[] acrReservedPrefixes;
    private String[] acrsToBeAdded;
    private String userIdentifierAttribute;
    private String userLookupMode;
    private String staticUserExtSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(idpMetadataUrl) && !StringUtils.hasText(idpMetadataFile)) {
            throw new IllegalStateException("No URL nor file provided for metadata");
        }
        if (StringUtils.hasText(idpMetadataUrl)) {
            try {
                new URL(idpMetadataUrl);
                return;
            } catch (MalformedURLException e) {
                log.warn("'{}' is not a valid URL", idpMetadataUrl);
            }
        }
        File f = new File(idpMetadataFile);
        if (!f.exists()) {
            throw new IllegalStateException("File '" + idpMetadataFile + "' does not exist");
        }

        if (!StringUtils.hasText(userIdentifierAttribute)) {
            throw new ConfigurationException("No user identifier attribute has been configured");
        }

        switch (userLookupMode) {
            case LOOKUP_STATIC_EXT_SOURCE: {
                if (!StringUtils.hasText(staticUserExtSource)) {
                    throw new ConfigurationException(
                            "No static ext source has been configured, while static ext source lookup has been set");
                }
            } break;
            case LOOKUP_ORIGINAL_AUTH:
            case LOOKUP_PERUN_USER_ID: {
                // nothing that needs to be checked
            } break;
            default: {
                throw new ConfigurationException(
                        "Invalid configuration - unknown user lookup method, check your config. Allowed values are: "
                        + LOOKUP_ORIGINAL_AUTH + ", " + LOOKUP_PERUN_USER_ID + ", " + LOOKUP_STATIC_EXT_SOURCE
                );
            }
        }
    }

    public void setAcrReservedPrefixes(String[] acrReservedPrefixes) {
        if (acrReservedPrefixes == null) {
            this.acrReservedPrefixes = new String[] {};
        } else {
            List<String> nonNull = new ArrayList<>();
            for (String prefix: acrReservedPrefixes) {
                if (StringUtils.hasText(prefix)) {
                    nonNull.add(prefix);
                }
            }

            this.acrReservedPrefixes = nonNull.toArray(new String[0]);
        }
    }

}
