package cz.muni.ics.oidc.saml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

@Slf4j
public class SamlProperties implements InitializingBean {

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

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getKeystoreDefaultKey() {
        return keystoreDefaultKey;
    }

    public void setKeystoreDefaultKey(String keystoreDefaultKey) {
        this.keystoreDefaultKey = keystoreDefaultKey;
    }

    public String getKeystoreDefaultKeyPassword() {
        return keystoreDefaultKeyPassword;
    }

    public void setKeystoreDefaultKeyPassword(String keystoreDefaultKeyPassword) {
        this.keystoreDefaultKeyPassword = keystoreDefaultKeyPassword;
    }

    public String getDefaultIdpEntityId() {
        return defaultIdpEntityId;
    }

    public void setDefaultIdpEntityId(String defaultIdpEntityId) {
        this.defaultIdpEntityId = defaultIdpEntityId;
    }

    public String getIdpMetadataFile() {
        return idpMetadataFile;
    }

    public void setIdpMetadataFile(String idpMetadataFile) {
        this.idpMetadataFile = idpMetadataFile;
    }

    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public void setIdpMetadataUrl(String idpMetadataUrl) {
        this.idpMetadataUrl = idpMetadataUrl;
    }

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
    }

    public String[] getAcrReservedPrefixes() {
        return acrReservedPrefixes;
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

    public String[] getAcrsToBeAdded() {
        return acrsToBeAdded;
    }

    public void setAcrsToBeAdded(String[] acrsToBeAdded) {
        this.acrsToBeAdded = acrsToBeAdded;
    }

    public String getUserIdentifierAttribute() {
        return userIdentifierAttribute;
    }

    public void setUserIdentifierAttribute(String userIdentifierAttribute) {
        this.userIdentifierAttribute = userIdentifierAttribute;
    }
}
