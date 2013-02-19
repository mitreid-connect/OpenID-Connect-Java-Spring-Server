/**
 * 
 */
package org.mitre.jose;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.nimbusds.jose.JWSAlgorithm;

/**
 * 
 * Wrapper class for Nimbus JOSE objects to fit into JPA
 * 
 * @author jricher
 *
 */
@Embeddable
public class JWSAlgorithmEntity {

	private JWSAlgorithm algorithm;
	
	public JWSAlgorithmEntity() { 
		
	}

	public JWSAlgorithmEntity(JWSAlgorithm algorithm) {
	    this.algorithm = algorithm;
    }
	
	public JWSAlgorithmEntity(String algorithmName) {
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
	 * Calls JWSAlgorithm.parse()
	 * @param algorithmName
	 */
	public void setAlgorithmName(String algorithmName) {
		if (algorithmName != null) {
			algorithm = JWSAlgorithm.parse(algorithmName);
		} else {
			algorithm = null;
		}
	}

	/**
	 * @return the algorithm
	 */
	@Transient
	public JWSAlgorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(JWSAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
	    return "JWSAlgorithmEntity [algorithm=" + algorithm + "]";
    }

	
	
}
