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