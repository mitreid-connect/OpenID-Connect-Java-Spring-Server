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
/**
 *
 */
package org.mitre.openid.connect.client;

import org.springframework.security.core.GrantedAuthority;

import com.google.common.base.Strings;

/**
 *
 * Simple authority representing a user at an issuer.
 *
 * @author jricher
 *
 */
public class SubjectIssuerGrantedAuthority implements GrantedAuthority {

	private static final long serialVersionUID = 5584978219226664794L;

	private final String subject;
	private final String issuer;

	/**
	 * @param subject
	 * @param issuer
	 */
	public SubjectIssuerGrantedAuthority(String subject, String issuer) {
		if (Strings.isNullOrEmpty(subject) || Strings.isNullOrEmpty(issuer)) {
			throw new IllegalArgumentException("Neither subject nor issuer may be null or empty");
		}
		this.subject = subject;
		this.issuer = issuer;
	}

	/**
	 * Returns a string formed by concatenating the subject with the issuer, separated by _ and prepended with OIDC_
	 *
	 * For example, the user "bob" from issuer "http://id.example.com/" would return the authority string of:
	 *
	 * OIDC_bob_http://id.example.com/
	 */
	@Override
	public String getAuthority() {
		return "OIDC_" + subject + "_" + issuer;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SubjectIssuerGrantedAuthority)) {
			return false;
		}
		SubjectIssuerGrantedAuthority other = (SubjectIssuerGrantedAuthority) obj;
		if (issuer == null) {
			if (other.issuer != null) {
				return false;
			}
		} else if (!issuer.equals(other.issuer)) {
			return false;
		}
		if (subject == null) {
			if (other.subject != null) {
				return false;
			}
		} else if (!subject.equals(other.subject)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getAuthority();
	}
}
