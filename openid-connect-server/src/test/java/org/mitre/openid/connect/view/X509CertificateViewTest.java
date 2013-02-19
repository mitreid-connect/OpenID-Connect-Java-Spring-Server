/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

/**
 * @author jricher
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class X509CertificateViewTest {

	@Autowired
	private ConfigurationPropertiesBean config;
	
	/**
	 * Test method for {@link org.mitre.openid.connect.view.X509CertificateView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void testRenderMergedOutputModelMapOfStringObjectHttpServletRequestHttpServletResponse() throws IOException, NoSuchAlgorithmException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		
		X509CertificateView view = new X509CertificateView();
		
		// set the config bean
		Field field = view.getClass().getDeclaredField("config");
		field.setAccessible(true);
		field.set(view, config);
		
		Map<String, Object> model = createMock(Map.class);
		HttpServletRequest request = createMock(HttpServletRequest.class);
		HttpServletResponse response = createMock(HttpServletResponse.class);
		
		// make a signer from a randomly-generated key
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");		
		KeyPair kp = generator.generateKeyPair();
		
		// add that signer to the map
		Map<String, PublicKey> keys = new HashMap<String, PublicKey>();
		keys.put("rsa1", kp.getPublic());
		
		expect(model.get("keys")).andReturn(keys);
		
		// throw away output for now
		expect(response.getOutputStream()).andReturn(new ServletOutputStream() {
			public void write(int b) throws IOException { }
		});
			
		response.setContentType("application/x-pem-file");
		expectLastCall();
		
		replay(model);
		replay(request);
		replay(response);
		
		view.renderMergedOutputModel(model , request, response);
		
	}

}
