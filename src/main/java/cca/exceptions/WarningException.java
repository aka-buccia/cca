package cca.exceptions;

public abstract class WarningException extends FaaSChalCoreException {
    public WarningException(String message) {
        super(message);
    }
}
