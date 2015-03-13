/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/

package org.mitre.openid.connect.filter;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableSet;

/**
 * @author jricher
 *
 */
public class MultiUrlRequestMatcher implements RequestMatcher {
	private final Set<String> filterProcessesUrls;

	public MultiUrlRequestMatcher(Set<String> filterProcessesUrls) {
		for (String filterProcessesUrl : filterProcessesUrls) {
			Assert.hasLength(filterProcessesUrl, "filterProcessesUrl must be specified");
			Assert.isTrue(UrlUtils.isValidRedirectUrl(filterProcessesUrl), filterProcessesUrl + " isn't a valid redirect URL");
		}
		this.filterProcessesUrls = ImmutableSet.copyOf(filterProcessesUrls);
	}

	public boolean matches(HttpServletRequest request) {
		String uri = request.getRequestURI();
		int pathParamIndex = uri.indexOf(';');

		if (pathParamIndex > 0) {
			// strip everything after the first semi-colon
			uri = uri.substring(0, pathParamIndex);
		}

		if ("".equals(request.getContextPath())) {
			// if any one of the URLs match, return true
			for (String filterProcessesUrl : filterProcessesUrls) {
				if (uri.endsWith(filterProcessesUrl)) {
					return true;
				}
			}
			return false;
		}

		for (String filterProcessesUrl : filterProcessesUrls) {
			if (uri.endsWith(request.getContextPath() + filterProcessesUrl)) {
				return true;
			}
		}

		return false;
	}

}
