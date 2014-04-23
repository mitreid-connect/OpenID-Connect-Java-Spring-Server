/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
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
 ******************************************************************************/
package org.mitre.openid.connect.service.impl;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import java.io.IOException;

import org.mitre.openid.connect.service.MITREidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.repository.AuthenticationHolderRepository;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.oauth2.repository.OAuth2TokenRepository;
import org.mitre.openid.connect.repository.ApprovedSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * 
 * Data service to import and export MITREid 1.0 configuration.
 * 
 * @author jricher
 * @author arielak
 */
public class MITREidDataService_1_0 implements MITREidDataService {

	private final static Logger logger = LoggerFactory.getLogger(MITREidDataService_1_0.class);

	// member names
	private static final String REFRESHTOKENS = "refreshtokens";
    private static final String ACCESSTOKENS = "accesstokens";
    private static final String AUTHENTICATIONHOLDERS = "authenticationholders";
    private static final String GRANTS = "grants";
    private static final String CLIENTS = "clients";

    @Autowired
    private OAuth2ClientRepository clientRepo;
    @Autowired
    private ApprovedSiteRepository approvedSiteRepo;
    @Autowired
    private AuthenticationHolderRepository authHolderRepo;
    @Autowired
    private OAuth2TokenRepository tokenRepo;
    
    public void setClientRepo(OAuth2ClientRepository clientRepo) {
        this.clientRepo = clientRepo;
    }

    public void setApprovedSiteRepo(ApprovedSiteRepository approvedSiteRepo) {
        this.approvedSiteRepo = approvedSiteRepo;
    }

    public void setAuthHolderRepo(AuthenticationHolderRepository authHolderRepo) {
        this.authHolderRepo = authHolderRepo;
    }

    public void setTokenRepo(OAuth2TokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }
    
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
        for (OAuth2RefreshTokenEntity token : tokenRepo.getAllRefreshTokens()) {
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
        for (OAuth2AccessTokenEntity token : tokenRepo.getAllAccessTokens()) {
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
        for(AuthenticationHolderEntity holder : authHolderRepo.getAll()) {
            try {
                writer.beginObject();
                writer.name("id").value(holder.getId());
                writer.name("ownerId").value(holder.getOwnerId());
                writer.name("authentication");
                writer.beginObject();
                OAuth2Authentication oa2Auth = holder.getAuthentication();
                AuthorizationRequest authReq = oa2Auth.getAuthorizationRequest();
                writer.name("clientAuthorization");
                writeAuthorizationRequest(authReq, writer);
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
    
    private void writeAuthorizationRequest(AuthorizationRequest authReq, JsonWriter writer) throws IOException {
        writer.beginObject();
        Map<String, String> authParams = authReq.getAuthorizationParameters();
        writer.name("authorizationParameters");
        writer.beginObject();
        for(Entry<String, String> entry: authParams.entrySet()) {
            writer.name(entry.getKey()).value(entry.getValue());
        }
        writer.endObject();
        Map<String, String> approvalParams = authReq.getApprovalParameters();
        writer.name("approvalParameters");
        writer.beginObject();
        for(Entry<String, String> entry: approvalParams.entrySet()) {
            writer.name(entry.getKey()).value(entry.getValue());
        }
        writer.endObject();
        writer.name("clientId").value(authReq.getClientId());
        Set<String> scope = authReq.getScope();
        writer.name("scope");
        writer.beginArray();
        for(String s : scope) {
            writer.value(s);
        }
        writer.endArray();
        Set<String> resourceIds = authReq.getResourceIds();
        writer.name("resourceIds");
        writer.beginArray();
        for(String s : resourceIds) {
            writer.value(s);
        }
        writer.endArray();
        Collection<GrantedAuthority> authorities = authReq.getAuthorities();
        writer.name("authorities");
        writer.beginArray();
        for(GrantedAuthority authority : authorities) {
            writer.value(authority.getAuthority());
        }
        writer.endArray();
        writer.name("isApproved").value(authReq.isApproved());
        writer.name("isDenied").value(authReq.isDenied());
        writer.name("state").value(authReq.getState());
        writer.name("redirectUri").value(authReq.getRedirectUri());
        Set<String> responseTypes = authReq.getResponseTypes();
        writer.name("responseTypes");
        writer.beginArray();
        for(String s : responseTypes) {
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
        approvedSiteRepo.getAll();
    }

	/**
	 * @param writer
	 */
    private void writeClients(JsonWriter writer) {
        Gson gson = new Gson();
        for(ClientDetailsEntity client : clientRepo.getAllClients()) {
            String clientStr = gson.toJson(client);
            try {
                writer.value(clientStr);
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
	    	switch(tok) {
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
