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

package cz.muni.ics.openid.connect.service.impl;

import com.google.common.base.Strings;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.LoginHintExtracter;
import cz.muni.ics.openid.connect.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Checks the login hint against the User Info collection, only populates it if a user is found.
 * @author jricher
 *
 */
public class MatchLoginHintsAgainstUsers implements LoginHintExtracter {

	@Autowired
	private UserInfoService userInfoService;

	/* (non-Javadoc)
	 * @see cz.muni.ics.openid.connect.service.LoginHintTester#useHint(java.lang.String)
	 */
	@Override
	public String extractHint(String loginHint) {
		if (Strings.isNullOrEmpty(loginHint)) {
			return null;
		} else {
			UserInfo user = userInfoService.getByEmailAddress(loginHint);
			if (user == null) {
				user = userInfoService.getByUsername(loginHint);
				if (user == null) {
					return null;
				} else {
					return user.getPreferredUsername();
				}
			} else {
				return user.getPreferredUsername();
			}
		}
	}

}
