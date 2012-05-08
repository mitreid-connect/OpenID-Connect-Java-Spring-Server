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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author nemonik
 *
 */
public class OIDCServers implements InitializingBean {
	
	private Map<String, ? extends OIDCServer> servers = new HashMap<String, OIDCServer>();

	private static Log logger = LogFactory.getLog(OIDCServers.class);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// used for debugging...
		if (!servers.isEmpty()) {
			logger.info(this.toString());
		}
	}
	
	/**
	 * Return the OIDCServers associated with this
	 * 
	 * @return
	 */
	public Map<String, ? extends OIDCServer> getServers() {
		return servers;
	}	
	
	/**
	 * Set the OIDCServers associated with this
	 * 
	 * @param signers
	 *            List of JwtSigners to associate with this service
	 */
	public void setServers(Map<String, ? extends OIDCServer> servers) {
		this.servers = servers;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OIDCServers [servers=" + servers
				+ "]";
	}

}
