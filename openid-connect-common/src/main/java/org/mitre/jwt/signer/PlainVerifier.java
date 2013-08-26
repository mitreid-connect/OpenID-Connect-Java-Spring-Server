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
package org.mitre.jwt.signer;

import java.util.Set;

import com.google.common.collect.Sets;
import com.nimbusds.jose.DefaultJWSHeaderFilter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeaderFilter;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jose.ReadOnlyJWSHeader;
import com.nimbusds.jose.Requirement;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.PlainJWT;

/**
 * Verifier to support "alg:none" JWS signing option (no signature).
 * 
 * FIXME: The JWSVerifier interface was never intended to be used with plain JWTs.
 * Use of the signer/verifier pattern alongside the other JWSSigner/Verifiers will require refactoring.
 * 
 * @author wkim
 *
 */
public final class PlainVerifier implements JWSVerifier {

	// the NONE alg constant lives in the Algorithm superclass of JWSAlgorithm, not allowing its use as a JWSAlgorithm object.
	// redefining the constant here for convenience.
	private static final JWSAlgorithm NONE = new JWSAlgorithm("none", Requirement.REQUIRED);
	
	/**
	 * The JWS header filter.
	 */
	private final DefaultJWSHeaderFilter headerFilter;
	
	public PlainVerifier() {
		
		headerFilter = new DefaultJWSHeaderFilter(Sets.newHashSet(NONE));
		
	}

	
	@Override
	public Set<JWSAlgorithm> supportedAlgorithms() {
		return Sets.newHashSet(NONE); 
	}


	@Override
	public JWSHeaderFilter getJWSHeaderFilter() {
		return headerFilter;
	}

	@Override
	public boolean verify(ReadOnlyJWSHeader header, byte[] signingInput, Base64URL signature) throws JOSEException {
		
		if (header instanceof PlainHeader) {
			// XXX NOT POSSIBLE--Interface does not allow this.
			return signature.decode().length == 0;
			
		} else { // not a plain (unsigned) JWS
			
			throw new JOSEException("Not a plain JWT header.");
			
		}
	}
	
	/**
	 * Verifies that the third signature component of the JWT is null.
	 * 
	 * @param jwt
	 * @return
	 */
	public static boolean verify(PlainJWT jwt) {
		
		return jwt.getParsedParts()[2] == null;
	}

}
