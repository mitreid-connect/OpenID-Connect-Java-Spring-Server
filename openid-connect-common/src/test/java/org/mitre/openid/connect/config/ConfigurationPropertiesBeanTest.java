/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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
/**
 * 
 */
package org.mitre.openid.connect.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author jricher
 *
 */
public class ConfigurationPropertiesBeanTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	/**
	 * Test getters and setters for configuration object.
	 */
	@Test
	public void testConfigurationPropertiesBean() {

		// make sure the values that go in come back out unchanged
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();

		String iss = "http://localhost:8080/openid-connect-server/";
		String title = "OpenID Connect Server";
		String logoUrl = "/images/logo.png";

		bean.setIssuer(iss);
		bean.setTopbarTitle(title);
		bean.setLogoImageUrl(logoUrl);
		bean.setForceHttps(true);

		assertEquals(iss, bean.getIssuer());
		assertEquals(title, bean.getTopbarTitle());
		assertEquals(logoUrl, bean.getLogoImageUrl());
		assertEquals(true, bean.isForceHttps());
	}
	@Test
	public void testCheckForHttps() throws HttpsUrlRequiredException {
		ConfigurationPropertiesBean bean = new ConfigurationPropertiesBean();
		
		// issuer is http
		// leave as default, which is unset/false
		try {
			bean.checkForHttps();			
		}
		catch (HttpsUrlRequiredException e) {
			fail("Unexpected HttpsUrlRequiredException for http issuer with default forceHttps, message:" + e.getError());
		}
		
		// set to false
		try {
		bean.setForceHttps(false);
		bean.checkForHttps();
		}
		catch (HttpsUrlRequiredException e) {
			fail("Unexpected HttpsUrlRequiredException for http issuer with forceHttps=false, message:" + e.getError());
		}
		
		// set to true
		
		bean.setForceHttps(true);
		this.expectedException.expect(HttpsUrlRequiredException.class);
		bean.checkForHttps();
		
		// issuer is https
		// leave as default, which is unset/false
		try {
			bean.checkForHttps();			
		}
		catch (HttpsUrlRequiredException e) {
			fail("Unexpected HttpsUrlRequiredException for https issuer with default forceHttps, message:" + e.getError());
		}
		
		// set to false
		try {
		bean.setForceHttps(false);
		bean.checkForHttps();
		}
		catch (HttpsUrlRequiredException e) {
			fail("Unexpected HttpsUrlRequiredException for https issuer with forceHttps=false, message:" + e.getError());
		}
		
		// set to true
		try {
		bean.setForceHttps(true);
		bean.checkForHttps();
		}
		catch (HttpsUrlRequiredException e) {
			fail("Unexpected HttpsUrlRequiredException for https issuer with forceHttps=true, message:" + e.getError());
		}
		
	}

}
