package org.mitre.openid.connect.client;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UrlValidator implements Validator{
	


	@Override
	public boolean supports(Class clzz) {
		return OIDCServerConfiguration.class.equals(clzz);
	}

	@Override
	public void validate(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "x509EncryptUrl", "x509EncryptUrl.empty");
		
	}
	
	public void validate1(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "x509SigningUrl", "x509SigningUrl.empty");
	}
	
	public void validate2(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "jwkEncryptUrl", "jwkEncryptUrl.empty");
	}
	
	public void validate3(Object obj, Errors e) {
		ValidationUtils.rejectIfEmpty(e, "jwkSigningUrl", "jwkSigningUrl.empty");
	}

}