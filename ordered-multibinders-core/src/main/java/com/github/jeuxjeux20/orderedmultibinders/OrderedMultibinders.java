package com.github.jeuxjeux20.orderedmultibinders;

import com.google.inject.Module;

import java.util.Arrays;

/**
 * Sorts elements of {@link Module}s' multibinders according to their @{@link Order} annotation.
 */
public final class OrderedMultibinders {
    private OrderedMultibinders() {
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders.
     * @param modules the modules to sort
     * @return the module with sorted multibinder elements
     */
    public static Module sort(Iterable<? extends Module> modules) {
        return new ModuleMultibinderSorter(modules).sort();
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders.
     * @param modules the modules to sort
     * @return the module with sorted multibinder elements
     */
    public static Module sort(Module... modules) {
        return sort(Arrays.asList(modules));
    }
}
