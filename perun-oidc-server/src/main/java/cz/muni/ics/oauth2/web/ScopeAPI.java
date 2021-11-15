/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
package cz.muni.ics.oauth2.web;

import com.google.gson.Gson;
import cz.muni.ics.oauth2.model.SystemScope;
import cz.muni.ics.oauth2.service.SystemScopeService;
import cz.muni.ics.openid.connect.view.HttpCodeView;
import cz.muni.ics.openid.connect.view.JsonEntityView;
import cz.muni.ics.openid.connect.view.JsonErrorView;
import cz.muni.ics.openid.connect.web.RootController;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/" + ScopeAPI.URL)
@PreAuthorize("hasRole('ROLE_USER')")
@Slf4j
public class ScopeAPI {

	public static final String URL = RootController.API_URL + "/scopes";

	@Autowired
	private SystemScopeService scopeService;

	private Gson gson = new Gson();

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAll(ModelMap m) {

		Set<SystemScope> allScopes = scopeService.getAll();

		m.put(JsonEntityView.ENTITY, allScopes);

		return JsonEntityView.VIEWNAME;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getScope(@PathVariable("id") Long id, ModelMap m) {

		SystemScope scope = scopeService.getById(id);

		if (scope != null) {

			m.put(JsonEntityView.ENTITY, scope);

			return JsonEntityView.VIEWNAME;
		} else {

			log.error("getScope failed; scope not found: " + id);

			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "The requested scope with id " + id + " could not be found.");
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

				m.put(JsonEntityView.ENTITY, scope);

				return JsonEntityView.VIEWNAME;
			} else {

				log.error("updateScope failed; scope ids to not match: got "
						+ existing.getId() + " and " + scope.getId());

				m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				m.put(JsonErrorView.ERROR_MESSAGE, "Could not update scope. Scope ids to not match: got "
						+ existing.getId() + " and " + scope.getId());
				return JsonErrorView.VIEWNAME;
			}

		} else {

			log.error("updateScope failed; scope with id " + id + " not found.");
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not update scope. The scope with id " + id + " could not be found.");
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
			log.error("Error: attempting to save a scope with a value that already exists: " + scope.getValue());
			m.put(HttpCodeView.CODE, HttpStatus.CONFLICT);
			m.put(JsonErrorView.ERROR_MESSAGE, "A scope with value " + scope.getValue() + " already exists, please choose a different value.");
			return JsonErrorView.VIEWNAME;
		}

		scope = scopeService.save(scope);

		if (scope != null && scope.getId() != null) {

			m.put(JsonEntityView.ENTITY, scope);

			return JsonEntityView.VIEWNAME;
		} else {

			log.error("createScope failed; JSON was invalid: " + json);
			m.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not save new scope " + scope + ". The scope service failed to return a saved entity.");
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

			log.error("deleteScope failed; scope with id " + id + " not found.");
			m.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
			m.put(JsonErrorView.ERROR_MESSAGE, "Could not delete scope. The requested scope with id " + id + " could not be found.");
			return JsonErrorView.VIEWNAME;
		}
	}
}
