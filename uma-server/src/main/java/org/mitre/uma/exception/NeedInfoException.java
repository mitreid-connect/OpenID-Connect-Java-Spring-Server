/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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

package org.mitre.uma.exception;

import java.util.Collection;

import org.mitre.uma.model.Claim;

/**
 * @author jricher
 *
 */
public class NeedInfoException extends RuntimeException {

	private static final long serialVersionUID = -8886957523367481451L;

	private String ticketValue;
	private Collection<Claim> unmatched;

	/**
	 * @param ticketValue
	 * @param unmatched
	 */
	public NeedInfoException(String ticketValue, Collection<Claim> unmatched) {
		this.setTicketValue(ticketValue);
		this.setUnmatched(unmatched);
	}

	/**
	 * @return the ticketValue
	 */
	public String getTicketValue() {
		return ticketValue;
	}

	/**
	 * @param ticketValue the ticketValue to set
	 */
	public void setTicketValue(String ticketValue) {
		this.ticketValue = ticketValue;
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

}
