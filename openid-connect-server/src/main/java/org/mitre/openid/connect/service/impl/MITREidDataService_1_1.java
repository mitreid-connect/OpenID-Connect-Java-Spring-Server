/**
 * *****************************************************************************
 * Copyright 2014 The MITRE Corporation and the MIT Kerberos and Internet Trust
 * Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */
package org.mitre.openid.connect.service.impl;

import com.google.common.io.BaseEncoding;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
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
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.repository.WhitelistedSiteRepository;
import org.mitre.openid.connect.service.MITREidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Service;

/**
 *
 * Data service to import and export MITREid 1.0 configuration.
 *
 * @author jricher
 * @author arielak
 */
@Service
public class MITREidDataService_1_1 implements MITREidDataService {
    
    private final static Logger logger = LoggerFactory.getLogger(MITREidDataService_1_1.class);
    @Autowired
    private OAuth2ClientRepository clientRepository;
    @Autowired
    private ApprovedSiteRepository approvedSiteRepository;
    @Autowired
    private AuthenticationHolderRepository authHolderRepository;
    @Autowired
    private OAuth2TokenRepository tokenRepository;
    @Autowired
    private SystemScopeRepository sysScopeRepository;
    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
    private static final TimeZone utc = TimeZone.getTimeZone("UTC");

    /* (non-Javadoc)
     * @see org.mitre.openid.connect.service.MITREidDataService#export(com.google.gson.stream.JsonWriter)
     */
    @Override
    public void exportData(JsonWriter writer) throws IOException {

        // version tag at the root
        writer.name(MITREID_CONNECT_1_1);

        writer.beginObject();

        // clients list
        writer.name(CLIENTS);
        writer.beginArray();
        writeClients(writer);
        writer.endArray();

        writer.name(GRANTS);
        writer.beginArray();
        writeGrants(writer);
        writer.endArray();

        writer.name(AUTHENTICATIONHOLDERS);
        writer.beginArray();
        writeAuthenticationHolders(writer);
        writer.endArray();

        writer.name(ACCESSTOKENS);
        writer.beginArray();
        writeAccessTokens(writer);
        writer.endArray();

        writer.name(REFRESHTOKENS);
        writer.beginArray();
        writeRefreshTokens(writer);
        writer.endArray();

        writer.name(SYSTEMSCOPES);
        writer.beginArray();
        writeSystemScopes(writer);
        writer.endArray();

        writer.endObject(); // end mitreid-connect-1.1
    }

    private static String toUTCString(Date date) {
        if (date == null) {
            return null;
        }
        sdf.setTimeZone(utc);
        return sdf.format(date);
    }

    private static Date utcToDate(String s) throws ParseException {
        if (s == null) {
            return null;
        }
        return sdf.parse(s);
    }

    /**
     * @param writer
     */
    private void writeRefreshTokens(JsonWriter writer) {
        Collection<OAuth2RefreshTokenEntity> tokens = new ArrayList<OAuth2RefreshTokenEntity>();
        try {
            tokens = tokenRepository.getAllRefreshTokens();
        } catch (Exception ex) {
            logger.error("Unable to read refresh tokens from data source", ex);
        }
        for (OAuth2RefreshTokenEntity token : tokens) {
            try {
                writer.beginObject();
                writer.name("id").value(token.getId());
                writer.name("expiration").value(toUTCString(token.getExpiration()));
                writer.name("clientId")
                        .value((token.getClient() != null) ? token.getClient().getClientId() : null);
                writer.name("authenticationHolderId")
                        .value((token.getAuthenticationHolder() != null) ? token.getAuthenticationHolder().getId() : null);
                writer.name("value").value(token.getValue());
                writer.endObject();
                logger.debug("Wrote refresh token {}", token.getId());
            } catch (IOException ex) {
                logger.error("Unable to write refresh token {}", token.getId(), ex);
            }
        }
        logger.info("Done writing refresh tokens");
    }

    /**
     * @param writer
     */
    private void writeAccessTokens(JsonWriter writer) {
        Collection<OAuth2AccessTokenEntity> tokens = new ArrayList<OAuth2AccessTokenEntity>();
        try {
            tokens = tokenRepository.getAllAccessTokens();
        } catch (Exception ex) {
            logger.error("Unable to read access tokens from data source", ex);
        }
        for (OAuth2AccessTokenEntity token : tokens) {
            try {
                writer.beginObject();
                writer.name("id").value(token.getId());
                writer.name("expiration").value(toUTCString(token.getExpiration()));
                writer.name("clientId")
                        .value((token.getClient() != null) ? token.getClient().getClientId() : null);
                writer.name("authenticationHolderId")
                        .value((token.getAuthenticationHolder() != null) ? token.getAuthenticationHolder().getId() : null);
                writer.name("refreshTokenId")
                        .value((token.getRefreshToken() != null) ? token.getRefreshToken().getId() : null);
                writer.name("idTokenId")
                        .value((token.getIdToken() != null) ? token.getIdToken().getId() : null);
                writer.name("scope");
                writer.beginArray();
                for (String s : token.getScope()) {
                    writer.value(s);
                }
                writer.endArray();
                writer.name("type").value(token.getTokenType());
                writer.name("value").value(token.getValue());
                writer.endObject();
                logger.debug("Wrote access token {}", token.getId());
            } catch (IOException ex) {
                logger.error("Unable to write access token {}", token.getId(), ex);
            }
        }
        logger.info("Done writing access tokens");
    }

    /**
     * @param writer
     */
    private void writeAuthenticationHolders(JsonWriter writer) {
        Collection<AuthenticationHolderEntity> holders = new ArrayList<AuthenticationHolderEntity>();
        try {
            holders = authHolderRepository.getAll();
        } catch (Exception ex) {
            logger.error("Unable to read authentication holders from data source", ex);
        }
        for (AuthenticationHolderEntity holder : holders) {
            try {
                writer.beginObject();
                writer.name("id").value(holder.getId());
                writer.name("ownerId").value(holder.getOwnerId());
                writer.name("authentication");
                writer.beginObject();
                OAuth2Authentication oa2Auth = holder.getAuthentication();
                writer.name("clientAuthorization");
                writeAuthorizationRequest(oa2Auth.getOAuth2Request(), writer);
                String userAuthentication = base64UrlEncodeObject(oa2Auth.getUserAuthentication());
                writer.name("userAuthentication").value(userAuthentication);
                writer.endObject();
                writer.endObject();
                logger.debug("Wrote authentication holder {}", holder.getId());
            } catch (IOException ex) {
                logger.error("Unable to write authentication holder {}", holder.getId(), ex);
            }
        }
        logger.info("Done writing authentication holders");
    }

    //used by writeAuthenticationHolders
    private void writeAuthorizationRequest(OAuth2Request authReq, JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("requestParameters");
        writer.beginObject();
        for (Entry<String, String> entry : authReq.getRequestParameters().entrySet()) {
            writer.name(entry.getKey()).value(entry.getValue());
        }
        writer.endObject();
        writer.name("clientId").value(authReq.getClientId());
        Set<String> scope = authReq.getScope();
        writer.name("scope");
        writer.beginArray();
        for (String s : scope) {
            writer.value(s);
        }
        writer.endArray();
        writer.name("resourceIds");
        writer.beginArray();
        if(authReq.getResourceIds() != null) {
            for (String s : authReq.getResourceIds()) {
                writer.value(s);
            }
        }
        writer.endArray();
        writer.name("authorities");
        writer.beginArray();
        for (GrantedAuthority authority : authReq.getAuthorities()) {
            writer.value(authority.getAuthority());
        }
        writer.endArray();
        writer.name("approved").value(authReq.isApproved());
        writer.name("redirectUri").value(authReq.getRedirectUri());
        writer.name("responseTypes");
        writer.beginArray();
        for (String s : authReq.getResponseTypes()) {
            writer.value(s);
        }
        writer.endArray();
        writer.name("extensions");
        writer.beginObject();
        for (Entry<String, Serializable> entry : authReq.getExtensions().entrySet()) {
            writer.name(entry.getKey()).value(base64UrlEncodeObject(entry.getValue()));
        }
        writer.endObject();
        writer.endObject();
    }

    private String base64UrlEncodeObject(Serializable obj) {
        String encoded = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            encoded = BaseEncoding.base64Url().encode(baos.toByteArray());
            oos.close();
            baos.close();
        } catch (IOException ex) {
            logger.error("Unable to encode object", ex);
        }
        return encoded;
    }
    
    private <T> T base64UrlDecodeObject(String encoded, Class<T> type) {
        T deserialized = null;
        try {
            byte[] decoded = BaseEncoding.base64Url().decode(encoded);
            ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
            ObjectInputStream ois = new ObjectInputStream(bais);
            deserialized = type.cast(ois.readObject());
            ois.close();
            bais.close();
        } catch (Exception ex) {
            logger.error("Unable to decode object", ex);
        }
        return deserialized;
    }
    
    /**
     * @param writer
     */
    private void writeGrants(JsonWriter writer) {
        for (ApprovedSite site : approvedSiteRepository.getAll()) {
            try {
                writer.beginObject();
                writer.name("id").value(site.getId());
                writer.name("accessDate").value(toUTCString(site.getAccessDate()));
                writer.name("clientId").value(site.getClientId());
                writer.name("creationDate").value(toUTCString(site.getCreationDate()));
                writer.name("timeoutDate").value(toUTCString(site.getTimeoutDate()));
                writer.name("userId").value(site.getUserId());
                writer.name("allowedScopes");
                writer.beginArray();
                for (String s : site.getAllowedScopes()) {
                    writer.value(s);
                }
                writer.endArray();
                if (site.getIsWhitelisted()) {
                    WhitelistedSite wlSite = site.getWhitelistedSite();
                    writer.name("whitelistedSite");
                    writer.beginObject();
                    writer.name("id").value(wlSite.getId());
                    writer.name("clientId").value(wlSite.getClientId());
                    writer.name("creatorUserId").value(wlSite.getCreatorUserId());
                    writer.name("allowedScopes");
                    writer.beginArray();
                    for (String s : wlSite.getAllowedScopes()) {
                        writer.value(s);
                    }
                    writer.endArray();
                    writer.endObject();
                }
                writer.endObject();
                logger.debug("Wrote grant {}", site.getId());
            } catch (IOException ex) {
                logger.error("Unable to write grant {}", site.getId(), ex);
            }
        }
        logger.info("Done writing grants");
    }

    /**
     * @param writer
     */
    private void writeClients(JsonWriter writer) {
        for (ClientDetailsEntity client : clientRepository.getAllClients()) {
            try {
                writer.beginObject();
                writer.name("clientId").value(client.getClientId());
                writer.name("resourceIds");
                writeNullSafeArray(writer, client.getResourceIds());

                writer.name("secret").value(client.getClientSecret());

                writer.name("scope");
                writeNullSafeArray(writer, client.getScope());

                writer.name("authorities");
                writer.beginArray();
                for (GrantedAuthority authority : client.getAuthorities()) {
                    writer.value(authority.getAuthority());
                }
                writer.endArray();
                writer.name("accessTokenValiditySeconds").value(client.getAccessTokenValiditySeconds());
                writer.name("refreshTokenValiditySeconds").value(client.getRefreshTokenValiditySeconds());
                writer.name("redirectUris");
                writeNullSafeArray(writer, client.getRedirectUris());
                writer.name("name").value(client.getClientName());
                writer.name("uri").value(client.getClientUri());
                writer.name("logoUri").value(client.getLogoUri());
                writer.name("contacts");
                writeNullSafeArray(writer, client.getContacts());
                writer.name("tosUri").value(client.getTosUri());
                writer.name("tokenEndpointAuthMethod")
                        .value((client.getTokenEndpointAuthMethod() != null) ? client.getTokenEndpointAuthMethod().getValue() : null);
                writer.name("grantTypes");
                writer.beginArray();
                for (String s : client.getGrantTypes()) {
                    writer.value(s);
                }
                writer.endArray();
                writer.name("responseTypes");
                writer.beginArray();
                for (String s : client.getResponseTypes()) {
                    writer.value(s);
                }
                writer.endArray();
                writer.name("policyUri").value(client.getPolicyUri());
                writer.name("jwksUri").value(client.getJwksUri());
                writer.name("applicationType")
                        .value((client.getApplicationType() != null) ? client.getApplicationType().getValue() : null);
                writer.name("sectorIdentifierUri").value(client.getSectorIdentifierUri());
                writer.name("subjectType")
                        .value((client.getSubjectType() != null) ? client.getSubjectType().getValue() : null);
                writer.name("requestObjectSigningAlg")
                        .value((client.getRequestObjectSigningAlgEmbed() != null) ? client.getRequestObjectSigningAlgEmbed().getAlgorithmName() : null);
                writer.name("userInfoEncryptedResponseAlg")
                        .value((client.getUserInfoEncryptedResponseAlgEmbed() != null) ? client.getUserInfoEncryptedResponseAlgEmbed().getAlgorithmName() : null);
                writer.name("userInfoEncryptedResponseEnc")
                        .value((client.getUserInfoEncryptedResponseEncEmbed() != null) ? client.getUserInfoEncryptedResponseEncEmbed().getAlgorithmName() : null);
                writer.name("userInfoSignedResponseAlg")
                        .value((client.getUserInfoSignedResponseAlgEmbed() != null) ? client.getUserInfoSignedResponseAlgEmbed().getAlgorithmName() : null);
                writer.name("defaultMaxAge").value(client.getDefaultMaxAge());
                Boolean requireAuthTime = null;
                try {
                    requireAuthTime = client.getRequireAuthTime();
                } catch (NullPointerException e) {
                }
                if (requireAuthTime != null) {
                    writer.name("requireAuthTime").value(requireAuthTime);
                }
                writer.name("defaultACRValues");
                writeNullSafeArray(writer, client.getDefaultACRvalues());
                writer.name("intitateLoginUri").value(client.getInitiateLoginUri());
                writer.name("postLogoutRedirectUri").value(client.getPostLogoutRedirectUri());
                writer.name("requestUris");
				writeNullSafeArray(writer, client.getRequestUris());
                writer.name("description").value(client.getClientDescription());
                writer.name("allowIntrospection").value(client.isAllowIntrospection());
                writer.name("reuseRefreshToken").value(client.isReuseRefreshToken());
                writer.name("dynamicallyRegistered").value(client.isDynamicallyRegistered());
                writer.endObject();
                logger.debug("Wrote client {}", client.getId());
            } catch (IOException ex) {
                logger.error("Unable to write client {}", client.getId(), ex);
            }
        }
        logger.info("Done writing clients");
    }

	private void writeNullSafeArray(JsonWriter writer, Set<String> items)
			throws IOException {
		if (items != null) {
			writer.beginArray();
		    for (String s : items) {
		        writer.value(s);
		    }
		    writer.endArray();
		} else {
			writer.nullValue();
		}
	}

    /**
     * @param writer
     */
    private void writeSystemScopes(JsonWriter writer) {
        for (SystemScope sysScope : sysScopeRepository.getAll()) {
            try {
                writer.beginObject();
                writer.name("id").value(sysScope.getId());
                writer.name("description").value(sysScope.getDescription());
                writer.name("icon").value(sysScope.getIcon());
                writer.name("value").value(sysScope.getValue());
                writer.name("allowDynReg").value(sysScope.isAllowDynReg());
                writer.name("defaultScope").value(sysScope.isDefaultScope());
                writer.endObject();
                logger.debug("Wrote system scope {}", sysScope.getId());
            } catch (IOException ex) {
                logger.error("Unable to write system scope {}", sysScope.getId(), ex);
            }
        }
        logger.info("Done writing system scopes");
    }

    /* (non-Javadoc)
     * @see org.mitre.openid.connect.service.MITREidDataService#importData(com.google.gson.stream.JsonReader)
     */
    @Override
    public void importData(JsonReader reader) throws IOException {

        logger.info("Reading configuration for 1.1");

        // this *HAS* to start as an object
        /*reader.beginObject();

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
        * */
    }
    
    private Map<Long, String> refreshTokenToClientRefs = new HashMap<Long, String>();
    private Map<Long, Long> refreshTokenToAuthHolderRefs = new HashMap<Long, Long>();
    private Map<Long, Long> refreshTokenOldToNewIdMap = new HashMap<Long, Long>();

    /**
     * @param reader
     * @throws IOException
     */
    private void readRefreshTokens(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            try {
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
                                Date date = utcToDate(reader.nextString());
                                token.setExpiration(date);
                            } else if (name.equals("value")) {
                                token.setValue(reader.nextString());
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
            } catch (ParseException ex) {
                logger.error("Unable to read refresh token", ex);
            }
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
    private void readAccessTokens(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            try {
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
                                Date date = utcToDate(reader.nextString());
                                token.setExpiration(date);
                            } else if (name.equals("value")) {
                                token.setValue(reader.nextString());
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
                if(refreshTokenId != null) {
                    accessTokenToRefreshTokenRefs.put(currentId, refreshTokenId);
                }
                if(idTokenId != null) {
                    accessTokenToIdTokenRefs.put(currentId, idTokenId);
                }
                accessTokenOldToNewIdMap.put(currentId, newId);
                logger.debug("Read access token {}", currentId);
            } catch (ParseException ex) {
                logger.error("Unable to read access token", ex);
            }
        }
        reader.endArray();
        logger.info("Done reading access tokens");
    }

    private Map<Long, Long> authHolderOldToNewIdMap = new HashMap<Long, Long>();
    
    /**
     * @param reader
     * @throws IOException
     */
    /*private void readAuthenticationHolders(JsonReader reader) throws IOException {
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
                            AuthorizationRequest clientAuthorization = null;
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
                                            String authString = reader.nextString();
                                            userAuthentication = base64UrlDecodeObject(authString, Authentication.class);
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
    }*/

    //used by readAuthenticationHolders
    /*private AuthorizationRequest readAuthorizationRequest(JsonReader reader) throws IOException {
        Set<String> scope = new LinkedHashSet<String>();
        Set<String> resourceIds = new HashSet<String>();
        boolean approved = false;
        Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        Map<String, String> authorizationParameters = new HashMap<String, String>();
        Map<String, String> approvalParameters = new HashMap<String, String>();
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
                        approvalParameters = readMap(reader);
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
                            GrantedAuthority ga = new GrantedAuthorityImpl(s);
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
        DefaultAuthorizationRequest dar = new DefaultAuthorizationRequest(authorizationParameters, approvalParameters, clientId, scope);
        dar.setAuthorities(authorities);
        dar.setResourceIds(resourceIds);
        dar.setApproved(approved);
        dar.setRedirectUri(redirectUri);
        return dar;
    }*/
    
    @Autowired
    private WhitelistedSiteRepository wlSiteRepository;
    
    /**
     * @param reader
     * @throws IOException
     */
    private void readGrants(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            try {
                ApprovedSite site = new ApprovedSite();
                Long currentId = null;
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
                                Date date = utcToDate(reader.nextString());
                                site.setAccessDate(date);
                            } else if (name.equals("clientId")) {
                                site.setClientId(reader.nextString());
                            } else if (name.equals("creationDate")) {
                                Date date = utcToDate(reader.nextString());
                                site.setCreationDate(date);
                            } else if (name.equals("timeoutDate")) {
                                Date date = utcToDate(reader.nextString());
                                site.setTimeoutDate(date);
                            } else if (name.equals("userId")) {
                                site.setUserId(reader.nextString());
                            } else if (name.equals("allowedScopes")) {
                                Set<String> allowedScopes = readSet(reader);
                                site.setAllowedScopes(allowedScopes);
                            } else if (name.equals("whitelistedSite")) {
                                WhitelistedSite wlSite = new WhitelistedSite();
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    switch (reader.peek()) {
                                        case END_OBJECT:
                                            continue;
                                        case NAME:
                                            String wlName = reader.nextName();
                                            if (wlName.equals("id")) {
                                                //not needed
                                                reader.skipValue();
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
                                wlSite = wlSiteRepository.save(wlSite);
                                site.setWhitelistedSite(wlSite);
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
                approvedSiteRepository.save(site).getId();
                logger.debug("Read grant {}", currentId);
            } catch (ParseException ex) {
                logger.error("Unable to read grant", ex);
            }
        }
        reader.endArray();
        logger.info("Done reading grants");
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
                                GrantedAuthority ga = new GrantedAuthorityImpl(s);
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
                            client.setPostLogoutRedirectUri(reader.nextString());
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
     * Read the list of system scopes from the reader and insert them
     * into the scope repository.
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
  
    private Set readSet(JsonReader reader) throws IOException {
        Set arraySet = null;
        reader.beginArray();
        switch (reader.peek()) {
            case STRING:
                arraySet = new HashSet<String>();
                while (reader.hasNext()) {
                    arraySet.add(reader.nextString());
                }
                break;
            case NUMBER:
                arraySet = new HashSet<Long>();
                while (reader.hasNext()) {
                    arraySet.add(reader.nextLong());
                }
                break;
            default:
                arraySet = new HashSet();
                break;
        }
        reader.endArray();
        return arraySet;
    }
    
    private Map readMap(JsonReader reader) throws IOException {
        Map map = new HashMap<String, Object>();
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            Object value = null;
            switch(reader.peek()) {
                case STRING:
                    value = reader.nextString();
                    break;
                case BOOLEAN:
                    value = reader.nextBoolean();
                    break;
                case NUMBER:
                    value = reader.nextLong();
                    break;
            }
            map.put(name, value);
        }
        reader.endObject();
        return map;
    }
    
    private void fixObjectReferences() {
        for(Long oldRefreshTokenId : refreshTokenToClientRefs.keySet()) {
            String clientRef = refreshTokenToClientRefs.get(oldRefreshTokenId);
            ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
            Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
            OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
            refreshToken.setClient(client);
            tokenRepository.saveRefreshToken(refreshToken);
        }
        for(Long oldRefreshTokenId : refreshTokenToAuthHolderRefs.keySet()) {
            Long oldAuthHolderId = refreshTokenToAuthHolderRefs.get(oldRefreshTokenId);
            Long newAuthHolderId = authHolderOldToNewIdMap.get(oldAuthHolderId);
            AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
            Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
            OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
            refreshToken.setAuthenticationHolder(authHolder);
            tokenRepository.saveRefreshToken(refreshToken);
        }
        for(Long oldAccessTokenId : accessTokenToClientRefs.keySet()) {
            String clientRef = accessTokenToClientRefs.get(oldAccessTokenId);
            ClientDetailsEntity client = clientRepository.getClientByClientId(clientRef);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setClient(client);
            tokenRepository.saveAccessToken(accessToken);
        }
        for(Long oldAccessTokenId : accessTokenToAuthHolderRefs.keySet()) {
            Long oldAuthHolderId = accessTokenToAuthHolderRefs.get(oldAccessTokenId);
            Long newAuthHolderId = authHolderOldToNewIdMap.get(oldAuthHolderId);
            AuthenticationHolderEntity authHolder = authHolderRepository.getById(newAuthHolderId);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setAuthenticationHolder(authHolder);
            tokenRepository.saveAccessToken(accessToken);
        }
        for(Long oldAccessTokenId : accessTokenToRefreshTokenRefs.keySet()) {
            Long oldRefreshTokenId = accessTokenToRefreshTokenRefs.get(oldAccessTokenId);
            Long newRefreshTokenId = refreshTokenOldToNewIdMap.get(oldRefreshTokenId);
            OAuth2RefreshTokenEntity refreshToken = tokenRepository.getRefreshTokenById(newRefreshTokenId);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setRefreshToken(refreshToken);
            tokenRepository.saveAccessToken(accessToken);
        }
        for(Long oldAccessTokenId : accessTokenToIdTokenRefs.keySet()) {
            Long oldIdTokenId = accessTokenToIdTokenRefs.get(oldAccessTokenId);
            Long newIdTokenId = accessTokenOldToNewIdMap.get(oldIdTokenId);
            OAuth2AccessTokenEntity idToken = tokenRepository.getAccessTokenById(newIdTokenId);
            Long newAccessTokenId = accessTokenOldToNewIdMap.get(oldAccessTokenId);
            OAuth2AccessTokenEntity accessToken = tokenRepository.getAccessTokenById(newAccessTokenId);
            accessToken.setIdToken(idToken);
            tokenRepository.saveAccessToken(accessToken);
        }
    }
}
