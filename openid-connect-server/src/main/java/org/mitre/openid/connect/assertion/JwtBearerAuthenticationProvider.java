/**
 * 
 */
package org.mitre.openid.connect.assertion;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.mitre.jwt.model.Jwt;
import org.mitre.jwt.model.JwtClaims;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.DefaultJwtSigningAndValidationService;
import org.mitre.key.fetch.KeyFetcher;
import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author jricher
 *
 */
public class JwtBearerAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtBearerAuthenticationProvider.class);

	// map of validators, load keys for clients
	private Map<ClientDetailsEntity, JwtSigningAndValidationService> validators = new HashMap<ClientDetailsEntity, JwtSigningAndValidationService>();
	
	// Allow for time sync issues by having a window of X seconds.
	private int timeSkewAllowance = 300;

	// to load clients
	@Autowired
	private ClientDetailsEntityService clientService;
	
	// to get our server's issuer url
	@Autowired
	private ConfigurationPropertiesBean config;
	
	/**
	 * Try to validate the client credentials by parsing and validating the JWT.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	
    	JwtBearerAssertionAuthenticationToken jwtAuth = (JwtBearerAssertionAuthenticationToken)authentication;
    	
    	
    	try {
    		ClientDetailsEntity client = clientService.loadClientByClientId(jwtAuth.getClientId());
    		
    		JwtSigningAndValidationService validator = getValidatorForClient(client);
    		
    		if (validator == null) {
    			throw new AuthenticationServiceException("Could not find signing keys for " + jwtAuth.getClientId());
    		}
    		
    		// process the JWT
    		
    		Jwt jwt = jwtAuth.getJwt();
    		JwtClaims jwtClaims = jwt.getClaims();
    		
    		if (!validator.validateSignature(jwt.toString())) {
    			throw new AuthenticationServiceException("Invalid signature");
    		}
    		
			// check the issuer
			if (jwtClaims.getIssuer() == null) {
				throw new AuthenticationServiceException("Assertion Token Issuer is null");
			} else if (!jwtClaims.getIssuer().equals(client.getClientId())){
				throw new AuthenticationServiceException("Issuers do not match, expected " + client.getClientId() + " got " + jwtClaims.getIssuer());
			}
			
			// check expiration
			if (jwtClaims.getExpiration() == null) {
				throw new AuthenticationServiceException("Assertion Token does not have required expiration claim");
			} else {
				// it's not null, see if it's expired
				Date now = new Date(System.currentTimeMillis() - (timeSkewAllowance * 1000));
				if (now.after(jwtClaims.getExpiration())) {
					throw new AuthenticationServiceException("Assertion Token is expired: " + jwtClaims.getExpiration());
				}
			}
			
			// check not before
			if (jwtClaims.getNotBefore() != null) {
				Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
				if (now.before(jwtClaims.getNotBefore())){
					throw new AuthenticationServiceException("Assertion Token not valid untill: " + jwtClaims.getNotBefore());
				}
			}
			
			// check audience
			if (jwtClaims.getAudience() == null) {
				throw new AuthenticationServiceException("Assertion token audience is null");
			} else if (!jwtClaims.getAudience().equals(config.getIssuer())) {
				throw new AuthenticationServiceException("Audience does not match, expected " + config.getIssuer() + " got " + jwtClaims.getAudience());
			}
			
			// check issued at
			if (jwtClaims.getIssuedAt() != null) {
				// since it's not null, see if it was issued in the future
				Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
				if (now.before(jwtClaims.getIssuedAt())) {
					throw new AuthenticationServiceException("Assertion Token was issued in the future: " + jwtClaims.getIssuedAt());
				}
			}

    		// IFF we managed to get all the way down here, the token is valid
			return new JwtBearerAssertionAuthenticationToken(client.getClientId(), jwt, client.getAuthorities());
    		
    	} catch (ClientNotFoundException e) {
    		throw new UsernameNotFoundException("Could not find client: " + jwtAuth.getClientId());
    	}
    	
    	
    }

	/**
	 * We support {@link JwtBearerAssertionAuthenticationToken}s only.
     */
    @Override
    public boolean supports(Class<?> authentication) {
	    return (JwtBearerAssertionAuthenticationToken.class.isAssignableFrom(authentication));
    }

	protected JwtSigningAndValidationService getValidatorForClient(ClientDetailsEntity client) {

		if(validators.containsKey(client)){
			return validators.get(client);
		} else {
						
			KeyFetcher keyFetch = new KeyFetcher();
			PublicKey signingKey = null;
			
			if (client.getJwkUrl() != null) {
				// prefer the JWK
				signingKey = keyFetch.retrieveJwkKey(client.getJwkUrl());
			} else if (client.getX509Url() != null) {
				// use the x509 only if JWK isn't configured
				signingKey = keyFetch.retrieveX509Key(client.getX509Url());				
			} else {
				// no keys configured
				logger.warn("No server key URLs configured for " + client.getClientId());
			}
			
			if (signingKey != null) {
				Map<String, JwtSigner> signers = new HashMap<String, JwtSigner>();
				
				if (signingKey instanceof RSAPublicKey) {
					
					RSAPublicKey rsaKey = (RSAPublicKey)signingKey;
					
					// build an RSA signers
					RsaSigner signer256 = new RsaSigner(JwsAlgorithm.RS256.getJwaName(), rsaKey, null);
					RsaSigner signer384 = new RsaSigner(JwsAlgorithm.RS384.getJwaName(), rsaKey, null);
					RsaSigner signer512 = new RsaSigner(JwsAlgorithm.RS512.getJwaName(), rsaKey, null);

					signers.put(client.getClientId() + JwsAlgorithm.RS256.getJwaName(), signer256);
					signers.put(client.getClientId() + JwsAlgorithm.RS384.getJwaName(), signer384);
					signers.put(client.getClientId() + JwsAlgorithm.RS512.getJwaName(), signer512);
				}

                JwtSigningAndValidationService signingAndValidationService = new DefaultJwtSigningAndValidationService(signers);
				
				validators.put(client, signingAndValidationService);
				
				return signingAndValidationService;
				
			} else {
				// there were either no keys returned or no URLs configured to fetch them, assume no checking on key signatures
				return null;
			}
		}
	}
}
