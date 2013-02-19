/**
 * 
 */
package org.mitre.jose;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import com.nimbusds.jose.JWEAlgorithm;

/**
 * 
 * Wrapper class for Nimbus JOSE objects to fit into JPA
 * 
 * @author jricher
 *
 */
@Entity
@Embeddable
public class JWEAlgorithmEntity {

	private JWEAlgorithm algorithm;

	/**
	 * Get the name of this algorithm, return null if no algorithm set.
	 * @return
	 */
	@Basic
	public String getAlgorithm() {
		if (algorithm != null) {
			return algorithm.getName();
		} else {
			return null;
		}
	}
	
	/**
	 * Set the name of this algorithm. 
	 * Calls JWEAlgorithm.parse()
	 * @param algorithmName
	 */
	public void setAlgorithm(String algorithmName) {
		if (algorithmName != null) {
			algorithm = JWEAlgorithm.parse(algorithmName);
		} else {
			algorithm = null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
	    return "JWEAlgorithmEntity [algorithm=" + algorithm + "]";
    }
	
}
