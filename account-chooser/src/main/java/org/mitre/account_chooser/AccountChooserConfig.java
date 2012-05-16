/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
package org.mitre.account_chooser;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to the configure AccountChooserController
 * 
 * @author nemonik
 *
 */
public class AccountChooserConfig {

	private String[] validClientIds;

	private Map<String, ? extends OIDCServer> issuers = new HashMap<String, OIDCServer>();

	public Map<String, ? extends OIDCServer> getIssuers() {
		return issuers;
	}

	public String[] getValidClientIds() {
		return validClientIds;
	}

	public void setIssuers(Map<String, ? extends OIDCServer> issuers) {
		this.issuers = issuers;
	}

	public void setValidClientIds(String[] validClientIds) {
		
		this.validClientIds = validClientIds;
	}
}
