/*
 * Copyright 2014 arielak.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mitre.openid.connect.mitreadaptor.filter;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class RequestHeaderToken extends AbstractAuthenticationToken {

	private Object principal;
	
	
	/**
     * 
     */
    private static final long serialVersionUID = -8598928454566827917L;

    public RequestHeaderToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
    	super(authorities);
    	
    	this.principal = principal;
    	
    }
    
	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return principal;
	}

}