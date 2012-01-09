package org.mitre.oauth2.exception;
/**
 * 
 */


/**
 * @author aanganes
 *
 */
public class ClientNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final Long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ClientNotFoundException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ClientNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ClientNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClientNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
