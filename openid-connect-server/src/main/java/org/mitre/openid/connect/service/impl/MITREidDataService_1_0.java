/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.mitre.openid.connect.service.impl;

import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.mitre.jose.JWEAlgorithmEmbed;
import org.mitre.jose.JWEEncryptionMethodEmbed;
import org.mitre.jose.JWSAlgorithmEmbed;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.ClientDetailsEntity.AppType;
import org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod;
import org.mitre.oauth2.model.ClientDetailsEntity.SubjectType;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.oauth2.repository.SystemScopeRepository;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.BlacklistedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.repository.BlacklistedSiteRepository;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.mitre.openid.connect.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Service;
/** 
 *
 * Data service to import MITREid 1.0 configuration.
 *
 * @author jricher
 * @author arielak
 */
@Service
public class MITREidDataService_1_0 extends MITREidDataService_1_X {

    private final static Logger logger = LoggerFactory.getLogger(MITREidDataService_1_0.class);
    @Autowired
    private OAuth2ClientRepository clientRepository;
    @Autowired
    private ApprovedSiteRepository approvedSiteRepository;
    @Autowired
    private WhitelistedSiteRepository wlSiteRepository;
    @Autowired
    private BlacklistedSiteRepository blSiteRepository;
    @Autowired
    private AuthenticationHolderRepository authHolderRepository;
    @Autowired
    private OAuth2TokenRepository tokenRepository;
    @Autowired
    private SystemScopeRepository sysScopeRepository;
    /* (non-Javadoc)
     * @see org.mitre.openid.connect.service.MITREidDataService#export(com.google.gson.stream.JsonWriter)
     */

    @Override
    public void exportData(JsonWriter writer) throws IOException {
        throw new UnsupportedOperationException("Can not export 1.0 format from this version.");
    }

    /* (non-Javadoc)
     * @see org.mitre.openid.connect.service.MITREidDataService#importData(com.google.gson.stream.JsonReader)
     */
    @Override
    public void importData(JsonReader reader) throws IOException {

        logger.info("Reading configuration for 1.0");

        // this *HAS* to start as an object
        reader.beginObject();

        while (reader.hasNext()) {
            JsonToken tok = reader.peek();
            switch (tok) {
                case NAME:
                    String name = reader.nextName();
                    // find out which member it is
                    if (name.equals(CLIENTS)) {
                        readClients(reader);
                    } else if (name.equals(GRANTS)) {
                        readGrants(reader);
                    } else if (name.equals(WHITELISTEDSITES)) {
                        readWhitelistedSites(reader);
                    } else if (name.equals(BLACKLISTEDSITES)) {
                        readBlacklistedSites(reader);
                    } else if (name.equals(AUTHENTICATIONHOLDERS)) {
                        readAuthenticationHolders(reader);
                    } else if (name.equals(ACCESSTOKENS)) {
                        readAccessTokens(reader);
                    } else if (name.equals(REFRESHTOKENS)) {
                        readRefreshTokens(reader);
                    } else if (name.equals(SYSTEMSCOPES)) {
                        readSystemScopes(reader);
                    } else {
                        // unknown token, skip it
                        reader.skipValue();
                    }
                    break;
                case END_OBJECT:
                    // the object ended, we're done here
                    reader.endObject();
                    continue;
            }
        }
        fixObjectReferences();
    }
    private Map<Long, String> refreshTokenToClientRefs = new HashMap<Long, String>();
    private Map<Long, Long> refreshTokenToAuthHolderRefs = new HashMap<Long, Long>();
    private Map<Long, Long> refreshTokenOldToNewIdMap = new HashMap<Long, Long>();

    /**
     * @param reader
     * @throws IOException
     */
    /**
     * @param reader
     * @throws IOException
     */
    private void readRefreshTokens(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            OAuth2RefreshTokenEntity token = new OAuth2RefreshTokenEntity();
            reader.beginObject();
            Long currentId = null;
            String clientId = null;
            Long authHolderId = null;
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        } else if (name.equals("id")) {
                            currentId = reader.nextLong();
                        } else if (name.equals("expiration")) {
                            Date date = DateUtil.utcToDate(reader.nextString());
                            token.setExpiration(date);
                        } else if (name.equals("value")) {
                            String value = reader.nextString();
                            try {
                                token.setValue(value);
                            } catch (ParseException ex) {
                                logger.error("Unable to set refresh token value to {}", value, ex);
                            }
                        } else if (name.equals("clientId")) {
                            clientId = reader.nextString();
                        } else if (name.equals("authenticationHolderId")) {
                            authHolderId = reader.nextLong();
                        } else {
                            logger.debug("Found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            Long newId = tokenRepository.saveRefreshToken(token).getId();
            refreshTokenToClientRefs.put(currentId, clientId);
            refreshTokenToAuthHolderRefs.put(currentId, authHolderId);
            refreshTokenOldToNewIdMap.put(currentId, newId);
            logger.debug("Read refresh token {}", currentId);
        }
        reader.endArray();
        logger.info("Done reading refresh tokens");
    }
    private Map<Long, String> accessTokenToClientRefs = new HashMap<Long, String>();
    private Map<Long, Long> accessTokenToAuthHolderRefs = new HashMap<Long, Long>();
    private Map<Long, Long> accessTokenToRefreshTokenRefs = new HashMap<Long, Long>();
    private Map<Long, Long> accessTokenToIdTokenRefs = new HashMap<Long, Long>();
    private Map<Long, Long> accessTokenOldToNewIdMap = new HashMap<Long, Long>();

    /**
     * @param reader
     * @throws IOException
     */
    /**
     * @param reader
     * @throws IOException
     */
    private void readAccessTokens(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            OAuth2AccessTokenEntity token = new OAuth2AccessTokenEntity();
            reader.beginObject();
            Long currentId = null;
            String clientId = null;
            Long authHolderId = null;
            Long refreshTokenId = null;
            Long idTokenId = null;
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        } else if (name.equals("id")) {
                            currentId = reader.nextLong();
                        } else if (name.equals("expiration")) {
                            Date date = DateUtil.utcToDate(reader.nextString());
                            token.setExpiration(date);
                        } else if (name.equals("value")) {
                            String value = reader.nextString();
                            try {
                                token.setValue(value);
                            } catch (ParseException ex) {
                                logger.error("Unable to set refresh token value to {}", value, ex);
                            }
                        } else if (name.equals("clientId")) {
                            clientId = reader.nextString();
                        } else if (name.equals("authenticationHolderId")) {
                            authHolderId = reader.nextLong();
                        } else if (name.equals("refreshTokenId")) {
                            refreshTokenId = reader.nextLong();
                        } else if (name.equals("idTokenId")) {
                            idTokenId = reader.nextLong();
                        } else if (name.equals("scope")) {
                            Set<String> scope = readSet(reader);
                            token.setScope(scope);
                        } else if (name.equals("type")) {
                            token.setTokenType(reader.nextString());
                        } else {
                            logger.debug("Found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            Long newId = tokenRepository.saveAccessToken(token).getId();
            accessTokenToClientRefs.put(currentId, clientId);
            accessTokenToAuthHolderRefs.put(currentId, authHolderId);
            if (refreshTokenId != null) {
                accessTokenToRefreshTokenRefs.put(currentId, refreshTokenId);
            }
            if (idTokenId != null) {
                accessTokenToIdTokenRefs.put(currentId, idTokenId);
            }
            accessTokenOldToNewIdMap.put(currentId, newId);
            logger.debug("Read access token {}", currentId);
        }
        reader.endArray();
        logger.info("Done reading access tokens");
    }
    private Map<Long, Long> authHolderOldToNewIdMap = new HashMap<Long, Long>();

    /**
     * @param reader
     * @throws IOException
     */
    private void readAuthenticationHolders(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            AuthenticationHolderEntity ahe = new AuthenticationHolderEntity();
            reader.beginObject();
            Long currentId = null;
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        } else if (name.equals("id")) {
                            currentId = reader.nextLong();
                        } else if (name.equals("ownerId")) {
                            //not needed
                            reader.skipValue();
                        } else if (name.equals("authentication")) {
                            OAuth2Request clientAuthorization = null;
                            Authentication userAuthentication = null;
                            reader.beginObject();
                            while (reader.hasNext()) {
                                switch (reader.peek()) {
                                    case END_OBJECT:
                                        continue;
                                    case NAME:
                                        String subName = reader.nextName();
                                        if (subName.equals("clientAuthorization")) {
                                            clientAuthorization = readAuthorizationRequest(reader);
                                        } else if (subName.equals("userAuthentication")) {
                                        	if (reader.peek() == JsonToken.NULL) {
                                        		reader.skipValue();
                                        	} else {
	                                            String authString = reader.nextString();
	                                            userAuthentication = base64UrlDecodeObject(authString, Authentication.class);
                                        	}
                                        } else {
                                            logger.debug("Found unexpected entry");
                                            reader.skipValue();
                                        }
                                        break;
                                    default:
                                        logger.debug("Found unexpected entry");
                                        reader.skipValue();
                                        continue;
                                }
                            }
                            reader.endObject();
                            OAuth2Authentication auth = new OAuth2Authentication(clientAuthorization, userAuthentication);
                            ahe.setAuthentication(auth);
                        } else {
                            logger.debug("Found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            Long newId = authHolderRepository.save(ahe).getId();
            authHolderOldToNewIdMap.put(currentId, newId);
            logger.debug("Read authentication holder {}", currentId);
        }
        reader.endArray();
        logger.info("Done reading authentication holders");
    }

    //used by readAuthenticationHolders
    private OAuth2Request readAuthorizationRequest(JsonReader reader) throws IOException {
        Set<String> scope = new LinkedHashSet<String>();
        Set<String> resourceIds = new HashSet<String>();
        boolean approved = false;
        Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        Map<String, String> authorizationParameters = new HashMap<String, String>();
        Set<String> responseTypes = new HashSet<String>();
        String redirectUri = null;
        String clientId = null;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.peek()) {
                case END_OBJECT:
                    continue;
                case NAME:
                    String name = reader.nextName();
                    if (reader.peek() == JsonToken.NULL) {
                        reader.skipValue();
                    } else if (name.equals("authorizationParameters")) {
                        authorizationParameters = readMap(reader);
                    } else if (name.equals("approvalParameters")) {
                        reader.skipValue();
                    } else if (name.equals("clientId")) {
                        clientId = reader.nextString();
                    } else if (name.equals("scope")) {
                        scope = readSet(reader);
                    } else if (name.equals("resourceIds")) {
                        resourceIds = readSet(reader);
                    } else if (name.equals("authorities")) {
                        Set<String> authorityStrs = readSet(reader);
                        authorities = new HashSet<GrantedAuthority>();
                        for (String s : authorityStrs) {
                            GrantedAuthority ga = new SimpleGrantedAuthority(s);
                            authorities.add(ga);
                        }
                    } else if (name.equals("approved")) {
                        approved = reader.nextBoolean();
                    } else if (name.equals("denied")) {
                        if (approved == false) {
                            approved = !reader.nextBoolean();
                        }
                    } else if (name.equals("redirectUri")) {
                        redirectUri = reader.nextString();
                    } else if (name.equals("responseTypes")) {
                        responseTypes = readSet(reader);
                    } else {
                        reader.skipValue();
                    }
                    break;
                default:
                    logger.debug("Found unexpected entry");
                    reader.skipValue();
                    continue;
            }
        }
        reader.endObject();
        return new OAuth2Request(authorizationParameters, clientId, authorities, approved, scope, resourceIds, redirectUri, responseTypes, null);
    }
    Map<Long, Long> grantOldToNewIdMap = new HashMap<Long, Long>();
    Map<Long, Long> grantToWhitelistedSiteRefs = new HashMap<Long, Long>();
    Map<Long, Set<Long>> grantToAccessTokensRefs = new HashMap<Long, Set<Long>>();

    /**
     * @param reader
     * @throws IOException
     */
    private void readGrants(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            ApprovedSite site = new ApprovedSite();
            Long currentId = null;
            Long whitelistedSiteId = null;
            Set<Long> tokenIds = null;
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        } else if (name.equals("id")) {
                            currentId = reader.nextLong();
                        } else if (name.equals("accessDate")) {
                            Date date = DateUtil.utcToDate(reader.nextString());
                            site.setAccessDate(date);
                        } else if (name.equals("clientId")) {
                            site.setClientId(reader.nextString());
                        } else if (name.equals("creationDate")) {
                            Date date = DateUtil.utcToDate(reader.nextString());
                            site.setCreationDate(date);
                        } else if (name.equals("timeoutDate")) {
                            Date date = DateUtil.utcToDate(reader.nextString());
                            site.setTimeoutDate(date);
                        } else if (name.equals("userId")) {
                            site.setUserId(reader.nextString());
                        } else if (name.equals("allowedScopes")) {
                            Set<String> allowedScopes = readSet(reader);
                            site.setAllowedScopes(allowedScopes);
                        } else if (name.equals("whitelistedSiteId")) {
                            whitelistedSiteId = reader.nextLong();
                        } else if (name.equals("approvedAccessTokens")) {
                            tokenIds = readSet(reader);
                        } else {
                            logger.debug("Found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            Long newId = approvedSiteRepository.save(site).getId();
            grantOldToNewIdMap.put(currentId, newId);
            if (whitelistedSiteId != null) {
                grantToWhitelistedSiteRefs.put(currentId, whitelistedSiteId);
            }
            if (tokenIds != null) {
                grantToAccessTokensRefs.put(currentId, tokenIds);
            }
            logger.debug("Read grant {}", currentId);
        }
        reader.endArray();
        logger.info("Done reading grants");
    }
    Map<Long, Long> whitelistedSiteOldToNewIdMap = new HashMap<Long, Long>();

    /**
     * @param reader
     * @throws IOException
     */
    private void readWhitelistedSites(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            WhitelistedSite wlSite = new WhitelistedSite();
            Long currentId = null;
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (name.equals("id")) {
                            currentId = reader.nextLong();
                        } else if (name.equals("clientId")) {
                            wlSite.setClientId(reader.nextString());
                        } else if (name.equals("creatorUserId")) {
                            wlSite.setCreatorUserId(reader.nextString());
                        } else if (name.equals("allowedScopes")) {
                            Set<String> allowedScopes = readSet(reader);
                            wlSite.setAllowedScopes(allowedScopes);
                        } else {
                            logger.debug("Found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            Long newId = wlSiteRepository.save(wlSite).getId();
            whitelistedSiteOldToNewIdMap.put(currentId, newId);
        }
        reader.endArray();
        logger.info("Done reading whitelisted sites");
    }

    /**
     * @param reader
     * @throws IOException
     */
    private void readBlacklistedSites(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            BlacklistedSite blSite = new BlacklistedSite();
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (name.equals("id")) {
                            reader.skipValue();
                        } else if (name.equals("uri")) {
                            blSite.setUri(reader.nextString());
                        } else {
                            logger.debug("Found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            blSiteRepository.save(blSite);
        }
        reader.endArray();
        logger.info("Done reading blacklisted sites");
    }

    /**
     * @param reader
     * @throws IOException
     */
    private void readClients(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            ClientDetailsEntity client = new ClientDetailsEntity();
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        } else if (name.equals("clientId")) {
                            client.setClientId(reader.nextString());
                        } else if (name.equals("resourceIds")) {
                            Set<String> resourceIds = readSet(reader);
                            client.setResourceIds(resourceIds);
                        } else if (name.equals("secret")) {
                            client.setClientSecret(reader.nextString());
                        } else if (name.equals("scope")) {
                            Set<String> scope = readSet(reader);
                            client.setScope(scope);
                        } else if (name.equals("authorities")) {
                            Set<String> authorityStrs = readSet(reader);
                            Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
                            for (String s : authorityStrs) {
                                GrantedAuthority ga = new SimpleGrantedAuthority(s);
                                authorities.add(ga);
                            }
                            client.setAuthorities(authorities);
                        } else if (name.equals("accessTokenValiditySeconds")) {
                            client.setAccessTokenValiditySeconds(reader.nextInt());
                        } else if (name.equals("refreshTokenValiditySeconds")) {
                            client.setRefreshTokenValiditySeconds(reader.nextInt());
                        } else if (name.equals("redirectUris")) {
                            Set<String> redirectUris = readSet(reader);
                            client.setRedirectUris(redirectUris);
                        } else if (name.equals("name")) {
                            client.setClientName(reader.nextString());
                        } else if (name.equals("uri")) {
                            client.setClientUri(reader.nextString());
                        } else if (name.equals("logoUri")) {
                            client.setLogoUri(reader.nextString());
                        } else if (name.equals("contacts")) {
                            Set<String> contacts = readSet(reader);
                            client.setContacts(contacts);
                        } else if (name.equals("tosUri")) {
                            client.setTosUri(reader.nextString());
                        } else if (name.equals("tokenEndpointAuthMethod")) {
                            AuthMethod am = AuthMethod.getByValue(reader.nextString());
                            client.setTokenEndpointAuthMethod(am);
                        } else if (name.equals("grantTypes")) {
                            Set<String> grantTypes = readSet(reader);
                            client.setGrantTypes(grantTypes);
                        } else if (name.equals("responseTypes")) {
                            Set<String> responseTypes = readSet(reader);
                            client.setResponseTypes(responseTypes);
                        } else if (name.equals("policyUri")) {
                            client.setPolicyUri(reader.nextString());
                        } else if (name.equals("applicationType")) {
                            AppType appType = AppType.getByValue(reader.nextString());
                            client.setApplicationType(appType);
                        } else if (name.equals("sectorIdentifierUri")) {
                            client.setSectorIdentifierUri(reader.nextString());
                        } else if (name.equals("subjectType")) {
                            SubjectType st = SubjectType.getByValue(reader.nextString());
                            client.setSubjectType(st);
                        } else if (name.equals("requestObjectSigningAlg")) {
                            JWSAlgorithmEmbed alg = JWSAlgorithmEmbed.getForAlgorithmName(reader.nextString());
                            client.setRequestObjectSigningAlgEmbed(alg);
                        } else if (name.equals("userInfoEncryptedResponseAlg")) {
                            JWEAlgorithmEmbed alg = JWEAlgorithmEmbed.getForAlgorithmName(reader.nextString());
                            client.setUserInfoEncryptedResponseAlgEmbed(alg);
                        } else if (name.equals("userInfoEncryptedResponseEnc")) {
                            JWEEncryptionMethodEmbed alg = JWEEncryptionMethodEmbed.getForAlgorithmName(reader.nextString());
                            client.setUserInfoEncryptedResponseEncEmbed(alg);
                        } else if (name.equals("userInfoSignedResponseAlg")) {
                            JWSAlgorithmEmbed alg = JWSAlgorithmEmbed.getForAlgorithmName(reader.nextString());
                            client.setUserInfoSignedResponseAlgEmbed(alg);
                        } else if (name.equals("defaultMaxAge")) {
                            client.setDefaultMaxAge(reader.nextInt());
                        } else if (name.equals("requireAuthTime")) {
                            client.setRequireAuthTime(reader.nextBoolean());
                        } else if (name.equals("defaultACRValues")) {
                            Set<String> defaultACRvalues = readSet(reader);
                            client.setDefaultACRvalues(defaultACRvalues);
                        } else if (name.equals("initiateLoginUri")) {
                            client.setInitiateLoginUri(reader.nextString());
                        } else if (name.equals("postLogoutRedirectUri")) {
                            HashSet<String> postLogoutUris = Sets.newHashSet(reader.nextString());
							client.setPostLogoutRedirectUris(postLogoutUris);
                        } else if (name.equals("requestUris")) {
                            Set<String> requestUris = readSet(reader);
                            client.setRequestUris(requestUris);
                        } else if (name.equals("description")) {
                            client.setClientDescription(reader.nextString());
                        } else if (name.equals("allowIntrospection")) {
                            client.setAllowIntrospection(reader.nextBoolean());
                        } else if (name.equals("reuseRefreshToken")) {
                            client.setReuseRefreshToken(reader.nextBoolean());
                        } else if (name.equals("dynamicallyRegistered")) {
                            client.setDynamicallyRegistered(reader.nextBoolean());
                        } else {
                            logger.debug("Found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            clientRepository.saveClient(client);
        }
        reader.endArray();
        logger.info("Done reading clients");
    }

    /**
     * Read the list of system scopes from the reader and insert them into the
     * scope repository.
     *
     * @param reader
     * @throws IOException
     */
    private void readSystemScopes(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            SystemScope scope = new SystemScope();
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.peek()) {
                    case END_OBJECT:
                        continue;
                    case NAME:
                        String name = reader.nextName();
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        } else if (name.equals("value")) {
                            scope.setValue(reader.nextString());
                        } else if (name.equals("description")) {
                            scope.setDescription(reader.nextString());
                        } else if (name.equals("allowDynReg")) {
                            scope.setAllowDynReg(reader.nextBoolean());
                        } else if (name.equals("defaultScope")) {
                            scope.setDefaultScope(reader.nextBoolean());
                        } else if (name.equals("icon")) {
                            scope.setIcon(reader.nextString());
                        } else {
                            logger.debug("found unexpected entry");
                            reader.skipValue();
                        }
                        break;
                    default:
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                        continue;
                }
            }
            reader.endObject();
            sysScopeRepository.save(scope);
        }
        reader.endArray();
        logger.info("Done reading system scopes");
    }

    private void fixObjectReferences() {
        for (Long oldRefreshTokenId : refreshTokenToClientRefs.keySet()) {
            String clientRef = refreshTokenToClientRefs.get(oldRefreshTokenId);
            ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
            Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
            OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
            refreshToken.setClient(client);
            tokenRepository.saveRefreshToken(refreshToken);
        }
        refreshTokenToClientRefs.clear();
        for (Long oldRefreshTokenId : refreshTokenToAuthHolderRefs.keySet()) {
            Long oldAuthHolderId = refreshTokenToAuthHolderRefs.get(oldRefreshTokenId);
            Long newAuthHolderId = authHolderOldToNewIdMap.get(oldAuthHolderId);
            AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
            Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
            OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
            refreshToken.setAuthenticationHolder(authHolder);
            tokenRepository.saveRefreshToken(refreshToken);
        }
        refreshTokenToAuthHolderRefs.clear();
        for (Long oldAccessTokenId : accessTokenToClientRefs.keySet()) {
            String clientRef = accessTokenToClientRefs.get(oldAccessTokenId);
            ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setClient(client);
            tokenRepository.saveAccessToken(accessToken);
        }
        accessTokenToClientRefs.clear();
        for (Long oldAccessTokenId : accessTokenToAuthHolderRefs.keySet()) {
            Long oldAuthHolderId = accessTokenToAuthHolderRefs.get(oldAccessTokenId);
            Long newAuthHolderId = authHolderOldToNewIdMap.get(oldAuthHolderId);
            AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setAuthenticationHolder(authHolder);
            tokenRepository.saveAccessToken(accessToken);
        }
        accessTokenToAuthHolderRefs.clear();
        for (Long oldAccessTokenId : accessTokenToRefreshTokenRefs.keySet()) {
            Long oldRefreshTokenId = accessTokenToRefreshTokenRefs.get(oldAccessTokenId);
            Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
            OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setRefreshToken(refreshToken);
            tokenRepository.saveAccessToken(accessToken);
        }
        accessTokenToRefreshTokenRefs.clear();
        refreshTokenOldToNewIdMap.clear();
        for (Long oldAccessTokenId : accessTokenToIdTokenRefs.keySet()) {
            Long oldIdTokenId = accessTokenToIdTokenRefs.get(oldAccessTokenId);
            Long newIdTokenId = accessTokenOldToNewIdMap.get(oldIdTokenId);
            OAuth2AccessTokenEntity idToken = tokenRepository.getAccessTokenById(newIdTokenId);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setIdToken(idToken);
            tokenRepository.saveAccessToken(accessToken);
        }
        accessTokenToIdTokenRefs.clear();
        for (Long oldGrantId : grantToWhitelistedSiteRefs.keySet()) {
            Long oldWhitelistedSiteId = grantToWhitelistedSiteRefs.get(oldGrantId);
            Long newWhitelistedSiteId = whitelistedSiteOldToNewIdMap.get(oldWhitelistedSiteId);
            WhitelistedSite wlSite = wlSiteRepository.getById(newWhitelistedSiteId);
            Long newGrantId = grantOldToNewIdMap.get(oldGrantId);
            ApprovedSite approvedSite = approvedSiteRepository.getById(newGrantId);
            approvedSite.setWhitelistedSite(wlSite);
            approvedSiteRepository.save(approvedSite);
        }
        grantToWhitelistedSiteRefs.clear();
        whitelistedSiteOldToNewIdMap.clear();
        for (Long oldGrantId : grantToAccessTokensRefs.keySet()) {
            Set<Long> oldAccessTokenIds = grantToAccessTokensRefs.get(oldGrantId);
            Set<OAuth2AccessTokenEntity> tokens = new HashSet<OAuth2AccessTokenEntity>();
            for(Long oldTokenId : oldAccessTokenIds) {
                Long newTokenId = accessTokenOldToNewIdMap.get(oldTokenId);
                tokens.add(tokenRepository.getAccessTokenById(newTokenId));
            }
            Long newGrantId = grantOldToNewIdMap.get(oldGrantId);
            ApprovedSite site = approvedSiteRepository.getById(newGrantId);
            site.setApprovedAccessTokens(tokens);
            approvedSiteRepository.save(site);
        }
        accessTokenOldToNewIdMap.clear();
        grantOldToNewIdMap.clear();
    }
}
