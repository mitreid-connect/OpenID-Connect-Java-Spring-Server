package org.bbplus;

import org.springframework.security.core.Authentication;

public interface TrustedRegistrationValidator {
	boolean validate(String postBody, Authentication auth);
}
