/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
/**
 *
 */
package org.mitre.openid.connect.client.service.impl;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.IssuerService;

import com.google.common.base.Strings;

/**
 * @author jricher
 *
 */
public class StaticSingleIssuerService implements IssuerService {

	private String issuer;

	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}

	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * Always returns the configured issuer URL
	 *
	 * @see org.mitre.openid.connect.client.service.IssuerService#getIssuer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public IssuerServiceResponse getIssuer(HttpServletRequest request) {
		return new IssuerServiceResponse(getIssuer(), null, null);
	}

	@PostConstruct
	public void afterPropertiesSet() {

		if (Strings.isNullOrEmpty(issuer)) {
			throw new IllegalArgumentException("Issuer must not be null or empty.");
		}

	}

}
