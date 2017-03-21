/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
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
 * A modular extension to the data import/export layer. Any instances of this need to be
 * declared as beans to be picked up by the data services.
 *
 * @author jricher
 *
 */
public interface MITREidDataServiceExtension {

	/**
	 * Export any data for this extension. This is called from the top level object.
	 * All extensions MUST return the writer to a state such that another member of
	 * the top level object can be written next.
	 *
	 * @param writer
	 */
	void exportExtensionData(JsonWriter writer) throws IOException;

	/**
	 * Import data that's part of this extension. This is called from the context of
	 * reading the top level object. All extensions MUST return the reader to a state
	 * such that another member of the top level object can be read next. The name of
	 * the data element being imported is passed in as name. If the extension does not
	 * support this data element, it must return without advancing the reader.
	 *
	 * Returns "true" if the item was processed, "false" otherwise.
	 *
	 * @param reader
	 */
	boolean importExtensionData(String name, JsonReader reader) throws IOException;

	/**
	 * Signal the extension to wrap up all object processing and finalize its
	 */
	void fixExtensionObjectReferences(MITREidDataServiceMaps maps);

	/**
	 * Return
	 * @param mitreidConnect13
	 * @return
	 */
	boolean supportsVersion(String version);

}
