package org.mitre.oauth2.service.impl;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.service.BlacklistedSiteService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;

import static junit.framework.Assert.assertEquals;

/**
 * @author r3dlin3
 */

@RunWith(MockitoJUnitRunner.class)
public class BlacklistAwareRedirectResolverTest {

    private String blackListUri = "http://blacklist:9000/oauth/";
    private String standardUrl = "http://localhost:9000/oauth/";
    private String requestedUrlWithArguments = "http://localhost:9000/oauth/someAction?arg2=321";
    private String nonStandardUri = "org.mitre://test/";
    private String requestedUriWithArguments = "org.mitre://test/action?arg1=123&arg2=896";
    private String missingUri = "http://unknown";

    private ClientDetailsEntity client;

    @InjectMocks
    BlacklistAwareRedirectResolver resolver = new BlacklistAwareRedirectResolver();

    @Mock
    private BlacklistedSiteService mockBlacklistedSiteService;



    @Before
    public void setUp() throws Exception {
        Mockito.when(mockBlacklistedSiteService.isBlacklisted(blackListUri)).thenReturn(true);
        Mockito.when(mockBlacklistedSiteService.isBlacklisted(standardUrl)).thenReturn(false);
        Mockito.when(mockBlacklistedSiteService.isBlacklisted(nonStandardUri)).thenReturn(false);
        Mockito.when(mockBlacklistedSiteService.isBlacklisted(missingUri)).thenReturn(false);

        client = new ClientDetailsEntity();
        client.setClientId("clientId");
        client.setGrantTypes(Sets.newHashSet("implicit"));
        client.setRedirectUris(Sets.newHashSet(blackListUri, standardUrl, nonStandardUri));
    }

    @Test
    public void testResolveRedirectUrl() throws Exception {
        String s = resolver.resolveRedirect(standardUrl, client);
        assertEquals(standardUrl, s);

        s = resolver.resolveRedirect(requestedUrlWithArguments, client);
        assertEquals(requestedUrlWithArguments, s);
    }

    @Test
    public void testResolveRedirectUri() throws Exception {
        String s = resolver.resolveRedirect(nonStandardUri, client);
        assertEquals(nonStandardUri,s);

        s = resolver.resolveRedirect(requestedUriWithArguments, client);
        assertEquals(requestedUriWithArguments, s);
    }

    @Test(expected = RedirectMismatchException.class)
    public void testUnknownRedirectUri() throws Exception {
        String s = resolver.resolveRedirect(missingUri, client);

    }

    @Test(expected = InvalidRequestException.class)
    public void testBlacklistedRedirectUri() throws Exception {
        String s = resolver.resolveRedirect(blackListUri, client);

    }
}