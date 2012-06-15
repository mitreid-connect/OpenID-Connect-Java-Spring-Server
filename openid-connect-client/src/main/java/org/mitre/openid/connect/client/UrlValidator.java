package org.mitre.openid.connect.client;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

// TODO: is this used anywhere?

public class UrlValidator implements Validator{
	


	@Override
	public boolean supports(Class clzz) {
		return OIDCServerConfiguration.class.equals(clzz);
	}

	@Override
	public void validate(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "x509EncryptUrl", "x509EncryptUrl.empty");
		
	}
	
	// TODO this isn't called anywhere
	public void validate1(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "x509SigningUrl", "x509SigningUrl.empty");
	}
	
	// TODO this isn't called anywhere
	public void validate2(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "jwkEncryptUrl", "jwkEncryptUrl.empty");
	}
	
	// TODO this isn't called anywhere
	public void validate3(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "jwkSigningUrl", "jwkSigningUrl.empty");
	}

}