package org.opal.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ExternalAuthenticationManager {
	private static final Logger logger = LoggerFactory.getLogger(ExternalAuthenticationManager.class);
	private Map<String, ExternalAuthorities> authorities;

	public Map<String, ExternalAuthorities> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Map<String, ExternalAuthorities> authorities) {
		this.authorities = authorities;
	}

	
	public JsonObject getAccessToken(String issuer, String code, String state) {
		try {
			ExternalAuthorities eAuth = this.authorities.get(issuer);
			String client_id = eAuth.getClient_id();
			String client_secret = eAuth.getClient_secret();
			String access_token_uri = eAuth.getAccess_token_uri();
			
			// 1. Get Access token
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			ArrayList<MediaType> accept = new ArrayList<>();
			accept.add(MediaType.APPLICATION_JSON_UTF8);
			headers.setAccept(accept);
			
			MultiValueMap<String, String> reqParams = new LinkedMultiValueMap<>();
			reqParams.add("client_id", client_id);
			reqParams.add("client_secret", client_secret);
			reqParams.add("code", code);
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(reqParams, headers);
			
			RestTemplate restTemplate = this.getRestTemplate();			
			ResponseEntity<String> response = restTemplate.postForEntity(access_token_uri, request, String.class);
			Gson gson = new Gson();
			JsonObject json = gson.fromJson(response.getBody(), JsonObject.class);
			return json;
		}catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public JsonObject getUserInfo(String issuer, String access_token) {
		ExternalAuthorities eAuth = this.authorities.get(issuer);
		String user_info_uri = eAuth.getUser_info_uri();
		if(user_info_uri==null || user_info_uri.isEmpty()) {
			return null;
		}
		RestTemplate restTemplate = this.getRestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(user_info_uri+"?access_token="+access_token, String.class);
		Gson gson = new Gson();
		JsonObject json = gson.fromJson(response.getBody(), JsonObject.class);
		return json;
	}

	@SuppressWarnings(value = { "unused" })
	private Map<String, String> parseParams(String t){
		Map<String, String> respParams = new HashMap<>();
		String[] parts = t.split("&|=");
		for(int i=0;i < parts.length -1; i+=2) {
			respParams.put(parts[i],  parts[i+1]);
		}
		return respParams;
	}
	
	private RestTemplate getRestTemplate() {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			//Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("http.proxy.fmr.com", 8000));
			//requestFactory.setProxy(proxy);
			return new RestTemplate(requestFactory);
	}
}
