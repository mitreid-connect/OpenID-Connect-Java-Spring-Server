/**
 * 
 */
package org.mitre.jose;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.google.common.base.Strings;
import com.nimbusds.jose.JWEAlgorithm;

/**
 * 
 * Wrapper class for Nimbus JOSE objects to fit into JPA
 * 
 * @author jricher
 *
 */
@Embeddable
public class JWEAlgorithmEmbed {

	private JWEAlgorithm algorithm;

	public JWEAlgorithmEmbed() {
		
	}
	
	public JWEAlgorithmEmbed(JWEAlgorithm algorithm) {
	    this.algorithm = algorithm;
    }

	public static JWEAlgorithmEmbed getForAlgorithmName (String algorithmName) {
		JWEAlgorithmEmbed ent = new JWEAlgorithmEmbed();
		ent.setAlgorithmName(algorithmName);
		if (ent.getAlgorithm() == null) {
			return null;
		} else {
			return ent;
		}
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
		if (!Strings.isNullOrEmpty(algorithmName)) {
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
	    return "JWEAlgorithmEmbed [algorithm=" + algorithm + "]";
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
