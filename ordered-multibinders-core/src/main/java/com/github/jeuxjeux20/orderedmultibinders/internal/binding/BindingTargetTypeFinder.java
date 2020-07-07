package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.google.inject.Binding;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;
import org.jetbrains.annotations.Nullable;

/**
 * Finds the target identifier of a given binding.
 */
public interface BindingTargetTypeFinder {
    /**
     * The default implementation of a {@link BindingTargetTypeFinder}, which finds
     * the target identifier based on the binding identifier as follows:
     * <table border="1">
     *     <caption>Result for each type of binding</caption>
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
     *         <tr>
     *             <td>Something else</td>
     *             <td>{@code null}</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    BindingTargetTypeFinder DEFAULT = new DefaultBindingTargetTypeFinder();

    /**
     * Finds the target identifier of the specified binding.
     * <p>
     * This method might return {@code null} when there isn't a target identifier,
     * of if the given binding is not supported.
     *
     * @param binding the binding
     * @return the target identifier of the specified binding, or {@code null} if there isn't one
     */
    @Nullable TypeLiteral<?> findTargetType(Binding<?> binding);
}
