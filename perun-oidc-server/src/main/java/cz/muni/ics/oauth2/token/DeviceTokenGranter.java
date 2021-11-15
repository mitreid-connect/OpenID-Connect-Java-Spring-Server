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

package cz.muni.ics.oauth2.token;

import cz.muni.ics.oauth2.exception.AuthorizationPendingException;
import cz.muni.ics.oauth2.exception.DeviceCodeExpiredException;
import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.oauth2.service.DeviceCodeService;
import cz.muni.ics.oauth2.web.DeviceEndpoint;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;

/**
 * Implements https://tools.ietf.org/html/draft-ietf-oauth-device-flow
 *
 * @see DeviceEndpoint
 *
 * @author jricher
 *
 */
@Component("deviceTokenGranter")
public class DeviceTokenGranter extends AbstractTokenGranter {

	public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:device_code";

	@Autowired
	private DeviceCodeService deviceCodeService;

	/**
	 * @param tokenServices
	 * @param clientDetailsService
	 * @param requestFactory
	 * @param grantType
	 */
	protected DeviceTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.provider.token.AbstractTokenGranter#getOAuth2Authentication(org.springframework.security.oauth2.provider.ClientDetails, org.springframework.security.oauth2.provider.TokenRequest)
	 */
	@Override
	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {

		String deviceCode = tokenRequest.getRequestParameters().get("device_code");

		// look up the device code and consume it
		DeviceCode dc = deviceCodeService.findDeviceCode(deviceCode, client);

		if (dc != null) {

			// make sure the code hasn't expired yet
			if (dc.getExpiration() != null && dc.getExpiration().before(new Date())) {
				
				deviceCodeService.clearDeviceCode(deviceCode, client);
				
				throw new DeviceCodeExpiredException("Device code has expired " + deviceCode);

			} else if (!dc.isApproved()) {

				// still waiting for approval
				throw new AuthorizationPendingException("Authorization pending for code " + deviceCode);

			} else {
				// inherit the (approved) scopes from the original request
				tokenRequest.setScope(dc.getScope());

				OAuth2Authentication auth = new OAuth2Authentication(getRequestFactory().createOAuth2Request(client, tokenRequest), dc.getAuthenticationHolder().getUserAuth());

				deviceCodeService.clearDeviceCode(deviceCode, client);
				
				return auth;
			}
		} else {
			throw new InvalidGrantException("Invalid device code: " + deviceCode);
		}

	}




}
