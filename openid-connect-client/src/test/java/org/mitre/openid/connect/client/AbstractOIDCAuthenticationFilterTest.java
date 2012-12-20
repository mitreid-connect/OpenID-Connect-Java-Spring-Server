package org.mitre.openid.connect.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for AbstractOIDCAuthenticationFilter
 * 
 * @author amanda
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class AbstractOIDCAuthenticationFilterTest {

	//@Autowired
	private AbstractOIDCAuthenticationFilter filter;
	
	//@Test
	public void testUrlConstruction() {
		
	}

	/**
	 * @return the filter
	 */
	public AbstractOIDCAuthenticationFilter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(AbstractOIDCAuthenticationFilter filter) {
		this.filter = filter;
	}
	
	
	
}
