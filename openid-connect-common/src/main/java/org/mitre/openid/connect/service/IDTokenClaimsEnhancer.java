package org.mitre.openid.connect.service;

import java.util.Date;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.springframework.security.oauth2.provider.OAuth2Request;
import com.nimbusds.jwt.JWTClaimsSet;

public interface IDTokenClaimsEnhancer {

  void enhanceIdTokenClaims(JWTClaimsSet.Builder claimsBuilder, OAuth2Request request, Date issueTime,
      String sub, OAuth2AccessTokenEntity accessToken);
}
