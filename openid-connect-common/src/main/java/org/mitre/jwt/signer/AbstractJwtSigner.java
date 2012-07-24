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
package org.mitre.jwt.signer;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.mitre.jwt.model.Jwt;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class AbstractJwtSigner implements JwtSigner {
		
	private JwsAlgorithm algorithm;

	public AbstractJwtSigner(JwsAlgorithm algorithm) {
	    this.algorithm = algorithm;
    }

	/**
     * @return the algorithm
     */
    public JwsAlgorithm getAlgorithm() {
    	return algorithm;
    }

	/**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(JwsAlgorithm algorithm) {
    	this.algorithm = algorithm;
    }

    /**
     * Ensures that the 'alg' of the given JWT matches the {@link #algorithm} of this signer
     * and signs the jwt.
     * 
     * @param jwt the jwt to sign
     * @return the signed jwt
     * @throws NoSuchAlgorithmException 
     */
	@Override
	public Jwt sign(Jwt jwt) throws NoSuchAlgorithmException {
		
		//TODO: need a seperate check for Jwe. As is, it will change the alg param to be the enc param
		/*if (!Objects.equal(algorithm, jwt.getHeader().getAlgorithm())) {
			// algorithm type doesn't match
			// TODO: should this be an error or should we just fix it in the incoming jwt?
			// for now, we fix the Jwt
			jwt.getHeader().setAlgorithm(algorithm);			
		}*/
		
	    String sig = generateSignature(jwt.getSignatureBase());
        
        jwt.setSignature(sig);	
        
        return jwt;
	}

	/* (non-Javadoc)
     * @see org.mitre.jwt.JwtSigner#verify(java.lang.String)
     */
    @Override
    public boolean verify(String jwtString) throws NoSuchAlgorithmException {
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(jwtString));
		
		if (parts.size() != 3) {
			throw new IllegalArgumentException("Invalid JWT format.");
		}
		
		String h64 = parts.get(0);
		String c64 = parts.get(1);
		String s64 = parts.get(2);
    	
		String expectedSignature = generateSignature(h64 + "." + c64);
		
		return Strings.nullToEmpty(s64).equals(Strings.nullToEmpty(expectedSignature));
    	
    }
	
    
    protected abstract String generateSignature(String signatureBase) throws NoSuchAlgorithmException;
}
