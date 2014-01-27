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
package org.mitre.openid.connect.service;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author jricher
 *
 */
public interface MITREidDataService {

	/**
	 * Data member for 1.0 configuration
	 */
	public static final String MITREID_CONNECT_1_0 = "mitreid-connect-1.0";

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
