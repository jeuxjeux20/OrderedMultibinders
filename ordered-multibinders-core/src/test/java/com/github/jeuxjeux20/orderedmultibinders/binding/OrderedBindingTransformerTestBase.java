package com.github.jeuxjeux20.orderedmultibinders.binding;

import com.github.jeuxjeux20.orderedmultibinders.Order;
import com.github.jeuxjeux20.orderedmultibinders.Placeholders;
import com.google.inject.TypeLiteral;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class OrderedBindingTransformerTestBase {
    protected void testTransformer(OrderedBindingTransformer transformer, OrderedBinding input, OrderedBinding expected) {
        OrderedBinding transformed = transformer.transform(input);

        assertEquals(expected, transformed);
    }

    protected void testTransformerSame(OrderedBindingTransformer transformer, OrderedBinding input) {
        testTransformer(transformer, input, input);
    }

    protected OrderedBinding createOrderedBinding(TypeLiteral<?> type) {
        return OrderedBinding.fromType(type, Placeholders.BINDING);
    }

    protected OrderedBinding createOrderedBinding(TypeLiteral<?> type, Order order) {
        return new OrderedBinding(type, Placeholders.BINDING, order);
    }

    protected OrderedBinding createOrderedBinding(Class<?> clazz) {
        return OrderedBinding.fromType(clazz, Placeholders.BINDING);
    }

    protected OrderedBinding createOrderedBinding(Class<?> clazz, Order order) {
        return new OrderedBinding(clazz, Placeholders.BINDING, order);
    }
}
