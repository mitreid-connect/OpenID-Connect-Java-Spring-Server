package org.mitre.jwt;

import com.google.common.base.Objects;

public class AbstractJwtSigner implements JwtSigner {

	public static final String PLAINTEXT = "none";
	public static final String HS256 = "HS256";
	public static final String HS384 = "HS384";
	public static final String HS512 = "HS512";
	
	private String algorithm;

	public AbstractJwtSigner(String algorithm) {
	    this.algorithm = algorithm;
    }

	/**
     * @return the algorithm
     */
    public String getAlgorithm() {
    	return algorithm;
    }

	/**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
    	this.algorithm = algorithm;
    }

    /**
     * Ensures that the 'alg' of the given JWT matches the {@link #algorithm} of this signer
     */
	@Override
	public void sign(Jwt jwt) {
		if (!Objects.equal(algorithm, jwt.getHeader().getAlgorithm())) {
			// algorithm type doesn't match
			// TODO: should this be an error or should we just fix it in the incoming jwt?
			// for now, we fix the Jwt
			jwt.getHeader().setAlgorithm(algorithm);			
		}
		
	}


}