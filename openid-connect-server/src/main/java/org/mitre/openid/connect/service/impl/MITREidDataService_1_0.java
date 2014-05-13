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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
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

        writer.name(SYSTEMSCOPES);
        writer.beginArray();
        writeSystemScopes(writer);
        writer.endArray();

        writer.endObject(); // end mitreid-connect-1.0


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
        for (OAuth2RefreshTokenEntity token : tokenRepository.getAllRefreshTokens()) {
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
        for (OAuth2AccessTokenEntity token : tokenRepository.getAllAccessTokens()) {
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

    private String base64UrlEncodeObject(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return BaseEncoding.base64Url().encode(baos.toByteArray());
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
                    writer.name("requireAuthTime");
                    writer.value(requireAuthTime);
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
                    } else if (name.equals(SYSTEMSCOPES)) {
                        readSystemScopes(reader);
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
                    String name = reader.nextName();
                    if (name.equals("id")) {
                        currentId = reader.nextLong();
                    } else if (name.equals("expiration")) {
                    	if (reader.peek() == JsonToken.NULL) {
                    		reader.nextNull();
                    	} else {
                    		Date date = utcToDate(reader.nextString());
                    		token.setExpiration(date);
                    	}
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
                }
                reader.endObject();
                Long newId = tokenRepository.saveRefreshToken(token).getId();
                refreshTokenToClientRefs.put(currentId, clientId);
                refreshTokenToAuthHolderRefs.put(currentId, authHolderId);
                refreshTokenOldToNewIdMap.put(currentId, newId);
                logger.debug("Read refresh token {}", token.getId());
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
                    String name = reader.nextName();
                    if (name.equals("id")) {
                        currentId = reader.nextLong();
                    } else if (name.equals("expiration")) {
                    	if (reader.peek() == JsonToken.NULL) {
                    		reader.nextNull();
                    	} else {
	                        Date date = utcToDate(reader.nextString());
	                        token.setExpiration(date);
                    	}
                    } else if (name.equals("value")) {
                        token.setValue(reader.nextString());
                    } else if (name.equals("clientId")) {
                        clientId = reader.nextString();
                    } else if (name.equals("authenticationHolderId")) {
                        authHolderId = reader.nextLong();
                    } else if (name.equals("refreshTokenId")) {
                    	if (reader.peek() == JsonToken.NULL) {
                    		reader.nextNull();
                    	} else {
                    		refreshTokenId = reader.nextLong();
                    	}
                    } else if (name.equals("idTokenId")) {
                    	if (reader.peek() == JsonToken.NULL) {
                    		reader.nextNull();
                    	} else {
                    		idTokenId = reader.nextLong();
                    	}
                    } else if (name.equals("scope")) {
                        reader.beginArray();
                        Set<String> scope = new HashSet<String>();
                        while (reader.hasNext()) {
                            scope.add(reader.nextString());
                        }
                        reader.endArray();
                        token.setScope(scope);
                    } else if (name.equals("type")) {
                        token.setTokenType(reader.nextString());
                    } else {
                        logger.debug("Found unexpected entry");
                        reader.skipValue();
                    }
                }
                reader.endObject();
                Long newId = tokenRepository.saveAccessToken(token).getId();
                accessTokenToClientRefs.put(currentId, clientId);
                accessTokenToAuthHolderRefs.put(currentId, authHolderId);
                accessTokenToRefreshTokenRefs.put(currentId, refreshTokenId);
                accessTokenToIdTokenRefs.put(currentId, idTokenId);
                accessTokenOldToNewIdMap.put(currentId, newId);
                logger.debug("Read access token {}", token.getId());
            } catch (ParseException ex) {
                logger.error("Unable to read access token", ex);
            }
        }
        reader.endArray();
        logger.info("Done reading access tokens");
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
        		 		if (name.equals("value")) {
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
         logger.info("Done reading system scopes.");
    }
}
