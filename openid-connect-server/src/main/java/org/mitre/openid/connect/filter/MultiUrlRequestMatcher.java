/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

/**
 * @author jricher
 *
 */
public class MultiUrlRequestMatcher implements RequestMatcher {
	private final Set<RequestMatcher> matchers;

	public MultiUrlRequestMatcher(Set<String> filterProcessesUrls) {
		this.matchers = new HashSet<>(filterProcessesUrls.size());
		for (String filterProcessesUrl : filterProcessesUrls) {
			Assert.hasLength(filterProcessesUrl, "filterProcessesUrl must be specified");
			Assert.isTrue(UrlUtils.isValidRedirectUrl(filterProcessesUrl), filterProcessesUrl + " isn't a valid URL");
			matchers.add(new AntPathRequestMatcher(filterProcessesUrl));
		}

	}

	@Override
	public boolean matches(HttpServletRequest request) {
		if (OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
			return false;
		}
		for (RequestMatcher matcher : matchers) {
			if (matcher.matches(request)) {
				return true;
			}
		}

		return false;
	}

}
