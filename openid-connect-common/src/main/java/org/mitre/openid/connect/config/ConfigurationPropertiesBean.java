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
package org.mitre.openid.connect.config;


/**
 * Bean to hold configuration information that must be injected into various parts
 * of our application. Set all of the properties here, and autowire a reference
 * to this bean if you need access to any configuration properties. 
 * 
 * @author AANGANES
 *
 */
public class ConfigurationPropertiesBean {

	private String issuer;
	
	private String defaultJwtSigner;

	public ConfigurationPropertiesBean() {
	}
	
	/**
	 * @return the defaultJwtSigner
	 */
	public String getDefaultJwtSigner() {
		return defaultJwtSigner;
	}
	
	public void setDefaultJwtSigner(String signer) {
		defaultJwtSigner = signer;
	}

	/**
	 * @return the baseUrl
	 */
	public String getIssuer() {
		return issuer;
	}
	
	/**
	 * @param iss the issuer to set
	 */
	public void setIssuer(String iss) {
		issuer = iss;
	}
}
