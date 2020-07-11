package com.github.jeuxjeux20.orderedmultibinders;

import com.github.jeuxjeux20.orderedmultibinders.config.SortingConfiguration;
import com.google.inject.Module;
import com.google.inject.spi.*;

import java.util.Arrays;

/**
 * Sorts elements of {@link Module}s' multibinders according to their @{@link Order} annotation.
 * <h2>Order ties</h2>
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
 * <h2>Identifiers</h2>
 * Every binding's identifier (how they are referred to in @{@link Order}) is determined
 * by the following:
 * <table border="1">
 *     <caption>Identifier for each type of binding</caption>
 *     <thead>
 *         <tr>
 *             <th>Type</th>
 *             <th>Result</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>{@link UntargettedBinding}</td>
 *             <td>Its {@linkplain UntargettedBinding#getKey() key}'s type</td>
 *         </tr>
 *         <tr>
 *             <td>{@link InstanceBinding}</td>
 *             <td>Its {@linkplain InstanceBinding#getInstance() instance}'s class</td>
 *         </tr>
 *         <tr>
 *             <td>{@link ConstructorBinding}</td>
 *             <td>Its {@linkplain ConstructorBinding#getConstructor() constructor}'s declaring class</td>
 *         </tr>
 *         <tr>
 *             <td>{@link ConvertedConstantBinding}</td>
 *             <td>Its {@linkplain ConvertedConstantBinding#getValue() value}'s class</td>
 *         </tr>
 *         <tr>
 *             <td>{@link ProviderKeyBinding}</td>
 *             <td>Its {@linkplain ProviderKeyBinding#getProviderKey() provider key}'s type</td>
 *         </tr>
 *         <tr>
 *             <td>{@link ProviderInstanceBinding}</td>
 *             <td>Its {@linkplain ProviderInstanceBinding#getUserSuppliedProvider() provider instance}'s class</td>
 *         </tr>
 *         <tr>
 *             <td>{@link LinkedKeyBinding}</td>
 *             <td>Its {@linkplain LinkedKeyBinding#getLinkedKey() linked key}'s type</td>
 *         </tr>
 *     </tbody>
 * </table>
 * If multiple bindings have the same identifier, a {@link DuplicateIdentifiersException} is thrown.
 */
public final class OrderedMultibinders {
    private OrderedMultibinders() {
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the default configuration.
     * <p>
     * More info about the sorting behavior is available in the {@link OrderedMultibinders} class.
     *
     * @param modules the modules to sort
     * @return the module with sorted multibinder elements
     */
    public static Module sort(Iterable<? extends Module> modules) {
        return sort(SortingConfiguration.DEFAULT, modules);
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the default configuration.
     * <p>
     * More info about the sorting behavior is available in the {@link OrderedMultibinders} class.
     *
     * @param modules the modules to sort
     * @return the module with sorted multibinder elements
     */
    public static Module sort(Module... modules) {
        return sort(SortingConfiguration.DEFAULT, modules);
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the given configuration.
     * <p>
     * More info about the sorting behavior is available in the {@link OrderedMultibinders} class.
     *
     * @param modules       the modules to sort
     * @param configuration the configuration to use
     * @return the module with sorted multibinder elements
     */
    public static Module sort(SortingConfiguration configuration, Iterable<? extends Module> modules) {
        return new ModuleMultibinderSorter(modules, configuration).sort();
    }

    /**
     * Sorts elements of the given {@link Module}s' multibinders using the given configuration.
     * <p>
     * More info about the sorting behavior is available in the {@link OrderedMultibinders} class.
     *
     * @param modules       the modules to sort
     * @param configuration the configuration to use
     * @return the module with sorted multibinder elements
     */
    public static Module sort(SortingConfiguration configuration, Module... modules) {
        return sort(configuration, Arrays.asList(modules));
    }
}
