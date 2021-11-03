package cz.muni.ics.oidc.exceptions;

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
