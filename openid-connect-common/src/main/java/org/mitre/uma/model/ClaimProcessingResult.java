/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.mitre.uma.model;

import java.util.Collection;

/**
 * Data shuttle to return results of the claims processing service.
 *
 * @author jricher
 *
 */
public class ClaimProcessingResult {

	private boolean satisfied;
	private Collection<Claim> unmatched;
	private Policy matched;

	/**
	 * Create an unmatched result. isSatisfied is false.
	 * @param unmatched
	 */
	public ClaimProcessingResult(Collection<Claim> unmatched) {
		this.satisfied = false;
		this.unmatched = unmatched;
		this.matched = null;
	}

	/**
	 * Create a matched result. isSatisfied is true.
	 * @param matched
	 */
	public ClaimProcessingResult(Policy matched) {
		this.satisfied = true;
		this.matched = matched;
		this.unmatched = null;
	}

	/**
	 * @return the satisfied
	 */
	public boolean isSatisfied() {
		return satisfied;
	}

	/**
	 * @param satisfied the satisfied to set
	 */
	public void setSatisfied(boolean satisfied) {
		this.satisfied = satisfied;
	}

	/**
	 * @return the unmatched
	 */
	public Collection<Claim> getUnmatched() {
		return unmatched;
	}

	/**
	 * @param unmatched the unmatched to set
	 */
	public void setUnmatched(Collection<Claim> unmatched) {
		this.unmatched = unmatched;
	}

	/**
	 * @return the matched
	 */
	public Policy getMatched() {
		return matched;
	}

	/**
	 * @param matched the matched to set
	 */
	public void setMatched(Policy matched) {
		this.matched = matched;
	}

}
