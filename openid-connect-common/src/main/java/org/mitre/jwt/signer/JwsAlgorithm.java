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
package org.mitre.jwt.signer;

import org.apache.commons.lang.StringUtils;

/**
 * Enum to translate between the JWS defined algorithm names and the JSE algorithm names
 * 
 * @author jricher
 *
 */
public enum JwsAlgorithm {
	
	// PLAINTEXT
	NONE("plaintext", "none"),

	// HMAC
	HS256("HMACSHA256", "HS256"), 
	HS384("HMACSHA384", "HS384"), 
	HS512("HMACSHA512", "HS512"),
	// RSA
	RS256("SHA256withRSA", "RS256"), 
	RS384("SHA384withRSA", "RS384"), 
	RS512("SHA512withRSA", "RS512");
	
	
	/**
	 * Returns the Algorithm for the JWS-registered name
	 * 
	 * @param name
	 * @return
	 */
	public static JwsAlgorithm getByName(String name) {
		for (JwsAlgorithm correspondingType : JwsAlgorithm.values()) {
			if (correspondingType.toString().equals(name)) {
				return correspondingType;
			}
		}

		// corresponding type not found
		throw new IllegalArgumentException("JwsAlgorithm name " + name + " does not have a corresponding JwsAlgorithm: expected one of [" + StringUtils.join(JwsAlgorithm.values(), ", ") + "]");
	}

	private final String standardName;
	private final String jwaName;
	
	/**
	 * Constructor of JwsAlgorithm
	 * 
	 * @param standardName
	 */
	JwsAlgorithm(String standardName, String jwaName) {
		this.standardName = standardName;
		this.jwaName = jwaName;
	}

	/**
	 * Return the Java standard JwsAlgorithm name
	 * 
	 * @return
	 */
	public String getStandardName() {
		return standardName;
	}

	/**
	 * Return the JWA Standard name
	 * @return
	 */
	public String getJwaName() {
		return jwaName;
	}

}
