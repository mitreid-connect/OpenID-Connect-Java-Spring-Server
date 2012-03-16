package org.mitre.jwt.signer.impl;

import org.mitre.jwt.signer.AbstractJwtSigner;

public class PlaintextSigner extends AbstractJwtSigner {

	public static final String PLAINTEXT = "none";
	
	public PlaintextSigner() {
	    super(PLAINTEXT);
    }

	@Override
    protected String generateSignature(String signatureBase) {
	    return null;
    }

}
