/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.openid.connect.web;

import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.UserInfoJWTView;
import cz.muni.ics.openid.connect.view.UserInfoView;
import java.util.List;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.model.UserInfo;
import cz.muni.ics.openid.connect.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;

/**
 * OpenID Connect UserInfo endpoint, as specified in Standard sec 5 and Messages sec 2.4.
 *
 * @author AANGANES
 *
 */
@Controller
@RequestMapping("/" + UserInfoEndpoint.URL)
@Slf4j
public class UserInfoEndpoint {

	public static final String URL = "userinfo";

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private ClientDetailsEntityService clientService;

	/**
	 * Get information about the user as specified in the accessToken included in this request
	 */
	@PreAuthorize("hasRole('ROLE_USER') and #oauth2.hasScope('" + SystemScopeService.OPENID_SCOPE + "')")
	@RequestMapping(method= {RequestMethod.GET, RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_VALUE, UserInfoJWTView.JOSE_MEDIA_TYPE_VALUE})
	public String getInfo(@RequestParam(value="claims", required=false) String claimsRequestJsonString,
			@RequestHeader(value=HttpHeaders.ACCEPT, required=false) String acceptHeader,
			OAuth2Authentication auth, Model model) {

		if (auth == null) {
			log.error("getInfo failed; no principal. Requester is not authorized.");
			model.addAttribute(HttpCodeView.CODE, HttpStatus.FORBIDDEN);
			return HttpCodeView.VIEWNAME;
		}

		String username = auth.getName();
		UserInfo userInfo = userInfoService.getByUsernameAndClientId(username, auth.getOAuth2Request().getClientId());

		if (userInfo == null) {
			log.error("getInfo failed; user not found: " + username);
			model.addAttribute(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			return HttpCodeView.VIEWNAME;
		}

		model.addAttribute(UserInfoView.SCOPE, auth.getOAuth2Request().getScope());

		model.addAttribute(UserInfoView.AUTHORIZED_CLAIMS, auth.getOAuth2Request().getExtensions().get("claims"));

		if (!Strings.isNullOrEmpty(claimsRequestJsonString)) {
			model.addAttribute(UserInfoView.REQUESTED_CLAIMS, claimsRequestJsonString);
		}

		model.addAttribute(UserInfoView.USER_INFO, userInfo);

		// content negotiation

		// start off by seeing if the client has registered for a signed/encrypted JWT from here
		ClientDetailsEntity client = clientService.loadClientByClientId(auth.getOAuth2Request().getClientId());
		model.addAttribute(UserInfoJWTView.CLIENT, client);

		List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
		MediaType.sortBySpecificityAndQuality(mediaTypes);

		if (client.getUserInfoSignedResponseAlg() != null
				|| client.getUserInfoEncryptedResponseAlg() != null
				|| client.getUserInfoEncryptedResponseEnc() != null) {
			// client has a preference, see if they ask for plain JSON specifically on this request
			for (MediaType m : mediaTypes) {
				if (!m.isWildcardType() && m.isCompatibleWith(UserInfoJWTView.JOSE_MEDIA_TYPE)) {
					return UserInfoJWTView.VIEWNAME;
				} else if (!m.isWildcardType() && m.isCompatibleWith(MediaType.APPLICATION_JSON)) {
					return UserInfoView.VIEWNAME;
				}
			}

			// otherwise return JWT
			return UserInfoJWTView.VIEWNAME;
		} else {
			// client has no preference, see if they asked for JWT specifically on this request
			for (MediaType m : mediaTypes) {
				if (!m.isWildcardType() && m.isCompatibleWith(MediaType.APPLICATION_JSON)) {
					return UserInfoView.VIEWNAME;
				} else if (!m.isWildcardType() && m.isCompatibleWith(UserInfoJWTView.JOSE_MEDIA_TYPE)) {
					return UserInfoJWTView.VIEWNAME;
				}
			}

			// otherwise return JSON
			return UserInfoView.VIEWNAME;
		}

	}

}
