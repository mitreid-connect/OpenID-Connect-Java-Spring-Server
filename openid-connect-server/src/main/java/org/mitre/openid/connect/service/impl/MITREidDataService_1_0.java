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
 *****************************************************************************
 */
package org.mitre.openid.connect.service.impl;

import com.google.common.io.BaseEncoding;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.Set;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.openid.connect.model.ApprovedSite;
import org.mitre.openid.connect.model.WhitelistedSite;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.mitre.openid.connect.service.MITREidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

/**
 *
 * Data service to import and export MITREid 1.0 configuration.
 *
 * @author jricher
 * @author arielak
 */
@Service
public class MITREidDataService_1_0 implements MITREidDataService {

    private final static Logger logger = LoggerFactory.getLogger(MITREidDataService_1_0.class);
    // member names
    private static final String REFRESHTOKENS = "refreshTokens";
    private static final String ACCESSTOKENS = "accessTokens";
    private static final String AUTHENTICATIONHOLDERS = "authenticationHolders";
    private static final String GRANTS = "grants";
    private static final String CLIENTS = "clients";
    @Autowired
    private OAuth2ClientRepository clientRepository;
    @Autowired
    private ApprovedSiteRepository approvedSiteRepository;
    @Autowired
    private AuthenticationHolderRepository authHolderRepository;
    @Autowired
    private OAuth2TokenRepository tokenRepository;

    /* (non-Javadoc)
     * @see org.mitre.openid.connect.service.MITREidDataService#export(com.google.gson.stream.JsonWriter)
     */
    @Override
    public void exportData(JsonWriter writer) throws IOException {

        // version tag at the root
        writer.name(MITREID_CONNECT_1_0);

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

        writer.endObject(); // end mitreid-connect-1.0


    }

    /**
     * @param writer
     */
    private void writeRefreshTokens(JsonWriter writer) {
        for (OAuth2RefreshTokenEntity token : tokenRepository.getAllRefreshTokens()) {
            try {
                writer.value(token.getJwt().serialize());
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
        for (OAuth2AccessTokenEntity token : tokenRepository.getAllAccessTokens()) {
            try {
                writer.value(token.getJwt().serialize());
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
        for (AuthenticationHolderEntity holder : authHolderRepository.getAll()) {
            try {
                writer.beginObject();
                writer.name("id").value(holder.getId());
                writer.name("ownerId").value(holder.getOwnerId());
                writer.name("authentication");
                writer.beginObject();
                OAuth2Authentication oa2Auth = holder.getAuthentication();
                writer.name("clientAuthorization");
                writeAuthorizationRequest(oa2Auth.getAuthorizationRequest(), writer);
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
    private void writeAuthorizationRequest(AuthorizationRequest authReq, JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("authorizationParameters");
        writer.beginObject();
        for (Entry<String, String> entry : authReq.getAuthorizationParameters().entrySet()) {
            writer.name(entry.getKey()).value(entry.getValue());
        }
        writer.endObject();
        writer.name("approvalParameters");
        writer.beginObject();
        for (Entry<String, String> entry : authReq.getApprovalParameters().entrySet()) {
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
        for (String s : authReq.getResourceIds()) {
            writer.value(s);
        }
        writer.endArray();
        writer.name("authorities");
        writer.beginArray();
        for (GrantedAuthority authority : authReq.getAuthorities()) {
            writer.value(authority.getAuthority());
        }
        writer.endArray();
        writer.name("approved").value(authReq.isApproved());
        writer.name("denied").value(authReq.isDenied());
        writer.name("state").value(authReq.getState());
        writer.name("redirectUri").value(authReq.getRedirectUri());
        writer.name("responseTypes");
        writer.beginArray();
        for (String s : authReq.getResponseTypes()) {
            writer.value(s);
        }
        writer.endArray();
        writer.endObject();
    }

    private String base64UrlEncodeObject(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(BaseEncoding.base64Url().encodingStream(new OutputStreamWriter(baos)));
        oos.writeObject(obj);
        return baos.toString("ascii");
    }

    /**
     * @param writer
     */
    private void writeGrants(JsonWriter writer) {
        for (ApprovedSite site : approvedSiteRepository.getAll()) {
            try {
                writer.beginObject();
                writer.name("id").value(site.getId());
                writer.name("accessDate").value(site.getAccessDate().toString());
                writer.name("clientId").value(site.getClientId());
                writer.name("creationDate").value(site.getCreationDate().toString());
                writer.name("timeoutDate").value(site.getTimeoutDate().toString());
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
                writer.name("id").value(client.getClientId());
                writer.name("resourceIds");
                writer.beginArray();
                for (String s : client.getResourceIds()) {
                    writer.value(s);
                }
                writer.endArray();

                writer.name("secret").value(client.getClientSecret());

                writer.name("scope");
                writer.beginArray();
                for (String s : client.getScope()) {
                    writer.value(s);
                }
                writer.endArray();

                writer.name("authorities");
                writer.beginArray();
                for (GrantedAuthority authority : client.getAuthorities()) {
                    writer.value(authority.getAuthority());
                }
                writer.endArray();
                writer.name("accessTokenValiditySeconds").value(client.getAccessTokenValiditySeconds());
                writer.name("refreshTokenValiditySeconds").value(client.getRefreshTokenValiditySeconds());
                writer.name("additionalInformation");
                writer.beginObject();
                for (Entry<String, Object> entry : client.getAdditionalInformation().entrySet()) {
                    writer.name(entry.getKey()).value(entry.getValue().toString());
                }
                writer.endObject();
                writer.name("redirectUris");
                writer.beginArray();
                for (String s : client.getRedirectUris()) {
                    writer.value(s);
                }
                writer.endArray();
                writer.name("name").value(client.getClientName());
                writer.name("uri").value(client.getClientUri());
                writer.name("logoUri").value(client.getLogoUri());
                writer.name("contacts");
                writer.beginArray();
                for (String s : client.getContacts()) {
                    writer.value(s);
                }
                writer.endArray();
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
                } catch (NullPointerException e) {}
                if(requireAuthTime != null) {
                    writer.name("requireAuthTime");
                    writer.value(requireAuthTime);
                }
                writer.name("defaultACRValues");
                writer.beginArray();
                for (String s : client.getDefaultACRvalues()) {
                    writer.value(s);
                }
                writer.endArray();
                writer.name("intitateLoginUri").value(client.getInitiateLoginUri());
                writer.name("postLogoutRedirectUri").value(client.getPostLogoutRedirectUri());
                writer.name("requestUris");
                writer.beginArray();
                for (String s : client.getRequestUris()) {
                    writer.value(s);
                }
                writer.endArray();
                writer.name("description").value(client.getClientDescription());
                writer.name("allowIntrospection").value(client.isAllowIntrospection());
                writer.name("allowRefresh").value(client.isAllowRefresh());
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
                    } else if (name.equals(AUTHENTICATIONHOLDERS)) {
                        readAuthenticationHolders(reader);
                    } else if (name.equals(ACCESSTOKENS)) {
                        readAccessTokens(reader);
                    } else if (name.equals(REFRESHTOKENS)) {
                        readRefreshTokens(reader);
                    } else {
                        // unknown token, skip it
                        reader.skipValue();
                    }
                    break;
                case END_OBJECT:
                    // the object ended, we're done here
                    return;
            }
        }
    }

    /**
     * @param reader
     * @throws IOException
     */
    private void readRefreshTokens(JsonReader reader) throws IOException {
        // TODO Auto-generated method stub
        reader.skipValue();
    }

    /**
     * @param reader
     * @throws IOException
     */
    private void readAccessTokens(JsonReader reader) throws IOException {
        // TODO Auto-generated method stub
        reader.skipValue();
    }

    /**
     * @param reader
     * @throws IOException
     */
    private void readAuthenticationHolders(JsonReader reader) throws IOException {
        // TODO Auto-generated method stub
        reader.skipValue();
    }

    /**
     * @param reader
     * @throws IOException
     */
    private void readGrants(JsonReader reader) throws IOException {
        // TODO Auto-generated method stub
        reader.skipValue();
    }

    /**
     * @param reader
     * @throws IOException
     */
    private void readClients(JsonReader reader) throws IOException {
        // TODO Auto-generated method stub
        reader.skipValue();
    }
}
