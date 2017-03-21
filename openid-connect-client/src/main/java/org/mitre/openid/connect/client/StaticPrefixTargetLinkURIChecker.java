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
package org.mitre.openid.connect.client;

/**
 * Simple target URI checker, checks whether the string in question starts
 * with a configured prefix. Returns "/" if the match fails.
 *
 * @author jricher
 *
 */
public class StaticPrefixTargetLinkURIChecker implements TargetLinkURIChecker {

	private String prefix = "";

	@Override
	public String filter(String target) {
		if (target == null) {
			return "/";
		} else if (target.startsWith(prefix)) {
			return target;
		} else {
			return "/";
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
