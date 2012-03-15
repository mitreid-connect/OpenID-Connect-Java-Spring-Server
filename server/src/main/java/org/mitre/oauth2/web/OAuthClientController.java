/**
 * 
 */
package org.mitre.oauth2.web;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Sets;


/**
 * 
 * Endpoint for managing OAuth2 clients
 * 
 * @author jricher
 *
 */
@Controller
@RequestMapping("/manager/oauth/clients")
public class OAuthClientController {
	
	private final static Set<String> GRANT_TYPES = Sets.newHashSet("authorization_code", "client_credentials", "password", "implicit");

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private OAuth2TokenEntityService tokenService;
	
	private Logger logger;
	
	public OAuthClientController() {
		logger = LoggerFactory.getLogger(this.getClass());
	}
	
	public OAuthClientController(ClientDetailsEntityService clientService, OAuth2TokenEntityService tokenService) {
		this.clientService = clientService;
		this.tokenService = tokenService;
		logger = LoggerFactory.getLogger(this.getClass());
	}
	
	/**
	 * Redirect to the "/" version of the root
	 * @param modelAndView
	 * @return
	 */
	@RequestMapping("")
	public ModelAndView redirectRoot(ModelAndView modelAndView) {
		modelAndView.setViewName("redirect:/manager/oauth/clients/");
		return modelAndView;
	}
	
	/**
	 * View all clients
	 * @param modelAndView
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/")
	public ModelAndView viewAllClients(ModelAndView modelAndView) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		//ClientAuthenticationToken clientAuth = (ClientAuthenticationToken) ((OAuth2Authentication) auth).getClientAuthentication();
		AuthorizationRequest clientAuth = ((OAuth2Authentication) auth).getAuthorizationRequest();
		
		logger.info("Client auth = " + clientAuth);		
		logger.info("Granted authorities = " + clientAuth.getAuthorities().toString());
		
		Collection<ClientDetailsEntity> clients = clientService.getAllClients();
		modelAndView.addObject("clients", clients);
		modelAndView.setViewName("/management/oauth/clientIndex");
		
		return modelAndView;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/add")
	public ModelAndView redirectAdd(ModelAndView modelAndView) {
		modelAndView.setViewName("redirect:/manager/oauth/clients/add/");
		return modelAndView;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/add/")
	public ModelAndView addClientPage(ModelAndView modelAndView) {
		
		Set<GrantedAuthority> auth = Sets.newHashSet();
		auth.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
		
		ClientDetailsEntity client = ClientDetailsEntity.makeBuilder()
				.setScope(Sets.newHashSet("scope"))
				.setAuthorities(auth) // why do we have to pull this into a separate list?
				.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code"))
				.finish();
		modelAndView.addObject("availableGrantTypes", GRANT_TYPES);
		modelAndView.addObject("client", client);
		
		modelAndView.setViewName("/management/oauth/editClient");
		return modelAndView;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/delete/{clientId}")
	public ModelAndView deleteClientConfirmation(ModelAndView modelAndView,
			@PathVariable String clientId) {

		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
		modelAndView.addObject("client", client);
		modelAndView.setViewName("/management/oauth/deleteClientConfirm");
		
		return modelAndView;
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/edit/{clientId}")
	public ModelAndView editClientPage(ModelAndView modelAndView,
			@PathVariable String clientId) {
		
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
		
		modelAndView.addObject("availableGrantTypes", GRANT_TYPES);
		modelAndView.addObject("client", client);
		modelAndView.setViewName("/management/oauth/editClient");
		
		return modelAndView;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/view/{clientId}")
	public ModelAndView viewClientDetails(ModelAndView modelAndView,
			@PathVariable String clientId) {
		
		ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
		
		List<OAuth2AccessTokenEntity> accessTokens = tokenService.getAccessTokensForClient(client);
		List<OAuth2RefreshTokenEntity> refreshTokens = tokenService.getRefreshTokensForClient(client);
		
		modelAndView.addObject("client", client);
		modelAndView.addObject("accessTokens", accessTokens);
		modelAndView.addObject("refreshTokens", refreshTokens);
		
		modelAndView.setViewName("/management/oauth/viewClient");
		return modelAndView;
	}
}
