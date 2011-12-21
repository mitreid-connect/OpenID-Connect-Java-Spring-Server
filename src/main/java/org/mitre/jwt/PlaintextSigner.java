package org.mitre.jwt;

public class PlaintextSigner extends AbstractJwtSigner {

	public PlaintextSigner() {
	    super(PLAINTEXT);
    }

	@Override
	public void sign(Jwt jwt) {
		super.sign(jwt);
		
		jwt.setSignature("");
		
	}

}
