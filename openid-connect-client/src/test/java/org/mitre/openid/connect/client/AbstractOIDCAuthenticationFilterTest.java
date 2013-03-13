package org.mitre.openid.connect.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for OIDCAuthenticationFilter
 * 
 * @author amanda
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class AbstractOIDCAuthenticationFilterTest {

	//@Autowired
	private OIDCAuthenticationFilter filter;
	
	//@Test
	public void testUrlConstruction() {
		
	}

	/**
	 * @return the filter
	 */
	public OIDCAuthenticationFilter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(OIDCAuthenticationFilter filter) {
		this.filter = filter;
	}
	
	
	
}
