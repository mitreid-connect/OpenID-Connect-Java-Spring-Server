package org.opal.web;

import java.util.List;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.UserInfoService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.UserInfoJWTView;
import org.mitre.openid.connect.view.UserInfoView;
import org.opal.data.CrudRepository;
import org.opal.data.model.FIAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


@RestController
@RequestMapping("/"+FIAccessEndpoint.VIEWNAME)
public class FIAccessEndpoint {

	public static final String VIEWNAME = "fiaccess";
	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private CrudRepository crudRepository;
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(FIAccessEndpoint.class);

	/**
	 * Get information about the user as specified in the accessToken included in this request
	 */
	@PreAuthorize("hasRole('ROLE_USER')") // and #oauth2.hasScope('" + SystemScopeService.OPENID_SCOPE + "')")
	@RequestMapping(method= {RequestMethod.GET, RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public String getFIAccess(@RequestParam(value="issuer", required=false) String issuer,
			@RequestHeader(value=HttpHeaders.ACCEPT, required=false) String acceptHeader,
			AbstractAuthenticationToken authentication) {

		OAuth2Authentication auth = null;
		JsonObject model = new JsonObject();
		String message = "";
		try {
			auth = (OAuth2Authentication)authentication;
		}catch(ClassCastException e) {
			message = "FIAccess failed; no OAuth2";
			logger.error(message);
			model.addProperty("error", message);
			return model.toString();
		}
		if (auth == null) {
			message = "FIAccess failed. no principal. Requester is not authorized.";
			logger.error(message);
			model.addProperty("error",message);
			return model.toString();
		}

		String username = auth.getName();
		String clientId = auth.getOAuth2Request().getClientId();
		UserInfo userInfo = userInfoService.getByUsernameAndClientId(username, clientId);

		if (userInfo == null) {
			message = "FIAccess failed; user not found: "+username;
			logger.error(message);
			model.addProperty("error", message);
			return model.toString();
		}

		model.addProperty(UserInfoView.SCOPE, auth.getOAuth2Request().getScope().toString());
		model.add(UserInfoView.USER_INFO, userInfo.toJson());
		model.addProperty(UserInfoJWTView.CLIENT, clientId);

		FIAccess fiAccess = crudRepository.getFIAccessByUsernameAndClientId(username, clientId);
		model.addProperty("SESSION_INFO", fiAccess.getSessionInfo());
		return model.toString();
	}
}

