package cz.muni.ics.oidc.saml;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_POST_LOGOUT_REDIRECT_URI;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_STATE;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class PerunOidcLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);
        if (getDefaultTargetUrl().equals(targetUrl)) {
            String referer = request.getHeader("Referer");
            if (StringUtils.hasText(referer)) {
                try {
                    MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpUrl(referer)
                            .build().getQueryParams();
                    UriComponentsBuilder builder;
                    if (params != null && params.containsKey(PARAM_POST_LOGOUT_REDIRECT_URI)) {
                        String postLogoutRedirectUri = params.getFirst(PARAM_POST_LOGOUT_REDIRECT_URI);
                        log.trace("Reconstruct from post_logout_redirect_uri: {}", postLogoutRedirectUri);
                        postLogoutRedirectUri = URLDecoder.decode(postLogoutRedirectUri, StandardCharsets.UTF_8.toString());
                        builder = UriComponentsBuilder.fromUriString(postLogoutRedirectUri);
                        if (params.containsKey(PARAM_STATE) && StringUtils.hasText(params.getFirst(PARAM_STATE))) {
                            String state = params.getFirst(PARAM_STATE);
                            log.trace("Add state param to target: {}", state);
                            builder.queryParam(PARAM_STATE, state);
                        }
                        targetUrl = builder.build().toString();
                    }
                } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                    log.debug("Invalid URL or error has occurred when parsing it, do not use it and fall back");
                }
            }
        }
        return targetUrl;
    }

}
