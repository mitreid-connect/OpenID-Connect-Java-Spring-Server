/**
 * 
 */
package org.mitre.openid.connect.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jricher
 *
 */
public class ConfigurationPropertiesBeanTest {

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
		
		assertEquals(iss, bean.getIssuer());
		assertEquals(title, bean.getTopbarTitle());
		assertEquals(logoUrl, bean.getLogoImageUrl());
		
	}

}
