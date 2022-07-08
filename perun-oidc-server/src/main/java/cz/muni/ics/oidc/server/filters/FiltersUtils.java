package cz.muni.ics.oidc.server.filters;

import static cz.muni.ics.oauth2.web.endpoint.DeviceEndpoint.DEVICE_CODE_SESSION_ATTRIBUTE;
import static cz.muni.ics.oidc.server.filters.AuthProcFilterConstants.PARAM_FORCE_AUTHN;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.oauth2.service.ClientDetailsEntityService;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.saml.SamlPrincipal;
import cz.muni.ics.oidc.saml.SamlProperties;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.claims.ClaimInitContext;
import cz.muni.ics.oidc.server.claims.ClaimSourceInitContext;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.util.StringUtils;

/**
 * Utility class for filters. Contains common methods used by most of filter classes.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class FiltersUtils {

	public static final String NO_VALUE = null;

	public static String fillStringMandatoryProperty(String suffix, AuthProcFilterInitContext ctx) {
		String filled = fillStringPropertyOrDefaultVal(ctx.getProperty(suffix, NO_VALUE), NO_VALUE);

		if (filled == null) {
			throw new IllegalArgumentException(ctx.getFilterName() + " - missing mandatory configuration option: " + suffix);
		}

		return filled;
	}

	public static String fillStringPropertyOrDefaultVal(String suffix, AuthProcFilterInitContext ctx, String defaultVal) {
		return fillStringPropertyOrDefaultVal(ctx.getProperty(suffix, NO_VALUE), defaultVal);
	}

	private static String fillStringPropertyOrDefaultVal(String prop, String defaultVal) {
		if (StringUtils.hasText(prop)) {
			return prop;
		} else {
			return defaultVal;
		}
	}

	public static boolean fillBooleanPropertyOrDefaultVal(String suffix, AuthProcFilterInitContext ctx, boolean defaultVal) {
		return fillBooleanPropertyOrDefaultVal(ctx.getProperty(suffix, NO_VALUE), defaultVal);
	}

	private static boolean fillBooleanPropertyOrDefaultVal(String prop, boolean defaultVal) {
		if (StringUtils.hasText(prop)) {
			return Boolean.parseBoolean(prop);
		} else {
			return defaultVal;
		}
	}

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
		if (request.getParameter("response_type") == null
				&& request.getSession() == null
				&& request.getSession().getAttribute(DEVICE_CODE_SESSION_ATTRIBUTE) == null
		) {
			return null;
		}

		String clientId;
		if (request.getSession() != null && request.getSession().getAttribute(DEVICE_CODE_SESSION_ATTRIBUTE) != null) {
			clientId = ((DeviceCode) request.getSession().getAttribute(DEVICE_CODE_SESSION_ATTRIBUTE)).getClientId();
		} else {
			clientId = authRequestFactory.createAuthorizationRequest(
					FiltersUtils.createRequestMap(request.getParameterMap())).getClientId();
		}

		ClientDetailsEntity client;
		if (!StringUtils.hasText(clientId)) {
			log.debug("cannot extract client - ClientID is null or empty");
			return null;
		}

		client = clientService.loadClientByClientId(clientId);
		if (!StringUtils.hasText(client.getClientName())) {
			log.warn("cannot extract clientName for the clientID '{}'", client.getClientId());
			return null;
		}

		log.debug("returning client '{}' with ID '{}'", client.getClientId(), client.getClientName());
		return client;
	}

	public static PerunUser getPerunUser(HttpServletRequest request,
										 PerunAdapter perunAdapter,
										 SamlProperties samlProperties) {
		return getPerunUser(getSamlCredential(request), perunAdapter, samlProperties);
	}

	public static PerunUser getPerunUser(SAMLCredential samlCredential,
										 PerunAdapter perunAdapter,
										 SamlProperties samlProperties) {
		if (perunAdapter == null) {
			throw new IllegalArgumentException("Cannot fetch user, no adapter passed");
		}
		if (samlCredential == null) {
			return null;
		}
		switch (samlProperties.getUserLookupMode()) {
			case SamlProperties.LOOKUP_ORIGINAL_AUTH:
			case SamlProperties.LOOKUP_STATIC_EXT_SOURCE: {
				return getPerunUserByExtSourceAndExtLogin(perunAdapter, samlCredential, samlProperties);
			}
			case SamlProperties.LOOKUP_PERUN_USER_ID: {
				return getPerunUserById(perunAdapter, samlCredential, samlProperties);
			}
			default: {
				log.debug("Could not find user, invalid user lookup configured");
				return null;
			}
		}
	}

	public static PerunUser getPerunUserByExtSourceAndExtLogin(PerunAdapter perunAdapter, SAMLCredential samlCredential, SamlProperties samlProperties) {
		String extSourceName;
		if (SamlProperties.LOOKUP_STATIC_EXT_SOURCE.equalsIgnoreCase(samlProperties.getUserLookupMode())) {
			extSourceName = samlProperties.getStaticUserExtSource();
		} else {
			extSourceName = getExtSourceName(samlCredential);
		}
		String extLogin = getExtLogin(samlCredential, samlProperties.getUserIdentifierAttribute());
		if (!StringUtils.hasText(extLogin)) {
			return null;
		} else if (!StringUtils.hasText(extSourceName)) {
			return null;
		}
		return perunAdapter.getPreauthenticatedUserId(extLogin, extSourceName);
	}

	public static PerunUser getPerunUserById(PerunAdapter perunAdapter, SAMLCredential samlCredential, SamlProperties samlProperties) {
		String userIdString = getExtLogin(samlCredential, samlProperties.getUserIdentifierAttribute());
		if (!StringUtils.hasText(userIdString)) {
			return null;
		}
		Long userId = null;
		try {
			userId = Long.parseLong(userIdString);
		} catch (NumberFormatException e) {
			log.debug("UserID '{}' cannot be parsed as long", userId);
		}
		if (userId == null) {
			return null;
		}
		return perunAdapter.getPerunUser(userId);
	}

	public static SAMLCredential getSamlCredential(HttpServletRequest request) {
		ExpiringUsernameAuthenticationToken p = (ExpiringUsernameAuthenticationToken) request.getUserPrincipal();
		if (p == null) {
			return null;
		}
		return (SAMLCredential) p.getCredentials();
	}

	public static String getExtLogin(SAMLCredential credential, String idAttribute) {
		if (credential == null) {
			throw new IllegalArgumentException("No SAML credential passed");
		} else if (!StringUtils.hasText(idAttribute)) {
			throw new IllegalArgumentException("No identifier from SAML configured");
		}
		String identifierAttrOid = AuthProcFilterConstants.SAML_IDS.getOrDefault(idAttribute, null);
		if (identifierAttrOid == null) {
			throw new IllegalStateException("SAML credentials has no value for attribute: " + idAttribute);
		}
		return credential.getAttributeAsString(identifierAttrOid);
	}

	public static String getExtSourceName(SAMLCredential credential) {
		if (credential == null) {
			throw new IllegalArgumentException("No SAML credential passed");
		}
		return credential.getRemoteEntityID();
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
	 * @param base Base URL
	 * @param response response object
	 * @param clientId identifier of the service
	 */
	public static void redirectUnapproved(String base, HttpServletResponse response, String clientId, String redirectMapping)
	{
		// cannot register, redirect to unapproved
		Map<String, String> params = new HashMap<>();
		if (clientId != null) {
			params.put("client_id", clientId);
		}

		String redirectUrl = ControllerUtils.createRedirectUrl(base, redirectMapping, params);
		response.reset();
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", redirectUrl);
	}

	/**
	 * Redirect user to the correct page when cannot access the service based on membership.
	 * @param base base URL
	 * @param response Response object
	 * @param facility Facility representing the client
	 * @param user User accessing the service
	 * @param clientIdentifier ClientID
	 * @param facilityAttrsConfig Config object for facility attributes
	 * @param facilityAttributes Actual facility attributes
	 * @param perunAdapter Adapter to call Perun
	 */
	public static void redirectUserCannotAccess(String base,
												HttpServletResponse response,
												Facility facility,
												PerunUser user,
												String clientIdentifier,
												FacilityAttrsConfig facilityAttrsConfig,
												PerunAdapter perunAdapter,
												String redirectUrl)
	{
		Map<String, PerunAttributeValue> facilityAttributes = perunAdapter.getFacilityAttributeValues(
				facility, facilityAttrsConfig.getMembershipAttrNames());
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
					FiltersUtils.redirectToRegistrationForm(base, response, clientIdentifier, facility, user);
					return;
				}
			}
		}

		// cannot register, redirect to unapproved
		log.debug("user cannot register to obtain access, redirecting user '{}' to unapproved page", user);
		FiltersUtils.redirectUnapproved(base, response, clientIdentifier, redirectUrl);
	}

	public static String fillStringMandatoryProperty(String propertyName,
													 String filterName,
													 AuthProcFilterInitContext params) {
		String filled = params.getProperty(propertyName);

		if (!StringUtils.hasText(filled)) {
			throw new IllegalArgumentException("No value configured for '" + propertyName + "' in filter " + filterName);
		}

		return filled;
	}

	private static void redirectToRegistrationForm(String base, HttpServletResponse response,
												   String clientIdentifier, Facility facility, PerunUser user) {
		Map<String, String> params = new HashMap<>();
		params.put("client_id", clientIdentifier);
		params.put("facility_id", facility.getId().toString());
		params.put("user_id", String.valueOf(user.getId()));
		String redirectUrl = ControllerUtils.createRedirectUrl(base,
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

	public static String getUserIdentifier(HttpServletRequest req, String identifierSamlAttribute) {
		return getExtLogin(getSamlCredential(req), identifierSamlAttribute);
	}

	public static String getClientId(HttpServletRequest req) {
		return req.getParameter(AuthProcFilterConstants.PARAM_CLIENT_ID);
	}
}
