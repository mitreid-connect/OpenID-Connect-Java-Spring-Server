/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

package org.mitre.oauth2.exception;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @author jricher
 *
 */
public class AuthorizationPendingException extends OAuth2Exception {

	/**
	 * @param msg
	 */
	public AuthorizationPendingException(String msg) {
		super(msg);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -7078098692596870940L;

	/* (non-Javadoc)
	 * @see org.springframework.security.oauth2.common.exceptions.OAuth2Exception#getOAuth2ErrorCode()
	 */
	@Override
	public String getOAuth2ErrorCode() {
		return "authorization_pending";
	}



}
