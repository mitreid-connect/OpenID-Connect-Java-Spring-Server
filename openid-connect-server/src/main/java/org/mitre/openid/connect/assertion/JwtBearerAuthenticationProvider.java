/**
 * 
 */
package org.mitre.openid.connect.assertion;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.mitre.jwt.signer.JwsAlgorithm;
import org.mitre.jwt.signer.JwtSigner;
import org.mitre.jwt.signer.impl.RsaSigner;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.DefaultJwtSigningAndValidationService;
import org.mitre.key.fetch.KeyFetcher;
import org.mitre.oauth2.exception.ClientNotFoundException;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.config.OIDCServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
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

	private Map<ClientDetailsEntity, JwtSigningAndValidationService> validators = new HashMap<ClientDetailsEntity, JwtSigningAndValidationService>();
	
	@Autowired
	private ClientDetailsEntityService clientService;
	
	/**
	 * Try to validate the client credentials by parsing and validating the JWT.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	
    	JwtBearerAssertionAuthenticationToken jwtAuth = (JwtBearerAssertionAuthenticationToken)authentication;
    	
    	
    	try {
    		ClientDetailsEntity client = clientService.loadClientByClientId(jwtAuth.getClientId());
    		
    		JwtSigningAndValidationService validator = getValidatorForClient(client);
    		
    		
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
				signingKey = keyFetch.retrieveJwkKey(client);
			} else if (client.getX509Url() != null) {
				// use the x509 only if JWK isn't configured
				signingKey = keyFetch.retrieveX509Key(client);				
			} else {
				// no keys configured
				logger.warn("No server key URLs configured for " + client.getIssuer());
			}
			
			if (signingKey != null) {
				Map<String, JwtSigner> signers = new HashMap<String, JwtSigner>();
				
				if (signingKey instanceof RSAPublicKey) {
					
					RSAPublicKey rsaKey = (RSAPublicKey)signingKey;
					
					// build an RSA signer
					RsaSigner signer256 = new RsaSigner(JwsAlgorithm.RS256.getJwaName(), rsaKey, null);
					RsaSigner signer384 = new RsaSigner(JwsAlgorithm.RS384.getJwaName(), rsaKey, null);
					RsaSigner signer512 = new RsaSigner(JwsAlgorithm.RS512.getJwaName(), rsaKey, null);

					signers.put(client.getIssuer() + JwsAlgorithm.RS256.getJwaName(), signer256);
					signers.put(client.getIssuer() + JwsAlgorithm.RS384.getJwaName(), signer384);
					signers.put(client.getIssuer() + JwsAlgorithm.RS512.getJwaName(), signer512);
				}

                JwtSigningAndValidationService signingAndValidationService = new DefaultJwtSigningAndValidationService(signers);
				
				validationServices.put(client, signingAndValidationService);
				
				return signingAndValidationService;
				
			} else {
				// there were either no keys returned or no URLs configured to fetch them, assume no checking on key signatures
				return null;
			}
		}
}
