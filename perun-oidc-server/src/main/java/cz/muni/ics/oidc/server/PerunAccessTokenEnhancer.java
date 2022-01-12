package cz.muni.ics.oidc.server;

import static cz.muni.ics.oauth2.service.IntrospectionResultAssembler.SCOPE;
import static cz.muni.ics.oauth2.service.IntrospectionResultAssembler.SCOPE_SEPARATOR;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.jwt.signer.service.JWTSigningAndValidationService;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.OIDCTokenService;
import cz.muni.ics.openid.connect.service.UserInfoService;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

/**
 * Copy of ConnectTokenEnhancer.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class PerunAccessTokenEnhancer implements TokenEnhancer {

    private final ConfigurationPropertiesBean configBean;
    private final JWTSigningAndValidationService jwtService;
    private final ClientDetailsEntityService clientService;
    private final UserInfoService userInfoService;
    private final OIDCTokenService connectTokenService;

    private AccessTokenClaimsModifier accessTokenClaimsModifier;

    @Autowired
    public PerunAccessTokenEnhancer(ConfigurationPropertiesBean configBean,
                                    JWTSigningAndValidationService jwtService,
                                    ClientDetailsEntityService clientService,
                                    UserInfoService userInfoService,
                                    OIDCTokenService connectTokenService)
    {
        this.configBean = configBean;
        this.jwtService = jwtService;
        this.clientService = clientService;
        this.userInfoService = userInfoService;
        this.connectTokenService = connectTokenService;
    }

    public void setAccessTokenClaimsModifier(AccessTokenClaimsModifier accessTokenClaimsModifier) {
        this.accessTokenClaimsModifier = accessTokenClaimsModifier;
    }

    /**
     * Exact copy from ConnectTokenEnhancer with added hooks.
     */
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Date iat = new Date();
        OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) accessToken;
        OAuth2Request originalAuthRequest = authentication.getOAuth2Request();

        String clientId = originalAuthRequest.getClientId();
        ClientDetailsEntity client = clientService.loadClientByClientId(clientId);

        UserInfo userInfo = null;
        if (originalAuthRequest.getScope().contains(SystemScopeService.OPENID_SCOPE)
                && !authentication.isClientOnly()) {
            userInfo = userInfoService.get(authentication.getName(), clientId,
                    accessToken.getScope(),
                    ((OAuth2AccessTokenEntity) accessToken).getAuthenticationHolder().getUserAuth());
        }

        // create signed access token
        String sub = userInfo != null ? userInfo.getSub() : authentication.getName();
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .claim("azp", clientId)
                .issuer(configBean.getIssuer())
                .issueTime(iat)
                .expirationTime(token.getExpiration())
                .subject(sub)
                .claim(SCOPE, Joiner.on(SCOPE_SEPARATOR).join(accessToken.getScope()))
                .jwtID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it
        accessTokenClaimsHook(sub, builder, accessToken, authentication, userInfo);

        String audience = (String) authentication.getOAuth2Request().getExtensions().get("aud");
        if (!Strings.isNullOrEmpty(audience)) {
            builder.audience(Lists.newArrayList(audience));
        }

        JWTClaimsSet claims = builder.build();

        JWSAlgorithm signingAlg = jwtService.getDefaultSigningAlgorithm();
        JWSHeader header = new JWSHeader(signingAlg, JOSEObjectType.JWT, null, null, null, null, null, null, null, null,
                jwtService.getDefaultSignerKeyId(), true, null, null);
        SignedJWT signed = new SignedJWT(header, claims);

        jwtService.signJwt(signed);
        token.setJwtValue(signed);

        if (userInfo != null) {
            //needs access token
            JWT idToken = connectTokenService.createIdToken(client, originalAuthRequest, iat, userInfo.getSub(), token);
            // attach the id token to the parent access token
            token.setIdToken(idToken);
            if (log.isDebugEnabled()) log.debug("idToken: {}", idToken.serialize());
        } else {
            // can't create an id token if we can't find the user
            log.warn("Request for ID token when no user is present.");
        }

        this.logHook(token, authentication);
        return token;
    }

    private void logHook(OAuth2AccessTokenEntity token, OAuth2Authentication authentication) {
        //log request info from authentication
        Object principal = authentication.getPrincipal();
        String userId = principal instanceof User ? ((User) principal).getUsername() : principal.toString();
        OAuth2Request oAuth2Request = authentication.getOAuth2Request();
        log.info("userId: {}, clientId: {}, grantType: {}, redirectUri: {}, scopes: {}",
                userId, oAuth2Request.getClientId(), oAuth2Request.getGrantType(), oAuth2Request.getRedirectUri(), token.getScope());
        if (log.isDebugEnabled()) log.debug("access token: {}", token.getValue());
    }

    private void accessTokenClaimsHook(String sub, JWTClaimsSet.Builder builder, OAuth2AccessToken accessToken,
                                       OAuth2Authentication authentication, UserInfo userInfo)
    {
        if (accessTokenClaimsModifier != null) {
            accessTokenClaimsModifier.modifyClaims(sub, builder, accessToken, authentication, userInfo);
        }
    }

    @FunctionalInterface
    public interface AccessTokenClaimsModifier {
        void modifyClaims(String sub, JWTClaimsSet.Builder builder, OAuth2AccessToken accessToken,
                          OAuth2Authentication authentication, UserInfo userInfo);
    }

    public static class NoOpAccessTokenClaimsModifier implements AccessTokenClaimsModifier {
        @Override
        public void modifyClaims(String sub, JWTClaimsSet.Builder builder, OAuth2AccessToken accessToken,
                                 OAuth2Authentication authentication, UserInfo userInfo)
        {
            log.debug("no modification");
        }
    }

}
