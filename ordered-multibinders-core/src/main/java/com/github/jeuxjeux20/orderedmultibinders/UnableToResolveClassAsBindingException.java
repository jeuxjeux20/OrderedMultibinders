package com.github.jeuxjeux20.orderedmultibinders;

/**
 * Thrown when a binding couldn't be resolved
 * from the class of the @{@link Order} annotation.
 */
public class UnableToResolveClassAsBindingException extends RuntimeException {
    private final Class<?> unresolvedClass;

    public UnableToResolveClassAsBindingException(Class<?> unresolvedClass) {
        super("Cannot resolve class '" + unresolvedClass + "' as a binding.");
        this.unresolvedClass = unresolvedClass;
    }

    public UnableToResolveClassAsBindingException(String message, Class<?> unresolvedClass) {
        super(message);
        this.unresolvedClass = unresolvedClass;
    }

    public UnableToResolveClassAsBindingException(String message, Throwable cause, Class<?> unresolvedClass) {
        super(message, cause);
        this.unresolvedClass = unresolvedClass;
    }

    public UnableToResolveClassAsBindingException(Throwable cause, Class<?> unresolvedClass) {
        super(cause);
        this.unresolvedClass = unresolvedClass;
    }

    public Class<?> getUnresolvedClass() {
        return unresolvedClass;
    }
}
