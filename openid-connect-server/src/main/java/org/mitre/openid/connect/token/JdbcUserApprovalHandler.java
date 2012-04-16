package org.mitre.openid.connect.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;

public class JdbcUserApprovalHandler implements UserApprovalHandler {

	@Override
	public boolean isApproved(AuthorizationRequest authorizationRequest,
			Authentication userAuthentication) {
		
		//Check database to see if the user identified by the userAuthentication has stored an approval decision
		userAuthentication.getPrincipal();
		
		
		return false;
	}

}
