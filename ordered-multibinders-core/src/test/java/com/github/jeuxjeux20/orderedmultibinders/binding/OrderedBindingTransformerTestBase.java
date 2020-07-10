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

}
