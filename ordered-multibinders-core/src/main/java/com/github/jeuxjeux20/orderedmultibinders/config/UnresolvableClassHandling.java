package com.github.jeuxjeux20.orderedmultibinders.config;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.UnableToResolveClassAsBindingException;

/**
 * Represents the behavior when a class in @{@link Order} can't be resolved as a binding.
 */
public enum UnresolvableClassHandling {
    /**
     * Ignores the class.
     */
    IGNORE,
    /**
     * Throws a {@link UnableToResolveClassAsBindingException}.
     */
    THROW
}
