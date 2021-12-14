package cz.muni.ics.oidc.server.claims;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class ClaimInitContext {

    private final String propertyPrefix;

    private final Properties properties;

    @Getter
    private final String claimName;

    public ClaimInitContext(String propertyPrefix, Properties properties, String claimName) {
        this.propertyPrefix = propertyPrefix;
        this.properties = properties;
        this.claimName = claimName;
    }

    public String getProperty(String suffix, String defaultValue) {
        return properties.getProperty(propertyPrefix + '.' + suffix, defaultValue);
    }

    public Long getLongProperty(String suffix, Long defaultValue) {
        String propKey = propertyPrefix + '.' + suffix;
        String prop = properties.getProperty(propertyPrefix + "." + suffix);
        try {
            return Long.parseLong(prop);
        } catch (NumberFormatException e) {
            log.warn("Could not parse value '{}' for property '{}' as Long", prop, propKey);
        }
        return defaultValue;
    }
}
