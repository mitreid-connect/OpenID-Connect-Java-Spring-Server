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
package org.mitre.oauth2.service;

import java.util.Collection;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetailsService;

public interface ClientDetailsEntityService extends ClientDetailsService {

	public ClientDetailsEntity getClientById(Long id);
	
	public ClientDetailsEntity loadClientByClientId(String clientId) throws OAuth2Exception;

	public void deleteClient(ClientDetailsEntity client);
	
	public ClientDetailsEntity updateClient(ClientDetailsEntity oldClient, ClientDetailsEntity newClient);

    public ClientDetailsEntity saveClient(ClientDetailsEntity client);

	public Collection<ClientDetailsEntity> getAllClients();
}
