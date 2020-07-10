package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.config.SortingConfiguration;
import com.google.inject.Module;

import java.util.Arrays;

/**
 * Sorts elements of {@link Module}s' multibinders according to their @{@link Order} annotation.
 * <p>
 * In case of an order tie (when the exact position of the element cannot be determined),
 * the exact position is determined by:
 * <ul>
 *     <li>
 *         their {@link Order#position()} value,
 *         which defaults to the {@linkplain SortingConfiguration#getDefaultPosition()
 *         given configuration's default position}, note that it is applied even when the value is 0.
 *     </li>
 *     <li>
 *         or their position in the multibinder set
 *     </li>
 * </ul>
 */
public final class OrderedMultibinders {
    private OrderedMultibinders() {
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the default configuration.
     *
     * @param modules the modules to sort
     * @return the module with sorted multibinder elements
     */
    public static Module sort(Iterable<? extends Module> modules) {
        return sort(SortingConfiguration.DEFAULT, modules);
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the default configuration.
     *
     * @param modules the modules to sort
     * @return the module with sorted multibinder elements
     */
    public static Module sort(Module... modules) {
        return sort(SortingConfiguration.DEFAULT, modules);
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the given configuration.
     *
     * @param modules the modules to sort
     * @param configuration the configuration to use
     * @return the module with sorted multibinder elements
     */
    public static Module sort(SortingConfiguration configuration, Iterable<? extends Module> modules) {
        return new ModuleMultibinderSorter(modules, configuration).sort();
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the given configuration.
     *
     * @param modules the modules to sort
     * @param configuration the configuration to use
     * @return the module with sorted multibinder elements
     */
    public static Module sort(SortingConfiguration configuration, Module... modules) {
        return sort(configuration, Arrays.asList(modules));
    }
}
