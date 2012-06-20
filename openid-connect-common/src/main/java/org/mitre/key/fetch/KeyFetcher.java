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
import org.mitre.openid.connect.client.OIDCServerConfiguration;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KeyFetcher {
	
	public static List<Jwk> retrieveJwk(){
		
		OIDCServerConfiguration serverConfig = new OIDCServerConfiguration();
		
		List<Jwk> keys = new ArrayList<Jwk>();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(httpFactory);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		
		String jsonString = null;

		try {
			jsonString = restTemplate.postForObject(
					serverConfig.getTokenEndpointURI(), form, String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.");
		}
		
		JsonObject json = (JsonObject) new JsonParser().parse(jsonString);
		JsonArray getArray = json.getAsJsonArray("jwk");
		
		for(int i = 0; i < getArray.size(); i++){
			
			JsonObject object = getArray.get(i).getAsJsonObject();
			String algorithm = object.get("alg").getAsString();
			
			if(algorithm.equals("RSA")){
				Rsa rsa = new Rsa(object);
				keys.add(rsa);
			}

			else{
				EC ec = new EC(object);
				keys.add(ec);
			}
		}	
		return keys;
	}
	
	public static Key retrieveX509Key() throws CertificateException {
		
		OIDCServerConfiguration serverConfig = new OIDCServerConfiguration();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(httpFactory);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		
		InputStream jsonStream = null;

		try {
			jsonStream = restTemplate.postForObject(
					serverConfig.getTokenEndpointURI(), form, InputStream.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.");
		}
		
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) factory.generateCertificate(jsonStream);
		Key key = cert.getPublicKey();

		return key;
	}
	
	public static Key retrieveJwkKey() throws NoSuchAlgorithmException, InvalidKeySpecException{
		
		OIDCServerConfiguration serverConfig = new OIDCServerConfiguration();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpComponentsClientHttpRequestFactory httpFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(httpFactory);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		
		String jsonString = null;

		try {
			jsonString = restTemplate.postForObject(
					serverConfig.getTokenEndpointURI(), form, String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.");
		}
		
		JsonObject json = (JsonObject) new JsonParser().parse(jsonString);
		JsonArray getArray = json.getAsJsonArray("jwk");
		JsonObject object = getArray.get(0).getAsJsonObject();
			
		byte[] modulusByte = Base64.decodeBase64(object.get("mod").getAsString());
		BigInteger modulus = new BigInteger(modulusByte);
		byte[] exponentByte = Base64.decodeBase64(object.get("exp").getAsString());
		BigInteger exponent = new BigInteger(exponentByte);
				
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PublicKey pub = factory.generatePublic(spec);

		return pub;
	}

}
