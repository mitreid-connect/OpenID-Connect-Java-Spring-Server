package cz.muni.ics.oidc.server.userInfo;

import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oidc.exceptions.ConfigurationException;
import cz.muni.ics.oidc.server.claims.ClaimModifier;
import cz.muni.ics.oidc.server.claims.ClaimModifierInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.PerunCustomClaimDefinition;
import cz.muni.ics.oidc.server.claims.modifiers.NoOperationModifier;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class UserInfoUtils {

    public static final String CUSTOM_CLAIM = "custom.claim.";
    public static final String SOURCE = ".source";
    public static final String CLASS = ".class";
    public static final String NAMES = ".names";
    public static final String MODIFIER = ".modifier";
    public static final String SCOPE = ".scope";

    public static List<PerunCustomClaimDefinition> loadCustomClaims(Collection<String> customClaimNames,
                                                                    Properties properties,
                                                                    PerunOidcConfig oidcConfig,
                                                                    JWTSigningAndValidationService jwtService)
            throws ConfigurationException
    {
        List<PerunCustomClaimDefinition> customClaims = new ArrayList<>();
        if (customClaimNames == null || customClaimNames.isEmpty()) {
            return customClaims;
        }
        for (String claimName : customClaimNames) {
            String propertyBase = UserInfoUtils.CUSTOM_CLAIM + claimName;
            //get scope
            String scopeProperty = propertyBase + UserInfoUtils.SCOPE;
            String scope = properties.getProperty(scopeProperty);
            if (scope == null) {
                log.error("property {} not found, skipping custom claim {}", scopeProperty, claimName);
                continue;
            }
            //get ClaimSource
            ClaimSource claimSource = UserInfoUtils.loadClaimSource(
                    properties, oidcConfig, jwtService, claimName, propertyBase);
            //optional claim value modifier
            List<ClaimModifier> claimModifiers = UserInfoUtils.loadClaimValueModifiers(
                    properties, claimName, propertyBase);
            //add claim definition
            customClaims.add(new PerunCustomClaimDefinition(scope, claimName, claimSource, claimModifiers));
        }
        return customClaims;
    }

    public static ClaimSource loadClaimSource(Properties properties,
                                              PerunOidcConfig perunOidcConfig,
                                              JWTSigningAndValidationService jwtService,
                                              String claimName,
                                              String propertyBase)
            throws ConfigurationException
    {
        String propertyPrefix = propertyBase + SOURCE;
        String sourceClass = properties.getProperty(propertyPrefix + CLASS);
        if (!StringUtils.hasText(sourceClass)) {
            log.error("{} - failed to initialized claim source: no class has ben configured", claimName);
            throw new ConfigurationException("No class configured for claim source");
        }

        log.trace("{} - loading ClaimSource class '{}'", claimName, sourceClass);

        try {
            Class<?> rawClazz = Class.forName(sourceClass);
            if (!ClaimSource.class.isAssignableFrom(rawClazz)) {
                log.error("{} - failed to initialized claim source: class '{}' does not extend ClaimSource",
                        claimName, sourceClass);
                throw new ConfigurationException("No instantiable class source configured for claim " + claimName);
            }
            @SuppressWarnings("unchecked") Class<ClaimSource> clazz = (Class<ClaimSource>) rawClazz;
            Constructor<ClaimSource> constructor = clazz.getConstructor(ClaimSourceInitContext.class);
            ClaimSourceInitContext ctx = new ClaimSourceInitContext(perunOidcConfig, jwtService, propertyPrefix,
                    properties, claimName);
            return constructor.newInstance(ctx);
        } catch (ClassNotFoundException e) {
            log.error("{} - failed to initialize claim source: class '{}' was not found", claimName, sourceClass);
            log.trace("{} - details:", claimName, e);
            throw new ConfigurationException("Error has occurred when instantiating claim source for claim " + claimName);
        } catch (NoSuchMethodException e) {
            log.error("{} - failed to initialize claim source: class '{}' does not have proper constructor",
                    claimName, sourceClass);
            log.trace("{} - details:", claimName, e);
            throw new ConfigurationException("Error has occurred when instantiating claim source for claim " + claimName);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("{} - failed to initialize claim source: class '{}' cannot be instantiated", claimName, sourceClass);
            log.trace("{} - details:", claimName, e);
            throw new ConfigurationException("Error has occurred when instantiating claim source for claim " + claimName);
        }
    }

    public static List<ClaimModifier> loadClaimValueModifiers(Properties properties,
                                                              String claimName,
                                                              String propertyBase)
            throws ConfigurationException
    {
        String propertyPrefix = propertyBase  + MODIFIER;
        String names = properties.getProperty(propertyPrefix + NAMES, "");
        String[] nameArr = names.split(",");
        List<ClaimModifier> modifiers = new ArrayList<>();
        if (nameArr.length > 0) {
            for (String name : nameArr) {
                modifiers.add(loadClaimValueModifier(properties, claimName, propertyPrefix + '.' + name, name));
            }
        }
        return modifiers;
    }

    private static ClaimModifier loadClaimValueModifier(Properties properties,
                                                        String claimName,
                                                        String propertyPrefix,
                                                        String modifierName)
            throws ConfigurationException
    {
        String modifierClass = properties.getProperty(propertyPrefix + CLASS, NoOperationModifier.class.getName());
        if (!StringUtils.hasText(modifierClass)) {
            log.debug("{}:{} - no class has ben configured for claim value modifier, use noop modifier",
                    claimName, modifierName);
            modifierClass = NoOperationModifier.class.getName();
        }
        log.trace("{}:{} - loading ClaimModifier class '{}'", claimName, modifierName, modifierClass);

        try {
            Class<?> rawClazz = Class.forName(modifierClass);
            if (!ClaimModifier.class.isAssignableFrom(rawClazz)) {
                log.error("{}:{} - failed to initialized claim modifier: class '{}' does not extend ClaimModifier",
                        claimName, modifierName, modifierClass);
                throw new ConfigurationException("No instantiable class modifier configured for claim " + claimName);
            }
            @SuppressWarnings("unchecked") Class<ClaimModifier> clazz = (Class<ClaimModifier>) rawClazz;
            Constructor<ClaimModifier> constructor = clazz.getConstructor(ClaimModifierInitContext.class);
            ClaimModifierInitContext ctx = new ClaimModifierInitContext(
                    propertyPrefix, properties, claimName, modifierName);
            return constructor.newInstance(ctx);
        } catch (ClassNotFoundException e) {
            log.error("{}:{} - failed to initialize claim modifier: class '{}' was not found",
                    claimName, modifierName, modifierClass);
            log.trace("{}:{} - details:", claimName, modifierName, e);
            throw new ConfigurationException("Error has occurred when instantiating claim modifier '"
                    + modifierName + "' of claim '" + claimName + '\'');
        } catch (NoSuchMethodException e) {
            log.error("{}:{} - failed to initialize claim modifier: class '{}' does not have proper constructor",
                    claimName, modifierName, modifierClass);
            log.trace("{}:{} - details:", claimName, e);
            throw new ConfigurationException("Error has occurred when instantiating claim modifier '"
                    + modifierName + "' of claim '" + claimName + '\'');
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("{}:{} - failed to initialize claim modifier: class '{}' cannot be instantiated",
                    claimName, modifierName, modifierClass);
            log.trace("{}:{} - details:", claimName, e);
            throw new ConfigurationException("Error has occurred when instantiating claim modifier '"
                    + modifierName + "' of claim '" + claimName + '\'');
        }
    }

}