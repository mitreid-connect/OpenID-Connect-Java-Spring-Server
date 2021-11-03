package cz.muni.ics.oidc.exceptions;

public class MissingFieldException extends RuntimeException {
    public MissingFieldException() {
        super();
    }

    public MissingFieldException(String s) {
        super(s);
    }

    public MissingFieldException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MissingFieldException(Throwable throwable) {
        super(throwable);
    }

    protected MissingFieldException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
