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
package org.mitre.oauth2.repository;

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;

public interface OAuth2ClientRepository {

	public ClientDetailsEntity getById(Long id);

	public ClientDetailsEntity getClientByClientId(String clientId);

	public ClientDetailsEntity saveClient(ClientDetailsEntity client);

	public void deleteClient(ClientDetailsEntity client);

	public ClientDetailsEntity updateClient(Long id, ClientDetailsEntity client);

	public Collection<ClientDetailsEntity> getAllClients();


}
