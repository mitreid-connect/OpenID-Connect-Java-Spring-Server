package org.mitre.openid.connect.client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.mitre.openid.connect.view.JwkKeyListView;
import org.mitre.openid.connect.view.X509CertificateView;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;

public class OIDCSignedRequestFilter extends AbstractOIDCAuthenticationFilter implements BeanDefinitionRegistryPostProcessor {
	
	private OIDCServerConfiguration oidcServerConfig;
	
	private JwtSigningAndValidationService signingAndValidationService;

	private String jwkPublishUrl;

	private String x509PublishUrl;

	private BeanDefinitionRegistry registry;

	private String jwkViewName = "jwkKeyList";

	private String x509ViewName;


	protected OIDCSignedRequestFilter() {
		super();

		oidcServerConfig = new OIDCServerConfiguration();
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		// Validating configuration

		Assert.notNull(oidcServerConfig.getAuthorizationEndpointUrl(),
				"An Authorization Endpoint URI must be supplied");

		Assert.notNull(oidcServerConfig.getTokenEndpointUrl(),
				"A Token ID Endpoint URI must be supplied");
		
		Assert.notNull(oidcServerConfig.getClientId(),
				"A Client ID must be supplied");

		Assert.notNull(oidcServerConfig.getClientSecret(),
				"A Client Secret must be supplied");
		
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {

		// Enter AuthenticationFilter here...

		super.attemptAuthentication(request, response);

		if (StringUtils.isNotBlank(request.getParameter("error"))) {

			handleError(request, response);

		} else if (StringUtils.isNotBlank(request.getParameter("code"))) {

			try {
				return handleAuthorizationGrantResponse(request.getParameter("code"), request, oidcServerConfig);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			handleAuthorizationRequest(request, response, oidcServerConfig);
		}

		return null;
	}
	
	@Override
	public void handleAuthorizationRequest(HttpServletRequest request, HttpServletResponse response, 
			OIDCServerConfiguration serverConfiguration) throws IOException {

			Jwt jwt = createAndSignRequestJwt(request, response, serverConfiguration);
			
			Map<String, String> urlVariables = new HashMap<String, String>();
			
			urlVariables.put("request", jwt.toString());
			
			String authRequest = AbstractOIDCAuthenticationFilter.buildURL(serverConfiguration.getAuthorizationEndpointUrl(), urlVariables);

			logger.debug("Auth Request:  " + authRequest);

			response.sendRedirect(authRequest);
	}
	
	public Jwt createAndSignRequestJwt(HttpServletRequest request, HttpServletResponse response, OIDCServerConfiguration serverConfiguration) {
		HttpSession session = request.getSession();
		Jwt jwt = new Jwt();
		JwtClaims claims = jwt.getClaims();
		
		//set parameters to JwtHeader
//		header.setAlgorithm(JwsAlgorithm.getByName(SIGNING_ALGORITHM).toString());
		
		//set parameters to JwtClaims
		claims.setClaim("response_type", "code");
		claims.setClaim("client_id", serverConfiguration.getClientId());
		claims.setClaim("scope", scope);
		
		// build our redirect URI
		String redirectUri = buildRedirectURI(request, null);
		claims.setClaim("redirect_uri", redirectUri);
		session.setAttribute(REDIRECT_URI_SESION_VARIABLE, redirectUri);
		
		//create random nonce and state, save them to the session
		
		String nonce = createNonce(session);
		claims.setClaim("nonce", nonce);
		
		String state = createState(session);
		claims.setClaim("state", state);
		
		try {
			signingAndValidationService.signJwt(jwt);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jwt;
	}

	/**
	 * @return the signingAndValidationService
	 */
	public JwtSigningAndValidationService getSigningAndValidationService() {
		return signingAndValidationService;
	}

	/**
	 * @param signingAndValidationService the signingAndValidationService to set
	 */
	public void setSigningAndValidationService(JwtSigningAndValidationService signingAndValidationService) {
		this.signingAndValidationService = signingAndValidationService;
	}

	/**
     * @param authorizationEndpointURI
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setAuthorizationEndpointUrl(java.lang.String)
     */
    public void setAuthorizationEndpointUrl(String authorizationEndpointURI) {
	    oidcServerConfig.setAuthorizationEndpointUrl(authorizationEndpointURI);
    }

	/**
     * @param clientId
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setClientId(java.lang.String)
     */
    public void setClientId(String clientId) {
	    oidcServerConfig.setClientId(clientId);
    }

	/**
     * @param issuer
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setIssuer(java.lang.String)
     */
    public void setIssuer(String issuer) {
	    oidcServerConfig.setIssuer(issuer);
    }

	/**
     * @param clientSecret
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setClientSecret(java.lang.String)
     */
    public void setClientSecret(String clientSecret) {
	    oidcServerConfig.setClientSecret(clientSecret);
    }

	/**
     * @param tokenEndpointURI
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setTokenEndpointUrl(java.lang.String)
     */
    public void setTokenEndpointUrl(String tokenEndpointURI) {
	    oidcServerConfig.setTokenEndpointUrl(tokenEndpointURI);
    }

	/**
     * @param x509EncryptUrl
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setX509EncryptUrl(java.lang.String)
     */
    public void setX509EncryptUrl(String x509EncryptUrl) {
	    oidcServerConfig.setX509EncryptUrl(x509EncryptUrl);
    }

	/**
     * @param x509SigningUrl
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setX509SigningUrl(java.lang.String)
     */
    public void setX509SigningUrl(String x509SigningUrl) {
	    oidcServerConfig.setX509SigningUrl(x509SigningUrl);
    }

	/**
     * @param jwkEncryptUrl
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setJwkEncryptUrl(java.lang.String)
     */
    public void setJwkEncryptUrl(String jwkEncryptUrl) {
	    oidcServerConfig.setJwkEncryptUrl(jwkEncryptUrl);
    }

	/**
     * @param jwkSigningUrl
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setJwkSigningUrl(java.lang.String)
     */
    public void setJwkSigningUrl(String jwkSigningUrl) {
	    oidcServerConfig.setJwkSigningUrl(jwkSigningUrl);
    }

	/**
     * @param userInfoUrl
     * @see org.mitre.openid.connect.config.OIDCServerConfiguration#setUserInfoUrl(java.lang.String)
     */
    public void setUserInfoUrl(String userInfoUrl) {
	    oidcServerConfig.setUserInfoUrl(userInfoUrl);
    }

	/**
     * @return the jwkPublishUrl
     */
    public String getJwkPublishUrl() {
    	return jwkPublishUrl;
    }

	/**
     * @param jwkPublishUrl the jwkPublishUrl to set
     */
    public void setJwkPublishUrl(String jwkPublishUrl) {
    	this.jwkPublishUrl = jwkPublishUrl;
    }

    /**
     * @return the x509PublishUrl
     */
    public String getX509PublishUrl() {
	    return x509PublishUrl;
    }

	/**
     * @param x509PublishUrl the x509PublishUrl to set
     */
    public void setX509PublishUrl(String x509PublishUrl) {
	    this.x509PublishUrl = x509PublishUrl;
    }

	/**
     * Return a view to publish all keys in JWK format. Only used if jwkPublishUrl is set.
     * @return
     */
	public ModelAndView publishClientJwk() {
		
		// map from key id to signer
		Map<String, JwtSigner> signers = signingAndValidationService.getAllSigners();

		// TODO: check if keys are empty, return a 404 here or just an empty list?
		
		return new ModelAndView(jwkViewName, "signers", signers);
	}

	/**
	 * Return a view to publish all keys in x509 format. Only used if x509publishUrl is set.
	 * @return
	 */
	public ModelAndView publishClientx509() {
		// map from key id to signer
		Map<String, JwtSigner> signers = signingAndValidationService.getAllSigners();
		
		// TODO: check if keys are empty, return a 404 here or just an empty list?
		
		return new ModelAndView(x509ViewName, "signers", signers);
	}
	
	/**
	 * If either the jwkPublishUrl or x509PublishUrl fields are set on this bean, set up a listener on that URL to publish keys.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (!Strings.isNullOrEmpty(getJwkPublishUrl()) || !Strings.isNullOrEmpty(getX509PublishUrl())) {

			// add a mapping to this class
			BeanDefinitionBuilder clientKeyMapping = BeanDefinitionBuilder.rootBeanDefinition(ClientKeyPublisherMapping.class);
			// custom view resolver
			BeanDefinitionBuilder viewResolver = BeanDefinitionBuilder.rootBeanDefinition(JwkViewResolver.class);
			
			if (!Strings.isNullOrEmpty(getJwkPublishUrl())) {
				clientKeyMapping.addPropertyValue("jwkPublishUrl", getJwkPublishUrl());

				// randomize view name to make sure it doesn't conflict with local views
				jwkViewName = "jwkKeyList-" + UUID.randomUUID().toString();
				viewResolver.addPropertyValue("jwkViewName", jwkViewName);

				// view bean
				BeanDefinitionBuilder jwkView = BeanDefinitionBuilder.rootBeanDefinition(JwkKeyListView.class);
				registry.registerBeanDefinition("jwkKeyList", jwkView.getBeanDefinition());
				viewResolver.addPropertyReference("jwk", "jwkKeyList");
			}
			
			if (!Strings.isNullOrEmpty(getX509PublishUrl())) {
				clientKeyMapping.addPropertyValue("x509PublishUrl", getX509PublishUrl());
				
				// randomize view name to make sure it doesn't conflict with local views
				x509ViewName = "x509certs-" + UUID.randomUUID().toString();
				viewResolver.addPropertyValue("x509ViewName", x509ViewName);

				// view bean
				BeanDefinitionBuilder x509View = BeanDefinitionBuilder.rootBeanDefinition(X509CertificateView.class);
				registry.registerBeanDefinition("x509certs", x509View.getBeanDefinition());
				viewResolver.addPropertyReference("x509", "x509certs");
			}
			
			registry.registerBeanDefinition("clientKeyMapping", clientKeyMapping.getBeanDefinition());
			registry.registerBeanDefinition("jwkViewResolver", viewResolver.getBeanDefinition());
			
		}
	    
    }

	/* (non-Javadoc)
     * @see org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.registry = registry;
    }


}
