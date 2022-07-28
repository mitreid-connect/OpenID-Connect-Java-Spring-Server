package cz.muni.ics.oidc.server.connectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.oidc.aop.LogTimes;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Connector for calling Perun RPC
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunConnectorRpc {

	public static final String ATTRIBUTES_MANAGER = "attributesManager";
	public static final String FACILITIES_MANAGER = "facilitiesManager";
	public static final String GROUPS_MANAGER = "groupsManager";
	public static final String MEMBERS_MANAGER = "membersManager";
	public static final String REGISTRAR_MANAGER = "registrarManager";
	public static final String SEARCHER = "searcher";
	public static final String USERS_MANAGER = "usersManager";
	public static final String VOS_MANAGER = "vosManager";
	public static final String RESOURCES_MANAGER = "resourcesManager";

	private String perunUrl;
	private String perunUser;
	private String perunPassword;
	private boolean isEnabled;
	private String serializer;

	private int connectionRequestTimeout = 30000;
	private int connectionTimeout = 30000;
	private int responseTimeout = 60000;
	private RestTemplate restTemplate;

	public PerunConnectorRpc(String url,
							 String username,
							 String password,
							 String enabled,
							 String serializer,
							 int connectionRequestTimeout,
							 int connectionTimeout,
							 int responseTimeout)
	{
		this.isEnabled = Boolean.parseBoolean(enabled);
		this.setPerunUrl(url);
		this.setPerunUser(username);
		this.setPerunPassword(password);
		this.setSerializer(serializer);
		this.setConnectionRequestTimeout(connectionRequestTimeout);
		this.setConnectionTimeout(connectionTimeout);
		this.setResponseTimeout(responseTimeout);
	}

	private void setEnabled(String enabled) {
		this.isEnabled = Boolean.parseBoolean(enabled);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	private void setPerunUrl(String perunUrl) {
		if (!StringUtils.hasText(perunUrl)) {
			throw new IllegalArgumentException("Perun URL cannot be null or empty");
		} else if (perunUrl.endsWith("/")) {
			perunUrl = perunUrl.substring(0, perunUrl.length() - 1);
		}

		this.perunUrl = perunUrl;
	}

	private void setPerunUser(String perunUser) {
		if (!StringUtils.hasText(perunUser)) {
			throw new IllegalArgumentException("Perun USER cannot be null or empty");
		}

		this.perunUser = perunUser;
	}

	private void setPerunPassword(String perunPassword) {
		if (!StringUtils.hasText(perunPassword)) {
			throw new IllegalArgumentException("Perun PASSWORD cannot be null or empty");
		}

		this.perunPassword = perunPassword;
	}

	private void setSerializer(String serializer) {
		if (!StringUtils.hasText(serializer)) {
			serializer = "json";
		}

		this.serializer = serializer;
	}

	private void setConnectionRequestTimeout(int connectionRequestTimeout) {
		if (0 >= connectionRequestTimeout) {
			throw new IllegalArgumentException("Connection request timeout must be greater than 0ms");
		}
 		this.connectionRequestTimeout = connectionRequestTimeout;
	}

	private void setConnectionTimeout(int connectionTimeout) {
		if (0 >= connectionTimeout) {
			throw new IllegalArgumentException("Connection timeout must be greater than 0ms");
		}
		this.connectionTimeout = connectionTimeout;
	}

	private void setResponseTimeout(int responseTimeout) {
		if (0 >= responseTimeout) {
			throw new IllegalArgumentException("Response timeout must be greater than 0ms");
		}
		this.responseTimeout = responseTimeout;
	}

	@PostConstruct
	public void postInit() {
		restTemplate = new RestTemplate();
		//HTTP connection pooling, see https://howtodoinjava.com/spring-restful/resttemplate-httpclient-java-config/
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(this.connectionRequestTimeout) // The timeout when requesting a connection from the connection manager
				.setConnectTimeout(this.connectionTimeout) // Determines the timeout in milliseconds until a connection is established
				.setSocketTimeout(this.responseTimeout) // The timeout for waiting for data
				.build();
		PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
		poolingConnectionManager.setMaxTotal(20); // maximum connections total
		poolingConnectionManager.setDefaultMaxPerRoute(18);
		ConnectionKeepAliveStrategy connectionKeepAliveStrategy = (response, context) -> {
			HeaderElementIterator it = new BasicHeaderElementIterator
					(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			while (it.hasNext()) {
				HeaderElement he = it.nextElement();
				String param = he.getName();
				String value = he.getValue();

				if (value != null && param.equalsIgnoreCase("timeout")) {
					return Long.parseLong(value) * 1000;
				}
			}
			return 20000L;
		};
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(poolingConnectionManager)
				.setKeepAliveStrategy(connectionKeepAliveStrategy)
				.build();
		HttpComponentsClientHttpRequestFactory poolingRequestFactory = new HttpComponentsClientHttpRequestFactory();
		poolingRequestFactory.setHttpClient(httpClient);
		//basic authentication
		List<ClientHttpRequestInterceptor> interceptors =
				Collections.singletonList(new BasicAuthorizationInterceptor(perunUser, perunPassword));
		InterceptingClientHttpRequestFactory authenticatingRequestFactory = new InterceptingClientHttpRequestFactory(poolingRequestFactory, interceptors);
		restTemplate.setRequestFactory(authenticatingRequestFactory);
	}

	/**
	 * Make post call to Perun RPC
	 * @param manager String value representing manager to be called. Use constants from this class.
	 * @param method Method to be called (i.e. getUserById)
	 * @param map Map of parameters to be passed as request body
	 * @return Response from Perun
	 */
	@LogTimes
	public JsonNode post(String manager, String method, Map<String, Object> map) {
		if (!this.isEnabled) {
			return JsonNodeFactory.instance.nullNode();
		}

		String actionUrl = perunUrl + '/' + serializer + '/' + manager + '/' + method;
		//make the call
		try {
			log.debug("calling {} with {}", actionUrl, map);
			return restTemplate.postForObject(actionUrl, map, JsonNode.class);
		} catch (HttpClientErrorException ex) {
			MediaType contentType = ex.getResponseHeaders().getContentType();
			String body = ex.getResponseBodyAsString();
			log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + actionUrl + " Content-Type: " + contentType);
			if ("json".equals(contentType.getSubtype())) {
				try {
					log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
				} catch (IOException e) {
					log.error("cannot parse error message from JSON", e);
				}
			} else {
				log.error(ex.getMessage());
			}
			throw new RuntimeException("cannot connect to Perun RPC", ex);
		}
	}

}
