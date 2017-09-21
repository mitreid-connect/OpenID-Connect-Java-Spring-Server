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

package org.mitre.uma.service.impl;

import java.util.Collection;
import java.util.HashSet;

import org.mitre.uma.model.Claim;
import org.mitre.uma.model.ClaimProcessingResult;
import org.mitre.uma.model.PermissionTicket;
import org.mitre.uma.model.Policy;
import org.mitre.uma.model.ResourceSet;
import org.mitre.uma.service.ClaimsProcessingService;
import org.springframework.stereotype.Service;

/**
 * Tests if all the claims in the required set have a matching
 * value in the supplied set.
 *
 * @author jricher
 *
 */
@Service("matchAllClaimsOnAnyPolicy")
public class MatchAllClaimsOnAnyPolicy implements ClaimsProcessingService {

	/* (non-Javadoc)
	 * @see org.mitre.uma.service.ClaimsProcessingService#claimsAreSatisfied(java.util.Collection, java.util.Collection)
	 */
	@Override
	public ClaimProcessingResult claimsAreSatisfied(ResourceSet rs, PermissionTicket ticket) {
		Collection<Claim> allUnmatched = new HashSet<>();
		for (Policy policy : rs.getPolicies()) {
			Collection<Claim> unmatched = checkIndividualClaims(policy.getClaimsRequired(), ticket.getClaimsSupplied());
			if (unmatched.isEmpty()) {
				// we found something that's satisfied the claims, let's go with it!
				return new ClaimProcessingResult(policy);
			} else {
				// otherwise add it to the stack to send back
				allUnmatched.addAll(unmatched);
			}
		}

		// otherwise, tell the caller that we'll need some set of these fulfilled somehow
		return new ClaimProcessingResult(allUnmatched);
	}

	private Collection<Claim> checkIndividualClaims(Collection<Claim> claimsRequired, Collection<Claim> claimsSupplied) {

		Collection<Claim> claimsUnmatched = new HashSet<>(claimsRequired);

		// see if each of the required claims has a counterpart in the supplied claims set
		for (Claim required : claimsRequired) {
			for (Claim supplied : claimsSupplied) {

				if (required.getIssuer().containsAll(supplied.getIssuer())) {
					// it's from the right issuer

					if (required.getName().equals(supplied.getName()) &&
							required.getValue().equals(supplied.getValue())) {

						// the claim matched, pull it from the set
						claimsUnmatched.remove(required);

					}

				}
			}
		}

		// if there's anything left then the claims aren't satisfied, return the leftovers
		return claimsUnmatched;

	}

}
