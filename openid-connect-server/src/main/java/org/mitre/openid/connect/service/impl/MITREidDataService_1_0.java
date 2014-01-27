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

import java.io.IOException;

import org.mitre.openid.connect.service.MITREidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * 
 * Data service to import and export MITREid 1.0 configuration.
 * 
 * @author jricher
 *
 */
public class MITREidDataService_1_0 implements MITREidDataService {

	private final static Logger logger = LoggerFactory.getLogger(MITREidDataService_1_0.class);

	// member names
	private static final String REFRESHTOKENS = "refreshtokens";
    private static final String ACCESSTOKENS = "accesstokens";
    private static final String AUTHENTICATIONHOLDERS = "authenticationholders";
    private static final String GRANTS = "grants";
    private static final String CLIENTS = "clients";

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
	    // TODO Auto-generated method stub
	    
    }

	/**
	 * @param writer
	 */
    private void writeAccessTokens(JsonWriter writer) {
	    // TODO Auto-generated method stub
	    
    }

	/**
	 * @param writer
	 */
    private void writeAuthenticationHolders(JsonWriter writer) {
	    // TODO Auto-generated method stub
	    
    }

	/**
	 * @param writer
	 */
    private void writeGrants(JsonWriter writer) {
	    // TODO Auto-generated method stub
	    
    }

	/**
	 * @param writer
	 */
    private void writeClients(JsonWriter writer) {
	    // TODO Auto-generated method stub
	    
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
