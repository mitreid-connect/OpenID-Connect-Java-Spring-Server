/**
 * 
 */
package org.mitre.jose;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.google.common.base.Strings;
import com.nimbusds.jose.JWSAlgorithm;

/**
 * 
 * Wrapper class for Nimbus JOSE objects to fit into JPA
 * 
 * @author jricher
 *
 */
@Embeddable
public class JWSAlgorithmEmbed {

	public static final JWSAlgorithmEmbed NONE = getForAlgorithmName("none");

	private JWSAlgorithm algorithm;

	public JWSAlgorithmEmbed() {

	}

	public JWSAlgorithmEmbed(JWSAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public static JWSAlgorithmEmbed getForAlgorithmName (String algorithmName) {
		JWSAlgorithmEmbed ent = new JWSAlgorithmEmbed();
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
	 * Calls JWSAlgorithm.parse()
	 * @param algorithmName
	 */
	public void setAlgorithmName(String algorithmName) {
		if (!Strings.isNullOrEmpty(algorithmName)) {
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
		return "JWSAlgorithmEmbed [algorithm=" + algorithm + "]";
	}



}
