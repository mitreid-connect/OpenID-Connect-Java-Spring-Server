package org.mitre.openid.connect.service.impl;

import java.util.Date;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.openid.connect.service.IDTokenClaimsEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Service;
import com.nimbusds.jwt.JWTClaimsSet;

@Service("defaultIdTokenClaimsEnhancer")
public class DefaultIdTokenClaimsEnhancer implements IDTokenClaimsEnhancer {

  /**
   * Logger for this class
   */
  private static final Logger logger = LoggerFactory.getLogger(DefaultOIDCTokenService.class);

  @Override
  public void enhanceIdTokenClaims(JWTClaimsSet.Builder claimsBuilder, OAuth2Request request, Date issueTime,
      String sub, OAuth2AccessTokenEntity accessToken) {

    logger.debug("Enhancing Id-Token claims: no claims added.");
  }

}
