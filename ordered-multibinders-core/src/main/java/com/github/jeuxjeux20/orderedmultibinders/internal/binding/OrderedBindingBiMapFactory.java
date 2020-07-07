package com.github.jeuxjeux20.orderedmultibinders.internal.binding;

import com.github.jeuxjeux20.orderedmultibinders.DuplicateIdentifiersException;
import com.github.jeuxjeux20.orderedmultibinders.binding.OrderedBinding;
import com.google.common.collect.ImmutableBiMap;
import com.google.inject.Binding;

/**
 * Creates an {@link ImmutableBiMap} of many bindings
 * using an {@link OrderedBindingFactory}.
 */
public final class OrderedBindingBiMapFactory {
    private final OrderedBindingFactory orderedBindingFactory;

    public OrderedBindingBiMapFactory(OrderedBindingFactory orderedBindingFactory) {
        this.orderedBindingFactory = orderedBindingFactory;
    }

    /**
     * Creates an {@link ImmutableBiMap} of the specified bindings.
     * <p>
     * If the factory returns {@code null} for a binding, there will be
     * no entry in the result for this binding.
     *
     * @param bindings an iterable of bindings where the ordered bindings should be created from
     * @return an {@link ImmutableBiMap} with the corresponding {@link OrderedBinding} for each
     * {@link Binding}
     * @throws IllegalArgumentException when multiple bindings have the same identifier
     */
    public ImmutableBiMap<Binding<?>, OrderedBinding> createOrderedBindings(Iterable<Binding<?>> bindings) {
        ImmutableBiMap.Builder<Binding<?>, OrderedBinding> builder = ImmutableBiMap.builder();

        for (Binding<?> binding : bindings) {
            OrderedBinding orderedBinding = orderedBindingFactory.create(binding);

            if (orderedBinding != null) {
                builder.put(binding, orderedBinding);
            }
        }

        try {
            return builder.build();
        } catch (IllegalArgumentException e) {
            throw new DuplicateIdentifiersException("Multiple bindings have the same identifier.", e);
        }
    }
}
