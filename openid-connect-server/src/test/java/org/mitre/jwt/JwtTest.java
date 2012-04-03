package org.mitre.jwt;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;

import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.HmacSigner;
import org.mitre.jwt.signer.impl.PlaintextSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.impl.KeyStore;
import org.mitre.jwt.signer.service.impl.KeyStoreTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SuppressWarnings("deprecation")
// BC sez X509V3CertificateGenerator is deprecated and the docs say to use
// another, but it seemingly isn't included jar...
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
public class JwtTest {

	@Autowired
	KeyStore keystore;

	@Test
	public void testGenerateHmacSignature() {
		Jwt jwt = new Jwt();
		jwt.getHeader().setType("JWT");
		jwt.getHeader().setAlgorithm("HS256");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);

		byte[] key = null;
		JwtSigner signer;

		// sign it
		try {
			key = "secret".getBytes("UTF-8");

			signer = new HmacSigner();
			((HmacSigner) signer).setPassphrase(key);
			((HmacSigner) signer).afterPropertiesSet();
			
			signer.sign(jwt);
	
			/*
			 * Expected string based on the following structures, serialized exactly
			 * as follows and base64 encoded:
			 * 
			 * header: {"typ":"JWT","alg":"HS256"} claims:
			 * {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
			 * 
			 * Expected signature: iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E
			 */
			String signature = "p-63Jzz7mgi3H4hvW6MFB7lmPRZjhsL666MYkmpX33Y";
			String expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ." + signature;
	
			String actual = jwt.toString();
	
			assertThat(actual, equalTo(expected));
			assertThat(jwt.getSignature(), equalTo(signature));

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testGenerateRsaSignature() throws Exception {

		// Hard code the private/public key so as not to depend on it being in
		// the keystore...

		RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(
				new BigInteger(
						"AD6E684550542947AD95EF9BACDC0AC2C9168C6EB3212D378C23E5539266111DB2E5B4D42B1E47EB4F7A65DB63D9782C72BC365492FD1E5C7B4CD2174C611668C29013FEDE22619B3F58DA3531BB6C02B3266768B7895CBDAFB3F9AC7A7B2F3DB17EF4DCF03BD2575604BDE0A01BB1FB7B0E733AD63E464DB4D7D89626297A214D7CECCD0C50421A322A01E9DCEA23443F6A9339576B31DFA504A133076394562CB57F3FDEDB26F9A82BED2F6D52D6F6BF8286E2497EF0B5C8456F32B4668F5A9F5FCD3781345DDDB749792C37238A53D18FD976C0C9D1F1E211F1A4A9AAE679C45B92D1741EF0D3C3F373232CE7FB93E9BC461E1C508A20B74E7E3361B3C527",
						16),
				new BigInteger(
						"627CDD67E75B33EA0990A8F64DEED389942A62EB867C23B274B9F9C440D2078C47089D6D136369D21E5B52B688F8797F3C54D7C1A58B6A8F7851C2C90A4DE42CEFB864328B31191ED19582AD4CA5B38BC0F2E12C9D75BB1DD946AA55A1648D0A4ADEDEED0CDBDBF24EDDF87A345225FBBB0114BCE7E78B831B5CAC197068837AB0B3F07157952A05F67A72B9852972C704B6B32A70C3BB3DEB186936B0F7D6ABE012DEB89BC2DBE1F88AE7A28C06C53D1FB2459E58D8ED266E3BFC28266981D2A5F624D36555DD64F410461ADA5D53F448BA5EEBBD4BCEC3AF53285FB394650D7B3BFB06712E081AAD160EED6E83A3EA2D092712C07A6331209F62D27184BFC9",
						16));

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		PrivateKey privateKey = keyFactory.generatePrivate(privateSpec);

		RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(
				new BigInteger(
						"AD6E684550542947AD95EF9BACDC0AC2C9168C6EB3212D378C23E5539266111DB2E5B4D42B1E47EB4F7A65DB63D9782C72BC365492FD1E5C7B4CD2174C611668C29013FEDE22619B3F58DA3531BB6C02B3266768B7895CBDAFB3F9AC7A7B2F3DB17EF4DCF03BD2575604BDE0A01BB1FB7B0E733AD63E464DB4D7D89626297A214D7CECCD0C50421A322A01E9DCEA23443F6A9339576B31DFA504A133076394562CB57F3FDEDB26F9A82BED2F6D52D6F6BF8286E2497EF0B5C8456F32B4668F5A9F5FCD3781345DDDB749792C37238A53D18FD976C0C9D1F1E211F1A4A9AAE679C45B92D1741EF0D3C3F373232CE7FB93E9BC461E1C508A20B74E7E3361B3C527",
						16), new BigInteger("10001", 16));

		PublicKey publicKey = keyFactory.generatePublic(publicSpec);

		// BC sez X509V3CertificateGenerator is deprecated and the docs say to
		// use another, but it seemingly isn't included jar...
		X509V3CertificateGenerator v3CertGen = KeyStoreTest.createCertificate("testGenerateRsaSignature", 30, 30);

		v3CertGen.setPublicKey(publicKey);
		v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		// BC docs say to use another, but it seemingly isn't included...
		X509Certificate certificate = v3CertGen.generateX509Certificate(privateKey);

		// if exist, overwrite
		java.security.KeyStore ks = keystore.getKeystore();
		ks.setKeyEntry("testGenerateRsaSignature", privateKey, RsaSigner.DEFAULT_PASSWORD.toCharArray(), new java.security.cert.Certificate[] { certificate });

		keystore.setKeystore(ks);

		Jwt jwt = new Jwt();
		jwt.getHeader().setType("JWT");
		jwt.getHeader().setAlgorithm("RS256");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);

		JwtSigner signer = new RsaSigner(JwsAlgorithm.RS256.toString(),	keystore, "testGenerateRsaSignature", RsaSigner.DEFAULT_PASSWORD);
		((RsaSigner) signer).afterPropertiesSet();

		/*
		 * Expected string based on the following structures, serialized exactly
		 * as follows and base64 encoded:
		 * 
		 * header: {"typ":"JWT","alg":"HS256"} claims:
		 * {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
		 * 
		 * Expected signature: dSRvtD-ExzGN-
		 * fRXd1wRZOPo1JFPuqgwvaIKp8jgcyMXJegy6IUjssfUfUcICN5yvh0ggOMWMeWkwQ7
		 * -PlXMJWymdhXVI3BOpNt7ZOB2vMFYSOOHNBJUunQoe1lmNxuHQdhxqoHahn3u1cLDXz
		 * -xx-
		 * JELduuMmaDWqnTFPodVPl45WBKHaQhlOiFWj3ZClUV2k5p2yBT8TmxekL8gWwgVbQk5yPnYOs
		 * -PcMjzODc9MZX4yI10ZSCSDciwf-
		 * rgkQLT7wW4uZCoqTZ7187sCodHd6nw3nghqbtqN05fQ3Yq7ykwaR8pdQBFb2L9l7DhLLuXIREDKIFUHBSUs8OnvXFMg
		 */

		String signature = "dSRvtD-ExzGN-fRXd1wRZOPo1JFPuqgwvaIKp8jgcyMXJegy6IUjssfUfUcICN5yvh0ggOMWMeWkwQ7-PlXMJWymdhXVI3BOpNt7ZOB2vMFYSOOHNBJUunQoe1lmNxuHQdhxqoHahn3u1cLDXz-xx-JELduuMmaDWqnTFPodVPl45WBKHaQhlOiFWj3ZClUV2k5p2yBT8TmxekL8gWwgVbQk5yPnYOs-PcMjzODc9MZX4yI10ZSCSDciwf-rgkQLT7wW4uZCoqTZ7187sCodHd6nw3nghqbtqN05fQ3Yq7ykwaR8pdQBFb2L9l7DhLLuXIREDKIFUHBSUs8OnvXFMg";
		String expected = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ"
				+ "." + signature;

		signer.sign(jwt);

		String actual = jwt.toString();
		
		assertThat(signer.verify(actual), equalTo(true));
		assertThat(actual, equalTo(expected));
		assertThat(jwt.getSignature(), equalTo(signature));
	}

	@Test
	public void testParse() {
		String source = "eyJhbGciOiJub25lIn0.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";

		Jwt jwt = Jwt.parse(source);

		assertThat(jwt.getHeader().getAlgorithm(), equalTo(PlaintextSigner.PLAINTEXT));
		assertThat(jwt.getClaims().getIssuer(), equalTo("joe"));
		assertThat(jwt.getClaims().getExpiration(), equalTo(new Date(1300819380L * 1000L)));
		assertThat((Boolean) jwt.getClaims().getClaim("http://example.com/is_root"), equalTo(Boolean.TRUE));

	}

	@Test
	public void testToStringPlaintext() {
		Jwt jwt = new Jwt();
		jwt.getHeader().setAlgorithm("none");
		jwt.getClaims().setExpiration(new Date(1300819380L * 1000L));
		jwt.getClaims().setIssuer("joe");
		jwt.getClaims().setClaim("http://example.com/is_root", Boolean.TRUE);

		// sign it with a blank signature
		JwtSigner signer = new PlaintextSigner();
		signer.sign(jwt);

		/*
		 * Expected string based on the following structures, serialized exactly
		 * as follows and base64 encoded:
		 * 
		 * header: {"alg":"none"} claims:
		 * {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
		 */
		String expected = "eyJhbGciOiJub25lIn0.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.";

		String actual = jwt.toString();

		assertThat(actual, equalTo(expected));

	}

	@Test
	public void testValidateHmacSignature() {
		
		byte[] key = null;
		JwtSigner signer;
		
		// sign it
		try {
			key = "secret".getBytes("UTF-8");

			signer = new HmacSigner();
			((HmacSigner) signer).setPassphrase(key);
			((HmacSigner) signer).afterPropertiesSet();

			/*
			 * Token string based on the following structures, serialized exactly as
			 * follows and base64 encoded:
			 * 
			 * header: {"typ":"JWT","alg":"HS256"} claims:
			 * {"exp":1300819380,"iss":"joe","http://example.com/is_root":true}
			 * 
			 * Expected signature: iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E
			 */
			String jwtString = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjEzMDA4MTkzODAsImlzcyI6ImpvZSIsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.iGBPJj47S5q_HAhSoQqAdcS6A_1CFj3zrLaImqNbt9E";
	
			boolean valid = signer.verify(jwtString);
	
			assertThat(valid, equalTo(Boolean.TRUE));

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
