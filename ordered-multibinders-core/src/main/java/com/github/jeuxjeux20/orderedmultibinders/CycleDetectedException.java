package com.github.jeuxjeux20.orderedmultibinders;

/**
 * Thrown when an ordering cycle has been detected between two bindings.
 */
public class CycleDetectedException extends RuntimeException {
    public CycleDetectedException() {

    }

    public CycleDetectedException(String message) {
        super(message);
    }

    public CycleDetectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CycleDetectedException(Throwable cause) {
        super(cause);
    }
}
