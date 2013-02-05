/**
 * 
 */
package org.mitre.oauth2.web;

import java.util.Set;

import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.SystemScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
public class ScopeAPI {

	
	@Autowired
	private SystemScopeService scopeService;
	
	private Gson gson = new Gson();
	
	@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
	public String getAll(ModelMap m) {
		
		Set<SystemScope> allScopes = scopeService.getAll();
		
		m.put("entity", allScopes);
		
		return "jsonEntityView";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public String getScope(@PathVariable("id") Long id, ModelMap m) {
		
		SystemScope scope = scopeService.getById(id);
		
		if (scope != null) {
		
			m.put("entity", scope);
			
			return "jsonEntityView";
		} else {
			m.put("code", HttpStatus.NOT_FOUND);
			
			return "httpCodeView";
		}
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	public String updateScope(@PathVariable("id") Long id, @RequestBody String json, ModelMap m) {
		
		SystemScope existing = scopeService.getById(id);
		
		SystemScope scope = gson.fromJson(json, SystemScope.class);
		
		if (existing != null && scope != null) {
		
			if (existing.getId().equals(scope.getId())) {
				// sanity check
				
				scope = scopeService.save(scope);
				
				m.put("entity", scope);
				
				return "jsonEntityView";
			} else {
				m.put("code", HttpStatus.BAD_REQUEST);
				
				return "httpCodeView";
			}
			
		} else {
			
			m.put("code", HttpStatus.NOT_FOUND);
			
			return "httpCodeView";
		}
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public String createScope(@RequestBody String json, ModelMap m) {
		SystemScope scope = gson.fromJson(json, SystemScope.class);
		
		scope = scopeService.save(scope);
		
		if (scope != null && scope.getId() != null) {

			m.put("entity", scope);
			
			return "jsonEntityView";
		} else {
			m.put("code", HttpStatus.BAD_REQUEST);
			
			return "httpCodeView";
			
		}
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public String deleteScope(@PathVariable("id") Long id, ModelMap m) {
		SystemScope existing = scopeService.getById(id);
		
		if (existing != null) {

			scopeService.remove(existing);
			
			return "httpCodeView";
		} else {
			
			m.put("code", HttpStatus.NOT_FOUND);
			
			return "httpCodeView";
		}
	}
	
}
