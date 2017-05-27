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
package org.mitre.openid.connect.client.service.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.mitre.openid.connect.client.model.IssuerServiceResponse;
import org.mitre.openid.connect.client.service.IssuerService;

import com.google.common.collect.Sets;

/**
 *
 * Issuer service that tries to parse input from the inputs from a third-party
 * account chooser service (if possible), but falls back to webfinger discovery
 * if not.
 *
 * @author jricher
 *
 */
public class HybridIssuerService implements IssuerService {

	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.ThirdPartyIssuerService#getAccountChooserUrl()
	 */
	public String getAccountChooserUrl() {
		return thirdPartyIssuerService.getAccountChooserUrl();
	}

	/**
	 * @param accountChooserUrl
	 * @see org.mitre.openid.connect.client.service.impl.ThirdPartyIssuerService#setAccountChooserUrl(java.lang.String)
	 */
	public void setAccountChooserUrl(String accountChooserUrl) {
		thirdPartyIssuerService.setAccountChooserUrl(accountChooserUrl);
	}

	/**
	 * @return
	 * @see org.mitre.openid.connect.client.service.impl.WebfingerIssuerService#isForceHttps()
	 */
	public boolean isForceHttps() {
		return webfingerIssuerService.isForceHttps();
	}

	/**
	 * @param forceHttps
	 * @see org.mitre.openid.connect.client.service.impl.WebfingerIssuerService#setForceHttps(boolean)
	 */
	public void setForceHttps(boolean forceHttps) {
		webfingerIssuerService.setForceHttps(forceHttps);
	}

	private ThirdPartyIssuerService thirdPartyIssuerService = new ThirdPartyIssuerService();
	private WebfingerIssuerService webfingerIssuerService = new WebfingerIssuerService();

	@Override
	public IssuerServiceResponse getIssuer(HttpServletRequest request) {

		IssuerServiceResponse resp = thirdPartyIssuerService.getIssuer(request);
		if (resp.shouldRedirect()) {
			// if it wants us to redirect, try the webfinger approach first
			return webfingerIssuerService.getIssuer(request);
		} else {
			return resp;
		}

	}

	public Set<String> getWhitelist() {
		return Sets.union(thirdPartyIssuerService.getWhitelist(), webfingerIssuerService.getWhitelist());
	}

	public void setWhitelist(Set<String> whitelist) {
		thirdPartyIssuerService.setWhitelist(whitelist);
		webfingerIssuerService.setWhitelist(whitelist);
	}

	public Set<String> getBlacklist() {
		return Sets.union(thirdPartyIssuerService.getBlacklist(), webfingerIssuerService.getWhitelist());
	}

	public void setBlacklist(Set<String> blacklist) {
		thirdPartyIssuerService.setBlacklist(blacklist);
		webfingerIssuerService.setBlacklist(blacklist);
	}

	public String getParameterName() {
		return webfingerIssuerService.getParameterName();
	}

	public void setParameterName(String parameterName) {
		webfingerIssuerService.setParameterName(parameterName);
	}

	public String getLoginPageUrl() {
		return webfingerIssuerService.getLoginPageUrl();
	}

	public void setLoginPageUrl(String loginPageUrl) {
		webfingerIssuerService.setLoginPageUrl(loginPageUrl);
		thirdPartyIssuerService.setAccountChooserUrl(loginPageUrl); // set the same URL on both, but this one gets ignored
	}


}
