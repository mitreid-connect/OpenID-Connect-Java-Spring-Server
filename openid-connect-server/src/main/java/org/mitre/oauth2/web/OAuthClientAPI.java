package org.mitre.oauth2.web;

import java.util.Collection;
import java.util.Set;

import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.exception.DuplicateClientIdException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Controller
@RequestMapping("/manager/oauth/clients/api")
public class OAuthClientAPI {

	@Autowired
	private ClientDetailsEntityService clientService;
	
	private static final Logger logger = LoggerFactory.getLogger(OAuthClientAPI.class);
	
	public OAuthClientAPI() {
		
	}
	
	public OAuthClientAPI(ClientDetailsEntityService clientService) {
		this.clientService = clientService;
	}
	
	// TODO: i think this needs a fancier binding than just strings on the way in
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/add")
    public ModelAndView apiAddClient(ModelAndView modelAndView,
    		@RequestParam String clientId, @RequestParam String clientSecret, 
    		@RequestParam String scope, // space delimited 
    		@RequestParam String grantTypes, // space delimited
    		@RequestParam(required=false) String redirectUri, 
    		@RequestParam String authorities, // space delimited
    		@RequestParam(required=false) String resourceIds, // space delimited
    		@RequestParam(required=false) String name, 
    		@RequestParam(required=false) String description, 
    		@RequestParam(required=false, defaultValue="false") boolean allowRefresh,
    		@RequestParam(required=false) Long accessTokenTimeout, 
    		@RequestParam(required=false) Long refreshTokenTimeout, 
    		@RequestParam(required=false) String owner
    		) {
    	logger.info("apiAddClient - start");
    	ClientDetailsEntity oldClient = clientService.loadClientByClientId(clientId);
    	if (oldClient != null) {
    		throw new DuplicateClientIdException(clientId);
    	}
    	
    	Splitter spaceDelimited = Splitter.on(" ");
    	// parse all of our space-delimited lists
    	Set<String> scopeSet = Sets.newHashSet(spaceDelimited.split(scope));
    	Set<String> grantTypesSet = Sets.newHashSet(spaceDelimited.split(grantTypes)); // TODO: make a stronger binding to GrantTypes
    	logger.info("apiAddClient - before creating authorities list");
    	Set<GrantedAuthority> authoritiesSet = Sets.newHashSet(
    			Iterables.transform(spaceDelimited.split(authorities), new Function<String, GrantedAuthority>() {
    				@Override
    				public GrantedAuthority apply(String auth) {
    					return new SimpleGrantedAuthority(auth);
    				}
    			}));
    	logger.info("apiAddClient - printing client details");
    	logger.info("Making call to create client with " + clientId + ", " + clientSecret 
    			+ ", " + scopeSet + ", " + grantTypesSet + ", " + redirectUri + ", " 
    			+ authoritiesSet + ", " + name + ", " + description + ", " + allowRefresh 
    			+ ", " + accessTokenTimeout + ", " + refreshTokenTimeout + ", " + owner);
    	
    	Set<String> resourceIdSet = Sets.newHashSet(spaceDelimited.split(resourceIds));
    	
		ClientDetailsEntity client = clientService.createClient(clientId, clientSecret, 
    			scopeSet, grantTypesSet, redirectUri, authoritiesSet, resourceIdSet, name, description, 
    			allowRefresh, accessTokenTimeout, refreshTokenTimeout, owner);
    	logger.info("apiAddClient - adding model objects");
    	modelAndView.addObject("entity", client);
    	modelAndView.setViewName("jsonOAuthClientView");
    	logger.info("apiAddClient - end");
    	return modelAndView;
    }

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/delete")
    public ModelAndView apiDeleteClient(ModelAndView modelAndView,
    		@RequestParam String clientId) {
    	
    	ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
    	
    	if (client == null) {
    		throw new ClientNotFoundException("Client not found: " + clientId);
    	}
    	
    	clientService.deleteClient(client);
    	
    	modelAndView.setViewName("management/successfullyRemoved");
    	return modelAndView;
    }

	// TODO: the serializtion of this falls over, don't know why
	@PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/getAll")
    public ModelAndView apiGetAllClients(ModelAndView modelAndView) {
    	
    	Collection<ClientDetailsEntity> clients = clientService.getAllClients();
    	modelAndView.addObject("entity", clients);
    	modelAndView.setViewName("jsonOAuthClientView");
    	
    	return modelAndView;
    }

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/update")
    public ModelAndView apiUpdateClient(ModelAndView modelAndView,
    		@RequestParam String clientId, @RequestParam String clientSecret, 
    		@RequestParam String scope, // space delimited 
    		@RequestParam String grantTypes, // space delimited
    		@RequestParam(required=false) String redirectUri, 
    		@RequestParam String authorities, // space delimited
    		@RequestParam(required=false) String resourceIds, // space delimited
    		@RequestParam(required=false) String name, 
    		@RequestParam(required=false) String description, 
    		@RequestParam(required=false, defaultValue="false") boolean allowRefresh,
    		@RequestParam(required=false) Long accessTokenTimeout, 
    		@RequestParam(required=false) Long refreshTokenTimeout, 
    		@RequestParam(required=false) String owner			
    ) {
    	ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
    	
    	if (client == null) {
    		throw new ClientNotFoundException("Client not found: " + clientId);
    	}
    	
    	Splitter spaceDelimited = Splitter.on(" ");
    	// parse all of our space-delimited lists
    	Set<String> scopeSet = Sets.newHashSet(spaceDelimited.split(scope));
    	Set<String> grantTypesSet = Sets.newHashSet(spaceDelimited.split(grantTypes)); // TODO: make a stronger binding to GrantTypes
    	Set<GrantedAuthority> authoritiesSet = Sets.newHashSet(
    			Iterables.transform(spaceDelimited.split(authorities), new Function<String, GrantedAuthority>() {
    				@Override
    				public GrantedAuthority apply(String auth) {
    					return new SimpleGrantedAuthority(auth);
    				}
    			}));
    	Set<String> resourceIdSet = Sets.newHashSet(spaceDelimited.split(resourceIds));
    	
    	
    	client.setClientSecret(clientSecret);
    	client.setScope(scopeSet);
    	client.setAuthorizedGrantTypes(grantTypesSet);
    	client.setRegisteredRedirectUri(redirectUri);
    	client.setAuthorities(authoritiesSet);
    	client.setResourceIds(resourceIdSet);
    	client.setClientName(name);
    	client.setClientDescription(description);
    	client.setAllowRefresh(allowRefresh);
    	client.setAccessTokenTimeout(accessTokenTimeout);
    	client.setRefreshTokenTimeout(refreshTokenTimeout);
    	client.setOwner(owner);		
    	
    	clientService.updateClient(client, client);
    	
    	modelAndView.addObject("entity", client);
    	modelAndView.setViewName("jsonOAuthClientView");
    	
    	return modelAndView;
    }

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/getById")
    public ModelAndView getClientById(ModelAndView modelAndView,
    		@RequestParam String clientId) {
    	
    	ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
    	
    	if (client == null) {
    		throw new ClientNotFoundException("Client not found: " + clientId);
    	}
    	
    	modelAndView.addObject("entity", client);
    	modelAndView.setViewName("jsonOAuthClientView");
    
    	return modelAndView;
    }

}
