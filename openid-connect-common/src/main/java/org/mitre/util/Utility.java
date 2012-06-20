/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
package org.mitre.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwk.model.AbstractJwk;
import org.mitre.jwk.model.EC;
import org.mitre.jwk.model.Jwk;
import org.mitre.jwk.model.Rsa;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * A collection of utility methods.
 * 
 */
public class Utility {

	/**
	 * Returns the base URL from a HttpServletRequest
	 * 
	 * @param request
	 * @return
	 */
	public static String findBaseUrl(HttpServletRequest request) {
		String issuer = String.format("%s://%s%s", request.getScheme(),
				request.getServerName(), request.getContextPath());

		if ((request.getScheme().equals("http") && request.getServerPort() != 80)
				|| (request.getScheme().equals("https") && request
						.getServerPort() != 443)) {
			// nonstandard port, need to include it
			issuer = String.format("%s://%s:%d%s", request.getScheme(),
					request.getServerName(), request.getServerPort(),
					request.getContextPath());
		}
		return issuer;
	}

}
