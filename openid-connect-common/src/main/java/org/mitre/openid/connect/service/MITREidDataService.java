/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
package org.mitre.openid.connect.service;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author jricher
 * @author arielak
 */
public interface MITREidDataService {

	/**
	 * Data member for 1.X configurations
	 */
	public static final String MITREID_CONNECT_1_0 = "mitreid-connect-1.0";
	public static final String MITREID_CONNECT_1_1 = "mitreid-connect-1.1";
	public static final String MITREID_CONNECT_1_2 = "mitreid-connect-1.2";
	public static final String MITREID_CONNECT_1_3 = "mitreid-connect-1.3";

	// member names
	public static final String REFRESHTOKENS = "refreshTokens";
	public static final String ACCESSTOKENS = "accessTokens";
	public static final String WHITELISTEDSITES = "whitelistedSites";
	public static final String BLACKLISTEDSITES = "blacklistedSites";
	public static final String AUTHENTICATIONHOLDERS = "authenticationHolders";
	public static final String GRANTS = "grants";
	public static final String CLIENTS = "clients";
	public static final String SYSTEMSCOPES = "systemScopes";

	/**
	 * Write out the current server state to the given JSON writer as a JSON object
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void exportData(JsonWriter writer) throws IOException;

	/**
	 * Read in the current server state from the given JSON reader as a JSON object
	 * 
	 * @param reader
	 */
	void importData(JsonReader reader) throws IOException;

}