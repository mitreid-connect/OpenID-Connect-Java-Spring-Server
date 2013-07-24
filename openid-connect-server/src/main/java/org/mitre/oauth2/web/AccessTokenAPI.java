package org.mitre.oauth2.web;

import java.util.Set;

import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * REST-ish API for managing access tokens (no PUT; tokens cannot be updated and creation = OAuth).
 * @author Amanda Anganes
 *
 */
@Controller
@RequestMapping("/api/tokens/at")
@PreAuthorize("hasRole('ROLE_USER')")
public class AccessTokenAPI {

	@Autowired
	private OAuth2TokenEntityService tokenService;
	
	private static Logger logger = LoggerFactory.getLogger(AccessTokenAPI.class);
	
	@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
	public String getAll(ModelMap m) {

		Set<OAuth2AccessTokenEntity> allTokens = tokenService.getAllAccessTokens();

		m.put("entity", allTokens);

		return "jsonEntityView";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String getById(@PathVariable("id") Long id, ModelMap m) {
		
		OAuth2AccessTokenEntity token = tokenService.getAccessTokenById(id);
		
		if (token != null) {

			m.put("entity", token);

			return "jsonEntityView";
		} else {

			logger.error("getToken failed; token not found: " + id);

			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested token with id " + id + " could not be found.");
			return "jsonErrorView";
		}
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("id") Long id, ModelMap m) {
		
		OAuth2AccessTokenEntity token = tokenService.getAccessTokenById(id);
		
		if (token != null) {

			tokenService.revokeAccessToken(token);
			m.put("code", HttpStatus.OK);
			return "httpCodeView";
			
		} else {

			logger.error("Delete token failed; token not found: " + id);

			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested token with id " + id + " could not be found.");
			return "jsonErrorView";
		}

	}
	
}
