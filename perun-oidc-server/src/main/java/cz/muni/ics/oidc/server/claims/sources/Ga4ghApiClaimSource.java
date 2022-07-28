package cz.muni.ics.oidc.server.claims.sources;


import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.oidc.server.claims.ClaimSource;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceProduceContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Set;

@Slf4j
public class Ga4ghApiClaimSource extends ClaimSource {

    private static final String ENDPOINT = "endpoint";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String PARAM_NAME = "param_name";
    private static final String EPPN = "{eppn}";

    private final String endpoint;
    private final String username;
    private final String password;
    private final String paramName;

    public Ga4ghApiClaimSource(ClaimSourceInitContext ctx) {
        super(ctx);

        this.endpoint = ClaimUtils.fillStringMandatoryProperty(ENDPOINT, ctx, getClaimName());
        this.username = ClaimUtils.fillStringMandatoryProperty(USERNAME, ctx, getClaimName());
        this.password = ClaimUtils.fillStringMandatoryProperty(PASSWORD, ctx, getClaimName());

        this.paramName = ClaimUtils.fillStringPropertyOrDefaultVal(PARAM_NAME, ctx, EPPN);

        log.debug("{} - initialized", getClaimName());
    }

    @Override
    public Set<String> getAttrIdentifiers() {
        return Collections.emptySet();
    }

    @Override
    public JsonNode produceValue(ClaimSourceProduceContext pctx) {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        JsonNode result = restTemplate.getForObject(endpoint, JsonNode.class, Collections.singletonMap(paramName, pctx.getSub()));

        log.debug("{} - user: {}, GA4GH passports: {}", getClaimName(), pctx.getSub(), result);

        return result;
    }

    private HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory()
    {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient());

        return clientHttpRequestFactory;
    }

    private HttpClient httpClient()
    {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        return HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }
}
