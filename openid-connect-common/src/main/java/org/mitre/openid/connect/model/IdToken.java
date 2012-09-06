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
package org.mitre.openid.connect.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.model.JwtHeader;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Entity
@Table(name="idtoken")
@NamedQueries({
	@NamedQuery(name = "IdToken.getAll", query = "select i from IdToken i")
})
public class IdToken extends Jwt {

	/**
	 * Create a blank IdToken
	 */
	public IdToken() {
	    super();
    }

	/**
	 * Create an IdToken from the requisite pieces.
	 * @param header
	 * @param claims
	 * @param signature
	 */
	public IdToken(JwtHeader header, JwtClaims claims, String signature) {
	    super(header, claims, signature);
    }


	private Long id;

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the tokenClaims
	 */
	@Transient
	public IdTokenClaims getClaims() {
		return (IdTokenClaims) super.getClaims();
	}

	/**
	 * @param tokenClaims the tokenClaims to set
	 */
	public void setClaims(JwtClaims tokenClaims) {
		if (tokenClaims instanceof IdTokenClaims) {
			super.setClaims(tokenClaims);
		} else {
			super.setClaims(new IdTokenClaims(tokenClaims));
		}
	}
	
	
	/**
	 * Parse a wire-encoded IdToken.
	 * 
	 */
	public static IdToken parse(String s) {

		// TODO: this code was copied nearly verbatim from Jwt.parse, and
		//       we should figure out how to re-use and abstract bits, likely
		
		// null string is a null token
		if (s == null) {
			return null;
		}
		
		// split on the dots
		List<String> parts = Lists.newArrayList(Splitter.on(".").split(s));
		
		if (parts.size() != 3) {
			throw new IllegalArgumentException("Invalid JWT format.");
		}
		
		String h64 = parts.get(0);
		String c64 = parts.get(1);
		String s64 = parts.get(2);
		
		// shuttle for return value
		IdToken idToken = new IdToken(new JwtHeader(h64), new IdTokenClaims(c64), s64);
		
		// TODO: save the wire-encoded string in the Jwt object itself?
		
		return idToken;
		
	}
	
}
