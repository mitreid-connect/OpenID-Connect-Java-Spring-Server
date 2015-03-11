/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package org.mitre.oauth2.web;

import java.util.Set;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/api/scopes")
@PreAuthorize("hasRole('ROLE_USER')")
public class ScopeAPI {

	@Autowired
	private SystemScopeService scopeService;

	@Autowired
	private WebResponseExceptionTranslator providerExceptionHandler;

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ScopeAPI.class);

	private Gson gson = new Gson();

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAll(ModelMap m) {

		Set<SystemScope> allScopes = scopeService.getAll();

		m.put("entity", allScopes);

		return JsonEntityView.VIEWNAME;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getScope(@PathVariable("id") Long id, ModelMap m) {

		SystemScope scope = scopeService.getById(id);

		if (scope != null) {

			m.put("entity", scope);

			return JsonEntityView.VIEWNAME;
		} else {

			logger.error("getScope failed; scope not found: " + id);

			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "The requested scope with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String updateScope(@PathVariable("id") Long id, @RequestBody String json, ModelMap m) {

		SystemScope existing = scopeService.getById(id);

		SystemScope scope = gson.fromJson(json, SystemScope.class);

		if (existing != null && scope != null) {

			if (existing.getId().equals(scope.getId())) {
				// sanity check

				scope = scopeService.save(scope);

				m.put("entity", scope);

				return JsonEntityView.VIEWNAME;
			} else {

				logger.error("updateScope failed; scope ids to not match: got "
						+ existing.getId() + " and " + scope.getId());

				m.put("code", HttpStatus.BAD_REQUEST);
				m.put("errorMessage", "Could not update scope. Scope ids to not match: got "
						+ existing.getId() + " and " + scope.getId());
				return JsonErrorView.VIEWNAME;
			}

		} else {

			logger.error("updateScope failed; scope with id " + id + " not found.");
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "Could not update scope. The scope with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String createScope(@RequestBody String json, ModelMap m) {
		SystemScope scope = gson.fromJson(json, SystemScope.class);

		SystemScope alreadyExists = scopeService.getByValue(scope.getValue());
		if (alreadyExists != null) {
			//Error, cannot save a scope with the same value as an existing one
			logger.error("Error: attempting to save a scope with a value that already exists: " + scope.getValue());
			m.put("code", HttpStatus.CONFLICT);
			m.put("errorMessage", "A scope with value " + scope.getValue() + " already exists, please choose a different value.");
			return JsonErrorView.VIEWNAME;
		}

		scope = scopeService.save(scope);

		if (scope != null && scope.getId() != null) {

			m.put("entity", scope);

			return JsonEntityView.VIEWNAME;
		} else {

			logger.error("createScope failed; JSON was invalid: " + json);
			m.put("code", HttpStatus.BAD_REQUEST);
			m.put("errorMessage", "Could not save new scope " + scope + ". The scope service failed to return a saved entity.");
			return JsonErrorView.VIEWNAME;

		}
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public String deleteScope(@PathVariable("id") Long id, ModelMap m) {
		SystemScope existing = scopeService.getById(id);

		if (existing != null) {

			scopeService.remove(existing);

			return HttpCodeView.VIEWNAME;
		} else {

			logger.error("deleteScope failed; scope with id " + id + " not found.");
			m.put("code", HttpStatus.NOT_FOUND);
			m.put("errorMessage", "Could not delete scope. The requested scope with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}
	}

	@ExceptionHandler(OAuth2Exception.class)
	public ResponseEntity<OAuth2Exception> handleException(Exception e) throws Exception {
		logger.info("Handling error: " + e.getClass().getSimpleName() + ", " + e.getMessage());
		return providerExceptionHandler.translate(e);
	}
}
