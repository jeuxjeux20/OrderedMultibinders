package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBindingTransformer;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binding;
import org.jetbrains.annotations.Nullable;

/**
 * A factory to create an {@link OrderedBinding} from a {@link Binding}.
 * <p>
 * The default implementation of this interface finds
 * the type using the given {@link BindingTargetTypeFinder}, returns {@code null}
 * if the type couldn't be found, else, it then creates the ordered binding using
 * {@link OrderedBinding#fromType(Class, Binding)},
 * and runs all the given {@link OrderedBindingTransformer} on the result.
 */
public interface OrderedBindingFactory {
    /**
     * Returns the default {@link OrderedBindingFactory}, which uses
     * {@link BindingTargetTypeFinder#DEFAULT} and the
     * annotation transformer.
     */
    OrderedBindingFactory DEFAULT = new DefaultOrderedBindingFactory(BindingTargetTypeFinder.DEFAULT,
            ImmutableList.of(OrderedBindingTransformer.ANNOTATIONS));

    /**
     * Creates an {@link OrderedBinding} from the specified binding.
     * <p>
     * This may return {@code null} if the creation has failed.
     *
     * @param binding the binding
     * @return the ordered binding, or {@code null} if it has failed
     */
    @Nullable OrderedBinding create(Binding<?> binding);
}
