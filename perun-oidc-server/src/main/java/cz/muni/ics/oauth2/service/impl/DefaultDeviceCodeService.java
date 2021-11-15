/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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

package cz.muni.ics.oauth2.service.impl;

import cz.muni.ics.data.AbstractPageOperationTemplate;
import cz.muni.ics.oauth2.model.AuthenticationHolderEntity;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.oauth2.repository.impl.DeviceCodeRepository;
import cz.muni.ics.oauth2.service.DeviceCodeService;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Service("defaultDeviceCodeService")
public class DefaultDeviceCodeService implements DeviceCodeService {

	@Autowired
	private DeviceCodeRepository repository;

	private final RandomValueStringGenerator randomGenerator = new RandomValueStringGenerator();

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.DeviceCodeService#save(cz.muni.ics.oauth2.model.DeviceCode)
	 */
	@Override
	public DeviceCode createNewDeviceCode(Set<String> requestedScopes, ClientDetailsEntity client, Map<String, String> parameters) {

		// create a device code, should be big and random
		String deviceCode = UUID.randomUUID().toString();

		// create a user code, should be random but small and typable, and always uppercase (lookup is case insensitive)
		String userCode = randomGenerator.generate().toUpperCase();

		DeviceCode dc = new DeviceCode(deviceCode, userCode, requestedScopes, client.getClientId(), parameters);

		if (client.getDeviceCodeValiditySeconds() != null) {
			dc.setExpiration(new Date(System.currentTimeMillis() + client.getDeviceCodeValiditySeconds() * 1000L));
		}

		dc.setApproved(false);

		return repository.save(dc);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.DeviceCodeService#lookUpByUserCode(java.lang.String)
	 */
	@Override
	public DeviceCode lookUpByUserCode(String userCode) {
		// always up-case the code for lookup
		return repository.getByUserCode(userCode.toUpperCase());
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.DeviceCodeService#approveDeviceCode(cz.muni.ics.oauth2.model.DeviceCode)
	 */
	@Override
	public DeviceCode approveDeviceCode(DeviceCode dc, OAuth2Authentication auth) {
		DeviceCode found = repository.getById(dc.getId());

		found.setApproved(true);

		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setAuthentication(auth);

		found.setAuthenticationHolder(authHolder);

		return repository.save(found);
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.DeviceCodeService#consumeDeviceCode(java.lang.String, org.springframework.security.oauth2.provider.ClientDetails)
	 */
	@Override
	public DeviceCode findDeviceCode(String deviceCode, ClientDetails client) {
		DeviceCode found = repository.getByDeviceCode(deviceCode);

		if (found != null) {
			if (found.getClientId().equals(client.getClientId())) {
				// make sure the client matches, if so, we're good
				return found;
			} else {
				// if the clients don't match, pretend the code wasn't found
				return null;
			}
		} else {
			// didn't find the code, return null
			return null;
		}

	}

	
	
	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.DeviceCodeService#clearExpiredDeviceCodes()
	 */
	@Override
	@Transactional(value="defaultTransactionManager")
	public void clearExpiredDeviceCodes() {

		new AbstractPageOperationTemplate<DeviceCode>("clearExpiredDeviceCodes"){
			@Override
			public Collection<DeviceCode> fetchPage() {
				return repository.getExpiredCodes();
			}

			@Override
			protected void doOperation(DeviceCode item) {
				repository.remove(item);
			}
		}.execute();
	}

	/* (non-Javadoc)
	 * @see cz.muni.ics.oauth2.service.DeviceCodeService#clearDeviceCode(java.lang.String, org.springframework.security.oauth2.provider.ClientDetails)
	 */
	@Override
	public void clearDeviceCode(String deviceCode, ClientDetails client) {
		DeviceCode found = findDeviceCode(deviceCode, client);
		
		if (found != null) {
			// make sure it's not used twice
			repository.remove(found);
		}

	}

}
