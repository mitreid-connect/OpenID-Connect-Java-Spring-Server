/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
 ******************************************************************************/
package org.mitre.oauth2.exception;

import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

public class NonceReuseException extends OAuth2Exception {

	private static final long serialVersionUID = 1L;

	public NonceReuseException(String clientId, String nonce) {
		super("Client " + clientId + " attempted to use reuse nonce " + nonce);
	}

}
