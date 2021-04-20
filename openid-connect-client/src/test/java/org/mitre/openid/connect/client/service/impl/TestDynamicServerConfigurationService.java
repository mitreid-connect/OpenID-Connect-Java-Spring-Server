package org.mitre.openid.connect.client.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Loic Gangloff
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDynamicServerConfigurationService {


	private DynamicServerConfigurationService service;

	private String issuer = "https://www.example.com/";

	
	private String json = "{\"issuer\": \"https://www.example.com/\"}";
	
	private String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<configuration>"
			+ "<issuer>https://www.example.com/</issuer>"
			+ "</configuration>";
	
	@Mock
	private HttpClient mockHttpClient;

	@Before
	public void prepare() {
		service = new DynamicServerConfigurationService(mockHttpClient);
	}
	

	@Test
	public void resttemplate_must_have_xml_accept_header() throws ClientProtocolException, IOException {

		HttpResponse response = Mockito.mock(HttpResponse.class);
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(statusLine.getStatusCode()).thenReturn(200);
		Mockito.when(response.getAllHeaders()).thenReturn(new Header[] {});
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(response.getEntity()).thenReturn(new StringEntity(json));
		Mockito.when(mockHttpClient.execute(Mockito.any(), (HttpContext) Mockito.any())).thenReturn(response);
		
		RestTemplate restemplate = Mockito.spy(new RestTemplate(new HttpComponentsClientHttpRequestFactory(mockHttpClient)));

		restemplate.getForObject(issuer, String.class);
		
		ArgumentCaptor<HttpGet> argument = ArgumentCaptor.forClass(HttpGet.class);
		Mockito.verify(mockHttpClient).execute(argument.capture(), (HttpContext) Mockito.any());
		

		Header acceptHeader = argument.getValue().getFirstHeader("Accept");
		List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader.getValue());
		
		assertThat(mediaTypes, hasItem(MediaType.APPLICATION_JSON));
		assertThat(mediaTypes, hasItem(MediaType.APPLICATION_XML));		
		
	}

	@Test
	public void serverconfiguration_must_not_have_xml_accept_header() throws ClientProtocolException, IOException {

		HttpResponse response = Mockito.mock(HttpResponse.class);
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(statusLine.getStatusCode()).thenReturn(200);
		Mockito.when(response.getAllHeaders()).thenReturn(new Header[] {});
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(response.getEntity()).thenReturn(new StringEntity(json));
		Mockito.when(mockHttpClient.execute(Mockito.any(), (HttpContext) Mockito.any())).thenReturn(response);

		ServerConfiguration result = service.getServerConfiguration(issuer);
		
		ArgumentCaptor<HttpGet> argument = ArgumentCaptor.forClass(HttpGet.class);
		Mockito.verify(mockHttpClient).execute(argument.capture(), (HttpContext) Mockito.any());
		

		Header acceptHeader = argument.getValue().getFirstHeader("Accept");
		List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader.getValue());
		
		assertThat(mediaTypes, hasItem(MediaType.APPLICATION_JSON));
		assertThat(mediaTypes, not(hasItem(MediaType.APPLICATION_XML)));		
		
		assertThat(result.getIssuer(), is(equalTo(issuer)));
	}

}
