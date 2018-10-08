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
/**
 *
 */
package org.mitre.openid.connect.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.host.util.HostUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.BeanCreationException;

/**
 * @author jricher
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HostUtils.class)
public class ConfigurationPropertiesBeanTest {
	
	@Before
	public void setup() {
		PowerMockito.mockStatic(HostUtils.class);
	}

	/**
	 * Test getters and setters for configuration object.
	 */
	@Test
	public void testConfigurationPropertiesBean() {

		// make sure the values that go in come back out unchanged
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();

		String iss = "https://localhost:8080/openid-connect-server/";
		PowerMockito.when(HostUtils.getCurrentRunningFullPath()).thenReturn(iss);
		String title = "OpenID Connect Server";
		String logoUrl = "/images/logo.png";

		bean.setTopbarTitle(title);
		bean.setLogoImageUrl(logoUrl);
		bean.setForceHttps(true);

		assertEquals(iss, bean.getIssuer());
		assertEquals(title, bean.getTopbarTitle());
		assertEquals(logoUrl, bean.getLogoImageUrl());
		assertEquals(true, bean.isForceHttps());
	}

	@Test
	public void testCheckForHttpsIssuerHttpDefaultFlag() {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();

		// issuer is http
		// leave as default, which is unset/false
		try {
			PowerMockito.when(HostUtils.getCurrentRunningFullPath()).thenReturn("http://localhost:8080/openid-connect-server/");
			bean.getIssuer();
		} catch (BeanCreationException e) {
			fail("Unexpected BeanCreationException for http issuer with default forceHttps, message:" + e.getMessage());
		}
	}

	@Test
	public void testCheckForHttpsIssuerHttpFalseFlag() {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();
		// issuer is http
		// set to false
		try {
			bean.setForceHttps(false);
			PowerMockito.when(HostUtils.getCurrentRunningFullPath()).thenReturn("http://localhost:8080/openid-connect-server/");
			bean.getIssuer();
		} catch (BeanCreationException e) {
			fail("Unexpected BeanCreationException for http issuer with forceHttps=false, message:" + e.getMessage());
		}
	}

	@Test(expected = BeanCreationException.class)
	public void testCheckForHttpsIssuerHttpTrueFlag() {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();
		// issuer is http
		// set to true
		bean.setForceHttps(true);
		PowerMockito.when(HostUtils.getCurrentRunningFullPath()).thenReturn("http://localhost:8080/openid-connect-server/");
		bean.getIssuer();
	}

	@Test
	public void testCheckForHttpsIssuerHttpsDefaultFlag() {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();
		// issuer is https
		// leave as default, which is unset/false
		try {
			PowerMockito.when(HostUtils.getCurrentRunningFullPath()).thenReturn("https://localhost:8080/openid-connect-server/");
			bean.getIssuer();
		} catch (BeanCreationException e) {
			fail("Unexpected BeanCreationException for https issuer with default forceHttps, message:" + e.getMessage());
		}
	}

	@Test
	public void testCheckForHttpsIssuerHttpsFalseFlag() {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();
		// issuer is https
		// set to false
		try {
			PowerMockito.when(HostUtils.getCurrentRunningFullPath()).thenReturn("https://localhost:8080/openid-connect-server/");
			bean.setForceHttps(false);
			bean.getIssuer();
		} catch (BeanCreationException e) {
			fail("Unexpected BeanCreationException for https issuer with forceHttps=false, message:" + e.getMessage());
		}
	}

	@Test
	public void testCheckForHttpsIssuerHttpsTrueFlag() {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();
		// issuer is https
		// set to true
		try {
			PowerMockito.when(HostUtils.getCurrentRunningFullPath()).thenReturn("https://localhost:8080/openid-connect-server/");
			bean.setForceHttps(true);
			bean.getIssuer();
		} catch (BeanCreationException e) {
			fail("Unexpected BeanCreationException for https issuer with forceHttps=true, message:" + e.getMessage());
		}

	}

	@Test
	public void testShortTopbarTitle() {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();
		bean.setTopbarTitle("LONG");
		assertEquals("LONG", bean.getShortTopbarTitle());
		bean.setShortTopbarTitle("SHORT");
		assertEquals("SHORT", bean.getShortTopbarTitle());
	}

}
