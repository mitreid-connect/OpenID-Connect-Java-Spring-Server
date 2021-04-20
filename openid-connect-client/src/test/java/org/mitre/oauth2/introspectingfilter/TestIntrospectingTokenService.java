package org.mitre.oauth2.introspectingfilter;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.introspectingfilter.service.IntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.JWTParsingIntrospectionConfigurationService;
import org.mitre.oauth2.introspectingfilter.service.impl.StaticIntrospectionConfigurationService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.impl.DynamicServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Loic Gangloff
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestIntrospectingTokenService {


	private IntrospectingTokenService service;

	private String introspectionUrl = "http://www.example.com";
	private RegisteredClient client = new RegisteredClient();
	
	private String json = "{\"active\":true,\"scope\":\"sub openid profile\",\"exp\":1591344840,\"sub\":\"test@example.com\",\"username\":\"test@example.com\",\"expires_at\":\"2020-06-05T10:14:00+0200\",\"client_id\":\"a-client-id\",\"token_type\":\"Bearer\"}";
	
	private String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<introspectResponse>" +
				"<active>true</active>" +
				"<client_id>a-client-id</client_id>" +
				"<exp>1591344840</exp>n" +
				"<expires_at>2020-06-05T10:14:00+0200</expires_at>" +
				"<scope>sub openid profile</scope>" +
				"<sub>test@example.com</sub>" +
				"<token_type>Bearer</token_type>" +
				"<username>test@example.com</username>"
			+ "</introspectResponse>";
	
	@Mock
	private HttpClient mockHttpClient;

	@Before
	public void prepare() {
		client.setClientId("a-client-id");
		client.setClientSecret("a-client-secret");

		StaticIntrospectionConfigurationService introspectionConfig = new StaticIntrospectionConfigurationService();
		introspectionConfig.setClientConfiguration(client);
		introspectionConfig.setIntrospectionUrl(introspectionUrl);

		service = new IntrospectingTokenService(mockHttpClient);
		service.setIntrospectionConfigurationService(introspectionConfig);
	}
	

	@Test
	public void resttemplate_must_have_xml_accept_header() throws IOException {

		HttpResponse response = Mockito.mock(HttpResponse.class);
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(statusLine.getStatusCode()).thenReturn(200);
		Mockito.when(response.getAllHeaders()).thenReturn(new Header[] {});
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(response.getEntity()).thenReturn(new StringEntity(json));
		Mockito.when(mockHttpClient.execute(Mockito.any(), (HttpContext) Mockito.any())).thenReturn(response);
		
		RestTemplate restemplate = Mockito.spy(new RestTemplate(new HttpComponentsClientHttpRequestFactory(mockHttpClient)));

		restemplate.postForObject(introspectionUrl, "a-token", String.class);
		
		ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
		Mockito.verify(mockHttpClient).execute(argument.capture(), (HttpContext) Mockito.any());
		

		Header acceptHeader = argument.getValue().getFirstHeader("Accept");
		List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader.getValue());
		
		assertThat(mediaTypes, hasItem(MediaType.APPLICATION_JSON));
		assertThat(mediaTypes, hasItem(MediaType.APPLICATION_XML));		
		
	}

	@Test
	public void introspectingtokenservice_must_not_have_xml_accept_header() throws IOException {

		HttpResponse response = Mockito.mock(HttpResponse.class);
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(statusLine.getStatusCode()).thenReturn(200);
		Mockito.when(response.getAllHeaders()).thenReturn(new Header[] {});
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(response.getEntity()).thenReturn(new StringEntity(json));
		Mockito.when(mockHttpClient.execute(Mockito.any(), (HttpContext) Mockito.any())).thenReturn(response);

		OAuth2Authentication result = service.loadAuthentication("a-token");
		
		ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
		Mockito.verify(mockHttpClient).execute(argument.capture(), (HttpContext) Mockito.any());
		

		Header acceptHeader = argument.getValue().getFirstHeader("Accept");
		List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader.getValue());
		
		assertThat(mediaTypes, hasItem(MediaType.APPLICATION_JSON));
		assertThat(mediaTypes, not(hasItem(MediaType.APPLICATION_XML)));		

	}

}
