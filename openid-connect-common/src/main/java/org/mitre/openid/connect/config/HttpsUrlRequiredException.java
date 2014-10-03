package org.mitre.openid.connect.config;

public class HttpsUrlRequiredException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1318613592371145910L;
	private String error;
	/**
	 * @param error
	 */
	public HttpsUrlRequiredException(String error) {
		this.setError(error);
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	@Override
	public String toString() {
		return "HttpsUrlRequiredException [error=" + this.error + "]";
	}
	
}
