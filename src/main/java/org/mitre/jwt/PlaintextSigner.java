package org.mitre.jwt;

public class PlaintextSigner extends AbstractJwtSigner {

	public PlaintextSigner() {
	    super(PLAINTEXT);
    }

	@Override
    protected String generateSignature(String signatureBase) {
	    return null;
    }

}
