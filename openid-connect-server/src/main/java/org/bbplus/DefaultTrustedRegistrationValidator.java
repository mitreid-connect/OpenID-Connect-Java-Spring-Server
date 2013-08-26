package org.bbplus;

import org.springframework.security.core.Authentication;

public class DefaultTrustedRegistrationValidator implements TrustedRegistrationValidator {

	// Happy validator doesn't care about trusted registration and always approves.
	@Override
	public boolean validate(String postBody, Authentication auth) {
		return false;
	}

}
