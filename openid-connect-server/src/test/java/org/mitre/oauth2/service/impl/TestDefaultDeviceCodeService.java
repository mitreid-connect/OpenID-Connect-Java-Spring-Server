
package org.mitre.oauth2.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.DeviceCode;
import org.mitre.oauth2.repository.impl.DeviceCodeRepository;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultDeviceCodeService {

	private String deviceCodeFound;
	private String deviceCodeClientNotMatch;
	private String deviceCodeNotFound;

	private String userCodeFound;
	private String userCodeNotFound;

	private ClientDetailsEntity clientDetailsEntity1;
	private ClientDetailsEntity clientDetailsEntity2;

	private DeviceCode deviceCode1;
	private DeviceCode deviceCode2;

	private String clientId1;
	private String clientId2;

	private Set<DeviceCode> expiredDeviceCodes1;
	private Set<DeviceCode> expiredDeviceCodes2;

	private Set<String> requestedScopes;

	@Mock
	private DeviceCodeRepository repository;

	@InjectMocks
	private DefaultDeviceCodeService service;

	@Before
	public void setUp() {

		Mockito.reset(repository);

		deviceCodeFound = "deviceCodeFound";
		deviceCodeClientNotMatch = "deviceCodeClientNotMatch";
		deviceCodeNotFound = "deviceCodeNotFound";

		deviceCode1 = new DeviceCode();
		clientId1 = "clientId1";
		deviceCode1.setClientId(clientId1);

		deviceCode2 = new DeviceCode();
		clientId2 = "clientId2";
		deviceCode2.setClientId(clientId2);

		clientDetailsEntity1 = new ClientDetailsEntity();
		clientDetailsEntity1.setClientId(clientId1);

		clientDetailsEntity2 = new ClientDetailsEntity();
		clientDetailsEntity2.setClientId(clientId1);

		userCodeFound = "userCodeFound";
		deviceCode1.setUserCode(userCodeFound);

		userCodeNotFound = "userCodeNotFound";

		requestedScopes = Sets.newHashSet("scope1", "scope2");		

		Mockito.when(repository.getByDeviceCode(deviceCodeFound)).thenReturn(deviceCode1);
		Mockito.when(repository.getByDeviceCode(deviceCodeClientNotMatch)).thenReturn(deviceCode2);
		Mockito.when(repository.getByDeviceCode(deviceCodeNotFound)).thenReturn(null);		
		Mockito.when(repository.getByUserCode(userCodeFound.toLowerCase().toUpperCase())).thenReturn(deviceCode1);
		Mockito.when(repository.getByUserCode(userCodeNotFound)).thenReturn(null);
		deviceCode1.setId(new Long(1001));
		Mockito.when(repository.getById(new Long(1001))).thenReturn(deviceCode1);
	
		Mockito.when(repository.save(Matchers.any(DeviceCode.class))).thenAnswer(new Answer<DeviceCode>() {

			@Override
			public DeviceCode answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (DeviceCode) args[0];
			}

		});

	}

	@Test
	public void testCreateNewDeviceCodeWithZeroValidity() {

		DeviceCode code = service.createNewDeviceCode(requestedScopes, clientDetailsEntity1,
				new HashMap<String, String>());
		Mockito.verify(repository).save(code);

	}

	@Test
	public void testCreateNewDeviceCodeWithValidity() {
		clientDetailsEntity1.setDeviceCodeValiditySeconds(1200);
		DeviceCode code = service.createNewDeviceCode(requestedScopes, clientDetailsEntity1,
				new HashMap<String, String>());
		Mockito.verify(repository).save(code);

	}

	@Test
	public void testLookUpByUserCodeNotFound() {
		assertThat(null, equalTo(service.lookUpByUserCode(userCodeNotFound)));
	}

	@Test
	public void testLookUpByUserCodeFound() {
		assertThat(deviceCode1, equalTo(service.lookUpByUserCode(userCodeFound)));
	}

	@Test
	public void testApproveDeviceCode() {
		deviceCode1.setScope(requestedScopes);
		OAuth2Request auth2Request = Mockito.mock(OAuth2Request.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		OAuth2Authentication oauth = new OAuth2Authentication(auth2Request, authentication);
		DeviceCode code = service.approveDeviceCode(deviceCode1, oauth);
		Mockito.verify(repository).save(code);
	}

	@Test
	public void testFindDeviceCodeNotFound() {
		assertThat(null, equalTo(service.findDeviceCode(deviceCodeNotFound, clientDetailsEntity1)));
	}

	@Test
	public void testFindDeviceCodeFound() {
		assertThat(deviceCode1, equalTo(service.findDeviceCode(deviceCodeFound, clientDetailsEntity1)));
	}

	@Test
	public void testFindDeviceCodeClientNotMatch() {
		assertThat(null, equalTo(service.findDeviceCode(deviceCodeClientNotMatch, clientDetailsEntity2)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testClearExpiredDeviceCodes() {		
		expiredDeviceCodes1 = Sets.newHashSet(deviceCode1, deviceCode2);
		expiredDeviceCodes2 = Sets.newHashSet();

		Mockito.when(repository.getExpiredCodes()).thenReturn(expiredDeviceCodes1, expiredDeviceCodes1,expiredDeviceCodes2);
		service.clearExpiredDeviceCodes();
		
		Mockito.verify(repository, atLeast(2)).remove(Matchers.any(DeviceCode.class));	
		Mockito.verify(repository,times(4)).getExpiredCodes();
	}

	@Test
	public void testClearDeviceCode() {
		service.clearDeviceCode(deviceCodeFound, clientDetailsEntity1);		
		Mockito.verify(repository).remove(Matchers.any(DeviceCode.class));
	}
}
