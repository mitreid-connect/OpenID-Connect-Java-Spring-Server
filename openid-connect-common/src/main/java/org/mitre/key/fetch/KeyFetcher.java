package org.mitre.key.fetch;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.jwk.model.EC;
import org.mitre.jwk.model.Jwk;
import org.mitre.jwk.model.Rsa;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KeyFetcher {
	
	private HttpClient httpClient = new DefaultHttpClient();
	private HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
	private RestTemplate restTemplate = new RestTemplate(httpFactory);
	
	public List<Jwk> retrieveJwk(OIDCServerConfiguration serverConfig){
		
		List<Jwk> keys = new ArrayList<Jwk>();
		
		String jsonString = null;

		try {
			jsonString = restTemplate.getForObject(
					serverConfig.getTokenEndpointURI(), String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.");
		}
		
		JsonObject json = (JsonObject) new JsonParser().parse(jsonString);
		JsonArray getArray = json.getAsJsonArray("jwk");
		
		for (int i = 0; i < getArray.size(); i++){
			
			JsonObject object = getArray.get(i).getAsJsonObject();
			String algorithm = object.get("alg").getAsString();
			
			if (algorithm.equals("RSA")){
				Rsa rsa = new Rsa(object);
				keys.add(rsa);
			} else {
				EC ec = new EC(object);
				keys.add(ec);
			}
		}	
		return keys;
	}
	
	public PublicKey retrieveX509Key(OIDCServerConfiguration serverConfig) {
		

		PublicKey key = null;

		try {
			InputStream x509Stream = restTemplate.getForObject(serverConfig.getX509SigningUrl(), InputStream.class);
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) factory.generateCertificate(x509Stream);
			key = cert.getPublicKey();
		} catch (HttpClientErrorException httpClientErrorException) {
			// TODO: add to log instead of this
			httpClientErrorException.printStackTrace();
		} catch (CertificateException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

		return key;
	}
	
	public PublicKey retrieveJwkKey(OIDCServerConfiguration serverConfig) {
		RSAPublicKey pub = null;
		
		try {
			String jwkString = restTemplate.getForObject(serverConfig.getJwkSigningUrl(), String.class);
			JsonObject json = (JsonObject) new JsonParser().parse(jwkString);
			JsonArray getArray = json.getAsJsonArray("keys");
			JsonObject object = getArray.get(0).getAsJsonObject(); // TODO: this only does something on the first key and assumes it's RSA...
				
			byte[] modulusByte = Base64.decodeBase64(object.get("mod").getAsString());
			BigInteger modulus = new BigInteger(modulusByte);
			byte[] exponentByte = Base64.decodeBase64(object.get("exp").getAsString());
			BigInteger exponent = new BigInteger(exponentByte);
					
			RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
			KeyFactory factory = KeyFactory.getInstance("RSA");
			pub = (RSAPublicKey) factory.generatePublic(spec);
		} catch (HttpClientErrorException httpClientErrorException) {
			// TODO: add to log
			httpClientErrorException.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (InvalidKeySpecException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return pub;
	}

}
