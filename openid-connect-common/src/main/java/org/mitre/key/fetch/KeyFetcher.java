package org.mitre.key.fetch;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static Logger logger = LoggerFactory.getLogger(KeyFetcher.class);
	
	public JsonArray retrieveJwk(OIDCServerConfiguration serverConfig){
		
		String jsonString = null;

		try {
			jsonString = restTemplate.getForObject(
					serverConfig.getTokenEndpointUrl(), String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			throw new AuthenticationServiceException(
					"Unable to obtain Access Token.");
		}
		
		JsonObject json = (JsonObject) new JsonParser().parse(jsonString);
		JsonArray getArray = json.getAsJsonArray("jwk");
		
		return getArray;
	}
	
	public PublicKey retrieveX509Key(OIDCServerConfiguration serverConfig) {
		

		PublicKey key = null;

		try {
			InputStream x509Stream = restTemplate.getForObject(serverConfig.getX509SigningUrl(), InputStream.class);
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) factory.generateCertificate(x509Stream);
			key = cert.getPublicKey();
		} catch (HttpClientErrorException e) {
			logger.error("HttpClientErrorException in KeyFetcher.java: ", e);
		} catch (CertificateException e) {
			logger.error("CertificateException in KeyFetcher.java: ", e);
        }

		return key;
	}
	
	public PublicKey retrieveJwkKey(OIDCServerConfiguration serverConfig) {
		RSAPublicKey pub = null;
		
		try {
			String jwkString = restTemplate.getForObject(serverConfig.getJwkSigningUrl(), String.class);
			JsonObject json = (JsonObject) new JsonParser().parse(jwkString);
			JsonArray getArray = json.getAsJsonArray("keys");
			for(int i = 0; i < getArray.size(); i++) {
				JsonObject object = getArray.get(i).getAsJsonObject();
				String algorithm = object.get("alg").getAsString();
				
				if(algorithm.equals("RSA")){
					byte[] modulusByte = Base64.decodeBase64(object.get("mod").getAsString());
					BigInteger modulus = new BigInteger(modulusByte);
					byte[] exponentByte = Base64.decodeBase64(object.get("exp").getAsString());
					BigInteger exponent = new BigInteger(exponentByte);
							
					RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
					KeyFactory factory = KeyFactory.getInstance("RSA");
					pub = (RSAPublicKey) factory.generatePublic(spec);
				}
			}

		} catch (HttpClientErrorException e) {
			logger.error("HttpClientErrorException in KeyFetcher.java: ", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("NoSuchAlgorithmException in KeyFetcher.java: ", e);
        } catch (InvalidKeySpecException e) {
        	logger.error("InvalidKeySpecException in KeyFetcher.java: ", e);
        }
		return pub;
	}

}
