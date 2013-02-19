/**
 * 
 */
package org.mitre.jose;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Transient;

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

	public JWEAlgorithmEntity() {
		
	}
	
	public JWEAlgorithmEntity(JWEAlgorithm algorithm) {
	    this.algorithm = algorithm;
    }

	public JWEAlgorithmEntity(String algorithmName) {
		setAlgorithmName(algorithmName);
	}
	
	/**
	 * Get the name of this algorithm, return null if no algorithm set.
	 * @return
	 */
	@Basic
	public String getAlgorithmName() {
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
	public void setAlgorithmName(String algorithmName) {
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

	/**
	 * @return the algorithm
	 */
    @Transient
	public JWEAlgorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(JWEAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
	
}
