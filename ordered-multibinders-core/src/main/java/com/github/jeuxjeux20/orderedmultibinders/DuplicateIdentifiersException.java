package com.github.jeuxjeux20.orderedmultibinders;

/**
 * Thrown when multiple bindings have the same identifier.
 */
public class DuplicateIdentifiersException extends RuntimeException {
    public DuplicateIdentifiersException() {
    }

    public DuplicateIdentifiersException(String message) {
        super(message);
    }

    public DuplicateIdentifiersException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateIdentifiersException(Throwable cause) {
        super(cause);
    }
}
