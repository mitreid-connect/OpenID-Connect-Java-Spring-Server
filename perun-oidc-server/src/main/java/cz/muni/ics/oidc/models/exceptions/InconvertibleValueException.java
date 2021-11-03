package cz.muni.ics.oidc.models.exceptions;

/**
 * Exception represents state when value of an attribute cannot be converted into desired type.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class InconvertibleValueException extends RuntimeException {

	public InconvertibleValueException() {
		super();
	}

	public InconvertibleValueException(String s) {
		super(s);
	}

	public InconvertibleValueException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public InconvertibleValueException(Throwable throwable) {
		super(throwable);
	}

	protected InconvertibleValueException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
