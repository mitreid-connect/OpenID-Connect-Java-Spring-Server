package cz.muni.ics.oidc.web.controllers;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.muni.ics.oidc.server.PerunScopeClaimTranslationService;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class with common methods used for Controllers.
 *
 * @author Dominik Frantisek Bucik (bucik@ics.muni.cz)
 */
@Slf4j
public class ControllerUtils {

    private static final String LANG_KEY = "lang";
    private static final String REQ_URL_KEY = "reqURL";
    private static final String LANGS_MAP_KEY = "langsMap";
    public static final String LANG_PROPS_KEY = "langProps";

    /**
     * Set language properties for page.

     * @param model model object
     * @param req request object
     * @param localization localization with texts
     */
    public static void setLanguageForPage(Map<String, Object> model, HttpServletRequest req,
                                          Localization localization) {
        String langFromParam = req.getParameter(LANG_KEY);
        String browserLang = req.getLocale().getLanguage();

        List<String> enabledLangs = localization.getEnabledLanguages();
        String langKey = "en";

        if (langFromParam != null
            && enabledLangs.stream().anyMatch(x -> x.equalsIgnoreCase(langFromParam))) {
            langKey = langFromParam;
        } else if (enabledLangs.stream().anyMatch(x -> x.equalsIgnoreCase(browserLang))) {
            langKey = browserLang;
        }

        String reqUrl = req.getRequestURL().toString();

        if (!Strings.isNullOrEmpty(req.getQueryString())) {
            reqUrl += ('?' + req.getQueryString());
        }

        try {
            reqUrl = removeQueryParameter(reqUrl, LANG_KEY);
        } catch (URISyntaxException e) {
            log.warn("Could not remove lang param");
        }

        Properties langProperties = localization.getLocalizationFiles().get(langKey);

        model.put(LANG_KEY, langKey);
        model.put(REQ_URL_KEY, reqUrl);
        model.put(LANGS_MAP_KEY, localization.getEntriesAvailable());
        model.put(LANG_PROPS_KEY, langProperties);
    }

    /**
     * Create redirect URL.
     *
     * @param request     Request object
     * @param removedPart Part of URL to be removed
     * @param pathPart    What to include as Path
     * @param params      Map object of parameters
     * @return Modified redirect URL
     */
    public static String createRedirectUrl(HttpServletRequest request, String removedPart,
                                           String pathPart, Map<String, String> params) {
        String baseUrl = request.getRequestURL().toString();
        int endIndex = baseUrl.indexOf(removedPart);
        if (endIndex > 1) {
            baseUrl = baseUrl.substring(0, endIndex);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(baseUrl);
        builder.append(pathPart);
        if (!params.isEmpty()) {
            builder.append('?');
            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    String encodedParamVal =
                        URLEncoder.encode(entry.getValue(), String.valueOf(StandardCharsets.UTF_8));
                    builder.append(entry.getKey());
                    builder.append('=');
                    builder.append(encodedParamVal);
                    builder.append('&');
                } catch (UnsupportedEncodingException e) {
                    log.warn("Failed to encode param: {}, {}", entry.getKey(), entry.getValue());
                }
            }
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    /**
     * Set all options for page.

     * @param model model object
     * @param req request object
     * @param localization localization with texts
     * @param classes additional html classes
     * @param perunOidcConfig oidc config class
     */
    public static void setPageOptions(Map<String, Object> model, HttpServletRequest req,
                                      Localization localization,
                                      WebHtmlClasses classes, PerunOidcConfig perunOidcConfig) {
        setLanguageForPage(model, req, localization);
        model.put("classes", classes.getWebHtmlClassesProperties());
        model.put("theme", perunOidcConfig.getTheme().toLowerCase());
        model.put("baseURL", perunOidcConfig.getBaseURL());
        model.put("samlResourcesURL", perunOidcConfig.getSamlResourcesURL());
        model.put("contactMail", perunOidcConfig.getEmailContact());
    }

    /**
     * Set scopes and claims for consent page.

     * @param scopeService service for working with scopes
     * @param translationService scope to claim translation service
     * @param model model object
     * @param scope set of scopes
     * @param user userInfo object
     */
    public static void setScopesAndClaims(SystemScopeService scopeService,
                                          PerunScopeClaimTranslationService translationService,
                                          Map<String, Object> model,
                                          Set<String> scope,
                                          UserInfo user) {
        Set<SystemScope> scopes = scopeService.fromStrings(scope);
        Set<SystemScope> sortedScopes = new LinkedHashSet<>(scopes.size());
        Set<SystemScope> systemScopes = scopeService.getAll();

        // sort scopes for display based on the inherent order of system scopes
        for (SystemScope s : systemScopes) {
            if (scopes.contains(s)) {
                sortedScopes.add(s);
            }
        }

        // add in any scopes that aren't system scopes to the end of the list
        sortedScopes.addAll(Sets.difference(scopes, systemScopes));

        Map<String, Map<String, Object>> claimsForScopes = new LinkedHashMap<>();
        if (user != null) {
            JsonObject userJson = user.toJson();
            for (SystemScope systemScope : sortedScopes) {
                Map<String, Object> claimValues = new LinkedHashMap<>();
                Set<String> claims =
                    translationService.getClaimsForScope(systemScope.getValue());
                for (String claim : claims) {
                    if (userJson.has(claim)) {
                        JsonElement claimJson = userJson.get(claim);
                        if (claimJson == null || claimJson.isJsonNull()) {
                            continue;
                        }
                        if (claimJson.isJsonPrimitive()) {
                            claimValues.put(claim, claimJson.getAsString());
                        } else if (claimJson.isJsonArray()) {
                            JsonArray arr = userJson.getAsJsonArray(claim);
                            List<String> values = new ArrayList<>();
                            for (int i = 0; i < arr.size(); i++) {
                                values.add(arr.get(i).getAsString());
                            }
                            claimValues.put(claim, values);
                        }
                    }
                }
                claimsForScopes.put(systemScope.getValue(), claimValues);
            }
        }

        sortedScopes = sortedScopes.stream()
            .filter(systemScope -> {
                if ("offline_access".equalsIgnoreCase(systemScope.getValue())) {
                    claimsForScopes.put("offline_access",
                        Collections.singletonMap("offline_access", true));
                    return true;
                }
                return claimsForScopes.containsKey(systemScope.getValue());
            })
            .sorted((o1, o2) -> compareByClaimsAmount(o1, o2, claimsForScopes))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        model.put("claims", claimsForScopes);
        model.put("scopes", sortedScopes);
    }

    /**
     * Create URL form base and parameters.

     * @param base String with base of the URL
     * @param params Map of parameters to be added
     * @return constructed URL
     */
    public static String createUrl(String base, Map<String, String> params) {
        String url = base;
        if (!params.isEmpty()) {
            url += '?';
            StringBuilder sb = new StringBuilder(url);
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            if (it.hasNext()) {
                while (it.hasNext()) {
                    Map.Entry<String, String> param = it.next();
                    try {
                        if (param.getKey() != null && param.getValue() != null) {
                            String encodedValue = URLEncoder.encode(param.getValue(),
                                StandardCharsets.UTF_8.toString());
                            sb.append(param.getKey()).append('=').append(encodedValue);
                        }
                    } catch (UnsupportedEncodingException e) {
                        //TODO: handle
                    }
                    if (it.hasNext()) {
                        sb.append('&');
                    }
                }
                url = sb.toString();
            }
        }
        return url;
    }

    /**
     * Reconstruct request URL.

     * @param oidcConfig oidc config object
     * @param newPath new path to be appended
     * @return request URL
     */
    public static String constructRequestUrl(PerunOidcConfig oidcConfig, String newPath) {
        String url = oidcConfig.getConfigBean().getIssuer();
        newPath = (url.endsWith("/") ? newPath.replaceFirst("/", "") : newPath);
        return url + newPath;
    }

    private static String removeQueryParameter(String url, String parameterName)
        throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        List<NameValuePair> queryParameters = uriBuilder.getQueryParams()
            .stream()
            .filter(p -> !p.getName().equals(parameterName))
            .collect(Collectors.toList());
        if (queryParameters.isEmpty()) {
            uriBuilder.removeQuery();
        } else {
            uriBuilder.setParameters(queryParameters);
        }
        return uriBuilder.build().toString();
    }

    private static int compareByClaimsAmount(SystemScope o1, SystemScope o2,
                                             Map<String, Map<String, Object>> claimsForScopes) {
        int o1ClaimsSize = claimsForScopes.getOrDefault(o1.getValue(),
            new LinkedHashMap<>()).size();
        int o2ClaimsSize = claimsForScopes.getOrDefault(o2.getValue(),
            new LinkedHashMap<>()).size();
        int compare = Integer.compare(o1ClaimsSize, o2ClaimsSize);
        if (o1ClaimsSize == 0 && compare == 0) {
            return 0;
        } else if (o1ClaimsSize == 0) {
            return 1;
        } else if (o2ClaimsSize == 0) {
            return -1;
        } else {
            return compare;
        }
    }

}
