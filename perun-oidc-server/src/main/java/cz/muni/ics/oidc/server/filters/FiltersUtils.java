package cz.muni.ics.oidc.server.filters;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_FORCE_AUTHN;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.SAML_EPUID;

import com.google.common.base.Strings;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.server.PerunPrincipal;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.FacilityAttrsConfig;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedRegistrationController;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

/**
 * Utility class for filters. Contains common methods used by most of filter classes.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class FiltersUtils {

	private static final RequestMatcher requestMatcher = new AntPathRequestMatcher(PerunFilterConstants.AUTHORIZE_REQ_PATTERN);

	/**
	 * Create map of request params in format key = name, value = paramValue.
	 *
	 * @param parameterMap Original map of parameters
	 * @return Map of parameters
	 */
	public static Map<String, String> createRequestMap(Map<String, String[]> parameterMap) {
		Map<String, String> requestMap = new HashMap<>();
		for (String key : parameterMap.keySet()) {
			String[] val = parameterMap.get(key);
			if (val != null && val.length > 0) {
				requestMap.put(key, val[0]); // add the first value only (which is what Spring seems to do)
			}
		}
		return requestMap;
	}

	/**
	 * Extract client from request
	 *
	 * @param request request to be matched and containing client
	 * @param authRequestFactory authorization request factory
	 * @param clientService service fetching client details
	 * @return extracted client, null if some error occurs
	 */
	@SuppressWarnings("unchecked")
	public static ClientDetailsEntity extractClientFromRequest(HttpServletRequest request,
															   OAuth2RequestFactory authRequestFactory,
															   ClientDetailsEntityService clientService)
	{
		if (!requestMatcher.matches(request) || request.getParameter("response_type") == null) {
			return null;
		}

		AuthorizationRequest authRequest = authRequestFactory.createAuthorizationRequest(
				FiltersUtils.createRequestMap(request.getParameterMap()));

		ClientDetailsEntity client;
		if (Strings.isNullOrEmpty(authRequest.getClientId())) {
			log.debug("cannot extract client - ClientID is null or empty");
			return null;
		}

		client = clientService.loadClientByClientId(authRequest.getClientId());
		if (Strings.isNullOrEmpty(client.getClientName())) {
			log.warn("cannot extract clientName for the clientID '{}'", client.getClientId());
			return null;
		}

		log.debug("returning client '{}' with ID '{}'", client.getClientId(), client.getClientName());
		return client;
	}

	/**
	 * Get Perun user
	 * @param request Request object
	 * @param perunAdapter Adapter of Perun interface
	 * @return Found PerunUser
	 */
	public static PerunUser getPerunUser(HttpServletRequest request, PerunAdapter perunAdapter, String samlIdAttribute) {
		SAMLCredential samlCredential = getSamlCredential(request);
		if (samlCredential == null) {
			return null;
		}
		PerunPrincipal principal = getPerunPrincipal(samlCredential, samlIdAttribute);
		log.debug("fetching Perun user with extLogin '{}' and extSourceName '{}'",
				principal.getExtLogin(), principal.getExtSourceName());
		return perunAdapter.getPreauthenticatedUserId(principal);
	}

	public static SAMLCredential getSamlCredential(HttpServletRequest request) {
		ExpiringUsernameAuthenticationToken p = (ExpiringUsernameAuthenticationToken) request.getUserPrincipal();
		if (p == null) {
			return null;
		}
		return (SAMLCredential) p.getCredentials();
	}

	public static PerunPrincipal getPerunPrincipal(SAMLCredential credential, String idAttribute) {
		if (credential == null) {
			throw new IllegalArgumentException("No SAML credential passed");
		} else if (!StringUtils.hasText(idAttribute)) {
			throw new IllegalArgumentException("No identifier from SAML configured");
		}
		String identifierAttrOid = PerunFilterConstants.SAML_IDS.getOrDefault(idAttribute, null);
		if (identifierAttrOid == null) {
			throw new IllegalStateException("SAML credentials has no value for attribute: " + idAttribute);
		}
		String extLogin = credential.getAttributeAsString(identifierAttrOid);
		String extSourceName = credential.getRemoteEntityID();
		return new PerunPrincipal(extLogin, extSourceName);
	}

	/**
	 * Extract PerunPrincipal from request
	 * @param req request object
	 * @param proxyExtSourceName name of proxy
	 * @return extracted principal or null if not present
	 */
	public static PerunPrincipal extractPerunPrincipal(HttpServletRequest req, String proxyExtSourceName) {
		String extLogin = null;
		String remoteUser = req.getRemoteUser();
		if (StringUtils.hasText(remoteUser)) {
			extLogin = remoteUser;
		} else if (req.getUserPrincipal() != null) {
			extLogin = ((User)req.getUserPrincipal()).getUsername();
		}

		PerunPrincipal principal = null;
		log.error("{}", req.getUserPrincipal());
		log.error("{}", req.getRemoteUser());


		if (extLogin != null) {
			principal = new PerunPrincipal(extLogin, proxyExtSourceName);
			log.debug("extracted principal '{}'", principal);
		} else {
			log.debug("could not extract principal");
		}

		return principal;
	}

	/**
	 * Check if given scope has been requested
	 * @param scopeParam Value of parameter "scope" from request
	 * @param scope Name of scope to be found.
	 * @return TRUE if present, false otherwise
	 */
	public static boolean isScopePresent(String scopeParam, String scope) {
		if (scopeParam == null || scopeParam.trim().isEmpty()) {
			log.trace("no scope has been requested");
			return false;
		}

		String[] scopes = scopeParam.split(" ");
		for (String s : scopes) {
			if (s.equals(scope)) {
				log.trace("scope '{}' has been requested", scope);
				return true;
			}
		}
		log.trace("scope has not been requested");
		return false;
	}

	/**
	 * Build URL of original request, remove forceAuthn parameter.
	 * @param req request wrapper object
	 * @return Rebuilt URL.
	 */
	public static String buildRequestURL(HttpServletRequest req) {
		return buildRequestURL(req, null);
	}

	/**
	 * Build URL of original request, remove forceAuthn parameter, add new parameters if passed.
	 * @param req request wrapper object
	 * @param additionalParams parameters to be added
	 * @return Rebuilt URL.
	 */
	public static String buildRequestURL(HttpServletRequest req, Map<String, String> additionalParams) {
		String returnURL = req.getRequestURL().toString();

		if (req.getQueryString() != null) {
			if (req.getQueryString().contains(PARAM_FORCE_AUTHN)) {
				String queryStr = removeForceAuthParam(req.getQueryString());
				returnURL += ('?' + queryStr);
			} else {
				returnURL += ('?' + req.getQueryString());
			}

			if (additionalParams != null) {
				returnURL += ('&' + additionalParams.entrySet().stream()
						.map(pair -> pair.getKey() + '=' + pair.getValue())
						.collect(Collectors.joining("&")));
			}
		}
		log.debug("returning rebuilt request URL: '{}'", returnURL);
		return returnURL;
	}

	/**
	 * Redirect user to the unapproved page.
	 * @param request original request object
	 * @param response response object
	 * @param clientId identifier of the service
	 */
	public static void redirectUnapproved(HttpServletRequest request, HttpServletResponse response, String clientId, String redirectMapping)
	{
		// cannot register, redirect to unapproved
		Map<String, String> params = new HashMap<>();
		if (clientId != null) {
			params.put("client_id", clientId);
		}

		String redirectUrl = ControllerUtils.createRedirectUrl(request, PerunFilterConstants.AUTHORIZE_REQ_PATTERN,
				redirectMapping, params);
		response.reset();
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", redirectUrl);
	}

	/**
	 * Redirect user to the correct page when cannot access the service based on membership.
	 * @param request Request object
	 * @param response Response object
	 * @param facility Facility representing the client
	 * @param user User accessing the service
	 * @param clientIdentifier ClientID
	 * @param facilityAttrsConfig Config object for facility attributes
	 * @param facilityAttributes Actual facility attributes
	 * @param perunAdapter Adapter to call Perun
	 */
	public static void redirectUserCannotAccess(HttpServletRequest request,
												HttpServletResponse response,
												Facility facility,
												PerunUser user,
												String clientIdentifier,
												FacilityAttrsConfig facilityAttrsConfig,
												Map<String, PerunAttributeValue> facilityAttributes,
												PerunAdapter perunAdapter,
												String redirectUrl)
	{
		if (facilityAttributes.get(facilityAttrsConfig.getAllowRegistrationAttr()).valueAsBoolean()) {
			boolean canRegister = perunAdapter.getAdapterRpc().groupWhereCanRegisterExists(facility);
			if (canRegister) {
				PerunAttributeValue customRegUrlAttr = facilityAttributes.get(facilityAttrsConfig.getRegistrationURLAttr());
				if (customRegUrlAttr != null && customRegUrlAttr.valueAsString() != null) {
					String customRegUrl = facilityAttributes.get(facilityAttrsConfig.getRegistrationURLAttr()).valueAsString();
					customRegUrl = validateUrl(customRegUrl);
					if (customRegUrl != null) {
						// redirect to custom registration URL
						FiltersUtils.redirectToCustomRegUrl(response, customRegUrl, user);
						return;
					}
				}

				if (facilityAttributes.get(facilityAttrsConfig.getDynamicRegistrationAttr()).valueAsBoolean()) {
					// redirect to registration form
					FiltersUtils.redirectToRegistrationForm(request, response, clientIdentifier, facility, user);
					return;
				}
			}
		}

		// cannot register, redirect to unapproved
		log.debug("user cannot register to obtain access, redirecting user '{}' to unapproved page", user);
		FiltersUtils.redirectUnapproved(request, response, clientIdentifier, redirectUrl);
	}

	private static void redirectToRegistrationForm(HttpServletRequest request, HttpServletResponse response,
												   String clientIdentifier, Facility facility, PerunUser user) {
		Map<String, String> params = new HashMap<>();
		params.put("client_id", clientIdentifier);
		params.put("facility_id", facility.getId().toString());
		params.put("user_id", String.valueOf(user.getId()));
		String redirectUrl = ControllerUtils.createRedirectUrl(request, PerunFilterConstants.AUTHORIZE_REQ_PATTERN,
				PerunUnapprovedRegistrationController.REGISTRATION_CONTINUE_MAPPING, params);
		log.debug("redirecting user '{}' to the registration form URL: {}", user, redirectUrl);
		response.reset();
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", redirectUrl);
	}

	private static void redirectToCustomRegUrl(HttpServletResponse response, String customRegUrl, PerunUser user) {
		log.debug("redirecting user '{}' to the custom registration URL: {}", user, customRegUrl);
		response.reset();
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", customRegUrl);
	}

	private static String validateUrl(String customRegUrl) {
		return (customRegUrl == null || customRegUrl.isEmpty()) ? null : customRegUrl;
	}

	private static String removeForceAuthParam(String query) {
		return Arrays.stream(query.split("&"))
				.map(FiltersUtils::splitQueryParameter)
				.filter(pair -> !PARAM_FORCE_AUTHN.equals(pair.getKey()))
				.map(pair -> pair.getKey() + "=" + pair.getValue())
				.collect(Collectors.joining("&"));
	}

	private static Map.Entry<String, String> splitQueryParameter(String it) {
		final int idx = it.indexOf("=");
		final String key = (idx > 0) ? it.substring(0, idx) : it;
		final String value = (idx > 0 && it.length() > idx + 1) ? it.substring(idx + 1) : "";
		return new AbstractMap.SimpleImmutableEntry<>(key, value);
	}

}
