/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
package cz.muni.ics.oauth2.service;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import java.util.Collection;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetailsService;

public interface ClientDetailsEntityService extends ClientDetailsService {

	public ClientDetailsEntity saveNewClient(ClientDetailsEntity client);

	public ClientDetailsEntity getClientById(Long id);

	@Override
	public ClientDetailsEntity loadClientByClientId(String clientId) throws OAuth2Exception;

	public void deleteClient(ClientDetailsEntity client);

	public ClientDetailsEntity updateClient(ClientDetailsEntity oldClient, ClientDetailsEntity newClient);

	public Collection<ClientDetailsEntity> getAllClients();

	public ClientDetailsEntity generateClientId(ClientDetailsEntity client);

	public ClientDetailsEntity generateClientSecret(ClientDetailsEntity client);

}
