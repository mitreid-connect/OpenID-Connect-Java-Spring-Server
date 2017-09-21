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
package org.mitre.openid.connect.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown by utility methods when a client fails to validate. Contains information
 * to be returned.
 * @author jricher
 *
 */
public class ValidationException extends Exception {
	private static final long serialVersionUID = 1820497072989294627L;

	private String error;
	private String errorDescription;
	private HttpStatus status;
	public ValidationException(String error, String errorDescription,
			HttpStatus status) {
		this.error = error;
		this.errorDescription = errorDescription;
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	public HttpStatus getStatus() {
		return status;
	}
	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "ValidationException [error=" + error + ", errorDescription="
				+ errorDescription + ", status=" + status + "]";
	}
}